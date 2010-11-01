package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;

public class ClientGen {

	private static final String GLOBALS =
		"/*global duel */\n";

	private static final String NS_ROOT =
		"\nvar %1$s;";

	private static final String NS_CHECK =
		"\nif (typeof %1$s === \"undefined\") {\n\t%1$s = {};\n}";

	private static final String VAR =
		"\nvar ";

	private static final String VIEW_BEGIN =
		"%1$s = duel(";

	private static final String VIEW_END =
		");";

	private boolean encodeLessThan;

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
		this.writeNamespaces(pwriter, views);

		for (ViewRootNode view : views) {
			this.writeView(pwriter, view);
		}
	}

	private void writeNamespaces(PrintWriter writer, Iterable<ViewRootNode> views)
		throws IOException {

		writer.append(GLOBALS);
		
		List<String> namespaces = JSUtility.cloneBrowserObjects();

		for (ViewRootNode view : views) {
			if (view == null) {
				continue;
			}

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
				if (namespaces.contains(ns)) {
					continue;
				}
				namespaces.add(ns);

				if (i == 0) {
					writer.format(NS_ROOT, ns);
				}

				writer.format(NS_CHECK, ns);
				nsEmitted = true;
			}

			if (nsEmitted) {
				writer.println();
			}
		}
	}

	private void writeView(PrintWriter writer, ViewRootNode view) {
		String viewName = view.getName();
		if (viewName.indexOf('.') < 0) {
			writer.write(VAR);
		} else {
			writer.write('\n');
		}
		writer.format(VIEW_BEGIN, viewName);

		if (view.childCount() == 1) {
			this.writeNode(writer, view.getFirstChild());
		} else {
			// emit as a document fragment
			this.writeElement(writer, "", view);
		}

		writer.write(VIEW_END);
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
		writer.write("[");

		this.writeString(writer, tagName);

		if (node.hasAttributes()) {
			writer.write(",{");
			for (String attr : node.getAttributeNames()) {
				this.writeString(writer, attr);
				writer.write(":");
				this.writeNode(writer, node.getAttribute(attr));
			}
			writer.write("}");
		}
		
		for (Node child : node.getChildren()) {
			writer.write(",");
			this.writeNode(writer, child);
		}
		
		writer.write("]");
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
			char ch = value.charAt(i);

			if (ch <= '\u001F' ||
				ch >= '\u007F' ||
				ch == '\"' ||
				ch == '\\' ||
				(this.encodeLessThan && ch == '<')) { // improves compatibility within script blocks

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
}
