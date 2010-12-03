package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.parsing.InvalidNodeException;

public class ClientCodeGen implements CodeGenerator {

	private List<String> namespaces;
	private int depth;
	private final CodeGenSettings settings;
	private boolean preMode;

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
	public void write(Appendable output, VIEWCommandNode... views)
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
	public void write(Appendable output, Iterable<VIEWCommandNode> views)
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
		for (VIEWCommandNode view : views) {
			if (view == null) {
				continue;
			}
			this.writeNamespaces(output, view);
			this.writeView(output, view);
		}
		this.namespaces = null;
	}

	private void writeNamespaces(Appendable output, VIEWCommandNode view)
		throws IOException {

		// prepend the client-side prefix
		String ident = this.settings.getFullName(view.getName());
		if (!JSUtility.isValidIdentifier(ident, true)) {
			throw new InvalidNodeException("Invalid view name: "+ident, view.getAttribute("name"));
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

	private void writeView(Appendable output, VIEWCommandNode view)
		throws IOException {

		this.depth = 0;
		this.writeln(output);

		// prepend the client-side prefix
		String viewName = this.settings.getFullName(view.getName());
		if (viewName.indexOf('.') < 0) {
			output.append("var ");
		}
		output.append(viewName);
		output.append(" = duel(");

		if (view.childCount() == 1) {
			DuelNode child = view.getFirstChild();
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

	private void writeNode(Appendable output, DuelNode node)
		throws IOException {

		if (node instanceof LiteralNode) {
			this.writeString(output, ((LiteralNode)node).getValue());

		} else if (node instanceof ElementNode) {
			this.writeElement(output, ((ElementNode)node).getTagName(), (ElementNode)node);

		} else if (node instanceof CodeBlockNode) {
			this.writeCodeBlock(output, (CodeBlockNode)node);

		} else if (node instanceof CommentNode) {
			this.writeSpecialElement(output, "!", ((CommentNode)node).getValue());

		} else if (node instanceof CodeCommentNode) {
			this.writeComment(output, ((CodeCommentNode)node).getValue());

		} else if (node instanceof DocTypeNode) {
			this.writeSpecialElement(output, "!doctype", ((DocTypeNode)node).getValue());

		} else if (node != null) {
			throw new UnsupportedOperationException("Node not yet implemented: "+node.getClass());
		}
	}

	private void writeComment(Appendable output, String value)
		throws IOException {

		output.append("/*");
		if (value != null) {
			output.append(value.replace("*/", "*\\/"));
		}
		output.append("*/");
	}

	private void writeCodeBlock(Appendable output, CodeBlockNode node)
		throws IOException {

		output.append(node.getClientCode());
	}

	private void writeSpecialElement(Appendable output, String name, String value)
		throws IOException {

		output.append('[');
		this.depth++;

		this.writeString(output, name);

		boolean hasValue = (value != null) && (value.length() > 0);
		if (hasValue) {
			output.append(',');
			this.writeln(output);
			this.writeString(output, value);
		}

		this.depth--;
		if (hasValue) {
			this.writeln(output);
		}
		output.append(']');
	}

	private void writeElement(Appendable output, String tagName, ElementNode node)
		throws IOException {

		boolean prevPreMode = this.preMode;
		this.preMode = prevPreMode || "pre".equalsIgnoreCase(tagName) || "script".equalsIgnoreCase(tagName);
		try {
			output.append('[');
			this.depth++;

			this.writeString(output, tagName);

			if (node.hasAttributes()) {
				Set<String> attrs = node.getAttributeNames();
				boolean singleAttr = (attrs.size() == 1);

				output.append(", {");
				this.depth++;

				boolean addPrefix = (node instanceof CALLCommandNode) && this.settings.hasNamePrefix();

				boolean needsDelim = false;
				for (String attr : attrs) {
					if (singleAttr) {
						output.append(' ');
					} else {
						// property delimiter
						if (needsDelim) {
							output.append(',');
						} else {
							needsDelim = true;
						}
						this.writeln(output);
					}

					this.writeString(output, attr);
					output.append(" : ");
					DuelNode attrVal = node.getAttribute(attr);
					if (attrVal instanceof CommentNode) {
						output.append("\"\"");
					} else {
						if (addPrefix && "view".equalsIgnoreCase(attr) && attrVal instanceof ExpressionNode) {
							// prepend the client-side prefix
							ExpressionNode nameAttr = (ExpressionNode)attrVal; 
							attrVal = new ExpressionNode(this.settings.getFullName(nameAttr.getValue()), nameAttr.getIndex(), nameAttr.getLine(), nameAttr.getColumn());
						}
						this.writeNode(output, attrVal);
					}
				}

				this.depth--;
				if (singleAttr) {
					output.append(' ');
				} else {
					this.writeln(output);
				}
				output.append('}');
			}

			boolean hasChildren = false;
			boolean needsDelim = false;
			List<DuelNode> children = node.getChildren();
			int length=children.size();

			// less efficient but allows better comma placement
			for (int j=0; j<length; j++) {
				if (!(children.get(j) instanceof CodeCommentNode)) {
					needsDelim = true;
					break;
				}
			}

			for (int i=0; i<length; i++) {
				DuelNode child = children.get(i);
				if (this.settings.getNormalizeWhitespace() &&
					!this.preMode &&
					child instanceof LiteralNode &&
					(child == node.getFirstChild() || child == node.getLastChild())) {

					String lit = ((LiteralNode)child).getValue();
					if (lit == null || lit.matches("^[\\r\\n]*$")) {
						// skip literals which will be normalized away 
						continue;
					}
				}

				if (needsDelim) {
					output.append(',');
				}
				needsDelim = false;
				hasChildren = true;

				this.writeln(output);
				this.writeNode(output, child);

				// less efficient but allows better comma placement
				if (!(child instanceof CodeCommentNode)) {
					// check if delimiter needed
					for (int j=i+1; j<length; j++) {
						if (!(children.get(j) instanceof CodeCommentNode)) {
							needsDelim = true;
							break;
						}
					}
				}
			}

			this.depth--;
			if (hasChildren) {
				this.writeln(output);
			}
			output.append(']');

		} finally {
			this.preMode = prevPreMode;
		}
	}

	private void writeString(Appendable output, String value)
		throws IOException {

		if (value == null) {
			output.append("null");
			return;
		}

		if (!this.preMode && this.settings.getNormalizeWhitespace() && value.length() > 0) {
			// not very efficient but allows simple normalization
			value = value.replaceAll("^[\\r\\n]+", "").replaceAll("[\\r\\n]+$", "").replaceAll("\\s+", " ");
			if (value.length() == 0) {
				value = " ";
			}

		} else if (this.settings.getConvertLineEndings()) {
			// not very efficient but allows simple normalization
			if (!"\t".equals(this.settings.getIndent())) {
				value = value.replace("\t", this.settings.getIndent());
			}
			if (!"\n".equals(this.settings.getNewline())) {
				value = value.replace("\n", this.settings.getNewline());
			}
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
				case '\t':
					escape = "\\t";
					break;
				case '\n':
					escape = "\\n";
					break;
				case '\r':
					// if the source came via the DuelLexer then CRLF have been
					// compressed to single LF and these will not be present
					escape = "\\r";
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
