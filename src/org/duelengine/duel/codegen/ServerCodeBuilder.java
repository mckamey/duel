package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeBuilder {

	private final CodeGenSettings settings;
	private final HTMLFormatter formatter;
	private final StringWriter buffer;
	private final Stack<CodeMethod> methodStack = new Stack<CodeMethod>();

	public ServerCodeBuilder() {
		this(null);
	}

	public ServerCodeBuilder(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
		this.buffer = new StringWriter();
		this.formatter = new HTMLFormatter(this.buffer, this.settings.getEncodeNonASCII());
	}

	public CodeType build(ViewRootNode viewNode) throws IOException {
		CodeType viewType = new CodeType();

		String fullName = viewNode.getName();
		int lastDot = fullName.lastIndexOf('.');
		if (lastDot > 0) {
			viewType.setNamespace(fullName.substring(0, lastDot));
		}
		viewType.setTypeName(fullName.substring(lastDot+1));

		CodeMethod method = new CodeMethod();
		viewType.addMethod(method);
		this.methodStack.add(method);

		for (Node node : viewNode.getChildren()) {
			this.buildNode(node);
		}

		this.flushBuffer();

		return viewType;
	}

	private void buildNode(Node node) throws IOException {
		if (node instanceof LiteralNode) {
			LiteralNode literal = (LiteralNode)node;
			this.formatter.writeLiteral(literal.getValue());
			return;
		}

		if (node instanceof CommandNode) {
			CommandNode command = (CommandNode)node;
			switch (command.getCommand()) {
				default:
					// TODO.
					break;
			}
			return;
		}

		if (node instanceof ElementNode) {
			ElementNode element = (ElementNode)node;
			this.buildElement(element);
			return;
		}

		if (node instanceof CodeCommentNode) {
			// emit comment code or suppress?
			return;
		}

		if (node instanceof CommentNode) {
			// emit comment markup
			CommentNode comment = (CommentNode)node;
			this.formatter.writeComment(comment.getValue());
			return;
		}

		if (node instanceof DocTypeNode) {
			// emit doctype
			DocTypeNode doctype = (DocTypeNode)node;
			this.formatter.writeComment(doctype.getValue());
			return;
		}
	}

	private void buildElement(ElementNode element)
		throws IOException {

		String tagName = element.getTagName();
		this.formatter.writeOpenElementBeginTag(tagName);

		Map<String, CodeBlockNode> codeAttribs = new LinkedHashMap<String, CodeBlockNode>();
		for (String attrName : element.getAttributeNames()) {
			Node attrVal = element.getAttribute(attrName);
			if (attrVal instanceof CodeBlockNode) {
				codeAttribs.put(attrName, (CodeBlockNode)attrVal);
				continue;
			}
			this.formatter.writeAttribute(attrName, ((LiteralNode)attrVal).getValue());
		}
		
		if (element.canHaveChildren()) {
			for (Node child : element.getChildren()) {
				this.buildNode(child);
			}

			this.formatter.writeCloseElementBeginTag();
		} else {
			this.formatter.writeCloseElementVoidTag();
		}

		if (codeAttribs.size() > 0) {
			// TODO: deal with any deferred attribute processing
		}
	}

	/**
	 * Resets the buffer returning the accumulated value
	 * @return
	 */
	private void flushBuffer() {
		StringBuffer sb = this.buffer.getBuffer();

		// get the accumulated value
		String value = sb.toString();

		if (value == null || value.length() == 0) {
			return;
		}

		this.methodStack.peek().addStatement(new CodeEmitLiteralStatement(value));

		// clear the buffer
		sb.setLength(0);
	}
}
