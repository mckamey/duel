package org.duelengine.duel.ast;

import java.util.HashMap;
import java.util.Map;

/**
 * Enables specialized handling of meta content attributes,
 * specifically for interception of URLs for transformation.
 */
public class MetaElementNode extends ElementNode {
	public static final String NAME = "meta";
	private static final Map<String,String> linkProperties;

	static {
		// TODO: move these into a properties resource
		linkProperties = new HashMap<String, String>();

		// Open Graph
		// http://ogp.me/
		linkProperties.put("og:url", "property");
		linkProperties.put("og:image", "property");
		linkProperties.put("og:image:url", "property");
		linkProperties.put("og:image:secure_url", "property");
		linkProperties.put("og:video", "property");
		linkProperties.put("og:video:url", "property");
		linkProperties.put("og:video:secure_url", "property");
		linkProperties.put("og:audio", "property");
		linkProperties.put("og:audio:url", "property");
		linkProperties.put("og:audio:secure_url", "property");

		// Schema.org microdata
		// http://schema.org/docs/gs.html
		linkProperties.put("image", "itemprop");

		// Twitter product card
		// https://dev.twitter.com/docs/cards/types/product-card
		linkProperties.put("twitter:image", "name");
		linkProperties.put("twitter:image:src", "name");
		linkProperties.put("twitter:image0", "name");
		linkProperties.put("twitter:image1", "name");
		linkProperties.put("twitter:image2", "name");
		linkProperties.put("twitter:image3", "name");
		linkProperties.put("twitter:player", "name");
		linkProperties.put("twitter:player:stream", "name");
	}

	public MetaElementNode(int index, int line, int column) {
		super(NAME, index, line, column);
	}

	public MetaElementNode(AttributePair[] attr, DuelNode[] children) {
		super(NAME, attr, children);
	}

	@Override
	public boolean isLinkAttribute(String name) {
		if ("content".equalsIgnoreCase(name) && hasAttributes()) {

			// unfortunately various providers have messed around with the key attribute name
			String key = "name";
			DuelNode attr = getAttribute(key);
			if (attr == null) {
				key = "property";
				attr = getAttribute(key);

				if (attr == null) {
					key = "itemprop";
					attr = getAttribute(key);
				}
			}

			if (attr instanceof LiteralNode) {
				// test if the property exists for the given key
				String property = ((LiteralNode)attr).getValue();
				if (property != null && key.equalsIgnoreCase(linkProperties.get(property))) {
					return true;
				}
			}
		}

		return super.isLinkAttribute(name);
	}
}
