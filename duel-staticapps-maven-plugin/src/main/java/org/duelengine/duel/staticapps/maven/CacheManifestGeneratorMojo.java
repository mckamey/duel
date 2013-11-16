package org.duelengine.duel.staticapps.maven;

import org.duelengine.duel.staticapps.SiteBuilder;
import org.duelengine.duel.staticapps.SiteConfig;

/**
 * Generates cache manifests from DUEL-based WAR
 *
 * @goal manifests
 * @phase prepare-package
 */
public class CacheManifestGeneratorMojo extends SiteMojo {

	@Override
	protected void execute(SiteConfig config, ClassLoader classLoader)
			throws Exception {

		// generate any manifests defined by the config
		new SiteBuilder(classLoader).generateManifests(config);
	}
}
