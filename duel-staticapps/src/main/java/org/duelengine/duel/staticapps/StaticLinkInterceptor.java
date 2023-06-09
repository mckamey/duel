package org.duelengine.duel.staticapps;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.duelengine.duel.CDNLinkInterceptor;

class StaticLinkInterceptor extends CDNLinkInterceptor {

	private final Map<String, String> cache = new HashMap<String, String>();
	private final Map<String, String> linksBundle;
	private final int cdnHostPrefix;

	public StaticLinkInterceptor(String cdnHost, ResourceBundle cdnBundle, ResourceBundle linksBundle, boolean isDevMode)
			throws URISyntaxException {

		this(cdnHost, bundleAsMap(cdnBundle, isDevMode), bundleAsMap(linksBundle, isDevMode), isDevMode);
	}

	public StaticLinkInterceptor(String cdnHost, Map<String, String> cdnBundle, Map<String, String> linksBundle, boolean isDevMode)
			throws URISyntaxException {

		super(cdnHost, cdnBundle, isDevMode);

		this.linksBundle = linksBundle;
		this.cdnHostPrefix = (cdnHost == null) ? 0 : cdnHost.length();
	}

	public Map<String, String> getLinkCache() {
		return cache;
	}

	@Override
	public String transformURL(String url) {
		if (url.indexOf("://") > 0 || url.indexOf("//") == 0) {
			// skip absolute URLs
			return url;
		}

		// query and hash
		String suffix = "";
		int query = url.indexOf('?');
		if (query >= 0) {
			suffix += url.substring(query);
			url = url.substring(0, query);
		}
		int hash = url.indexOf('#');
		if (hash >= 0) {
			suffix += url.substring(hash);
			url = url.substring(0, hash);
		}

		if (cache.containsKey(url)) {
			return cache.get(url);
		}

		// intercept requests for transformation
		String cdnURL = super.transformURL(url);

		// collect an accumulated list
		cache.put(url, cdnURL+suffix);

		// recursively transform and cache child links
		if (linksBundle.containsKey(url)) {
			String childLinks = linksBundle.get(url);
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

		if (isDevMode && (cdnURL.length() > cdnHostPrefix) && linksBundle.containsKey(cdnURL.substring(cdnHostPrefix))) {
			// note must trim the CDN host to match
			String childLinks = linksBundle.get(cdnURL.substring(cdnHostPrefix));
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

		return cdnURL+suffix;
	}
}
