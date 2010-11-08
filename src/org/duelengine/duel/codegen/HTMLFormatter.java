package org.duelengine.duel.codegen;

import java.io.*;

public class HTMLFormatter {

	private final boolean encodeNonASCII;
	private final Appendable output;

	public HTMLFormatter(Appendable output) {
		this(output, true);
	}

	public HTMLFormatter(Appendable output, boolean encodeNonASCII) {
		if (output == null) {
			throw new NullPointerException("output");
		}

		this.output = output;
		this.encodeNonASCII = encodeNonASCII;
	}

	public Appendable getOutput() {
		return this.output;
	}

	public void writeComment(String value)
		throws IOException {

		this.output.append("<!--");
		this.writeLiteral(value, false);
		this.output.append("-->");
	}

	public void writeDocType(String value)
		throws IOException {

		this.output.append("<!doctype");
		this.output.append(value);
		this.output.append(">");
	}

	public void writeOpenElementBeginTag(String tagName)
		throws IOException {

		this.output.append('<');
		this.output.append(tagName);
	}

	public void writeOpenAttribute(String name)
		throws IOException {

		this.output.append(' ');
		this.output.append(name);
		this.output.append("=\"");
	}

	public void writeCloseAttribute()
		throws IOException {

		this.output.append('"');
	}

	public void writeAttribute(String name, String value)
		throws IOException {

		this.output.append(' ');
		this.output.append(name);
		if (value != null) {
			this.output.append("=\"");
			this.writeLiteral(value, true);
			this.output.append('"');
		}
	}

	public void writeCloseElementBeginTag()
		throws IOException {

		this.output.append('>');
	}

	public void writeCloseElementVoidTag()
		throws IOException {

		this.output.append(" />");
	}

	public void writeElementEndTag(String tagName)
		throws IOException {

		this.output.append("</");
		this.output.append(tagName);
		this.output.append('>');
	}

	public void writeLiteral(String value)
		throws IOException {

		this.writeLiteral(value, false);
	}

	private void writeLiteral(String value, boolean isAttribute)
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
						(ch >= '\u007F' && (this.encodeNonASCII || ch <= '\u0084')) ||
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
				this.output.append(value, start, i);
			}
			start = i+1;

			// emit character reference
			this.output.append(entity);
		}

		if (length > start) {
			if (start == 0) {
				// nothing escaped can write entire string directly
				this.output.append(value);

			} else {
				// emit any trailing unescaped chunk
				this.output.append(value, start, length);
			}
		}
	}
}
