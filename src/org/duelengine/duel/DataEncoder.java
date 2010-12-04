package org.duelengine.duel;

import java.io.*;
import java.util.*;

/**
 * Utility for writing data as JavaScript literals
 * Inherently thread-safe as contains no data.
 */
public class DataEncoder {

	public static class Snippet {
		private final String snippet;

		public Snippet(String snippet) {
			this.snippet = snippet;
		}

		public String getSnippet() {
			return this.snippet;
		}
	}

	public static Snippet snippet(String text) {
		return new Snippet(text);
	}

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.JSVocab";
	private static Map<String, Boolean> reserved;
	private static Map<String, Boolean> globals;
	private static boolean inited;

	private final boolean prettyPrint;
	private final String indent;
	private final String newline;

	public DataEncoder() {
		this(null, null);
	}

	public DataEncoder(String newline, String indent) {
		this.indent = (indent != null) ? indent : "";
		this.newline = (newline != null) ? newline : "";
		this.prettyPrint = (this.indent.length() > 0) || (this.newline.length() > 0);
	}

	/**
	 * Serializes the data as JavaScript literals
	 * @param output
	 * @param data Data to serialize
	 * @throws IOException
	 */
	public void write(Appendable output, Object data)
		throws IOException {

		this.write(output, data, 0);
	}

	/**
	 * Serializes the data as JavaScript literals
	 * @param output 
	 * @param data Data to serialize
	 * @param depth Starting indentation depth
	 * @throws IOException
	 */
	public void write(Appendable output, Object data, int depth)
		throws IOException {

		if (data == null) {
			output.append("null");
			return;
		}

		Class<?> dataType = data.getClass();

		if (Snippet.class.equals(dataType)) {
			output.append(((Snippet)data).getSnippet());

		} else if (String.class.equals(dataType)) {
			this.writeString(output, (String)data);

		} else if (DuelData.isNumber(dataType)) {
			this.writeNumber(output, data);

		} else if (Date.class.equals(dataType)) {
			this.writeDate(output, (Date)data);

		} else if (DuelData.isBoolean(dataType)) {
			this.writeBoolean(output, DuelData.coerceBoolean(data));

		} else if (DuelData.isArray(dataType)) {
			this.writeArray(output, DuelData.coerceJSArray(data), depth);

		} else if (Date.class.equals(dataType)) {
			this.writeDate(output, (Date)data);

		} else {
			// need to also serialize RegExp literals
			
			this.writeObject(output, DuelData.coerceJSObject(data), depth);
		}
	}

	private void writeBoolean(Appendable output, boolean data)
		throws IOException {

		output.append(data ? "true" : "false");
	}

	private void writeNumber(Appendable output, Object data)
		throws IOException {

		// format like JavaScript
		double number = ((Number)data).doubleValue();

		Class<?> dataType = data.getClass();

		// TODO: support BigDecimal and BigInteger
		if (Long.class.equals(dataType) || long.class.equals(dataType)) {
			long numberLong = ((Number)data).longValue();

			// if overflows IEEE-754 precision then emit as String
			if (invalidIEEE754(numberLong)) {
				this.writeString(output, Long.toString(numberLong));
			} else {
				output.append(Long.toString(numberLong));
			}
		}

		else if (number == (double)((long)number)) {
			// integers should be formatted without trailing decimals
			output.append(Long.toString((long)number));

		} else {
			// correctly prints NaN, Infinity, -Infinity
			output.append(Double.toString(number));
		}
	}

	private void writeDate(Appendable output, Date data)
		throws IOException {

		output.append("new Date(");

		if (false) {
			// TODO: allow formatting in browser's timezone
			// new Date(yyyy, M, d, H, m, s, ms)
		} else {
			// format as UTC
			output.append(Long.toString(data.getTime()));
		}
		output.append(")");
	}

	private void writeArray(Appendable output, Collection<?> data, int depth)
		throws IOException {

		output.append('[');
		depth++;

		boolean singleAttr = (data.size() == 1);
		boolean hasChildren = singleAttr;
		boolean needsDelim = false;
		for (Object item : data) {
			if (singleAttr) {
				if (this.prettyPrint) {
					output.append(' ');
				}
			} else {
				// property delimiter
				if (needsDelim) {
					output.append(',');
				} else {
					hasChildren = needsDelim = true;
				}

				if (this.prettyPrint) {
					this.writeln(output, depth);
				}
			}

			this.write(output, item, depth);
		}

		depth--;
		if (this.prettyPrint) {
			if (singleAttr) {
				output.append(' ');
			} else if (hasChildren) {
				this.writeln(output, depth);
			}
		}
		output.append(']');
	}

	@SuppressWarnings("unchecked")
	private void writeObject(Appendable output, Map<?, ?> data, int depth)
		throws IOException {

		output.append('{');
		depth++;

		Set<?> properties = data.entrySet();
		boolean singleAttr = (properties.size() == 1);
		boolean hasChildren = singleAttr;
		boolean needsDelim = false;
		for (Map.Entry<?, ?> property : (Set<Map.Entry<?, ?>>)properties) {
			if (singleAttr) {
				if (this.prettyPrint) {
					output.append(' ');
				}
			} else {
				// property delimiter
				if (needsDelim) {
					output.append(',');
				} else {
					hasChildren = needsDelim = true;
				}

				if (this.prettyPrint) {
					this.writeln(output, depth);
				}
			}

			this.writePropertyName(output, property.getKey());
			if (this.prettyPrint) {
				output.append(" : ");
			} else {
				output.append(':');
			}
			this.write(output, property.getValue(), depth);
		}

		depth--;
		if (this.prettyPrint) {
			if (singleAttr) {
				output.append(' ');
			} else if (hasChildren) {
				this.writeln(output, depth);
			}
		}
		output.append('}');
	}

	private void writePropertyName(Appendable output, Object data)
		throws IOException {

		String name = DuelData.coerceString(data);

		if (isValidIdentifier(name, false)) {
			output.append(name);
		} else {
			this.writeString(output, name);
		}
	}

	private void writeString(Appendable output, String data)
		throws IOException {

		if (data == null) {
			output.append("null");
			return;
		}

		int start = 0,
			length = data.length();

		output.append('\"');

		for (int i=start; i<length; i++) {
			String escape;

			char ch = data.charAt(i);
			switch (ch) {
				case '\"':
					escape = "\\\"";
					break;
				case '\\':
					escape = "\\\\";
					break;
				case '\t':
					escape = "\\t";
					break;
				case '\n':
					escape = "\\n";
					break;
				case '\r':
					escape = "\\r";
					break;
				case '\f':
					escape = "\\f";
					break;
				case '\b':
					escape = "\\b";
					break;
				default:
					if (ch >= ' ' && ch < '\u007F') {
						// no need to escape ASCII chars
						continue;
					}

					escape = String.format("\\u%04X", data.codePointAt(i));
					break;
			}

			if (i > start) {
				output.append(data, start, i);
			}
			start = i+1;

			output.append(escape);
		}

		if (length > start) {
			output.append(data, start, length);
		}

		output.append('\"');
	}

	private void writeln(Appendable output, int depth)
		throws IOException {

		output.append(this.newline);

		for (int i=depth; i>0; i--) {
			output.append(this.indent);
		}
	}

	private static boolean isValidIdentifier(String ident, boolean nested) {
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

		// global objects
		tags = (config != null) && config.containsKey("globals") ?
				config.getString("globals").split(",") : new String[0];
		map = new HashMap<String, Boolean>(tags.length);
		for (String value : tags) {
			map.put(value, true);
		}
		globals = map;
	}

	/**
	 * Checks if Number cannot be represented in JavaScript without changing
	 * http://stackoverflow.com/questions/1601646
	 * http://stackoverflow.com/questions/4349155
	 */
	private static boolean invalidIEEE754(long value) {
		if (Long.MAX_VALUE == value || Long.MIN_VALUE == value) {
			// these are technically valid IEEE-754 but JavaScript truncates
			return true;
		}

		try {
			return ((long)((double)value)) != value;
		} catch (Exception ex) {
			return true;
		}
	}
}
