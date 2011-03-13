package org.duelengine.duel.compiler;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codegen.*;
import org.duelengine.duel.parsing.*;

public class DuelCompiler {

	private static final String HELP =
		"Usage:\n" +
		"\tjava -jar duel.jar <input-file|input-folder>\n" +
		"\tjava -jar duel.jar <input-file|input-folder> <output-folder>\n" +
		"\tjava -jar duel.jar <input-file|input-folder> <output-client-folder> <output-server-folder>\n\n"+
		"\tinput-file: path to the DUEL input file (e.g. foo.duel)\n"+
		"\tinput-folder: path to the input folder containing DUEL files\n"+
		"\toutput-folder: path to the output folder\n"+
		"\toutput-client-folder: path to the view scripts folder\n"+
		"\toutput-server-folder: path to the source code folder\n";

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(HELP);
			return;
		}

		DuelCompiler compiler = new DuelCompiler();
		compiler.setInputFolder(args[0]);

		if (args.length > 1) {
			compiler.setOutputClientFolder(args[1]);

			if (args.length > 2) {
				compiler.setOutputServerFolder(args[2]);
			}
		}

		try {
			compiler.execute();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean verbose;
	private File inputFolder;
	private File outputClientFolder;
	private File outputServerFolder;
	private String clientPrefix;
	private String serverPrefix;

	public String getInputFolder() {
		return this.inputFolder.getAbsolutePath();
	}

	public void setInputFolder(String value) {
		this.inputFolder = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getOutputClientFolder() {
		return this.outputClientFolder.getAbsolutePath();
	}

	public void setOutputClientFolder(String value) {
		this.outputClientFolder = (value != null) ? new File(value.replace('\\', '/')) : null;
	}

	public String getOutputServerFolder() {
		return this.outputServerFolder.getAbsolutePath();
	}

	public void setOutputServerFolder(String value) {
		this.outputServerFolder = (value != null) ? new File(value.replace('\\', '/')) : null;
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
		if (this.inputFolder == null || !this.inputFolder.exists()) {
			throw new IllegalArgumentException("Error: no input files found: "+this.inputFolder);
		}

		if (this.outputClientFolder == null) {
			this.outputClientFolder = this.inputFolder.getParentFile();
		}

		if (this.outputServerFolder == null) {
			this.outputServerFolder = this.inputFolder.getParentFile();
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

		List<File> inputFiles = findFiles(this.inputFolder);
		if (inputFiles.size() < 1) {
			throw new IllegalArgumentException("this.inputFolder.getAbsolutePath(): Error: no input files found");
		}

		for (File inputFile : inputFiles) {
			List<VIEWCommandNode> views;
			try {
				FileReader reader = new FileReader(inputFile);
				views = new DuelParser().parse(new DuelLexer(reader));

			} catch (SyntaxException ex) {
				this.reportSyntaxError(inputFile, ex);
				continue;
			}

			if (views == null || views.size() < 1) {
				throw new IllegalArgumentException(inputFile.getAbsolutePath()+": Syntax error: no view found");
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
				File outputFile = new File(this.outputClientFolder, outputName+codegen.getFileExtension());
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
					File outputFile = new File(this.outputServerFolder, settings.getFullServerName(view.getName()).replace('.', '/')+codegen.getFileExtension());
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

			System.err.println(String.format(
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

			System.err.println(text);
			if (col > 0) {
				System.err.println(String.format("%"+col+"s", "^"));
			} else {
				System.err.println("^");
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

	private static List<File> findFiles(File inputFolder) {

		List<File> files = new ArrayList<File>();
		Queue<File> folders = new LinkedList<File>();
		folders.add(inputFolder);

		while (!folders.isEmpty()) {
			File file = folders.poll();
			if (file.isDirectory()) {
				folders.addAll(Arrays.asList(file.listFiles()));
			} else if (file.getName().toLowerCase().endsWith(".duel")) {
				files.add(file);
			}
		}

		return files;
	}
}
