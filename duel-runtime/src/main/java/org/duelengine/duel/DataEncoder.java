package org.duelengine.duel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Utility for writing data as ECMAScript literals or JSON
 * Inherently thread-safe as contains no mutable instance data.
 */
public class DataEncoder {

	public static class Snippet {
		private final String snippet;

		public Snippet(String value) {
			snippet = value;
		}
		
		public String getSnippet() {
			return snippet;
		}
	}

	public static Snippet asSnippet(String text) {
		return new Snippet(text);
	}

	private enum EncodingFormat {
		ECMASCRIPT,
		JSON
	}

	private final static TimeZone UTC = TimeZone.getTimeZone("UTC");
	private final boolean prettyPrint;
	private final String indent;
	private final String newline;
	
	public DataEncoder() {
		this(null, null);
	}

	public DataEncoder(String newlineStr, String indentStr) {
		newline = (newlineStr != null) ? newlineStr : "";
		indent = (indentStr != null) ? indentStr : "";
		prettyPrint = (indent.length() > 0) || (newline.length() > 0);
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * Serializes the data as ECMAScript literals
	 * @param data Data to serialize
	 * @return encoded data
	 */
	public String encode(Object data) {

		StringBuilder buffer = new StringBuilder();
		try {
			write(buffer, data, EncodingFormat.ECMASCRIPT, 0);

		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}

		return buffer.toString();
	}

	/**
	 * Serializes the data as JSON
	 * @param data Data to serialize
	 * @return encoded data
	 */
	public String encodeJSON(Object data) {

		StringBuilder buffer = new StringBuilder();
		try {
			write(buffer, data, EncodingFormat.JSON, 0);

		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
		return buffer.toString();
	}

	/**
	 * Serializes the data as JSON
	 * @param output
	 * @param data Data to serialize
	 * @throws IOException
	 */
	public void writeJSON(Appendable output, Object data)
		throws IOException {

		write(output, data, EncodingFormat.JSON, 0);
	}

	/**
	 * Serializes the data as ECMAScript literals
	 * @param output
	 * @param data Data to serialize
	 * @throws IOException
	 */
	public void write(Appendable output, Object data)
		throws IOException {

		if (data instanceof SparseMap) {
			/**
			 * SparseMap collections aren't true JS objects
			 * but rather a set of property assignments which are
			 * emitted over the top of existing intrinsic objects.
			 * this is handled by the {@link #writeVars(Appendable, SparseMap)}
			 */
			return;
		}
		write(output, data, EncodingFormat.ECMASCRIPT, 0);
	}

	/**
	 * Serializes the data as ECMAScript literals
	 * @param output 
	 * @param data Data to serialize
	 * @param depth Starting indentation depth
	 * @throws IOException
	 */
	public void write(Appendable output, Object data, int depth)
		throws IOException {

		write(output, data, EncodingFormat.ECMASCRIPT, depth);
	}

	/**
	 * Serializes the data as ECMAScript literals or JSON
	 * @param output 
	 * @param data Data to serialize
	 * @param format Encoding format compatibility mode
	 * @param depth Starting indentation depth
	 * @throws IOException
	 */
	private void write(Appendable output, Object data, EncodingFormat format, int depth)
		throws IOException {

		if (data == null) {
			output.append("null");
			return;
		}
		if (data == JSUtility.UNDEFINED) {
			output.append(JSUtility.UNDEFINED_KEYWORD);
			return;
		}

		if (format == null) {
			format = EncodingFormat.ECMASCRIPT;
		}

		Class<?> dataType = data.getClass();

		if (Snippet.class.equals(dataType)) {
			output.append(((Snippet)data).getSnippet());

		} else if (String.class.equals(dataType)) {
			writeString(output, (String)data, format);

		} else if (DuelData.isNumber(dataType)) {
			writeNumber(output, data, format);

		} else if (DuelData.isBoolean(dataType)) {
			writeBoolean(output, DuelData.coerceBoolean(data), format);

		} else if (DuelData.isArray(dataType)) {
			writeArray(output, DuelData.coerceCollection(data), format, depth);

		} else if (Date.class.equals(dataType)) {
			writeDate(output, (Date)data, format);

			// need to also serialize RegExp literals

		} else {
			writeObject(output, DuelData.coerceMap(data), format, depth);
		}
	}

	private void writeBoolean(Appendable output, boolean data, EncodingFormat format)
		throws IOException {

		output.append(data ? "true" : "false");
	}

	private void writeNumber(Appendable output, Object data, EncodingFormat format)
		throws IOException {

		// format like JavaScript
		double number = ((Number)data).doubleValue();

		Class<?> dataType = data.getClass();

		// TODO: support BigDecimal and BigInteger
		if (Long.class.equals(dataType) || long.class.equals(dataType)) {
			long numberLong = ((Number)data).longValue();

			// if overflows IEEE-754 precision then emit as String
			if (invalidIEEE754(numberLong)) {
				// TODO: allow disabling this behavior for non-ECMAScript clients
				writeString(output, Long.toString(numberLong), format);

			} else {
				output.append(Long.toString(numberLong));
			}

		} else if (number == (double)((long)number)) {
			// integers should be formatted without trailing decimals
			output.append(Long.toString((long)number));

		} else {
			// correctly prints NaN, Infinity, -Infinity
			output.append(Double.toString(number));
		}
	}

	@SuppressWarnings("unused")
	private void writeDate(Appendable output, Date data, EncodingFormat format)
		throws IOException {

		if (format == EncodingFormat.ECMASCRIPT) {
			output.append("new Date(");

			if (false) {
				// TODO: allow formatting in browser's timezone
				// new Date(yyyy, M, d, H, m, s, ms)

			} else {
				// format as UTC
				output.append(Long.toString(data.getTime()));
			}
			output.append(")");

		} else {
			// http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
			// Date formats are not synchronized. It is recommended to create separate format instances for each thread.
			// If multiple threads access a format concurrently, it must be synchronized externally.
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
			dateFormat.setTimeZone(UTC);
			writeString(output, dateFormat.format(data), format);
		}
	}

	private void writeArray(Appendable output, Collection<?> data, EncodingFormat format, int depth)
		throws IOException {

		output.append('[');
		depth++;

		boolean singleAttr = (data.size() == 1);
		boolean hasChildren = singleAttr;
		boolean needsDelim = false;
		for (Object item : data) {
			if (data instanceof SparseMap) {
				// TODO: describe why
				continue;
			}

			if (singleAttr) {
				if (prettyPrint) {
					output.append(' ');
				}

			} else {
				// property delimiter
				if (needsDelim) {
					output.append(',');
				} else {
					hasChildren = needsDelim = true;
				}

				if (prettyPrint) {
					writeln(output, depth);
				}
			}

			write(output, item, format, depth);
		}

		depth--;
		if (prettyPrint) {
			if (singleAttr) {
				output.append(' ');
			} else if (hasChildren) {
				writeln(output, depth);
			}
		}
		output.append(']');
	}

	@SuppressWarnings("unchecked")
	private void writeObject(Appendable output, Map<?, ?> data, EncodingFormat format, int depth)
		throws IOException {

		output.append('{');
		depth++;

		Set<?> properties = data.entrySet();
		boolean singleAttr = (properties.size() == 1);
		boolean hasChildren = singleAttr;
		boolean needsDelim = false;
		for (Map.Entry<?, ?> property : (Set<Map.Entry<?, ?>>)properties) {
			Object value = property.getValue();
			if (value instanceof SparseMap) {
				// TODO: describe why
				continue;
			}

			if (singleAttr) {
				if (prettyPrint) {
					output.append(' ');
				}
			} else {
				// property delimiter
				if (needsDelim) {
					output.append(',');
				} else {
					hasChildren = needsDelim = true;
				}

				if (prettyPrint) {
					writeln(output, depth);
				}
			}

			writePropertyName(output, property.getKey(), format);
			if (prettyPrint) {
				output.append(": ");
			} else {
				output.append(':');
			}
			write(output, value, format, depth);
		}

		depth--;
		if (prettyPrint) {
			if (singleAttr) {
				output.append(' ');
			} else if (hasChildren) {
				writeln(output, depth);
			}
		}
		output.append('}');
	}

	private void writePropertyName(Appendable output, Object data, EncodingFormat format)
		throws IOException {

		String name = DuelData.coerceString(data);

		if (format == EncodingFormat.ECMASCRIPT && JSUtility.isValidIdentifier(name, false)) {
			output.append(name);

		} else {
			writeString(output, name, format);
		}
	}

	private void writeString(Appendable output, String data, EncodingFormat format)
		throws IOException {

		if (data == null) {
			output.append("null");
			return;
		}

		int start = 0,
			length = data.length();

		if (format == EncodingFormat.JSON) {
			output.append('"');

		} else {
			output.append('\'');
		}

		for (int i=start; i<length; i++) {
			String escape;

			char ch = data.charAt(i);
			switch (ch) {
				case '\'':
					if (format == EncodingFormat.JSON) {
						continue;
					}
					escape = "\\'";
					break;
				case '"':
					if (format != EncodingFormat.JSON) {
						continue;
					}
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
						// no need to escape printable ASCII chars
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

		if (format == EncodingFormat.JSON) {
			output.append('"');

		} else {
			output.append('\'');
		}
	}

	/**
	 * Produces more compact namespace declarations.
	 * @param output
	 * @param namespaces
	 * @param ident
	 * @return true if namespaces were emitted
	 * @throws IOException
	 */
	public boolean writeNamespace(Appendable output, List<String> namespaces, String ident)
		throws IOException {

		if (!JSUtility.isValidIdentifier(ident, true)) {
			throw new IllegalArgumentException("Invalid identifier: "+ident);
		}

		boolean wroteNS = false;
		boolean isRoot = true;
		int nextDot = ident.indexOf('.');
		while (nextDot > -1) {
			String ns = ident.substring(0, nextDot);

			// check if already exists
			if ((isRoot && JSUtility.isGlobalIdent(ns)) || namespaces.contains(ns)) {
				// next iteration
				nextDot = ident.indexOf('.', nextDot+1);
				isRoot = false;
				continue;
			}
			namespaces.add(ns);

			if (isRoot) {
				output.append("var ");
				isRoot = false;
			}

			output.append(ns);
			if (prettyPrint) {
				output.append(' ');
			}
			output.append('=');
			if (prettyPrint) {
				output.append(' ');
			}
			output.append(ns);
			if (prettyPrint) {
				output.append(' ');
			}
			output.append("||");
			if (prettyPrint) {
				output.append(' ');
			}
			output.append("{};");

			// next iteration
			nextDot = ident.indexOf('.', nextDot+1);
			writeln(output, 0);
			wroteNS = true;
		}

		return wroteNS;
	}

	/**
	 * Produces more verbose but technically more correct namespace declarations
	 * @param output
	 * @param namespaces
	 * @param ident
	 * @throws IOException
	 */
	@Deprecated
	public void writeNamespaceAlt(Appendable output, List<String> namespaces, String ident)
		throws IOException {

		if (!JSUtility.isValidIdentifier(ident, true)) {
			throw new IllegalArgumentException("Invalid identifier: "+ident);
		}

		boolean needsNewline = false;
		boolean isRoot = true;
		int nextDot = ident.indexOf('.');
		while (nextDot > -1) {
			String ns = ident.substring(0, nextDot);

			// check if already exists
			if ((isRoot && JSUtility.isGlobalIdent(ns)) || namespaces.contains(ns)) {
				// next iteration
				nextDot = ident.indexOf('.', nextDot+1);
				isRoot = false;
				continue;
			}
			namespaces.add(ns);

			if (isRoot) {
				writeln(output, 0);
				output.append("var ");
				output.append(ns);
				output.append(';');
				isRoot = false;
			}

			writeln(output, 0);
			output.append("if (typeof ");
			output.append(ns);
			output.append(" === 'undefined') {");
			writeln(output, 1);
			output.append(ns);
			output.append(" = {};");
			writeln(output, 0);
			output.append('}');

			// next iteration
			nextDot = ident.indexOf('.', nextDot+1);
			needsNewline = true;
		}

		if (needsNewline) {
			writeln(output, 0);
		}
	}

	public void indent(Appendable output, int depth)
		throws IOException {

		while (depth-- > 0) {
			output.append(indent);
		}
	}

	public void writeln(Appendable output, int depth)
		throws IOException {

		output.append(newline);

		while (depth-- > 0) {
			output.append(indent);
		}
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

	/**
	 * Serializes the items as JavaScript variable
	 * @param output 
	 * @param items Variables to serialize
	 * @throws IOException
	 */
	public void writeVars(Appendable output, SparseMap items)
		throws IOException {

		// begin by flattening the heirarchy whenever a SparseMap is encountered
		Map<String, Object> vars = new LinkedHashMap<String, Object>();
		accumulateVars(items, vars, new StringBuilder());

		// emit as a code block of var declarations
		List<String> namespaces = new ArrayList<String>();
		for (Map.Entry<String, Object> externalVar : vars.entrySet()) {
			String key = externalVar.getKey();
			writeNamespace(output, namespaces, key);

			if (key.indexOf('.') < 0) {
				output.append("var ");
			}
			output.append(key);
			if (prettyPrint) {
				output.append(' ');
			}
			output.append('=');
			if (prettyPrint) {
				output.append(' ');
			}
			write(output, externalVar.getValue());
			output.append(';');
			writeln(output, 0);
		}
	}

	private void accumulateVars(Object data, Map<String, Object> vars, StringBuilder buffer)
		throws IOException {

		if (data == null) {
			return;
		}

		final String ROOT = "window";
		int length = buffer.length();
		boolean emptyBuffer = (length < 1);
		Class<?> dataType = data.getClass();

		if (Map.class.isAssignableFrom(dataType)) {
			boolean isSparseMap = SparseMap.class.equals(dataType);
			for (Map.Entry<?,?> child : ((Map<?,?>)data).entrySet()) {
				String key = DuelData.coerceString(child.getKey());
				if (JSUtility.isValidIdentifier(key, false)) {
					if (!emptyBuffer) {
						buffer.append('.');
					}
					buffer.append(key);
				} else {
					if (emptyBuffer) {
						buffer.append(ROOT);
					}
					buffer.append('[');
					writeString(buffer, key, EncodingFormat.ECMASCRIPT);
					buffer.append(']');
				}
				Object value = child.getValue();
				if (isSparseMap && !(value instanceof SparseMap)) {
					vars.put(buffer.toString(), value);
				}
				accumulateVars(value, vars, buffer);
				buffer.setLength(length);
			}

		} else if (DuelData.isArray(dataType)) {
			int i = 0;
			for (Object child : DuelData.coerceCollection(data)) {
				if (emptyBuffer) {
					buffer.append(ROOT);
				}
				buffer.append('[');
				writeNumber(buffer, i++, EncodingFormat.ECMASCRIPT);
				buffer.append(']');
				accumulateVars(child, vars, buffer);
				buffer.setLength(length);
			}
		}
	}
}
