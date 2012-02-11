package org.duelengine.duel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDNLinkInterceptor implements LinkInterceptor {

	private static final Logger log = LoggerFactory.getLogger(CDNLinkInterceptor.class);
	protected final boolean isDevMode;
	protected final String cdnHost;
	private final Map<String, String> cdnMap;

	/**
	 * @param cdnHost the CDN server hostname (e.g., "cdn.example.com")
	 * @param cdnMap a mapping of paths to their compacted form (e.g., "/foo.js" => "/foo.min.js")
	 * @param isDevMode uses normal paths unless compacted form has a dev form (e.g., "/foo.js" => "/foo.min.js" => "/foo.test.js", else "/foo.js")
	 * @throws URISyntaxException
	 */
	public CDNLinkInterceptor(String cdnHost, ResourceBundle cdnBundle, boolean isDevMode)
			throws URISyntaxException {

		this(cdnHost, bundleAsMap(cdnBundle, isDevMode), isDevMode);
	}

	/**
	 * @param cdnHost the CDN server hostname (e.g., "cdn.example.com")
	 * @param cdnMap a mapping of paths to their compacted form (e.g., "/foo.js" => "/foo.min.js")
	 * @param isDevMode uses normal paths unless compacted form has a dev form (e.g., "/foo.js" => "/foo.min.js" => "/foo.test.js", else "/foo.js")
	 * @throws URISyntaxException
	 */
	public CDNLinkInterceptor(String cdnHost, Map<String, String> cdnMap, boolean isDevMode)
			throws URISyntaxException {

		if (cdnMap == null) {
			cdnMap = Collections.emptyMap();
		}

		this.isDevMode = isDevMode;
		this.cdnMap = cdnMap;

		// use URI class to check for proper host syntax
		this.cdnHost = formatURL(cdnHost);

		log.info("cdnHost="+this.cdnHost);
		log.info("isDevMode="+this.isDevMode);
	}

	private static String formatURL(String path) {
		path = path == null ? "" : path.trim();
		if (path.isEmpty() || path.equals("/")) {
			return "";
		}

		if (!path.startsWith(".")) {
			try {
				int index = path.indexOf('/');
				if (index < 0) {
					path = new URI("http", path, null, null).getRawSchemeSpecificPart();
				} else {
					path = new URI("http", path.substring(0, index), path.substring(index), null).getRawSchemeSpecificPart();
				}
	
			} catch (URISyntaxException ex) {
				log.error(ex.getMessage(), ex);
			}

			if (!path.startsWith("/")) {
				path = "//"+path;
			}
		}

		if (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}

		return path;
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

		// CDN resources are compacted and optionally served from a different root
		return this.cdnHost + cdnURL;
	}

	/**
	 * Converts ResourceBundle to a Map<String, String>
	 * @param cdnBundle
	 * @return
	 */
	protected static Map<String, String> bundleAsMap(final ResourceBundle bundle, boolean isDevMode) {

		if (bundle == null) {
			return null;
		}
		
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