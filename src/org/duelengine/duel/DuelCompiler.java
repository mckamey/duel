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

		String filename = args[0];

		List<ViewRootNode> views;
		try {
			FileReader reader = new FileReader(filename);
			views = new DuelParser().parse(new DuelLexer(reader));

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		CodeGenerator codegen;
		FileWriter writer ;
		try {
			codegen = new ClientCodeGen();
			writer = new FileWriter(filename+codegen.getFileExtension());
			codegen.write(writer, views);
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			codegen = new ServerCodeGen();
			writer = new FileWriter(filename+codegen.getFileExtension());
			codegen.write(writer, views);
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
