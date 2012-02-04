package org.duelengine.duel.compiler;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codegen.*;
import org.duelengine.duel.parsing.*;
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
		return this.inputDir.getAbsolutePath();
	}

	public void setInputDir(String value) {
		this.inputDir = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getOutputClientDir() {
		return this.outputClientDir.getAbsolutePath();
	}

	public void setOutputClientDir(String value) {
		this.outputClientDir = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getOutputServerDir() {
		return this.outputServerDir.getAbsolutePath();
	}

	public void setOutputServerDir(String value) {
		this.outputServerDir = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getClientPrefix() {
		return this.clientPrefix;
	}

	public void setClientPrefix(String value) {
		this.clientPrefix = value;
	}

	public String getServerPrefix() {
		return this.serverPrefix;
	}

	public void setServerPrefix(String value) {
		this.serverPrefix = value;
	}

	private boolean ensureSettings() {
		if (this.inputDir == null || !this.inputDir.exists()) {
			throw new IllegalArgumentException("ERROR: input directory is empty: "+this.inputDir);
		}

		if (this.outputClientDir == null) {
			this.outputClientDir = this.inputDir.isDirectory() ? this.inputDir :  this.inputDir.getParentFile();
		}

		if (this.outputServerDir == null) {
			this.outputServerDir = this.inputDir.isDirectory() ? this.inputDir :  this.inputDir.getParentFile();
		}

		return true;
	}

	/**
	 * Compiles view files
	 * @throws IOException 
	 */
	public void execute() throws IOException {
		if (!this.ensureSettings()) {
			return;
		}

		List<File> inputFiles = findFiles(this.inputDir);
		if (inputFiles.size() < 1) {
			throw new IllegalArgumentException("ERROR: no input files found: "+this.inputDir);
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
				this.reportSyntaxError(inputFile, ex);
				continue;
			}

			// TODO: allow setting of more properties from args
			CodeGenSettings settings = new CodeGenSettings();
			settings.setIndent("\t");
			settings.setNewline(System.getProperty("line.separator"));
			settings.setClientNamePrefix(this.clientPrefix);
			settings.setServerNamePrefix(this.serverPrefix);

			// compact client-side
			settings.setConvertLineEndings(false);
			settings.setNormalizeWhitespace(true);

			String outputName = inputFile.getName();
			for (VIEWCommandNode view : views) {
				// use the first view
				outputName = settings.getFullClientName(view.getName()).replace('.', '/');
				break;
			}

			CodeGenerator codegen = new ClientCodeGen(settings);
			try {
				File outputFile = new File(this.outputClientDir, outputName+codegen.getFileExtension());
				outputFile.getParentFile().mkdirs();

				FileWriter writer = new FileWriter(outputFile, false);
				try {
					codegen.write(writer, views);
				} finally {
					writer.flush();
					writer.close();
				}

			} catch (SyntaxException ex) {
				this.reportSyntaxError(inputFile, ex);
			}

			// directly emit server-side
			settings.setConvertLineEndings(true);
			settings.setNormalizeWhitespace(false);

			codegen = new JavaCodeGen(settings);
			for (VIEWCommandNode view : views) {
				try {
					File outputFile = new File(this.outputServerDir, settings.getFullServerName(view.getName()).replace('.', '/')+codegen.getFileExtension());
					outputFile.getParentFile().mkdirs();

					FileWriter writer = new FileWriter(outputFile, false);
					try {
						codegen.write(writer, view);
					} finally {
						writer.flush();
						writer.close();
					}

				} catch (SyntaxException ex) {
					this.reportSyntaxError(inputFile, ex);
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

			LineNumberReader reader = new LineNumberReader(new FileReader(inputFile));
			String text = "";
			for (int i=-1; i<line; i++) {
				text = reader.readLine();
			}

			log.error(text);
			if (col > 0) {
				log.error(String.format("%"+col+"s", "^"));
			} else {
				log.error("^");
			}

			if (this.verbose) {
				ex.printStackTrace();
			}

		} catch (Exception ex2) {
			ex.printStackTrace();

			if (this.verbose) {
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
