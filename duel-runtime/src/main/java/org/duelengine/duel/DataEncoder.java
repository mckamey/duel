package org.duelengine.duel;

import java.io.*;
import java.util.*;

/**
 * Utility for writing data as JavaScript literals
 * Inherently thread-safe as contains no mutable instance data.
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

	public static Snippet asSnippet(String text) {
		return new Snippet(text);
	}

	private final boolean prettyPrint;
	private final String indent;
	private final String newline;

	public DataEncoder() {
		this(null, null);
	}

	public DataEncoder(String newline, String indent) {
		this.newline = (newline != null) ? newline : "";
		this.indent = (indent != null) ? indent : "";
		this.prettyPrint = (this.indent.length() > 0) || (this.newline.length() > 0);
	}

	public boolean isPrettyPrint() {
		return this.prettyPrint;
	}

	/**
	 * Serializes the data as JavaScript literals
	 * @param output
	 * @param data Data to serialize
	 * @throws IOException
	 */
	public void write(Appendable output, Object data)
		throws IOException {

		if (data instanceof SparseMap) {
			return;
		}
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
			this.writeArray(output, DuelData.coerceCollection(data), depth);

		} else if (Date.class.equals(dataType)) {
			this.writeDate(output, (Date)data);

			// need to also serialize RegExp literals

		} else {
			this.writeObject(output, DuelData.coerceMap(data), depth);
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

	@SuppressWarnings("unused")
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
			if (data instanceof SparseMap) {
				continue;
			}

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
			Object value = property.getValue();
			if (value instanceof SparseMap) {
				continue;
			}

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
			this.write(output, value, depth);
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

		if (JSUtility.isValidIdentifier(name, false)) {
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

		output.append('\'');

		for (int i=start; i<length; i++) {
			String escape;

			char ch = data.charAt(i);
			switch (ch) {
				case '\'':
					escape = "\\'";
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

		output.append('\'');
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
			if (this.prettyPrint) {
				output.append(' ');
			}
			output.append('=');
			if (this.prettyPrint) {
				output.append(' ');
			}
			output.append(ns);
			if (this.prettyPrint) {
				output.append(' ');
			}
			output.append("||");
			if (this.prettyPrint) {
				output.append(' ');
			}
			output.append("{};");

			// next iteration
			nextDot = ident.indexOf('.', nextDot+1);
			this.writeln(output, 0);
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
				this.writeln(output, 0);
				output.append("var ");
				output.append(ns);
				output.append(';');
				isRoot = false;
			}

			this.writeln(output, 0);
			output.append("if (typeof ");
			output.append(ns);
			output.append(" === 'undefined') {");
			this.writeln(output, 1);
			output.append(ns);
			output.append(" = {};");
			this.writeln(output, 0);
			output.append('}');

			// next iteration
			nextDot = ident.indexOf('.', nextDot+1);
			needsNewline = true;
		}

		if (needsNewline) {
			this.writeln(output, 0);
		}
	}

	public void indent(Appendable output, int depth)
		throws IOException {

		while (depth-- > 0) {
			output.append(this.indent);
		}
	}

	public void writeln(Appendable output, int depth)
		throws IOException {

		output.append(this.newline);

		while (depth-- > 0) {
			output.append(this.indent);
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
		this.accumulateVars(items, vars, new StringBuilder());

		// emit as a code block of var declarations
		List<String> namespaces = new ArrayList<String>();
		for (Map.Entry<String, Object> externalVar : vars.entrySet()) {
			String key = externalVar.getKey();
			this.writeNamespace(output, namespaces, key);

			if (key.indexOf('.') < 0) {
				output.append("var ");
			}
			output.append(key);
			if (this.prettyPrint) {
				output.append(' ');
			}
			output.append('=');
			if (this.prettyPrint) {
				output.append(' ');
			}
			this.write(output, externalVar.getValue());
			output.append(';');
			this.writeln(output, 0);
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
					this.writeString(buffer, key);
					buffer.append(']');
				}
				Object value = child.getValue();
				if (isSparseMap && !(value instanceof SparseMap)) {
					vars.put(buffer.toString(), value);
				}
				this.accumulateVars(value, vars, buffer);
				buffer.setLength(length);
			}

		} else if (DuelData.isArray(dataType)) {
			int i = 0;
			for (Object child : DuelData.coerceCollection(data)) {
				if (emptyBuffer) {
					buffer.append(ROOT);
				}
				buffer.append('[');
				this.writeNumber(buffer, i++);
				buffer.append(']');
				this.accumulateVars(child, vars, buffer);
				buffer.setLength(length);
			}
		}
	}
}
