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
	public CodeGenSettings getSettings() {
		return this.settings;
	}

	@Override
	public String getFileExtension() {
		return ".js";
	}

	/**
	 * Generates client-side code for the given view
	 * @param writer
	 * @param view
	 * @throws IOException
	 */
	@Override
	public void write(Writer writer, ViewRootNode view)
		throws IOException {

		if (view == null) {
			throw new NullPointerException("view");
		}

		List<ViewRootNode> views = new ArrayList<ViewRootNode>();
		views.add(view);

		this.write(writer, views);
	}

	/**
	 * Generates client-side code for the given views
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
	 * Generates client-side code for the given views
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

		writer.write("/*global duel */");
		this.writeln(writer);

		this.namespaces = JSUtility.cloneBrowserObjects();
		for (ViewRootNode view : views) {
			if (view == null) {
				continue;
			}
			this.writeNamespaces(writer, view);
			this.writeView(writer, view);
		}
		this.namespaces = null;
	}

	private void writeNamespaces(Writer writer, ViewRootNode view)
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
				this.writeln(writer);
				writer.write("var ");
				writer.write(ns);
				writer.write(';');
			}

			this.writeln(writer);
			writer.write("if (typeof ");
			writer.write(ns);
			writer.write(" === \"undefined\") {");
			this.depth++;
			this.writeln(writer);
			writer.write(ns);
			writer.write(" = {};");
			this.depth--;
			this.writeln(writer);
			writer.write('}');

			if (!nsEmitted) {
				nsEmitted = true;
			}
		}

		if (nsEmitted) {
			this.writeln(writer);
		}
	}

	private void writeView(Writer writer, ViewRootNode view)
		throws IOException {

		this.depth = 0;
		this.writeln(writer);

		String viewName = view.getName();
		if (viewName.indexOf('.') < 0) {
			writer.write("var ");
		}
		writer.write(viewName);
		writer.write(" = duel(");

		if (view.childCount() == 1) {
			Node child = view.getFirstChild();
			if (child instanceof ElementNode &&
				((ElementNode)child).hasChildren()) {
				this.depth++;
				this.writeln(writer);
			}

			// just emit the single child
			this.writeNode(writer, child);
		} else {
			// wrap in a document fragment
			this.writeElement(writer, "", view);
		}

		writer.write(");");
		this.depth = 0;
		this.writeln(writer);
	}

	private void writeNode(Writer writer, Node node)
		throws IOException {

		if (node instanceof LiteralNode) {
			this.writeString(writer, ((LiteralNode)node).getValue());
			return;
		}

		if (node instanceof ElementNode) {
			this.writeElement(writer, ((ElementNode)node).getTagName(), (ElementNode)node);
			return;
		}

		if (node instanceof CodeBlockNode) {
			this.writeCodeBlock(writer, (CodeBlockNode)node);
		}
	}

	private void writeCodeBlock(Writer writer, CodeBlockNode node)
		throws IOException {

		writer.write(node.getClientCode());
	}

	private void writeElement(Writer writer, String tagName, ElementNode node)
		throws IOException {

		writer.write('[');
		this.depth++;

		this.writeString(writer, tagName);

		if (node.hasAttributes()) {
			Set<String> attrs = node.getAttributeNames();
			boolean singleAttr = (attrs.size() == 1);

			writer.write(", {");
			this.depth++;

			boolean needsDelim = false;
			for (String attr : attrs) {
				// property delimiter
				if (needsDelim) {
					writer.write(',');
				} else {
					needsDelim = true;
				}

				if (singleAttr) {
					writer.write(' ');
				} else {
					this.writeln(writer);
				}

				this.writeString(writer, attr);
				writer.write(" : ");
				this.writeNode(writer, node.getAttribute(attr));
			}

			this.depth--;
			if (singleAttr) {
				writer.write(' ');
			} else {
				this.writeln(writer);
			}
			writer.write('}');
		}

		for (Node child : node.getChildren()) {
			writer.write(',');
			this.writeln(writer);
			this.writeNode(writer, child);
		}

		this.depth--;
		if (node.hasChildren()) {
			this.writeln(writer);
		}
		writer.write(']');
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

	private void writeln(Writer writer)
		throws IOException {

		writer.write(this.settings.getNewline());

		for (int i=this.depth; i>0; i--) {
			writer.write(this.settings.getIndent());
		}
	}
}
