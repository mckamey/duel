package org.duelengine.duel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

public class CDNLinkInterceptor implements LinkInterceptor {

	private final boolean isDevMode;
	private final String cdnHost;
	private final Map<String, String> cdnMap;

	/**
	 * @param cdnHost the CDN server hostname (e.g., "cdn.example.com")
	 * @param cdnMap a mapping of paths to their compacted form (e.g., "/foo.js" => "/foo.min.js")
	 * @param isDevMode uses normal paths unless compacted form has a dev form (e.g., "/foo.js" => "/foo.min.js" => "/foo.test.js", else "/foo.js")
	 * @throws URISyntaxException
	 */
	public CDNLinkInterceptor(String cdnHost, Map<String, String> cdnMap, boolean isDevMode) throws URISyntaxException {

		this.isDevMode = isDevMode;
		this.cdnMap = cdnMap;

		// use URI class to check for proper host syntax
		this.cdnHost =
			(cdnHost == null || cdnHost.isEmpty()) ?
			"" :
			new URI("http", cdnHost, null, null).getRawSchemeSpecificPart();

		Logger log = Logger.getLogger(CDNLinkInterceptor.class.getCanonicalName());
		log.config("cdnHost="+this.cdnHost);
		log.config("isDevModet="+this.isDevMode);
	}

	public String transformURL(String url) {
		if (!this.cdnMap.containsKey(url)) {
			return url;
		}

		// lookup CDN resource
		String cdnURL = this.cdnMap.get(url);

		if (this.isDevMode) {
			if (!this.cdnMap.containsKey(cdnURL)) {
				// scripts & stylesheets served directly
				return url;
			}

			// merge files serve generated placeholder
			cdnURL = this.cdnMap.get(cdnURL);
		}

		// CDN resources are compacted and optionally served from a differen host
		return this.cdnHost + cdnURL;
	}
}