package org.duelengine.duel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
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
	public CDNLinkInterceptor(String cdnHost, ResourceBundle cdnBundle, boolean isDevMode) throws URISyntaxException {
		this(cdnHost, bundleAsMap(cdnBundle, isDevMode), isDevMode);
	}

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

	/**
	 * Converts ResourceBundle to a Map<String, String>
	 * @param cdnBundle
	 * @return
	 */
	private static Map<String, String> bundleAsMap(final ResourceBundle bundle, boolean isDevMode) {

		if (!isDevMode) {
			// dump key-value pairs into simple map
			Set<String> keys = bundle.keySet();
			Map<String, String> map = new HashMap<String, String>(keys.size());
			for (String key : keys) {
				map.put(key, bundle.getString(key));
			}
			return map;
		}

		// wrap bundle with an adaptor to allow dynamic reloading of bundle
		return new Map<String, String>() {

			@Override
			public void clear() {}

			@Override
			public boolean containsKey(Object arg0) {
				return bundle.containsKey((String)arg0);
			}

			@Override
			public boolean containsValue(Object arg0) { return false; }

			@Override
			public Set<java.util.Map.Entry<String, String>> entrySet() { return null; }

			@Override
			public String get(Object arg0) {
				return (String)bundle.getObject((String)arg0);
			}

			@Override
			public boolean isEmpty() { return false; }

			@Override
			public Set<String> keySet() { return null; }

			@Override
			public String put(String arg0, String arg1) { return null; }

			@Override
			public void putAll(Map<? extends String, ? extends String> arg0) {}

			@Override
			public String remove(Object arg0) { return null; }

			@Override
			public int size() { return 0; }

			@Override
			public Collection<String> values() { return null; }
		};
	}
}