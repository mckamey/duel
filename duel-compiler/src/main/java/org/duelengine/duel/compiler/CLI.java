package org.duelengine.duel.compiler;

import java.io.IOException;

public class CLI {

	private static final String HELP =
		"Usage:\n" +
		"\tjava -jar duel-compiler.jar <input-file|input-folder>\n" +
		"\tjava -jar duel-compiler.jar <input-file|input-folder> <output-folder>\n" +
		"\tjava -jar duel-compiler.jar <input-file|input-folder> <output-client-folder> <output-server-folder>\n\n"+
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
		compiler.setInputDir(args[0]);

		if (args.length > 1) {
			compiler.setOutputClientDir(args[1]);

			if (args.length > 2) {
				compiler.setOutputServerDir(args[2]);
			}
		}

		try {
			compiler.execute();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
