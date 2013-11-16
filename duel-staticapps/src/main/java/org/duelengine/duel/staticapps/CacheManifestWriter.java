package org.duelengine.duel.staticapps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.duelengine.duel.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheManifestWriter {
	private static final Logger log = LoggerFactory.getLogger(CacheManifestWriter.class);
	private static final char NEWLINE = '\n';
	private static final String HEADER = "CACHE MANIFEST";
	private static final String COMMENT = "# ";
	private static final String CACHE = "CACHE:";
	private static final String FALLBACK = "FALLBACK:";
	private static final String NETWORK = "NETWORK:";

	public void write(File outputDir, CacheManifest cacheManifest) {
		if (outputDir == null) {
			throw new NullPointerException("outputDir");
		}

		File manifestFile = new File(outputDir, cacheManifest.manifest());
		FileUtil.prepSavePath(manifestFile);

		log.info("Generating cache manifest: "+manifestFile);

		FileWriter writer = null;
		try {
			writer = new FileWriter(manifestFile);

			write(writer, cacheManifest);

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException ex) {}
			}
		}
	}

	private void write(Appendable output, CacheManifest cacheManifest)
			throws IOException {
		output.append(HEADER).append(NEWLINE);
		if (cacheManifest.version() != null && !cacheManifest.version().isEmpty()) {
			output.append(COMMENT).append(cacheManifest.version()).append(NEWLINE);
		}
		output.append(NEWLINE);

		if (cacheManifest.cache() != null && !cacheManifest.cache().isEmpty()) {
			// sort to ensure the resources don't shift positions
			List<String> paths = new ArrayList<String>(cacheManifest.cache());
			Collections.sort(paths);

			output.append(CACHE).append(NEWLINE);
			for (String path : paths) {
				output.append(path).append(NEWLINE);
			}
			output.append(NEWLINE);
		}

		if (cacheManifest.fallback() != null && !cacheManifest.fallback().isEmpty()) {
			// sort to ensure the resources don't shift positions
			List<String> paths = new ArrayList<String>(cacheManifest.fallback().keySet());
			Collections.sort(paths);

			output.append(FALLBACK).append(NEWLINE);
			for (String path : paths) {
				output.append(path).append(' ').append(cacheManifest.fallback().get(path)).append(NEWLINE);
			}
			output.append(NEWLINE);
		}

		if (cacheManifest.network() != null && !cacheManifest.network().isEmpty()) {
			// sort to ensure the resources don't shift positions
			List<String> paths = new ArrayList<String>(cacheManifest.network());
			Collections.sort(paths);

			output.append(NETWORK).append(NEWLINE);
			for (String path : paths) {
				output.append(path).append(NEWLINE);
			}
		}
	}
}
