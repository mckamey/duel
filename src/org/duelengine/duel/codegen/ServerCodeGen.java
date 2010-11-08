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
	 * @param output
	 * @param views
	 * @throws IOException 
	 */
	@Override
	public void write(Appendable output, ViewRootNode[] views)
		throws IOException {

		this.write(output, views != null ? Arrays.asList(views) : null);
	}

	/**
	 * Generates server-side code for the given views
	 * @param output
	 * @param views
	 * @throws IOException
	 */
	@Override
	public void write(Appendable output, Iterable<ViewRootNode> views)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}
		if (views == null) {
			throw new NullPointerException("views");
		}

		for (ViewRootNode view : views) {
			if (view != null) {
				this.writeView(output, view);
			}
		}
	}

	private void writeView(Appendable output, ViewRootNode view)
		throws IOException {

		CodeTypeDeclaration viewType = new CodeDOMBuilder(this.settings).build(view);

		// TODO: generate source from CodeDOM
	}

	private void writeString(Appendable output, String value)
		throws IOException {

		if (value == null) {
			output.append("null");
			return;
		}

		int start = 0,
			length = value.length();

		output.append('\"');

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
				output.append(value, start, i);
			}
			start = i+1;

			output.append(escape);
		}

		if (length > start) {
			output.append(value, start, length);
		}

		output.append('\"');
	}
}
