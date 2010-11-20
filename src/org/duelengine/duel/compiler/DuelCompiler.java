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
		File inputPath, outputClientFolder, outputServerFolder;

		if (args.length > 0) {
			inputPath = new File(args[0].replace('\\', '/'));

			if (args.length > 1) {
				outputClientFolder = new File(args[1].replace('\\', '/'));

				if (args.length > 2) {
					outputServerFolder = new File(args[2].replace('\\', '/'));
				} else {
					outputServerFolder = outputClientFolder;
				}
			} else {
				outputServerFolder = outputClientFolder = inputPath.getParentFile();
			}
		} else {
			System.out.println(HELP);
			return;
		}

		compile(inputPath, outputClientFolder, outputServerFolder);
	}

	/**
	 * Compiles a view file
	 * @param inputRoot
	 * @param outputClientFolder
	 * @param outputServerFolder
	 */
	public static void compile(File inputRoot, File outputClientFolder, File outputServerFolder) {
		if (!inputRoot.exists()) {
			System.err.println("Error: input not found: "+inputRoot.getAbsolutePath());
			return;
		}

		List<File> inputFiles = findFiles(inputRoot);
		if (inputFiles.size() < 1) {
			System.err.println("Error: no input files found: "+inputRoot.getAbsolutePath());
			return;
		}

		for (File inputFile : inputFiles) {
			List<VIEWCommandNode> views;
			try {
				FileReader reader = new FileReader(inputFile);
				views = new DuelParser().parse(new DuelLexer(reader));
	
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
	
			if (views == null || views.size() < 1) {
				System.err.println("Syntax error: no view found in: "+inputFile.getAbsolutePath());
				return;
			}

			// TODO: allow setting properties from args
			CodeGenSettings settings = new CodeGenSettings();
			settings.setNewline(System.getProperty("line.separator"));
			settings.setConvertLineEndings(true);

			CodeGenerator codegen = new ClientCodeGen(settings);
			try {
				File outputFile = new File(outputClientFolder, inputFile.getName()+codegen.getFileExtension());
				outputFile.getParentFile().mkdirs();
	
				FileWriter writer = new FileWriter(outputFile, false);
				try {
					codegen.write(writer, views);
				} finally {
					writer.flush();
					writer.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			codegen = new ServerCodeGen(settings);
			for (VIEWCommandNode view : views) {
				try {
					File outputFile = new File(outputServerFolder, view.getName().replace('.', '/')+codegen.getFileExtension());
					outputFile.getParentFile().mkdirs();
	
					FileWriter writer = new FileWriter(outputFile, false);
					try {
						codegen.write(writer, view);
					} finally {
						writer.flush();
						writer.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
