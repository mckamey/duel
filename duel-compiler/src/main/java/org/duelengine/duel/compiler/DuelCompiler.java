package org.duelengine.duel.compiler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.duelengine.duel.ast.VIEWCommandNode;
import org.duelengine.duel.codegen.ClientCodeGen;
import org.duelengine.duel.codegen.CodeGenSettings;
import org.duelengine.duel.codegen.CodeGenerator;
import org.duelengine.duel.codegen.JavaCodeGen;
import org.duelengine.duel.parsing.DuelLexer;
import org.duelengine.duel.parsing.DuelParser;
import org.duelengine.duel.parsing.SyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuelCompiler {

	private final Logger log = LoggerFactory.getLogger(DuelCompiler.class);
	private boolean verbose;
	private File inputDir;
	private File outputClientDir;
	private File outputServerDir;
	private String clientPrefix;
	private String serverPrefix;

	public String getInputDir() {
		return inputDir.getAbsolutePath();
	}

	public void setInputDir(String value) {
		inputDir = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getOutputClientDir() {
		return outputClientDir.getAbsolutePath();
	}

	public void setOutputClientDir(String value) {
		outputClientDir = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getOutputServerDir() {
		return outputServerDir.getAbsolutePath();
	}

	public void setOutputServerDir(String value) {
		outputServerDir = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getClientPrefix() {
		return clientPrefix;
	}

	public void setClientPrefix(String value) {
		clientPrefix = value;
	}

	public String getServerPrefix() {
		return serverPrefix;
	}

	public void setServerPrefix(String value) {
		serverPrefix = value;
	}

	private boolean ensureSettings() {
		if (inputDir == null || !inputDir.exists()) {
			throw new IllegalArgumentException("ERROR: input directory is empty: "+inputDir);
		}

		if (outputClientDir == null) {
			outputClientDir = inputDir.isDirectory() ? inputDir :  inputDir.getParentFile();
		}

		if (outputServerDir == null) {
			outputServerDir = inputDir.isDirectory() ? inputDir :  inputDir.getParentFile();
		}

		return true;
	}

	/**
	 * Compiles view files
	 * @throws IOException 
	 */
	public void execute() throws IOException {
		if (!ensureSettings()) {
			return;
		}

		List<File> inputFiles = findFiles(inputDir);
		if (inputFiles.size() < 1) {
			throw new IllegalArgumentException("ERROR: no input files found: "+inputDir);
		}

		for (File inputFile : inputFiles) {
			List<VIEWCommandNode> views;
			try {
				FileReader reader = new FileReader(inputFile);
				views = new DuelParser().parse(new DuelLexer(reader));

				if (views == null || views.size() < 1) {
					throw new SyntaxException("Syntax error: no view found in "+inputFile, 0, 0, 0);
				}

			} catch (SyntaxException ex) {
				reportSyntaxError(inputFile, ex);
				continue;
			}

			// TODO: allow setting of more properties from args
			CodeGenSettings settings = new CodeGenSettings();
			settings.setIndent("\t");
			settings.setNewline(System.getProperty("line.separator"));
			settings.setClientNamePrefix(clientPrefix);
			settings.setServerNamePrefix(serverPrefix);

			// compact client-side
			settings.setConvertLineEndings(false);
			settings.setNormalizeWhitespace(true);

			String outputName = inputFile.getName();
			for (VIEWCommandNode view : views) {
				if (view.isServerOnly()) {
					// skip server-only views
					continue;
				}

				// use the first view
				outputName = settings.getClientPath(view.getName());
				break;
			}

			CodeGenerator codegen = new ClientCodeGen(settings);

			// ensure has client-views before generating file
			int clientViews = 0;
			for (VIEWCommandNode view : views) {
				if (!view.isServerOnly()) {
					clientViews++;
				}
			}

			if (clientViews > 0) {
				try {
					File outputFile = new File(outputClientDir, outputName+codegen.getFileExtension());
					outputFile.getParentFile().mkdirs();

					FileWriter writer = new FileWriter(outputFile, false);
					try {
						codegen.write(writer, views);
					} finally {
						writer.flush();
						writer.close();
					}

				} catch (SyntaxException ex) {
					reportSyntaxError(inputFile, ex);
				}
			}

			// directly emit server-side
			settings.setConvertLineEndings(true);
			settings.setNormalizeWhitespace(false);

			codegen = new JavaCodeGen(settings);
			for (VIEWCommandNode view : views) {
				if (view.isClientOnly()) {
					// skip client-only views
					continue;
				}

				try {
					File outputFile = new File(outputServerDir, settings.getServerPath(view.getName(), codegen));
					outputFile.getParentFile().mkdirs();

					FileWriter writer = new FileWriter(outputFile, false);
					try {
						codegen.write(writer, view);
					} finally {
						writer.flush();
						writer.close();
					}

				} catch (SyntaxException ex) {
					reportSyntaxError(inputFile, ex);
				}
			}
		}
	}

	private void reportSyntaxError(File inputFile, SyntaxException ex) {
		try {
			String message = ex.getMessage();
			if (message == null) {
				if (ex.getCause() != null) {
					message = ex.getCause().getMessage();
				} else {
					message = "Error";
				}
			}

			log.error(String.format(
				"%s:%d: %s",
				inputFile.getAbsolutePath(),
				ex.getLine(),
				message));

			int col = ex.getColumn(),
				line=ex.getLine();

			String text = "";
			LineNumberReader reader = new LineNumberReader(new FileReader(inputFile));
			try {
				for (int i=-1; i<line; i++) {
					text = reader.readLine();
				}

			} finally {
				reader.close();
			}

			log.error(text);
			if (col > 0) {
				log.error(String.format("%"+col+"s", "^"));
			} else {
				log.error("^");
			}

			if (verbose) {
				ex.printStackTrace();
			}

		} catch (Exception ex2) {
			ex.printStackTrace();

			if (verbose) {
				ex2.printStackTrace();
			}
		}
	}

	private static List<File> findFiles(File inputDir) {

		List<File> files = new ArrayList<File>();
		Queue<File> dirs = new LinkedList<File>();
		dirs.add(inputDir);

		while (!dirs.isEmpty()) {
			File file = dirs.poll();
			if (file.isDirectory()) {
				dirs.addAll(Arrays.asList(file.listFiles()));
			} else if (file.getName().toLowerCase().endsWith(".duel")) {
				files.add(file);
			}
		}

		return files;
	}
}
