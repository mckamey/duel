package org.duelengine.duel.staticapps;

import java.io.File;
import java.io.FileNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CLI {

	private static final String HELP = "java -jar duel-static-apps.jar <config>\n"+
			"  <config>: path to configuration file\n";

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(HELP);
			return;
		}

		File configFile = new File(args[0]);
		
		try {
			if (!configFile.isFile()) {
				throw new FileNotFoundException(configFile.getPath());
			}

			// read config
			SiteConfig config = new ObjectMapper().reader(SiteConfig.class).readValue(configFile);

			// build site defined by config
			new SiteBuilder().build(config);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
