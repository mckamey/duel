package org.duelengine.duel.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.duelengine.duel.DataEncoder;
import org.duelengine.duel.ast.CALLCommandNode;
import org.duelengine.duel.ast.CodeBlockNode;
import org.duelengine.duel.ast.CodeCommentNode;
import org.duelengine.duel.ast.CommentNode;
import org.duelengine.duel.ast.DocTypeNode;
import org.duelengine.duel.ast.DuelNode;
import org.duelengine.duel.ast.ElementNode;
import org.duelengine.duel.ast.ExpressionNode;
import org.duelengine.duel.ast.LiteralNode;
import org.duelengine.duel.ast.VIEWCommandNode;
import org.duelengine.duel.parsing.InvalidNodeException;

/**
 * Generates client-side code for views
 * Inherently thread-safe as contains no mutable instance data.
 */
public class ClientCodeGen implements CodeGenerator {

	private final CodeGenSettings settings;
	private final DataEncoder encoder;

	public ClientCodeGen() {
		this(null);
	}

	public ClientCodeGen(CodeGenSettings codeGenSettings) {
		settings = (codeGenSettings != null) ? codeGenSettings : new CodeGenSettings();
		encoder = new DataEncoder(settings.getNewline(), settings.getIndent());
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

		write(output, views != null ? Arrays.asList(views) : null);
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
		writeln(output, 0);

		List<String> namespaces = new ArrayList<String>();
		for (VIEWCommandNode view : views) {
			if (view == null || view.isServerOnly()) {
				continue;
			}

			writeln(output, 0);

			// prepend the client-side prefix
			String viewName = settings.getClientName(view.getName());
			try {
				if (encoder.writeNamespace(output, namespaces, viewName)) {
					writeln(output, 0);
				}
			} catch (IllegalArgumentException ex) {
				throw new InvalidNodeException("Invalid view name: "+viewName, view.getAttribute("name"), ex);
			}
			writeView(output, view, viewName);
		}
	}

	private void writeView(Appendable output, VIEWCommandNode view, String viewName)
		throws IOException {

		// prepend the client-side prefix
		if (viewName.indexOf('.') < 0) {
			output.append("var ");
		}
		output.append(viewName);
		output.append(" = duel(");

		if (view.childCount() == 1) {
			DuelNode child = view.getFirstChild();
			if (child instanceof ElementNode &&
				(((ElementNode)child).hasChildren() ||
				((ElementNode)child).hasAttributes())) {
				writeln(output, 1);
			}

			// just emit the single child
			writeNode(output, child, 1, false);
		} else {
			// wrap in a document fragment
			writeElement(output, "", view, 0, false);
		}

		output.append(");");
		writeln(output, 0);
	}

	private void writeNode(Appendable output, DuelNode node, int depth, boolean preMode)
		throws IOException {

		if (node instanceof LiteralNode) {
			writeString(output, ((LiteralNode)node).getValue(), preMode);

		} else if (node instanceof ElementNode) {
			writeElement(output, ((ElementNode)node).getTagName(), (ElementNode)node, depth, preMode);

		} else if (node instanceof CodeBlockNode) {
			writeCodeBlock(output, (CodeBlockNode)node);

		} else if (node instanceof CommentNode) {
			writeSpecialElement(output, "!", ((CommentNode)node).getValue(), depth, preMode);

		} else if (node instanceof CodeCommentNode) {
			writeComment(output, ((CodeCommentNode)node).getValue());

		} else if (node instanceof DocTypeNode) {
			writeSpecialElement(output, "!DOCTYPE", ((DocTypeNode)node).getValue(), depth, preMode);

		} else if (node != null) {
			throw new UnsupportedOperationException("Node type not yet implemented: "+node.getClass());
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

		output.append(node.getClientCode(encoder.isPrettyPrint()));
	}

	private void writeSpecialElement(Appendable output, String name, String value, int depth, boolean preMode)
		throws IOException {

		output.append('[');
		depth++;

		writeString(output, name, preMode);

		boolean hasValue = (value != null) && (value.length() > 0);
		if (hasValue) {
			output.append(',');
			writeln(output, depth);
			writeString(output, value, preMode);
		}

		depth--;
		if (hasValue) {
			writeln(output, depth);
		}
		output.append(']');
	}

	private void writeElement(Appendable output, String tagName, ElementNode node, int depth, boolean preMode)
		throws IOException {

		if (!preMode) {
			preMode = "pre".equalsIgnoreCase(tagName) || "script".equalsIgnoreCase(tagName);
		}
		output.append('[');
		depth++;

		writeString(output, tagName, preMode);

		if (node.hasAttributes()) {
			Set<String> attrs = node.getAttributeNames();
			boolean singleAttr = (attrs.size() == 1);

			output.append(", {");
			depth++;

			boolean addPrefix = (node instanceof CALLCommandNode) && settings.hasClientNamePrefix();

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
					writeln(output, depth);
				}

				writeString(output, attr, preMode);
				output.append(" : ");
				DuelNode attrVal = node.getAttribute(attr);
				if (node.isBoolAttribute(attr)) {
					if (attrVal instanceof CodeBlockNode) {
						// if is code block then allow client-side to evaluate
						// truthy results emit the boolean attribute
						writeNode(output, attrVal, depth, preMode);

					} else {
						// boolean attributes allow empty or missing values as true
						// if attribute is static but present then force a truthy value
						output.append("true");
					}

				} else if (attrVal == null) {
					output.append("null");

				} else if (attrVal instanceof CommentNode) {
					output.append("\"\"");

				} else {
					if (addPrefix && "view".equalsIgnoreCase(attr) && attrVal instanceof ExpressionNode) {
						// prepend the client-side prefix
						ExpressionNode nameAttr = (ExpressionNode)attrVal; 
						attrVal = new ExpressionNode(settings.getClientName(nameAttr.getValue()), nameAttr.getIndex(), nameAttr.getLine(), nameAttr.getColumn());
					}
					writeNode(output, attrVal, depth, preMode);
				}
			}

			depth--;
			if (singleAttr) {
				output.append(' ');
			} else {
				writeln(output, depth);
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
			if (settings.getNormalizeWhitespace() &&
				!preMode &&
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

			writeln(output, depth);
			writeNode(output, child, depth, preMode);

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

		depth--;
		if (hasChildren) {
			writeln(output, depth);
		}
		output.append(']');
	}

	private void writeString(Appendable output, String value, boolean preMode)
		throws IOException {

		if (value == null) {
			output.append("null");
			return;
		}

		if (!preMode && settings.getNormalizeWhitespace() && value.length() > 0) {
			// not very efficient but allows simple normalization
			value = value.replaceAll("^[\\r\\n]+", "").replaceAll("[\\r\\n]+$", "").replaceAll("\\s+", " ");
			if (value.isEmpty()) {
				value = " ";
			}

		} else if (settings.getConvertLineEndings()) {
			// not very efficient but allows simple normalization
			if (!"\t".equals(settings.getIndent())) {
				value = value.replace("\t", settings.getIndent());
			}
			if (!"\n".equals(settings.getNewline())) {
				value = value.replace("\n", settings.getNewline());
			}
		}

		int start = 0,
			length = value.length();

		output.append('\'');

		for (int i=start; i<length; i++) {
			String escape;

			char ch = value.charAt(i);
			switch (ch) {
				case '\'':
					escape = "\\\'";
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

		output.append('\'');
	}

	private void writeln(Appendable output, int depth)
		throws IOException {

		output.append(settings.getNewline());

		String indent = settings.getIndent();
		while (depth-- > 0) {
			output.append(indent);
		}
	}
}
