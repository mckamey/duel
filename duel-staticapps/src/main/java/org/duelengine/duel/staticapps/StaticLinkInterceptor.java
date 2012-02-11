package org.duelengine.duel.staticapps;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.duelengine.duel.CDNLinkInterceptor;

class StaticLinkInterceptor extends CDNLinkInterceptor {

	private final Map<String, String> cache = new HashMap<String, String>();
	private final ResourceBundle linksBundle;

	public StaticLinkInterceptor(String cdnHost, ResourceBundle cdnBundle, ResourceBundle linksBundle, boolean isDevMode)
			throws URISyntaxException {

		super(cdnHost, cdnBundle, isDevMode);

		// TODO: replace with: bundleAsMap(linksBundle, isDevMode)
		this.linksBundle = linksBundle;
	}

	public Map<String, String> getLinkCache() {
		return cache;
	}

	@Override
	public String transformURL(String url) {
		if (cache.containsKey(url)) {
			return cache.get(url);
		}

		// intercept requests for transformation
		String cdnURL = super.transformURL(url);

		// collect an accumulated list
		cache.put(url, cdnURL);

		// recursively transform and cache child links
		if (linksBundle != null && linksBundle.containsKey(url)) {
			String childLinks = linksBundle.getString(url);
			if (childLinks != null && !childLinks.isEmpty()) {
				for (String child : childLinks.split("\\|")) {
					if (child == null || child.isEmpty()) {
						continue;
					}

					// ignore result, we only care about caching
					this.transformURL(child);
				}
			}
		}

		return cdnURL;
	}
}
