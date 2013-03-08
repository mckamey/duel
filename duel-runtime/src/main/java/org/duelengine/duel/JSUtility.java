package org.duelengine.duel;

import java.util.*;

public final class JSUtility {

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.JSUtility";
	private static Set<String> reserved;
	private static Set<String> globals;
	private static Set<String> properties;
	private static Set<String> browser;
	private static boolean inited;

	// static class
	private JSUtility() {}

	public static boolean isValidIdentifier(String ident, boolean nested) {
		if (ident == null || ident.isEmpty()) {
			return false;
		}

		if (!inited) {
			initLookups();
		}

		if (nested) {
			String[] parts = ident.split(".");
			for (int i=0, length=parts.length; i<length; i++) {
				if (!isValidIdentifier(parts[i], false))
				{
					return false;
				}
				if (i == 0 && globals.contains(parts[i])) {
					return false;
				}
			}
			return true;
		}

		if (reserved.contains(ident)) {
			return false;
		}

		boolean indentPart = false;
		for (int i=0, length=ident.length(); i<length; i++) {
			char ch = ident.charAt(i);
			if (indentPart && ((ch >= '0') && (ch <= '9'))) {
				// digits are only allowed after first char
				continue;
			}

			// can be start or part
			if (((ch >= 'a') && (ch <= 'z')) ||
				((ch >= 'A') && (ch <= 'Z')) ||
				(ch == '_') || (ch == '$')) {
				indentPart = true;
				continue;
			}

			return false;
		}

		return true;		
	}

	public static boolean isGlobalIdent(String ident) {
		if (ident == null || ident.isEmpty()) {
			return false;
		}

		if (!inited) {
			initLookups();
		}

		return globals.contains(ident) || browser.contains(ident);
	}

	public static boolean isObjectProperty(String ident) {
		if (ident == null || ident.isEmpty()) {
			return false;
		}

		if (!inited) {
			initLookups();
		}

		return properties.contains(ident);
	}
	
	private static void initLookups() {

		String[] tags;
		Set<String> set;

		// definitions maintained in JSVocab.properties
		ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE, Locale.ROOT);

		// reserved words
		tags = (config != null) && config.containsKey("reserved") ?
				config.getString("reserved").split(",") : new String[0];
		set = new HashSet<String>(tags.length);
		for (String value : tags) {
			set.add(value);
		}

		// add Object properties to reserved words
		tags = (config != null) && config.containsKey("properties") ?
				config.getString("properties").split(",") : new String[0];
		for (String value : tags) {
			set.add(value);
		}
		reserved = set;

		// global objects
		tags = (config != null) && config.containsKey("globals") ?
				config.getString("globals").split(",") : new String[0];
		set = new HashSet<String>(tags.length);
		for (String value : tags) {
			set.add(value);
		}
		globals = set;

		// object properties
		tags = (config != null) && config.containsKey("properties") ?
				config.getString("properties").split(",") : new String[0];
		set = new HashSet<String>(tags.length);
		for (String value : tags) {
			set.add(value);
		}
		properties = set;

		// browser objects
		tags = (config != null) && config.containsKey("browser") ?
				config.getString("browser").split(",") : new String[0];
		set = new HashSet<String>(tags.length);
		for (String value : tags) {
			set.add(value);
		}
		browser = set;
	}
}
