package org.duelengine.duel;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codegen.*;
import org.duelengine.duel.parsing.*;

public class DuelCompiler {

	private static final String HELP =
		"Usage:\n" +
		"\tjava -jar duel.jar <filename>";

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(HELP);
			return;
		}

		String filename = args[0].replace('\\', '/');

		List<ViewRootNode> views;
		try {
			FileReader reader = new FileReader(filename);
			views = new DuelParser().parse(new DuelLexer(reader));

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		if (views == null || views.size() < 1) {
			System.err.println("Syntax error: no view found in: "+filename);
			return;
		}

		CodeGenerator codegen;
		FileWriter writer ;
		try {
			codegen = new ClientCodeGen();
			writer = new FileWriter(filename+codegen.getFileExtension(), false);
			codegen.write(writer, views);
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String outputPath = filename.substring(0, filename.lastIndexOf('/')+1);

		for (ViewRootNode view : views) {
			try {
				codegen = new ServerCodeGen();
				File file = new File(outputPath+view.getName().replace('.', '/')+codegen.getFileExtension());
				file.getParentFile().mkdirs();
				writer = new FileWriter(file, false);
				codegen.write(writer, view);
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
