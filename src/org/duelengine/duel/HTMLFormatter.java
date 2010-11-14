package org.duelengine.duel;

import java.io.*;

/**
 * A simple abstraction for writing HTML.
 * Fully thread-safe as contains no data.
 */
public class HTMLFormatter {

	public void writeComment(Appendable output, String value)
		throws IOException {

		output.append("<!--");
		this.writeLiteral(output, value, false, false);
		output.append("-->");
	}

	public void writeDocType(Appendable output, String value)
		throws IOException {

		output.append("<!doctype");
		output.append(value);
		output.append(">");
	}

	public void writeOpenElementBeginTag(Appendable output, String tagName)
		throws IOException {

		output.append('<');
		output.append(tagName);
	}

	public void writeOpenAttribute(Appendable output, String name)
		throws IOException {

		output.append(' ');
		output.append(name);
		output.append("=\"");
	}

	public void writeCloseAttribute(Appendable output)
		throws IOException {

		output.append('"');
	}

	public void writeAttribute(Appendable output, String name, String value)
		throws IOException {

		output.append(' ');
		output.append(name);
		if (value != null) {
			output.append("=\"");
			this.writeLiteral(output, value, true, true);
			output.append('"');
		}
	}

	public void writeCloseElementBeginTag(Appendable output)
		throws IOException {

		output.append('>');
	}

	public void writeCloseElementVoidTag(Appendable output)
		throws IOException {

		output.append(" />");
	}

	public void writeElementEndTag(Appendable output, String tagName)
		throws IOException {

		output.append("</");
		output.append(tagName);
		output.append('>');
	}

	public void writeLiteral(Appendable output, String value)
		throws IOException {

		this.writeLiteral(output, value, false, false);
	}

	public void writeLiteral(Appendable output, String value, boolean encodeNonASCII)
		throws IOException {

		this.writeLiteral(output, value, false, encodeNonASCII);
	}

	private void writeLiteral(Appendable output, String value, boolean isAttribute, boolean encodeNonASCII)
		throws IOException {

		if (value == null) {
			return;
		}

		int start = 0,
			length = value.length();

		for (int i=start; i<length; i++) {
			String entity;

			// TODO: evaluate other common symbols OR expand to include all HTML 4 entities?
			char ch = value.charAt(i);
			switch (ch) {
				case '\t':
				case '\n':
				case '\r':
					if (!isAttribute) {
						// no need to encode
						continue;
					}
	
					entity = String.format("&#x%04X", value.codePointAt(i));
					break;
				case '&':
					entity = "&amp;";
					break;
				case '<':
					entity = "&lt;";
					break;
				case '>':
					entity = "&gt;";
					break;
				case '"':
					if (!isAttribute) {
						// no need to encode
						continue;
					}

				    entity = "&quot;";
				    break;
				case '\u00A0':
					entity = "&nbsp;";
					break;
				case '\u00A9':
					entity = "&copy;";
					break;
				case '\u2122':
					entity = "&trade;";
					break;
				case '\u00AE':
					entity = "&reg;";
					break;
				case '\u00E9':
					entity = "&eacute;";
					break;
				case '\u2026':
					entity = "&hellip;";
					break;
				case '\u00AD':
					entity = "&shy;";
					break;
				case '\u00B7':
					entity = "&moddot;";
					break;
				default:
					// encode control chars and optionally all non-ASCII
					if ((ch < ' ') ||
						(ch >= '\u007F' && (encodeNonASCII || ch <= '\u0084')) ||
						(ch >= '\u0086' && ch <= '\u009F') ||
						(ch >= '\uFDD0' && ch <= '\uFDEF')) {

						// encode chars as hex char refs
						entity = String.format("&#x%04X;", value.codePointAt(i));
						break;
					}

					// no need to encode
					continue;
			}

			if (i > start) {
				// emit any leading unescaped chunk
				output.append(value, start, i);
			}
			start = i+1;

			// emit character reference
			output.append(entity);
		}

		if (length > start) {
			if (start == 0) {
				// nothing escaped can write entire string directly
				output.append(value);

			} else {
				// emit any trailing unescaped chunk
				output.append(value, start, length);
			}
		}
	}
}
