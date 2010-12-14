package org.duelengine.duel;

import java.io.*;

/**
 * A simple abstraction for writing HTML.
 * Inherently thread-safe as contains no instance data.
 */
public class HTMLFormatter {

	public HTMLFormatter writeComment(Appendable output, String value)
		throws IOException {

		final String OPEN = "<!--";
		final String CLOSE = "-->";

		output.append(OPEN);
		if (value != null) {
			int start = 0,
				length = value.length();

			int close = value.indexOf(CLOSE);
			while (close >= 0) {
				output.append(value, start, close+2).append("&gt;");
				start = close+3;
				close = value.indexOf(CLOSE, start);
			}

			if (start < length) {
				output.append(value, start, length);
			}
		}
		output.append(CLOSE);

		return this;
	}

	public HTMLFormatter writeDocType(Appendable output, String value)
		throws IOException {

		final String OPEN = "<!DOCTYPE";
		final char CLOSE = '>';

		output.append(OPEN);
		if (value != null && value.length() > 0) {
			output.append(' ').append(value);
		}
		output.append(CLOSE);

		return this;
	}

	public HTMLFormatter writeOpenElementBeginTag(Appendable output, String tagName)
		throws IOException {

		output.append('<').append(tagName);

		return this;
	}

	public HTMLFormatter writeOpenAttribute(Appendable output, String name)
		throws IOException {

		output.append(' ').append(name).append("=\"");

		return this;
	}

	public HTMLFormatter writeCloseAttribute(Appendable output)
		throws IOException {

		output.append('"');

		return this;
	}

	public HTMLFormatter writeAttribute(Appendable output, String name, String value)
		throws IOException {

		output.append(' ').append(name);
		if (value != null) {
			output.append("=\"");
			this.writeLiteral(output, value, true, true);
			output.append('"');
		}

		return this;
	}

	public HTMLFormatter writeCloseElementBeginTag(Appendable output)
		throws IOException {

		output.append('>');

		return this;
	}

	public HTMLFormatter writeCloseElementVoidTag(Appendable output)
		throws IOException {

		output.append(" />");

		return this;
	}

	public HTMLFormatter writeElementEndTag(Appendable output, String tagName)
		throws IOException {

		output.append("</").append(tagName).append('>');

		return this;
	}

	public HTMLFormatter writeLiteral(Appendable output, String value)
		throws IOException {

		return this.writeLiteral(output, value, false, false);
	}

	public HTMLFormatter writeLiteral(Appendable output, String value, boolean encodeNonASCII)
		throws IOException {

		return this.writeLiteral(output, value, false, encodeNonASCII);
	}

	private HTMLFormatter writeLiteral(Appendable output, String value, boolean isAttribute, boolean encodeNonASCII)
		throws IOException {

		if (value == null) {
			return this;
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

		return this;
	}
}
