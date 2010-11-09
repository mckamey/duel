package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

/**
 * Translates the view AST to CodeDOM tree
 */
public class CodeDOMBuilder {

	private final CodeGenSettings settings;
	private final HTMLFormatter formatter;
	private final StringBuilder buffer;
	private final Stack<CodeStatementCollection> scopeStack = new Stack<CodeStatementCollection>();
	private CodeTypeDeclaration viewType;

	public CodeDOMBuilder() {
		this(null);
	}

	public CodeDOMBuilder(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
		this.buffer = new StringBuilder();
		this.formatter = new HTMLFormatter(this.buffer, this.settings.getEncodeNonASCII());
	}

	public CodeTypeDeclaration build(ViewRootNode viewNode) throws IOException {
		this.viewType = new CodeTypeDeclaration();
		try {
			this.viewType.setAccess(AccessModifierType.PUBLIC);
			String fullName = viewNode.getName();
			int lastDot = fullName.lastIndexOf('.');
			if (lastDot > 0) {
				this.viewType.setNamespace(fullName.substring(0, lastDot));
			}
			this.viewType.setTypeName(fullName.substring(lastDot+1));

			CodeMethod method = this.buildRenderMethod(viewNode.getChildren());

			method.setName("render");
			method.setAccess(AccessModifierType.PROTECTED);
			
			return this.viewType;

		} finally {
			this.viewType = null;
		}
	}

	private CodeMethod buildRenderMethod(List<Node> content)
		throws IOException {

		CodeMethod method = new CodeMethod(
			AccessModifierType.PRIVATE,
			Void.class,
			this.viewType.nextIdent("render_"),
			new CodeParameterDeclarationExpression[] {
				new CodeParameterDeclarationExpression(Appendable.class, "output"),
				new CodeParameterDeclarationExpression(Object.class, "data"),
				new CodeParameterDeclarationExpression(int.class, "index"),
				new CodeParameterDeclarationExpression(int.class, "count"),
				new CodeParameterDeclarationExpression(String.class, "key")
			},
			null);

		this.viewType.add(method);

		this.flushBuffer();
		this.scopeStack.push(method.getStatements());
		for (Node node : content) {
			this.buildNode(node);
		}
		this.flushBuffer();
		this.scopeStack.pop();

		return method;
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
		Node loopCount = node.getAttribute(FORCommandNode.COUNT);
		if (loopCount instanceof CodeBlockNode) {
			CodeExpression countExpr = this.translateExpression(((CodeBlockNode)loopCount).getClientCode());

			Node loopData = node.getAttribute(FORCommandNode.DATA);
			if (loopData instanceof CodeBlockNode) {
				dataExpr = this.translateExpression(((CodeBlockNode)loopData).getClientCode());
			} else {
				dataExpr = new CodeVariableReferenceExpression("data");
			}

			this.buildIterationCount(scope, countExpr, dataExpr, innerBind);

		} else {
			Node loopObj = node.getAttribute(FORCommandNode.IN);
			if (loopObj instanceof CodeBlockNode) {
				CodeExpression objExpr = this.translateExpression(((CodeBlockNode)loopObj).getClientCode());
				this.buildIterationObject(scope, objExpr, innerBind);

			} else {
				Node loopArray = node.getAttribute(FORCommandNode.EACH);
				if (!(loopArray instanceof CodeBlockNode)) {
					throw new IllegalArgumentException("FOR loop missing arguments");
				}

				CodeExpression arrayExpr = this.translateExpression(((CodeBlockNode)loopArray).getClientCode());
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
				new CodePrimitiveExpression(0));

		// the item count (embedded in for loop init statement)
		CodeVariableDeclarationStatement countDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("count_"),
				count);

		// the for loop init statement
		CodeVariableCompoundDeclarationStatement initStatement = new CodeVariableCompoundDeclarationStatement(
			new CodeVariableDeclarationStatement[] {
				indexDecl,
				countDecl
			});

		// the for loop block
		scope.add(
			new CodeIterationStatement(
				initStatement,// initStatement
				new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.LESS_THAN,
					new CodeVariableReferenceExpression(indexDecl.getName()),
					new CodeVariableReferenceExpression(countDecl.getName())),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl.getName()))),// incrementStatement
				new CodeStatement[] {
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeThisReferenceExpression(),
							innerBind.getName(),
							new CodeExpression[] {
								new CodeVariableReferenceExpression("output"),
								new CodeVariableReferenceExpression(dataDecl.getName()),
								new CodeVariableReferenceExpression(indexDecl.getName()),
								new CodeVariableReferenceExpression(countDecl.getName()),
								new CodePrimitiveExpression(null)
							}))
				}));
	}

	private void buildIterationObject(CodeStatementCollection scope, CodeExpression objExpr, CodeMethod innerBind) {
		CodeExpression data = new CodeMethodInvokeExpression(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"asEntries",
				new CodeExpression[] {
					objExpr
				}),
			"iterator",
			null);

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
				new CodePrimitiveExpression(0));
		scope.add(indexDecl);

		// the item count
		CodeVariableDeclarationStatement countDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("count_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl.getName()),
					"size",
					null));
		scope.add(countDecl);

		// the iterator (embedded in for init)
		CodeVariableDeclarationStatement iteratorDecl =
			new CodeVariableDeclarationStatement(
				Iterator.class,
				scope.nextIdent("iterator_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl.getName()),
					"iterator",
					null));

		// the entry (embedded in for body)
		CodeVariableDeclarationStatement entryDecl = 
			new CodeVariableDeclarationStatement(
				Map.Entry.class,
				scope.nextIdent("entry_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(iteratorDecl.getName()),
					"next",
					null));
		
		// the for loop block
		scope.add(
			new CodeIterationStatement(
				iteratorDecl,// initStatement
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(iteratorDecl.getName()),
					"hasNext",
					null),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl.getName()))),// incrementStatement
				new CodeStatement[] {
					entryDecl,
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeThisReferenceExpression(),
							innerBind.getName(),
							new CodeExpression[] {
								new CodeVariableReferenceExpression("output"),
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression(entryDecl.getName()),
									"getValue",
									null),
								new CodeVariableReferenceExpression(indexDecl.getName()),
								new CodeVariableReferenceExpression(countDecl.getName()),
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression(entryDecl.getName()),
									"getKey",
									null)
							}))
				}));
	}

	private void buildIterationArray(CodeStatementCollection scope, CodeExpression arrayExpr, CodeMethod innerBind) {

		CodeExpression items = new CodeMethodInvokeExpression(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"asItems",
				new CodeExpression[] {
					arrayExpr
				}),
			"iterator",
			null);

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
				new CodePrimitiveExpression(0));

		// the item count
		CodeVariableDeclarationStatement countDecl =
			new CodeVariableDeclarationStatement(
				int.class,
				scope.nextIdent("count_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl.getName()),
					"size",
					null));

		scope.add(new CodeVariableCompoundDeclarationStatement(new CodeVariableDeclarationStatement[] {
				indexDecl,
				countDecl 	
			}));

		// the iterator (embedded in for init)
		CodeVariableDeclarationStatement iteratorDecl =
			new CodeVariableDeclarationStatement(
				Iterator.class,
				scope.nextIdent("iterator_"),
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(collectionDecl.getName()),
					"iterator",
					null));

		// the for loop block
		scope.add(
			new CodeIterationStatement(
				iteratorDecl,// initStatement
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(iteratorDecl.getName()),
					"hasNext",
					null),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl.getName()))),// incrementStatement
				new CodeStatement[] {
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeThisReferenceExpression(),
							innerBind.getName(),
							new CodeExpression[] {
								new CodeVariableReferenceExpression("output"),
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression(iteratorDecl.getName()),
									"next",
									null),
								new CodeVariableReferenceExpression(indexDecl.getName()),
								new CodeVariableReferenceExpression(countDecl.getName()),
								new CodePrimitiveExpression(null)
							}))
				}));
	}

	private void buildConditional(XORCommandNode node)
		throws IOException {

		CodeStatementCollection scope = this.scopeStack.peek();

		for (Node conditional : node.getChildren()) {
			if (conditional instanceof IFCommandNode) {
				scope = this.buildConditional((IFCommandNode)conditional, scope);
			}
		}
	}

	private CodeStatementCollection buildConditional(IFCommandNode node, CodeStatementCollection scope)
		throws IOException {

		this.flushBuffer();

		String test = null;
		Node testNode = node.getTest();
		if (testNode instanceof CodeBlockNode) {
			test = ((CodeBlockNode)testNode).getClientCode();
		} else if (testNode instanceof LiteralNode) {
			test = ((LiteralNode)testNode).getValue();
		} else if (testNode != null) {
			throw new IllegalArgumentException("Unexpected conditional test attribute: "+testNode.getClass());
		}

		if (test == null || test.length() == 0) {
			// no condition block needed
			if (node.hasChildren()) {
				this.scopeStack.push(scope);
				for (Node child : node.getChildren()) {
					this.buildNode(child);
				}
				this.flushBuffer();
				this.scopeStack.pop();
			}
			return scope;
		}

		CodeConditionStatement condition = new CodeConditionStatement();
		scope.add(condition);

		condition.setCondition(this.translateExpression(test));

		if (node.hasChildren()) {
			this.scopeStack.push(condition.getTrueStatements());
			for (Node child : node.getChildren()) {
				this.buildNode(child);
			}
			this.flushBuffer();
			this.scopeStack.pop();
		}
		
		return condition.getFalseStatements();
	}

	private CodeExpression translateExpression(String script) {

		// convert from JavaScript source to CodeDOM
		List<CodeMember> members = new SourceTranslator(this.viewType).translate(script);

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
					new CodeExpression[] {
						new CodeVariableReferenceExpression("output"),
						new CodeVariableReferenceExpression("data"),
						new CodeVariableReferenceExpression("index"),
						new CodeVariableReferenceExpression("count"),
						new CodeVariableReferenceExpression("key")
					});
		}
		return expression;
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
		CodeVariableDeclarationStatement localVar = CodeDOMUtility.nextID(scope);
		scope.add(localVar);

		String id = localVar.getName();

		// emit the value of the var
		CodeStatement emitVar = CodeDOMUtility.emitVarValue(id);
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
			block = new ExpressionNode(block.getValue());
		}

		String script = block.getClientCode();
		CodeExpression codeExpr = this.translateExpression(script);
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
