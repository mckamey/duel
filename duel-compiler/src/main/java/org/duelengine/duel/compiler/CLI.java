package org.duelengine.duel.compiler;

public class CLI {

	private static final String SEPARATOR = "========================================";
	private static final String HELP = "java -jar duel-compiler.jar\n"+
			"  --help                       : this help text\n"+
			"  -in <source-file|source-dir> : file path to the source file or folder (required)\n"+
			"  -client-out <target-dir>     : file path to the target output directory (default: <source-dir>)\n"+
			"  -server-out <target-dir>     : file path to the target output directory (default: <source-dir>)\n"+
			"  -client-prefix <package>     : client-side package name\n"+
			"  -server-prefix <package>     : server-side package name\n";

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(HELP);
			return;
		}

		DuelCompiler compiler = new DuelCompiler();
		System.out.println(SEPARATOR);
		System.out.println("DUEL compiler");
		for (int i=0; i<args.length; i++) {
			String arg = args[i];

			if ("-in".equals(arg)) {
				compiler.setInputDir(args[++i]);

			} else if ("-client-out".equals(arg)) {
				compiler.setOutputClientDir(args[++i]);

			} else if ("-server-out".equals(arg)) {
				compiler.setOutputServerDir(args[++i]);

			} else if ("-client-prefix".equals(arg)) {
				compiler.setClientPrefix(args[++i]);

			} else if ("-server-prefix".equals(arg)) {
				compiler.setServerPrefix(args[++i]);

			} else if ("--help".equalsIgnoreCase(arg)) {
				System.out.println(HELP);
				System.out.println(SEPARATOR);
				return;

			} else {
				System.out.println(HELP);
				System.out.println(SEPARATOR);
				return;
			}
		}

		try {
			compiler.execute();

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		System.out.println(SEPARATOR);
	}
}
