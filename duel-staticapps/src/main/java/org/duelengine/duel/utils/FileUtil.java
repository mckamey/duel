package org.duelengine.duel.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	public enum HashEncoding {
		DEFAULT,
		HEX,
		BASE64
	}

	private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
	private static final int DEFAULT_BUFFER_SIZE = 4096;
	private static final int SHA1_HEX_LENGTH = 40;
	private static final int SHA1_BASE64_LENGTH = 27;
	private static final int MD5_HEX_LENGTH = 32;
	private static final int MD5_BASE64_LENGTH = 22;
	public static final String SHA1 = "SHA-1";
	public static final String MD5 = "MD5";

	public static void prepSavePath(File file) {
		if (file == null) {
			throw new NullPointerException("file");
		}

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
	}

	public static void copy(File source, File target, boolean overwrite)
			throws IOException {

		copy(source, target, overwrite, null);
	}

	public static void copy(File source, File target, boolean overwrite, byte[] buffer)
			throws IOException {

		if (target == null) {
			throw new NullPointerException("target");
		}
		if (source == null) {
			throw new NullPointerException("source");
		}
		if (!source.exists()) {
			throw new FileNotFoundException(source.toString());
		}
		if (!source.isFile()) {
			return;
		}
		if (!overwrite && target.exists()) {
			return;
		}
		if (buffer == null) {
			buffer = new byte[DEFAULT_BUFFER_SIZE];
		}

		prepSavePath(target);
		
		FileInputStream sourceStream = null;
		FileOutputStream targetStream = null;
		try {
			sourceStream = new FileInputStream(source);
			targetStream = new FileOutputStream(target);

			int count;
			while ((count = sourceStream.read(buffer)) > 0) {
				targetStream.write(buffer, 0, count);
			}

		} finally {
			if (targetStream != null) {
				try {
					targetStream.close();
				} catch (IOException e) {}
			}
			if (sourceStream != null) {
				try {
					sourceStream.close();
				} catch (IOException e) {}
			}
		}
	}

	public static List<File> findFiles(File root, String... extensions) {
		if (extensions == null || extensions.length < 1) {
			// no filter, returns all files
			return findFiles(root, (Set<String>)null);
		}

		Set<String> extSet = new HashSet<String>(extensions.length);
		for (String ext : extensions) {
			extSet.add(ext);
		}

		return findFiles(root, extSet);
	}

	public static List<File> findFiles(File root, Set<String> extensions) {

		// no filter, returns all files
		boolean noFilter = (extensions == null || extensions.size() < 1);

		List<File> files = new ArrayList<File>();
		Queue<File> dirs = new LinkedList<File>();
		dirs.add(root);
		while (!dirs.isEmpty()) {
			File file = dirs.remove();

			if (file.isDirectory()) {
				dirs.addAll(Arrays.asList(file.listFiles()));
				continue;
			}

			String ext = getExtension(file.getName());
			if (noFilter || extensions.contains(ext)) {
				files.add(file);
			} else {
				log.info("Skipping: "+file.getName());
			}
		}

		if (files.size() < 1) {
			log.warn("No input files found.");
		}
		return files;
	}

	public static String getRelativePath(File root, File child) {
		String prefix;
		try {
			prefix = root.getCanonicalPath();
		} catch (IOException e) {
			prefix = root.getAbsolutePath();
		}

		String path;
		try {
			path = child.getCanonicalPath();
		} catch (IOException e) {
			path = child.getAbsolutePath();
		}

		if (path.indexOf(prefix) != 0) {
			return child.getPath();
		}
		if (prefix.length() == path.length()) {
			return "";
		}

		int start = prefix.length();
		if (path.charAt(start) == '/') {
			start++;
		}
		return path.substring(start);
	}

	public static String getExtension(File file) {
		if (file == null) {
			return "";
		}

		return getExtension(file.getName());
	}
	
	public static String getExtension(String name) {
		int dot = name.lastIndexOf('.');
		if (dot < 0) {
			return "";
		}

		return name.substring(dot).toLowerCase();
	}

	public static File replaceExtension(File file, String ext) {
		if (file == null) {
			throw new NullPointerException("file");
		}
		if (ext == null || ext.isEmpty()) {
			throw new NullPointerException("ext");
		}

		String name = file.getName();
		int dot = name.lastIndexOf('.');
		String newName = (dot < 0) ? name : name.substring(0, dot);
		if (ext.indexOf('.') != 0) {
			ext = '.'+ext;
		}
		newName += ext;
		return new File(file.getParentFile(), newName);
	}

	public static String calcSHA1(File file)
			throws FileNotFoundException {

		return calcHash(file, SHA1, HashEncoding.DEFAULT, null);
	}

	public static String calcMD5(File file)
			throws FileNotFoundException {

		return calcHash(file, MD5, HashEncoding.DEFAULT, null);
	}

	public static String calcHash(File file, String algorithm, HashEncoding encoding)
			throws FileNotFoundException {

		return calcHash(file, algorithm, encoding, null);
	}

	public static String calcHash(File file, String algorithm, HashEncoding encoding, byte[] buffer)
			throws FileNotFoundException {

		if (file == null) {
			throw new NullPointerException("file");
		}
		if (!file.exists()) {
			throw new FileNotFoundException(file.toString());
		}
		if (buffer == null) {
			buffer = new byte[DEFAULT_BUFFER_SIZE];
		}

		FileInputStream stream = null;
		try {
			MessageDigest hash = MessageDigest.getInstance(algorithm);
			stream = new FileInputStream(file);

			int count;
			while ((count = stream.read(buffer)) > 0) {
				hash.update(buffer, 0, count);
			}

			byte[] digest = hash.digest();
			switch (encoding) {
				case BASE64:
					return encodeBytesBase64(digest);
				default:
				case HEX:
					return encodeBytesHex(digest);
			}

		} catch (Exception ex) {
			log.error(algorithm+" Error", ex);
			return null;

		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
	}

	public static boolean isSHA1(String signature) {
		// validate signature length and content
		if (signature == null) {
			return false;
		}

		switch (signature.length()) {
			case SHA1_HEX_LENGTH:
				return isHex(signature);
			case SHA1_BASE64_LENGTH:
				return Base64.isBase64(signature);
			default:
				return false;
		}
	}

	public static boolean isMD5(String signature) {
		// validate signature length and content
		if (signature == null) {
			return false;
		}

		switch (signature.length()) {
			case MD5_HEX_LENGTH:
				return isHex(signature);
			case MD5_BASE64_LENGTH:
				return Base64.isBase64(signature);
			default:
				return false;
		}
	}

	private static boolean isHex(String signature) {
		if (signature == null || signature.isEmpty()) {
			return false;
		}
		
		for (int i=signature.length()-1; i>=0; i--) {
			char ch = signature.charAt(i);
			if ((ch >= '0' && ch <= '9') ||
				(ch >= 'a' && ch <= 'f') ||
				(ch >= 'A' && ch <= 'F'))
			{
				continue;
			}
	
			return false;
		}

		return true;
	}

	private static String encodeBytesBase64(byte[] digest) {
		return Base64.encodeBase64URLSafeString(digest);
	}

	private static String encodeBytesHex(byte[] digest) {
		StringBuilder hex = new StringBuilder(SHA1_HEX_LENGTH);
		for (int i=0; i<digest.length; i++) {
			int digit = 0xFF & digest[i];
			if (digit < 0x10) {
				hex.append('0');
			}
			hex.append(Integer.toHexString(digit));
		}
		return hex.toString();
	}
}
