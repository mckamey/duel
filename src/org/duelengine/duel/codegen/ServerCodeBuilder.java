package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeBuilder {

	private final CodeGenSettings settings;
	private final HTMLFormatter formatter;
	private final StringWriter buffer;
	private final Stack<CodeStatementCollection> scopeStack = new Stack<CodeStatementCollection>();

	public ServerCodeBuilder() {
		this(null);
	}

	public ServerCodeBuilder(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
		this.buffer = new StringWriter();
		this.formatter = new HTMLFormatter(this.buffer, this.settings.getEncodeNonASCII());
	}

	public CodeTypeDeclaration build(ViewRootNode viewNode) throws IOException {
		CodeTypeDeclaration viewType = new CodeTypeDeclaration();

		String fullName = viewNode.getName();
		int lastDot = fullName.lastIndexOf('.');
		if (lastDot > 0) {
			viewType.setNamespace(fullName.substring(0, lastDot));
		}
		viewType.setTypeName(fullName.substring(lastDot+1));

		CodeMethod method = new CodeMethod();
		method.setMethodName(viewType.nextID());
		method.addParameter(Writer.class, "writer");
		method.addParameter(Object.class, "model");
		method.addParameter(Integer.class, "index");
		method.addParameter(Integer.class, "count");
		viewType.addMethod(method);
		this.scopeStack.add(method.getStatements());

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
			this.flushBuffer();

			CommandNode command = (CommandNode)node;
			switch (command.getCommand()) {
				case XOR:
					this.buildConditional((XORCommandNode)node);
					return;
				case IF:
					this.buildConditional((IFCommandNode)node, false);
					return;
				case FOR:
					this.buildLoop((FORCommandNode)node);
					return;
				case CALL:
				case PART:
					return;
				default:
					throw new IllegalStateException("Invalid command node type: "+command.getCommand());
			}
		}

		if (node instanceof ElementNode) {
			ElementNode element = (ElementNode)node;
			this.buildElement(element);
			return;
		}

		if (node instanceof CodeBlockNode) {
			CodeBlockNode block = (CodeBlockNode)node;
			this.buildCodeBlock(block);
			return;
		}

		if (node instanceof CodeCommentNode) {
			this.flushBuffer();

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

	private void buildLoop(FORCommandNode node) {
		CodeStatementCollection scope = this.scopeStack.peek();

		CodeIterationStatement loop = new CodeIterationStatement();
		scope.add(loop);
	}

	private void buildConditional(XORCommandNode node) {
		boolean asElse = false;
		for (Node conditional : node.getChildren()) {
			if (conditional instanceof IFCommandNode) {
				this.buildConditional((IFCommandNode)conditional, asElse);
				asElse = true;
			}
		}
	}

	private void buildConditional(IFCommandNode node, boolean asElse) {
		
	}

	private void buildElement(ElementNode element)
		throws IOException {

		String tagName = element.getTagName();
		this.formatter.writeOpenElementBeginTag(tagName);

		Map<String, CodeBlockNode> deferredAttrs = new LinkedHashMap<String, CodeBlockNode>();
		for (String attrName : element.getAttributeNames()) {
			Node attrVal = element.getAttribute(attrName);

			if (attrVal == null) {
				this.formatter.writeAttribute(attrName, null);

			} else if (attrVal instanceof LiteralNode) {
				this.formatter.writeAttribute(attrName, ((LiteralNode)attrVal).getValue());

			} else if (attrVal instanceof CodeBlockNode) {
				deferredAttrs.put(attrName, (CodeBlockNode)attrVal);

			} else {
				throw new IllegalStateException("Invalid attribute node type: "+attrVal.getClass());
			}
		}

		String idVar;
		if (deferredAttrs.size() > 0) {
			Node id = element.getAttribute("id");
			if (id == null) {
				this.formatter.writeOpenAttribute("id");
				idVar = this.emitClientID();
				this.formatter.writeCloseAttribute();

			} else if (id instanceof LiteralNode) {
				idVar = ((LiteralNode)id).getValue();

			} else {

				// TODO: check if was emitted
				idVar = "TODO";
			}
		}

		if (element.canHaveChildren()) {
			this.formatter.writeCloseElementBeginTag();

			for (Node child : element.getChildren()) {
				this.buildNode(child);
			}

			this.formatter.writeElementEndTag(tagName);

		} else {
			this.formatter.writeCloseElementVoidTag();
		}

		if (deferredAttrs.size() > 0) {
			// TODO: execute any deferred attributes using idVar
		}
	}

	private String emitClientID() {
		this.flushBuffer();
		CodeStatementCollection scope = this.scopeStack.peek();
		
		// the var contains a new unique ident
		CodeVariableDeclarationStatement localVar = CodeDomFactory.nextID(scope);
		scope.add(localVar);

		String id = localVar.getName();

		// emit the value of the var
		CodeStatement emitVar = CodeDomFactory.emitVarValue(id);
		scope.add(emitVar);

		return id;
	}

	private void buildCodeBlock(CodeBlockNode block) {
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

		this.scopeStack.peek().add(CodeDomFactory.emitLiteralValue(value));

		// clear the buffer
		sb.setLength(0);
	}
}
