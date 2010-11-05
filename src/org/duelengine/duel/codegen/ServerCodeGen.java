package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeGen implements CodeGenerator {

	private final CodeGenSettings settings;

	public ServerCodeGen() {
		this(null);
	}

	public ServerCodeGen(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
	}

	@Override
	public String getFileExtension() {
		return ".java";
	}

	/**
	 * Generates server-side code for the given views
	 * @param writer
	 * @param views
	 * @throws IOException 
	 */
	@Override
	public void write(Writer writer, ViewRootNode[] views)
		throws IOException {

		this.write(writer, views != null ? Arrays.asList(views) : null);
	}

	/**
	 * Generates server-side code for the given views
	 * @param writer
	 * @param views
	 * @throws IOException
	 */
	@Override
	public void write(Writer writer, Iterable<ViewRootNode> views)
		throws IOException {

		if (writer == null) {
			throw new NullPointerException("writer");
		}
		if (views == null) {
			throw new NullPointerException("views");
		}

		for (ViewRootNode view : views) {
			if (view != null) {
				this.writeView(writer, view);
			}
		}
	}

	private void writeView(Writer writer, ViewRootNode view)
		throws IOException {

		CodeTypeDeclaration viewType = new CodeDOMBuilder(this.settings).build(view);
	}

	private void writeString(Writer writer, String value)
		throws IOException {

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
}
