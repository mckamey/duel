package org.duelengine.duel.staticapps.maven;

import org.duelengine.duel.staticapps.SiteBuilder;
import org.duelengine.duel.staticapps.SiteConfig;

/**
 * Generates static app from DUEL-based WAR
 *
 * @goal generate
 * @phase package
 */
public class SiteGeneratorMojo extends SiteMojo {

	@Override
	protected void execute(SiteConfig config, ClassLoader classLoader)
			throws Exception {

		// generate a static site defined by the config
		new SiteBuilder(classLoader).build(config);
	}
}
