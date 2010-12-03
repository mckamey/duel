package org.duelengine.duel;

import java.io.*;
import java.util.*;

/**
 * Utility for writing data as JavaScript literals
 */
public class DataFormatter {

	private final boolean prettyPrint;
	private final String indent;
	private final String newline;
	private int depth;

	public DataFormatter() {
		this(true, "\t", "\n");
	}

	public DataFormatter(boolean prettyPrint) {
		this(prettyPrint, "\t", "\n");
	}

	public DataFormatter(boolean prettyPrint, String indent, String newline) {
		this.prettyPrint = prettyPrint;
		this.indent = (indent != null) ? indent : "";
		this.newline = (newline != null) ? newline : "";
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

		this.depth = depth;
		this.writeData(output, data);
	}

	private void writeData(Appendable output, Object data)
		throws IOException {

		if (data == null) {
			output.append("null");
			return;
		}

		Class<?> dataType = data.getClass();

		if (String.class.equals(dataType)) {
			this.writeString(output, (String)data);

		} else if (DuelData.isNumber(dataType)) {
			this.writeNumber(output, data);

		} else if (Date.class.equals(dataType)) {
			this.writeDate(output, (Date)data);

		} else if (DuelData.isBoolean(dataType)) {
			this.writeBoolean(output, DuelData.coerceBoolean(data));

		} else if (DuelData.isArray(dataType)) {
			this.writeArray(output, DuelData.coerceJSArray(data));

		} else if (Date.class.equals(dataType)) {
			this.writeDate(output, (Date)data);

		} else {
			// need to also serialize RegExp literals
			
			this.writeObject(output, DuelData.coerceJSObject(data));
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
			if (this.invalidIEEE754(numberLong)) {
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

		// TODO: allow choosing between UTC and "browser-timezone"
		if (false) {
			// format as browser-timezone
		} else {
			// format as UTC
			output.append(Long.toString(data.getTime()));
		}
		output.append(")");
	}

	private void writeArray(Appendable output, Collection<?> data)
		throws IOException {

		output.append('[');
		this.depth++;

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
					this.writeln(output);
				}
			}

			this.writeData(output, item);
		}

		this.depth--;
		if (this.prettyPrint) {
			if (singleAttr) {
				output.append(' ');
			} else if (hasChildren) {
				this.writeln(output);
			}
		}
		output.append(']');
	}

	@SuppressWarnings("unchecked")
	private void writeObject(Appendable output, Map<?, ?> data)
		throws IOException {

		output.append('{');
		this.depth++;

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
					this.writeln(output);
				}
			}

			this.writePropertyName(output, property.getKey());
			if (this.prettyPrint) {
				output.append(" : ");
			} else {
				output.append(':');
			}
			this.writeData(output, property.getValue());
		}

		this.depth--;
		if (this.prettyPrint) {
			if (singleAttr) {
				output.append(' ');
			} else if (hasChildren) {
				this.writeln(output);
			}
		}
		output.append('}');
	}

	private void writePropertyName(Appendable output, Object data)
		throws IOException {

		String name = DuelData.coerceString(data);

		if (false) {
			// write directly if doesn't need quoting
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

	private void writeln(Appendable output)
		throws IOException {

		output.append(this.newline);

		for (int i=this.depth; i>0; i--) {
			output.append(this.indent);
		}
	}

	/**
	 * Checks if Number cannot be represented in JavaScript without changing
	 * http://stackoverflow.com/questions/1601646
	 * http://stackoverflow.com/questions/4349155
	 */
	private boolean invalidIEEE754(long value) {
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
