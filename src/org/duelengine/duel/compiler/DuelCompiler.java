package org.duelengine.duel.compiler;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codegen.*;
import org.duelengine.duel.parsing.*;

public class DuelCompiler {

	private static final String HELP =
		"Usage:\n" +
		"\tjava -jar duel.jar <input-file>\n" +
		"\tjava -jar duel.jar <input-file> <output-folder>\n" +
		"\tjava -jar duel.jar <input-file> <output-client-folder> <output-server-folder>\n\n"+
		"\tinput-file: path to the DUEL input file (e.g. foo.duel)\n"+
		"\toutput-folder: path to the output folder\n"+
		"\toutput-client-folder: path to the view scripts folder\n"+
		"\toutput-server-folder: path to the source code folder\n";

	public static void main(String[] args) {
		File inputFile, outputClientFolder, outputServerFolder;

		if (args.length > 0) {
			inputFile = new File(args[0].replace('\\', '/'));

			if (args.length > 1) {
				outputClientFolder = new File(args[1].replace('\\', '/'));

				if (args.length > 2) {
					outputServerFolder = new File(args[2].replace('\\', '/'));
				} else {
					outputServerFolder = outputClientFolder;
				}
			} else {
				outputServerFolder = outputClientFolder = inputFile.getParentFile();
			}
		} else {
			System.out.println(HELP);
			return;
		}

		compile(inputFile, outputClientFolder, outputServerFolder);
	}

	/**
	 * Compiles a view file
	 * @param inputFile
	 * @param outputClientFolder
	 * @param outputServerFolder
	 */
	public static void compile(File inputFile, File outputClientFolder, File outputServerFolder) {
		if (!inputFile.exists()) {
			System.err.println("Error: input not found: "+inputFile.getAbsolutePath());
			return;
		}

		List<ViewRootNode> views;
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

		CodeGenerator codegen = new ClientCodeGen();
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

		codegen = new ServerCodeGen();
		for (ViewRootNode view : views) {
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
