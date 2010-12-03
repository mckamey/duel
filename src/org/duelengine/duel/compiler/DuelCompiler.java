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
		compiler.setInputRoot(new File(args[0].replace('\\', '/')));

		if (args.length > 1) {
			compiler.setOutputClientFolder(new File(args[1].replace('\\', '/')));

			if (args.length > 2) {
				compiler.setOutputServerFolder(new File(args[2].replace('\\', '/')));
			}
		}

		try {
			compiler.execute();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean verbose;
	private File inputRoot;
	private File outputClientFolder;
	private File outputServerFolder;
	private String clientPrefix;
	private String serverPrefix;

	public File getInputRoot() {
		return this.inputRoot;
	}

	public void setInputRoot(File value) {
		this.inputRoot = value;
	}

	public File getOutputClientFolder() {
		return this.outputClientFolder;
	}

	public void setOutputClientFolder(File value) {
		this.outputClientFolder = value;
	}

	public File getOutputServerFolder() {
		return this.outputServerFolder;
	}

	public void setOutputServerFolder(File value) {
		this.outputServerFolder = value;
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
		if (this.inputRoot == null || !this.inputRoot.exists()) {
			System.err.println("Error: no input files found: "+this.inputRoot);
			return false;
		}

		if (this.outputClientFolder == null) {
			this.outputClientFolder = this.inputRoot.getParentFile();
		}

		if (this.outputServerFolder == null) {
			this.outputServerFolder = this.inputRoot.getParentFile();
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

		List<File> inputFiles = findFiles(this.inputRoot);
		if (inputFiles.size() < 1) {
			System.err.println("this.inputRoot.getAbsolutePath(): Error: no input files found");
			return;
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
				System.err.println(inputFile.getAbsolutePath()+": Syntax error: no view found");
				return;
			}

			// TODO: allow setting properties from args
			CodeGenSettings settings = new CodeGenSettings();
			settings.setNewline(System.getProperty("line.separator"));

			// compact client-side
			settings.setConvertLineEndings(false);
			settings.setNormalizeWhitespace(true);
			settings.setNamePrefix(this.clientPrefix);

			CodeGenerator codegen = new ClientCodeGen(settings);
			try {
				File outputFile = new File(this.outputClientFolder, inputFile.getName()+codegen.getFileExtension());
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
			settings.setNamePrefix(this.serverPrefix);

			codegen = new ServerCodeGen(settings);
			for (VIEWCommandNode view : views) {
				try {
					File outputFile = new File(this.outputServerFolder, settings.getFullName(view.getName()).replace('.', '/')+codegen.getFileExtension());
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

	private static List<File> findFiles(File inputRoot) {

		List<File> files = new ArrayList<File>();
		Queue<File> folders = new LinkedList<File>();
		folders.add(inputRoot);

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
