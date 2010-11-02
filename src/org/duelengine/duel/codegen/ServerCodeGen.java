package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.duelengine.duel.ast.*;

public class ServerCodeGen implements CodeGenerator {

	private final CodeGenSettings settings;
	private final HTMLFormatter formatter;
	private final StringWriter buffer;

	public ServerCodeGen() {
		this(null);
	}

	public ServerCodeGen(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
		this.buffer = new StringWriter();
		this.formatter = new HTMLFormatter(this.buffer, this.settings.getEncodeNonASCII());
	}
	
	@Override
	public CodeGenSettings getSettings() {
		return this.settings;
	}

	@Override
	public String getFileExtension() {
		return ".java";
	}

	/**
	 * Generates server-side code for the given view
	 * @param writer
	 * @param view
	 * @throws Exception
	 */
	@Override
	public void write(Writer writer, ViewRootNode view) {
		if (view == null) {
			throw new NullPointerException("view");
		}

		List<ViewRootNode> views = new ArrayList<ViewRootNode>();
		views.add(view);

		this.write(writer, views);
	}

	/**
	 * Generates server-side code for the given views
	 * @param writer
	 * @param views
	 * @throws Exception
	 */
	@Override
	public void write(Writer writer, ViewRootNode[] views) {
		this.write(writer, views != null ? Arrays.asList(views) : null);
	}

	/**
	 * Generates server-side code for the given views
	 * @param writer
	 * @param views
	 * @throws Exception
	 */
	@Override
	public void write(Writer writer, Iterable<ViewRootNode> views) {
		if (writer == null) {
			throw new NullPointerException("writer");
		}
		if (views == null) {
			throw new NullPointerException("views");
		}

		PrintWriter pw = (writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer);
		for (ViewRootNode view : views) {
			if (view == null) {
				continue;
			}

			this.writeView(pw, view);
		}
	}

	private void writeView(PrintWriter writer, ViewRootNode view) {
		// TODO.
	}

	private void writeString(PrintWriter writer, String value) {
		if (value == null) {
			writer.write("null");
			return;
		}

		int start = 0,
			length = value.length();

		writer.write('\"');

		for (int i=start; i<length; i++) {
			String escape;

			char ch = value.charAt(i);
			switch (ch) {
				case '\"':
					escape = "\\\"";
					break;
				case '\\':
					escape = "\\\\";
					break;
				case '\n':
					escape = "\\n";
					break;
				case '\r':
					escape = "\\r";
					break;
				case '\t':
					escape = "\\t";
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

					escape = String.format("\\u%04X", value.codePointAt(i));
					break;
			}

			if (i > start) {
				writer.write(value, start, i-start);
			}
			start = i+1;

			writer.write(escape);
		}

		if (length > start) {
			writer.write(value, start, length-start);
		}

		writer.write('\"');
	}

	private String FlushBuffer() {
		StringBuffer sb = this.buffer.getBuffer();

		// get the accumulated value
		String value = sb.toString();

		// clear the buffer
		sb.setLength(0);

		return value;
	}
}
