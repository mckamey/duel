package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelView;
import org.duelengine.duel.HTMLFormatter;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;
import org.duelengine.duel.parsing.InvalidNodeException;

/**
 * Translates the view AST to CodeDOM tree
 */
public class CodeDOMBuilder {

	private enum TagMode {
		None,
		PreMode,
		SuspendMode
	}

	private final CodeGenSettings settings;
	private final HTMLFormatter formatter;
	private final StringBuilder buffer;
	private final Stack<CodeStatementCollection> scopeStack = new Stack<CodeStatementCollection>();
	private CodeTypeDeclaration viewType;
	private CodeMethod initMethod;
	private TagMode tagMode = TagMode.None;

	public CodeDOMBuilder() {
		this(null);
	}

	public CodeDOMBuilder(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
		this.buffer = new StringBuilder();
		this.formatter = new HTMLFormatter();
	}

	public CodeTypeDeclaration buildView(VIEWCommandNode viewNode) throws IOException {
		try {
			String fullName = viewNode.getName();
			int lastDot = fullName.lastIndexOf('.');
			String name = fullName.substring(lastDot+1);
			String ns = (lastDot > 0) ? fullName.substring(0, lastDot) : null;

			this.viewType = CodeDOMUtility.createViewType(ns, name);

			CodeMethod method = this.buildRenderMethod(viewNode.getChildren());

			method.setName("render");
			method.setAccess(AccessModifierType.PROTECTED);
			method.setOverride(true);

			return this.viewType;

		} finally {
			this.initMethod = null;
			this.viewType = null;
		}
	}

	private CodeMethod buildRenderMethod(List<DuelNode> content)
		throws IOException {

		CodeMethod method = new CodeMethod(
			AccessModifierType.PRIVATE,
			Void.class,
			this.viewType.nextIdent("render_"),
			new CodeParameterDeclarationExpression[] {
				new CodeParameterDeclarationExpression(DuelContext.class, "output"),
				new CodeParameterDeclarationExpression(Object.class, "data"),
				new CodeParameterDeclarationExpression(int.class, "index"),
				new CodeParameterDeclarationExpression(int.class, "count"),
				new CodeParameterDeclarationExpression(String.class, "key")
			},
			new Class<?>[] {
				IOException.class
			});

		this.viewType.add(method);

		this.flushBuffer();
		this.scopeStack.push(method.getStatements());
		for (DuelNode node : content) {
			this.buildNode(node);
		}
		this.flushBuffer();
		this.scopeStack.pop();

		return method;
	}

	private void buildNode(DuelNode node) throws IOException {
		if (node instanceof LiteralNode) {
			String literal = ((LiteralNode)node).getValue();
			if (this.tagMode == TagMode.SuspendMode) {
				this.buffer.append(literal);
			} else {
				if (literal != null &&
					literal.length() > 0 &&
					this.tagMode != TagMode.PreMode &&
					this.settings.getNormalizeWhitespace()) {

					// not very efficient but allows simple normalization
					literal = literal.replaceAll("^[\\r\\n]+", "").replaceAll("[\\r\\n]+$", "").replaceAll("\\s+", " ");
					if (literal.length() == 0) {
						literal = " ";
					}
				}

				this.formatter.writeLiteral(this.buffer, literal, this.settings.getEncodeNonASCII());
			}
			return;
		}

		if (node instanceof CommandNode) {
			CommandNode command = (CommandNode)node;
			switch (command.getCommand()) {
				case XOR:
					this.buildConditional((XORCommandNode)node);
					return;
				case IF:
					this.buildConditional((IFCommandNode)node, this.scopeStack.peek());
					return;
				case FOR:
					this.buildIteration((FORCommandNode)node);
					return;
				case CALL:
					this.buildCall((CALLCommandNode)node);
					return;
				case PART:
					this.buildPartPlaceholder((PARTCommandNode)node);
					return;
				default:
					throw new InvalidNodeException("Invalid command node type: "+command.getCommand(), command);
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

		if (node instanceof CommentNode) {
			// emit comment markup
			CommentNode comment = (CommentNode)node;
			this.formatter.writeComment(this.buffer, comment.getValue());
			return;
		}

		if (node instanceof DocTypeNode) {
			// emit doctype
			DocTypeNode doctype = (DocTypeNode)node;
			this.formatter.writeDocType(this.buffer, doctype.getValue());
			return;
		}

		if (node instanceof CodeCommentNode) {
			this.buildComment((CodeCommentNode)node);
			return;
		}
	}

	private void buildCall(CALLCommandNode node)
		throws IOException {

		// generate a field to hold the child template
		CodeField field = new CodeField(
				AccessModifierType.PRIVATE,
				DuelView.class,
				this.viewType.nextIdent("view_"));
		this.viewType.add(field);

		// determine the name of the template
		String viewName = null;
		DuelNode attr = node.getAttribute(CALLCommandNode.VIEW);
		if (attr instanceof LiteralNode) {
			viewName = ((LiteralNode)attr).getValue();
		} else if (attr instanceof ExpressionNode) {
			viewName = ((ExpressionNode)attr).getValue();
		}

		if (viewName == null) {
			// TODO: how to cover switcher method cases?
			throw new InvalidNodeException("Unexpected Call command view attribute: "+attr, attr);
		}

		CodeExpression[] ctorArgs = new CodeExpression[node.getChildren().size()];

		int i = 0;
		for (DuelNode child : node.getChildren()) {
			ctorArgs[i++] = this.buildPart((PARTCommandNode)child);
		}

		// insert an initialization statement into the init method
		CodeMethod initMethod = this.ensureInitMethod();
		initMethod.getStatements().add(
			new CodeExpressionStatement(
				new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ASSIGN,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), field),
					new CodeObjectCreateExpression(
						viewName.trim(),
						ctorArgs))));
		
		CodeExpression dataExpr;
		DuelNode callData = node.getAttribute(CALLCommandNode.DATA);
		if (callData instanceof CodeBlockNode) {
			dataExpr = this.translateExpression((CodeBlockNode)callData);
		} else {
			dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
		}

		CodeExpression indexExpr;
		DuelNode callIndex = node.getAttribute(CALLCommandNode.INDEX);
		if (callIndex instanceof CodeBlockNode) {
			indexExpr = this.translateExpression((CodeBlockNode)callIndex);
		} else {
			indexExpr = new CodeVariableReferenceExpression(int.class, "index");
		}

		CodeExpression countExpr;
		DuelNode callCount = node.getAttribute(CALLCommandNode.COUNT);
		if (callCount instanceof CodeBlockNode) {
			countExpr = this.translateExpression((CodeBlockNode)callCount);
		} else {
			countExpr = new CodeVariableReferenceExpression(int.class, "count");
		}

		CodeExpression keyExpr;
		DuelNode callKey = node.getAttribute(CALLCommandNode.KEY);
		if (callKey instanceof CodeBlockNode) {
			keyExpr = this.translateExpression((CodeBlockNode)callKey);
		} else {
			keyExpr = new CodeVariableReferenceExpression(String.class, "key");
		}

		CodeStatementCollection scope = this.scopeStack.peek();
		scope.add(new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"renderView",
			new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), field),
			new CodeVariableReferenceExpression(DuelContext.class, "output"),
			dataExpr,
			indexExpr,
			countExpr,
			keyExpr));
	}

	private CodeObjectCreateExpression buildPart(PARTCommandNode node)
		throws IOException {

		CodeTypeDeclaration part = CodeDOMUtility.createPartType(
				this.viewType.nextIdent("part_"));
		this.viewType.add(part);

		String partName = node.getName();
		if (partName == null) {
			throw new InvalidNodeException("PART command is missing name", node);
		}

		CodeMethod getNameMethod = new CodeMethod(
			AccessModifierType.PUBLIC,
			String.class,
			"getPartName",
			null,
			new CodeMethodReturnStatement(new CodePrimitiveExpression(partName)));
		getNameMethod.setOverride(true);
		part.add(getNameMethod);

		CodeTypeDeclaration parentView = this.viewType;
		try {
			this.viewType = part;

			CodeMethod renderMethod = this.buildRenderMethod(node.getChildren());

			renderMethod.setName("render");
			renderMethod.setAccess(AccessModifierType.PROTECTED);
			renderMethod.setOverride(true);

		} finally {
			this.viewType = parentView;
		}

		return new CodeObjectCreateExpression(part.getTypeName());
	}

	private void buildPartPlaceholder(PARTCommandNode part)
		throws IOException {

		CodeObjectCreateExpression createPart = this.buildPart(part);

		// insert an initialization statement into the init method
		CodeMethod initMethod = this.ensureInitMethod();
		initMethod.getStatements().add(
			new CodeExpressionStatement(
				new CodeMethodInvokeExpression(
					new CodeThisReferenceExpression(),
					"addPart",
					createPart)));

		// parent scope
		CodeStatementCollection scope = this.scopeStack.peek();

		scope.add(new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"renderPart",
			new CodePrimitiveExpression(part.getName()),
			new CodeVariableReferenceExpression(DuelContext.class, "output"),
			new CodeVariableReferenceExpression(Object.class, "data"),
			new CodeVariableReferenceExpression(int.class, "index"),
			new CodeVariableReferenceExpression(int.class, "count"),
			new CodeVariableReferenceExpression(String.class, "key")));
	}

	private void buildIteration(FORCommandNode node) throws IOException {
		if (!node.hasChildren()) {
			// no content to emit so can skip entire loop
			return;
		}

		// parent scope
		CodeStatementCollection scope = this.scopeStack.peek();

		// build a helper method to hold the inner content
		CodeMethod innerBind = this.buildRenderMethod(node.getChildren());

		CodeExpression dataExpr;
		DuelNode loopCount = node.getAttribute(FORCommandNode.COUNT);
		if (loopCount instanceof CodeBlockNode) {
			CodeExpression countExpr = this.translateExpression((CodeBlockNode)loopCount);

			DuelNode loopData = node.getAttribute(FORCommandNode.DATA);
			if (loopData instanceof CodeBlockNode) {
				dataExpr = this.translateExpression((CodeBlockNode)loopData);
			} else {
				dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
			}

			this.buildIterationCount(scope, countExpr, dataExpr, innerBind);

		} else {
			DuelNode loopObj = node.getAttribute(FORCommandNode.IN);
			if (loopObj instanceof CodeBlockNode) {
				CodeExpression objExpr = this.translateExpression((CodeBlockNode)loopObj);
				this.buildIterationObject(scope, objExpr, innerBind);

			} else {
				DuelNode loopArray = node.getAttribute(FORCommandNode.EACH);
				if (!(loopArray instanceof CodeBlockNode)) {
					throw new InvalidNodeException("FOR loop missing arguments", loopArray);
				}

				CodeExpression arrayExpr = this.translateExpression((CodeBlockNode)loopArray);
				this.buildIterationArray(scope, arrayExpr, innerBind);
			}
		}
	}

	private void buildIterationCount(CodeStatementCollection scope, CodeExpression count, CodeExpression data, CodeMethod innerBind) {

		// the collection to iterate over
		CodeVariableDeclarationStatement dataDecl =
			new CodeVariableDeclarationStatement(
				Object.class,
				scope.nextIdent("data_"),
				data);
		scope.add(dataDecl);

		// the current index (embedded in for loop init statement)
		CodeVariableDeclarationStatement indexDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("index_"),
				CodePrimitiveExpression.ZERO);

		// the item count (embedded in for loop init statement)
		CodeVariableDeclarationStatement countDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("count_"),
				count);

		// the for loop init statement
		CodeVariableCompoundDeclarationStatement initStatement =
			new CodeVariableCompoundDeclarationStatement(indexDecl, countDecl);

		// the for loop block
		scope.add(
			new CodeIterationStatement(
				initStatement,// initStatement
				new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.LESS_THAN,
					new CodeVariableReferenceExpression(indexDecl),
					new CodeVariableReferenceExpression(countDecl)),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl))),// incrementStatement
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						innerBind.getName(),
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(dataDecl),
						new CodeVariableReferenceExpression(indexDecl),
						new CodeVariableReferenceExpression(countDecl),
						CodePrimitiveExpression.NULL))));
	}

	private void buildIterationObject(CodeStatementCollection scope, CodeExpression objExpr, CodeMethod innerBind) {
		CodeExpression data =
			new CodeMethodInvokeExpression(
				new CodeMethodInvokeExpression(
					new CodeThisReferenceExpression(),
					"asObject",
					objExpr),
				"entrySet");

		// the collection to iterate over
		CodeVariableDeclarationStatement collectionDecl =
			new CodeVariableDeclarationStatement(
				Collection.class,
				scope.nextIdent("items_"),
				data);
		scope.add(collectionDecl);

		// the current index
		CodeVariableDeclarationStatement indexDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("index_"),
				CodePrimitiveExpression.ZERO);

		// the item count
		CodeVariableDeclarationStatement countDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("count_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl),
					"size"));

		scope.add(new CodeVariableCompoundDeclarationStatement(indexDecl, countDecl));

		// the iterator (embedded in for init)
		CodeVariableDeclarationStatement iteratorDecl =
			new CodeVariableDeclarationStatement(
				Iterator.class,
				scope.nextIdent("iterator_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl),
					"iterator"));

		// the entry (embedded in for body)
		CodeVariableDeclarationStatement entryDecl = 
			new CodeVariableDeclarationStatement(
				Map.Entry.class,
				scope.nextIdent("entry_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(iteratorDecl),
					"next"));
		
		// the for loop block
		scope.add(
			new CodeIterationStatement(
				iteratorDecl,// initStatement
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(iteratorDecl),
					"hasNext"),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl))),// incrementStatement
				entryDecl,
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						innerBind.getName(),
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(entryDecl),
								"getValue"),
							new CodeVariableReferenceExpression(indexDecl),
							new CodeVariableReferenceExpression(countDecl),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(entryDecl),
								"getKey")))));
	}

	private void buildIterationArray(CodeStatementCollection scope, CodeExpression arrayExpr, CodeMethod innerBind) {

		CodeExpression items = 
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"asArray",
				arrayExpr);

		// the collection to iterate over
		CodeVariableDeclarationStatement collectionDecl =
			new CodeVariableDeclarationStatement(
				Collection.class,
				scope.nextIdent("items_"),
				items);
		scope.add(collectionDecl);

		// the current index
		CodeVariableDeclarationStatement indexDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("index_"),
				CodePrimitiveExpression.ZERO);

		// the item count
		CodeVariableDeclarationStatement countDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("count_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl),
					"size"));

		scope.add(new CodeVariableCompoundDeclarationStatement(indexDecl, countDecl));

		// the iterator (embedded in for init)
		CodeVariableDeclarationStatement iteratorDecl =
			new CodeVariableDeclarationStatement(
				Iterator.class,
				scope.nextIdent("iterator_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl),
					"iterator"));

		// the for loop block
		scope.add(
			new CodeIterationStatement(
				iteratorDecl,// initStatement
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(iteratorDecl),
					"hasNext"),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl))),// incrementStatement
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						innerBind.getName(),
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(iteratorDecl),
								"next"),
							new CodeVariableReferenceExpression(indexDecl),
							new CodeVariableReferenceExpression(countDecl),
							CodePrimitiveExpression.NULL))));
	}

	private void buildConditional(XORCommandNode node)
		throws IOException {

		CodeStatementCollection scope = this.scopeStack.peek();

		for (DuelNode conditional : node.getChildren()) {
			if (conditional instanceof IFCommandNode) {
				scope = this.buildConditional((IFCommandNode)conditional, scope);
			}
		}
	}

	private CodeStatementCollection buildConditional(IFCommandNode node, CodeStatementCollection scope)
		throws IOException {

		this.flushBuffer();

		CodeBlockNode testNode = node.getTest();

		if (testNode == null) {
			// no condition block needed
			if (node.hasChildren()) {
				this.scopeStack.push(scope);
				for (DuelNode child : node.getChildren()) {
					this.buildNode(child);
				}
				this.flushBuffer();
				this.scopeStack.pop();
			}
			return scope;
		}

		CodeConditionStatement condition = new CodeConditionStatement();
		scope.add(condition);

		condition.setCondition(this.translateExpression(testNode));

		if (node.hasChildren()) {
			this.scopeStack.push(condition.getTrueStatements());
			for (DuelNode child : node.getChildren()) {
				this.buildNode(child);
			}
			this.flushBuffer();
			this.scopeStack.pop();
		}
		
		return condition.getFalseStatements();
	}

	private CodeExpression translateExpression(CodeBlockNode node) {

		try {
			// convert from JavaScript source to CodeDOM
			List<CodeMember> members = new ScriptTranslator(this.viewType).translate(node.getClientCode());

			CodeExpression expression = null;
			if (members.size() == 1 && members.get(0) instanceof CodeMethod) {
				// first attempt to extract single expression (inlines the return)
				expression = CodeDOMUtility.inlineMethod((CodeMethod)members.get(0));
			}

			if (expression == null && (members.size() > 0)) {
				// add all CodeDOM members to viewType
				this.viewType.addAll(members);

				// have the expression be a method invocation
				expression = new CodeMethodInvokeExpression(
					new CodeThisReferenceExpression(),
						members.get(0).getName(),
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key"));
			}
			return expression;

		} catch (Exception ex) {

			// TODO: differentiate client-side deferred execution from syntax errors
			throw new ScriptTranslationException(ex.getMessage(), node, ex);
		}
	}

	private void buildElement(ElementNode element)
		throws IOException {

		String tagName = element.getTagName();
		this.formatter.writeOpenElementBeginTag(this.buffer, tagName);

		Map<String, CodeBlockNode> deferredAttrs = new LinkedHashMap<String, CodeBlockNode>();
		for (String attrName : element.getAttributeNames()) {
			DuelNode attrVal = element.getAttribute(attrName);

			if (attrVal == null) {
				this.formatter.writeAttribute(this.buffer, attrName, null);

			} else if (attrVal instanceof LiteralNode) {
				this.formatter.writeAttribute(this.buffer, attrName, ((LiteralNode)attrVal).getValue());

			} else if (attrVal instanceof CodeBlockNode) {
				deferredAttrs.put(attrName, (CodeBlockNode)attrVal);

			} else {
				throw new InvalidNodeException("Invalid attribute node type: "+attrVal.getClass(), attrVal);
			}
		}

		String idVar;
		if (deferredAttrs.size() > 0) {
			DuelNode id = element.getAttribute("id");
			if (id == null) {
				this.formatter.writeOpenAttribute(this.buffer, "id");
				idVar = this.emitClientID();
				this.formatter.writeCloseAttribute(this.buffer);

			} else if (id instanceof LiteralNode) {
				idVar = ((LiteralNode)id).getValue();

			} else {

				// TODO: check if was emitted
				idVar = "TODO";
			}
		}

		if (element.canHaveChildren()) {
			this.formatter.writeCloseElementBeginTag(this.buffer);

			TagMode prevMode = this.tagMode;
			if ("script".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName)) {
				this.tagMode = TagMode.SuspendMode;
			} else if ("pre".equalsIgnoreCase(tagName)) {
				this.tagMode = TagMode.PreMode;
			}
			try {
				for (DuelNode child : element.getChildren()) {
					if (this.settings.getNormalizeWhitespace() &&
						this.tagMode == TagMode.None &&
						child instanceof LiteralNode &&
						(child == element.getFirstChild() || child == element.getLastChild())) {

						String lit = ((LiteralNode)child).getValue();
						if (lit == null || lit.matches("^[\\r\\n]*$")) {
							// skip literals which will be normalized away 
							continue;
						}
					}
					this.buildNode(child);
				}
			} finally {
				this.tagMode = prevMode;
			}

			this.formatter.writeElementEndTag(this.buffer, tagName);

		} else {
			this.formatter.writeCloseElementVoidTag(this.buffer);
		}

		if (deferredAttrs.size() > 0) {
			// TODO: execute any deferred attributes using idVar
		}
	}

	private String emitClientID() {
		this.flushBuffer();
		CodeStatementCollection scope = this.scopeStack.peek();
		
		// the var contains a new unique ident
		CodeVariableDeclarationStatement localVar = CodeDOMUtility.nextID(scope);
		scope.add(localVar);

		String id = localVar.getName();

		// emit the value of the var
		CodeStatement emitVar = CodeDOMUtility.emitVarValue(localVar);
		scope.add(emitVar);

		return id;
	}

	/**
	 * emits the values of code blocks
	 * @param block
	 */
	private void buildCodeBlock(CodeBlockNode block) {
		boolean htmlEncode = true;
		if (block instanceof MarkupExpressionNode) {
			htmlEncode = false;
			block = new ExpressionNode(block.getValue(), block.getIndex(), block.getLine(), block.getColumn());
		}

		CodeExpression codeExpr = this.translateExpression(block);
		if (codeExpr == null) {
			return;
		}

		CodeStatement writeStatement =
			htmlEncode ?
			CodeDOMUtility.emitExpressionSafe(codeExpr) :
			CodeDOMUtility.emitExpression(codeExpr);

		this.flushBuffer();
		CodeStatementCollection scope = this.scopeStack.peek();
		scope.add(writeStatement);
	}

	private CodeMethod ensureInitMethod() {
		if (this.initMethod != null) {
			return this.initMethod;
		}

		this.initMethod = new CodeMethod(
			AccessModifierType.PROTECTED,
			Void.class,
			"init",
			null);
		this.initMethod.setOverride(true);
		this.viewType.add(this.initMethod);

		return this.initMethod;
	}

	private void buildComment(CodeCommentNode comment) {
		this.flushBuffer();

		CodeStatementCollection scope = this.scopeStack.peek();
		scope.add(new CodeCommentStatement(comment.getValue()));
	}

	/**
	 * Resets the buffer returning the accumulated value
	 * @return
	 */
	private void flushBuffer() {
		if (this.buffer.length() < 1) {
			return;
		}

		// get the accumulated value
		CodeStatement emitLit = CodeDOMUtility.emitLiteralValue(this.buffer.toString());
		this.scopeStack.peek().add(emitLit);

		// clear the buffer
		this.buffer.setLength(0);
	}
}
