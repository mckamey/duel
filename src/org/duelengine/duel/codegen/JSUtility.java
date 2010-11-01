package org.duelengine.duel.codegen;

import java.util.*;

final class JSUtility {

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.codegen.JSVocab";
	private static Map<String, Boolean> reserved;
	private static Map<String, Boolean> globals;
	private static List<String> browserObjects;
	private static boolean inited;

	// static class
	private JSUtility() {}

	public static List<String> cloneBrowserObjects() {
		if (!inited) {
			initLookups();
		}

		return new ArrayList<String>(browserObjects);
	}

	public static boolean isValidIdentifier(String ident, boolean nested) {
		if (ident == null || ident.length() == 0) {
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
				if (i == 0 && globals.containsKey(parts[i])) {
					return false;
				}
			}
			return true;
		}

		if (reserved.containsKey(ident)) {
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

	private static void initLookups() {

		String[] tags;
		Map<String, Boolean> map;

		// definitions maintained in JSVocab.properties
		ResourceBundle config = ResourceBundle.getBundle(CONFIG_RESOURCE);

		// reserved words
		tags = (config != null) && config.containsKey("reserved") ?
				config.getString("reserved").split(",") : new String[0];
		map = new HashMap<String, Boolean>(tags.length);
		for (String value : tags) {
			map.put(value, true);
		}

		// add Object properties to reserved words
		tags = (config != null) && config.containsKey("properties") ?
				config.getString("properties").split(",") : new String[0];
		for (String value : tags) {
			map.put(value, true);
		}
		reserved = map;

		// globals objects
		tags = (config != null) && config.containsKey("globals") ?
				config.getString("globals").split(",") : new String[0];
		map = new HashMap<String, Boolean>(tags.length);
		for (String value : tags) {
			map.put(value, true);
		}
		globals = map;

		// browser objects
		tags = (config != null) && config.containsKey("browser") ?
				config.getString("browser").split(",") : new String[0];
		browserObjects = Arrays.asList(tags);
	}
}
