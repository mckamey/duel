package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;

public class ClientGen {

	private List<String> namespaces;
	private int depth;
	private String indent = "\t";
	private String newline = "\n";

	public String getIndent() {
		return this.indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public String getNewline() {
		return this.newline;
	}

	public void setNewline(String newline) {
		this.newline = newline;
	}

	/**
	 * Generates client-side code for the given view
	 * @param writer
	 * @param view
	 * @throws Exception
	 */
	public void write(Writer writer, ViewRootNode view)
		throws Exception {

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
	 * @throws Exception
	 */
	public void write(Writer writer, ViewRootNode[] views)
		throws Exception {

		this.write(writer, views != null ? Arrays.asList(views) : null);
	}

	/**
	 * Generates client-side code for the given views
	 * @param writer
	 * @param views
	 * @throws Exception
	 */
	public void write(Writer writer, Iterable<ViewRootNode> views)
		throws Exception {

		if (writer == null) {
			throw new NullPointerException("writer");
		}

		if (views == null) {
			throw new NullPointerException("views");
		}

		PrintWriter pwriter = (writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer);

		pwriter.append("/*global duel */");
		this.writeln(pwriter);

		this.namespaces = JSUtility.cloneBrowserObjects();
		try {
			for (ViewRootNode view : views) {
				if (view == null) {
					continue;
				}
				this.writeNamespaces(pwriter, view);
				this.writeView(pwriter, view);
			}

		} finally {
			this.namespaces = null;
		}
	}

	private void writeNamespaces(PrintWriter writer, ViewRootNode view) {

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
				writer.format("var %1$s;", ns);
			}

			this.writeln(writer);
			writer.format("if (typeof %1$s === \"undefined\") {", ns);
			this.depth++;
			this.writeln(writer);
			writer.format("%1$s = {};", ns);
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

	private void writeView(PrintWriter writer, ViewRootNode view) {
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

	private void writeNode(PrintWriter writer, Node node) {
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

	private void writeCodeBlock(PrintWriter writer, CodeBlockNode node) {
		writer.write(node.getClientCode());
	}

	private void writeElement(PrintWriter writer, String tagName, ElementNode node) {
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

	private void writeString(PrintWriter writer, String value) {
		// improves compatibility within script blocks
		boolean encodeLessThan = false;
		
		if (value == null) {
			writer.write("null");
			return;
		}

		int start = 0,
			length = value.length();

		writer.write('\"');

		for (int i=start; i<length; i++) {
			char ch = value.charAt(i);

			if (ch <= '\u001F' ||
				ch >= '\u007F' ||
				ch == '\"' ||
				ch == '\\' ||
				(encodeLessThan && ch == '<')) {

				if (i > start) {
					writer.write(value, start, i-start);
				}
				start = i+1;

				switch (ch) {
					case '\"':
					case '\\':
						writer.write('\\');
						writer.write(ch);
						continue;
					case '\b':
						writer.write("\\b");
						continue;
					case '\f':
						writer.write("\\f");
						continue;
					case '\n':
						writer.write("\\n");
						continue;
					case '\r':
						writer.write("\\r");
						continue;
					case '\t':
						writer.write("\\t");
						continue;
					default:
						writer.write("\\u");
						writer.format("%04X", value.codePointAt(i));
						continue;
				}
			}
		}
	
		if (length > start) {
			writer.write(value, start, length-start);
		}

		writer.write("\"");
	}

	private void writeln(PrintWriter writer) {
		writer.write(this.newline);

		for (int i=this.depth; i>0; i--) {
			writer.write(this.indent);
		}
	}
}
