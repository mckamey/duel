package org.duelengine.duel.codegen;

import java.io.*;

public class HTMLFormatter {

	private final boolean encodeNonASCII;
	private final Writer writer;

	public HTMLFormatter(Writer writer) {
		this(writer, true);
	}

	public HTMLFormatter(Writer writer, boolean encodeNonASCII) {
		if (writer == null) {
			throw new NullPointerException("writer");
		}

		this.writer = writer;
		this.encodeNonASCII = encodeNonASCII;
	}

	public Writer getWriter() {
		return this.writer;
	}

	public void writeComment(String value)
		throws IOException {

		this.writer.write("<!--");
		this.writeLiteral(value, false);
		this.writer.write("-->");
	}

	public void writeDocType(String value)
		throws IOException {

		this.writer.write("<!doctype");
		this.writer.write(value);
		this.writer.write(">");
	}

	public void writeOpenTagBegin(String tagName)
		throws IOException {

		this.writer.write('<');
		this.writer.write(tagName);
	}

	public void writeAttribute(String name, String value)
		throws IOException {

		this.writer.write(' ');
		this.writer.write(name);
		this.writer.write("=\"");
		this.writeLiteral(value, true);
		this.writer.write('"');
	}

	public void writeOpenTagEnd(String tagName)
		throws IOException {

		this.writer.write('>');
	}

	public void writeCloseTag(String tagName)
		throws IOException {

		this.writer.write("</");
		this.writer.write(tagName);
		this.writer.write('>');
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
				this.writer.write(value, start, i-start);
			}
			start = i+1;

			// emit character reference
			this.writer.write(entity);
		}

		if (length > start) {
			// emit any trailing unescaped chunk
			this.writer.write(value, start, length-start);
		}
	}
}
