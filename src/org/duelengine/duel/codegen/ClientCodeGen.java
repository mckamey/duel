package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;

public class ClientCodeGen implements CodeGenerator {

	private List<String> namespaces;
	private int depth;
	private final CodeGenSettings settings;

	public ClientCodeGen() {
		this(null);
	}

	public ClientCodeGen(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
	}

	@Override
	public String getFileExtension() {
		return ".js";
	}

	/**
	 * Generates client-side code for the given views
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
	 * Generates client-side code for the given views
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

		output.append("/*global duel */");
		this.writeln(output);

		this.namespaces = JSUtility.cloneBrowserObjects();
		for (ViewRootNode view : views) {
			if (view == null) {
				continue;
			}
			this.writeNamespaces(output, view);
			this.writeView(output, view);
		}
		this.namespaces = null;
	}

	private void writeNamespaces(Appendable output, ViewRootNode view)
		throws IOException {

		String ident = view.getName();
		if (!JSUtility.isValidIdentifier(ident, true)) {
			// TODO: syntax error
			throw new IllegalArgumentException("Invalid view name: "+ident);
		}

		boolean nsEmitted = false;
		StringBuilder buffer = new StringBuilder(ident.length());
		String[] parts = ident.split("\\.");
		for (int i=0, length=parts.length-1; i<length; i++) {
			if (i > 0) {
				buffer.append('.');
			}
			buffer.append(parts[i]);

			String ns = buffer.toString();
			if (this.namespaces.contains(ns)) {
				continue;
			}
			this.namespaces.add(ns);

			if (i == 0) {
				this.writeln(output);
				output.append("var ");
				output.append(ns);
				output.append(';');
			}

			this.writeln(output);
			output.append("if (typeof ");
			output.append(ns);
			output.append(" === \"undefined\") {");
			this.depth++;
			this.writeln(output);
			output.append(ns);
			output.append(" = {};");
			this.depth--;
			this.writeln(output);
			output.append('}');

			if (!nsEmitted) {
				nsEmitted = true;
			}
		}

		if (nsEmitted) {
			this.writeln(output);
		}
	}

	private void writeView(Appendable output, ViewRootNode view)
		throws IOException {

		this.depth = 0;
		this.writeln(output);

		String viewName = view.getName();
		if (viewName.indexOf('.') < 0) {
			output.append("var ");
		}
		output.append(viewName);
		output.append(" = duel(");

		if (view.childCount() == 1) {
			Node child = view.getFirstChild();
			if (child instanceof ElementNode &&
				((ElementNode)child).hasChildren()) {
				this.depth++;
				this.writeln(output);
			}

			// just emit the single child
			this.writeNode(output, child);
		} else {
			// wrap in a document fragment
			this.writeElement(output, "", view);
		}

		output.append(");");
		this.depth = 0;
		this.writeln(output);
	}

	private void writeNode(Appendable output, Node node)
		throws IOException {

		if (node instanceof LiteralNode) {
			this.writeString(output, ((LiteralNode)node).getValue());
			return;
		}

		if (node instanceof ElementNode) {
			this.writeElement(output, ((ElementNode)node).getTagName(), (ElementNode)node);
			return;
		}

		if (node instanceof CodeBlockNode) {
			this.writeCodeBlock(output, (CodeBlockNode)node);
		}
	}

	private void writeCodeBlock(Appendable output, CodeBlockNode node)
		throws IOException {

		output.append(node.getClientCode());
	}

	private void writeElement(Appendable output, String tagName, ElementNode node)
		throws IOException {

		output.append('[');
		this.depth++;

		this.writeString(output, tagName);

		if (node.hasAttributes()) {
			Set<String> attrs = node.getAttributeNames();
			boolean singleAttr = (attrs.size() == 1);

			output.append(", {");
			this.depth++;

			boolean needsDelim = false;
			for (String attr : attrs) {
				// property delimiter
				if (needsDelim) {
					output.append(',');
				} else {
					needsDelim = true;
				}

				if (singleAttr) {
					output.append(' ');
				} else {
					this.writeln(output);
				}

				this.writeString(output, attr);
				output.append(" : ");
				this.writeNode(output, node.getAttribute(attr));
			}

			this.depth--;
			if (singleAttr) {
				output.append(' ');
			} else {
				this.writeln(output);
			}
			output.append('}');
		}

		for (Node child : node.getChildren()) {
			output.append(',');
			this.writeln(output);
			this.writeNode(output, child);
		}

		this.depth--;
		if (node.hasChildren()) {
			this.writeln(output);
		}
		output.append(']');
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

	private void writeln(Appendable output)
		throws IOException {

		output.append(this.settings.getNewline());

		for (int i=this.depth; i>0; i--) {
			output.append(this.settings.getIndent());
		}
	}
}
