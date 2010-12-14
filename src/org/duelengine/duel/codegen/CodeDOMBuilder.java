package org.duelengine.duel.codegen;

import java.io.IOException;
import java.util.*;
import org.duelengine.duel.*;
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
	private final DataEncoder encoder;
	private final StringBuilder buffer;
	private final Stack<CodeStatementCollection> scopeStack = new Stack<CodeStatementCollection>();
	private CodeTypeDeclaration viewType;
	private TagMode tagMode;
	private boolean globalsEmitted;

	public CodeDOMBuilder() {
		this(null);
	}

	public CodeDOMBuilder(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
		this.buffer = new StringBuilder();
		this.formatter = new HTMLFormatter();
		this.encoder = new DataEncoder(this.settings.getNewline(), this.settings.getIndent());
	}

	public CodeTypeDeclaration buildView(VIEWCommandNode viewNode) throws IOException {
		try {
			// prepend the server-side prefix
			String fullName = this.settings.getFullName(viewNode.getName());
			int lastDot = fullName.lastIndexOf('.');
			String name = fullName.substring(lastDot+1);
			String ns = (lastDot > 0) ? fullName.substring(0, lastDot) : null;

			this.scopeStack.clear();
			this.tagMode = TagMode.None;
			this.globalsEmitted = false;
			this.viewType = CodeDOMUtility.createViewType(ns, name);

			CodeMethod method = this.buildRenderMethod(viewNode.getChildren()).withOverride();

			method.setName("render");
			method.setAccess(AccessModifierType.PROTECTED);

			return this.viewType;

		} finally {
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
				new CodeParameterDeclarationExpression(DuelContext.class, "context"),
				new CodeParameterDeclarationExpression(Object.class, "data"),
				new CodeParameterDeclarationExpression(int.class, "index"),
				new CodeParameterDeclarationExpression(int.class, "count"),
				new CodeParameterDeclarationExpression(String.class, "key")
			}).withThrows(IOException.class);

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

		} else if (node instanceof CommandNode) {
			CommandNode command = (CommandNode)node;
			switch (command.getCommand()) {
				case XOR:
					this.buildConditional((XORCommandNode)node);
					break;
				case IF:
					this.buildConditional((IFCommandNode)node, this.scopeStack.peek());
					break;
				case FOR:
					this.buildIteration((FORCommandNode)node);
					break;
				case CALL:
					this.buildCall((CALLCommandNode)node);
					break;
				case PART:
					this.buildPartPlaceholder((PARTCommandNode)node);
					break;
				default:
					throw new InvalidNodeException("Invalid command node type: "+command.getCommand(), command);
			}

		} else if (node instanceof ElementNode) {
			ElementNode element = (ElementNode)node;
			this.buildElement(element);

		} else if (node instanceof CodeBlockNode) {
			this.buildCodeBlock((CodeBlockNode)node);

		} else if (node instanceof CommentNode) {
			// emit comment markup
			CommentNode comment = (CommentNode)node;
			this.formatter.writeComment(this.buffer, comment.getValue());

		} else if (node instanceof DocTypeNode) {
			// emit doctype
			DocTypeNode doctype = (DocTypeNode)node;
			this.formatter.writeDocType(this.buffer, doctype.getValue());

		} else if (node instanceof CodeCommentNode) {
			this.buildComment((CodeCommentNode)node);
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
			// TODO: how to handle switcher method cases?
			throw new InvalidNodeException("Unexpected Call command view attribute: "+attr, attr);
		}

		// prepend the server-side prefix
		viewName = this.settings.getFullName(viewName);

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
			dataExpr = this.translateExpression((CodeBlockNode)callData, false);
		} else {
			dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
		}

		CodeExpression indexExpr;
		DuelNode callIndex = node.getAttribute(CALLCommandNode.INDEX);
		if (callIndex instanceof CodeBlockNode) {
			indexExpr = this.translateExpression((CodeBlockNode)callIndex, false);
		} else {
			indexExpr = new CodeVariableReferenceExpression(int.class, "index");
		}

		CodeExpression countExpr;
		DuelNode callCount = node.getAttribute(CALLCommandNode.COUNT);
		if (callCount instanceof CodeBlockNode) {
			countExpr = this.translateExpression((CodeBlockNode)callCount, false);
		} else {
			countExpr = new CodeVariableReferenceExpression(int.class, "count");
		}

		CodeExpression keyExpr;
		DuelNode callKey = node.getAttribute(CALLCommandNode.KEY);
		if (callKey instanceof CodeBlockNode) {
			keyExpr = this.translateExpression((CodeBlockNode)callKey, false);
		} else {
			keyExpr = new CodeVariableReferenceExpression(String.class, "key");
		}

		CodeStatementCollection scope = this.scopeStack.peek();
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"renderView",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), field),
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
			new CodeMethodReturnStatement(new CodePrimitiveExpression(partName))).withOverride();
		part.add(getNameMethod);

		CodeTypeDeclaration parentView = this.viewType;
		try {
			this.viewType = part;

			CodeMethod renderMethod = this.buildRenderMethod(node.getChildren()).withOverride();

			renderMethod.setName("render");
			renderMethod.setAccess(AccessModifierType.PROTECTED);

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
					Void.class,
					new CodeThisReferenceExpression(),
					"addPart",
					createPart)));

		// parent scope
		CodeStatementCollection scope = this.scopeStack.peek();

		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"renderPart",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			new CodePrimitiveExpression(part.getName()),
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
			CodeExpression countExpr = this.translateExpression((CodeBlockNode)loopCount, false);

			DuelNode loopData = node.getAttribute(FORCommandNode.DATA);
			if (loopData instanceof CodeBlockNode) {
				dataExpr = this.translateExpression((CodeBlockNode)loopData, false);
			} else {
				dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
			}

			this.buildIterationCount(scope, countExpr, dataExpr, innerBind);

		} else {
			DuelNode loopObj = node.getAttribute(FORCommandNode.IN);
			if (loopObj instanceof CodeBlockNode) {
				CodeExpression objExpr = this.translateExpression((CodeBlockNode)loopObj, false);
				this.buildIterationObject(scope, objExpr, innerBind);

			} else {
				DuelNode loopArray = node.getAttribute(FORCommandNode.EACH);
				if (!(loopArray instanceof CodeBlockNode)) {
					throw new InvalidNodeException("FOR loop missing arguments", loopArray);
				}

				CodeExpression arrayExpr = this.translateExpression((CodeBlockNode)loopArray, false);
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
						Void.class,
						new CodeThisReferenceExpression(),
						innerBind.getName(),
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(dataDecl),
						new CodeVariableReferenceExpression(indexDecl),
						new CodeVariableReferenceExpression(countDecl),
						CodePrimitiveExpression.NULL))));
	}

	private void buildIterationObject(CodeStatementCollection scope, CodeExpression objExpr, CodeMethod innerBind) {
		CodeExpression data =
			new CodeMethodInvokeExpression(
				Set.class,
				CodeDOMUtility.ensureMap(objExpr),
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
					int.class,
					new CodeVariableReferenceExpression(collectionDecl),
					"size"));

		scope.add(new CodeVariableCompoundDeclarationStatement(indexDecl, countDecl));

		// the iterator (embedded in for init)
		CodeVariableDeclarationStatement iteratorDecl =
			new CodeVariableDeclarationStatement(
				Iterator.class,
				scope.nextIdent("iterator_"),
				new CodeMethodInvokeExpression(
					Iterator.class,
					new CodeVariableReferenceExpression(collectionDecl),
					"iterator"));

		// the entry (embedded in for body)
		CodeVariableDeclarationStatement entryDecl = 
			new CodeVariableDeclarationStatement(
				Map.Entry.class,
				scope.nextIdent("entry_"),
				new CodeCastExpression(Map.Entry.class,
					new CodeMethodInvokeExpression(
						Object.class,
						new CodeVariableReferenceExpression(iteratorDecl),
						"next")));

		// the for loop block
		scope.add(
			new CodeIterationStatement(
				iteratorDecl,// initStatement
				new CodeMethodInvokeExpression(
					boolean.class,
					new CodeVariableReferenceExpression(iteratorDecl),
					"hasNext"),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl))),// incrementStatement
				entryDecl,
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						innerBind.getReturnType(),
						new CodeThisReferenceExpression(),
						innerBind.getName(),
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeMethodInvokeExpression(
							Object.class,
							new CodeVariableReferenceExpression(entryDecl),
							"getValue"),
						new CodeVariableReferenceExpression(indexDecl),
						new CodeVariableReferenceExpression(countDecl),
						CodeDOMUtility.ensureString(
							new CodeMethodInvokeExpression(
								Object.class,
								new CodeVariableReferenceExpression(entryDecl),
								"getKey"))))));
	}

	private void buildIterationArray(CodeStatementCollection scope, CodeExpression arrayExpr, CodeMethod innerBind) {

		CodeExpression items = CodeDOMUtility.ensureCollection(arrayExpr);

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
					int.class,
					new CodeVariableReferenceExpression(collectionDecl),
					"size"));

		scope.add(new CodeVariableCompoundDeclarationStatement(indexDecl, countDecl));

		// the iterator (embedded in for init)
		CodeVariableDeclarationStatement iteratorDecl =
			new CodeVariableDeclarationStatement(
				Iterator.class,
				scope.nextIdent("iterator_"),
				new CodeMethodInvokeExpression(
					Iterator.class,
					new CodeVariableReferenceExpression(collectionDecl),
					"iterator"));

		// the for loop block
		scope.add(
			new CodeIterationStatement(
				iteratorDecl,// initStatement
				new CodeMethodInvokeExpression(
					boolean.class,
					new CodeVariableReferenceExpression(iteratorDecl),
					"hasNext"),// testExpression
				new CodeExpressionStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_INCREMENT,
						new CodeVariableReferenceExpression(indexDecl))),// incrementStatement
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						innerBind.getReturnType(),
						new CodeThisReferenceExpression(),
						innerBind.getName(),
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeMethodInvokeExpression(
							Map.Entry.class,
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

		condition.setCondition(this.translateExpression(testNode, false));

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

	private CodeExpression translateExpression(CodeBlockNode node, boolean canWrite) {

		try {
			// convert from JavaScript source to CodeDOM
			List<CodeMember> members = new ScriptTranslator(this.viewType).translate(node.getClientCode());
			boolean firstIsMethod = (members.size() > 0) && members.get(0) instanceof CodeMethod;

			CodeExpression expression = null;
			if (firstIsMethod) {
				// attempt to extract single expression (inline the return expression)
				expression = CodeDOMUtility.inlineMethod((CodeMethod)members.get(0));
			}

			if (expression != null) {
				// add remaining CodeDOM members to viewType
				for (int i=1, length=members.size(); i<length; i++) {
					this.viewType.add(members.get(i));
				}

			} else if (firstIsMethod) {
				// add all CodeDOM members to viewType
				this.viewType.addAll(members);

				CodeMethod method = (CodeMethod)members.get(0);

				// have the expression be a method invocation
				expression = new CodeMethodInvokeExpression(
					method.getReturnType(),
					new CodeThisReferenceExpression(),
					method.getName(),
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					new CodeVariableReferenceExpression(int.class, "index"),
					new CodeVariableReferenceExpression(int.class, "count"),
					new CodeVariableReferenceExpression(String.class, "key"));
			}

			if (canWrite && members.size() > 0 &&
				members.get(0).getUserData(ScriptTranslator.EXTERNAL_REFS) instanceof Object[]) {

				Object[] refs = (Object[])members.get(0).getUserData(ScriptTranslator.EXTERNAL_REFS);
				CodeExpression[] args = new CodeExpression[refs.length+1];
				args[0] = new CodeVariableReferenceExpression(DuelContext.class, "context");
				for (int i=0, length=refs.length; i<length; i++) {
					args[i+1] = new CodePrimitiveExpression(refs[i]);
				}

				// if data present, then run server-side, else defer execution to client
				CodeConditionStatement runtimeCheck = new CodeConditionStatement(
					new CodeMethodInvokeExpression(
						boolean.class,
						new CodeThisReferenceExpression(),
						"hasGlobals",
						args));

				if (firstIsMethod && expression instanceof CodeMethodInvokeExpression) {
					CodeMethod method = (CodeMethod)members.get(0);
					runtimeCheck.getTrueStatements().addAll(method.getStatements());
					this.viewType.getMembers().remove(method);
				} else {
					runtimeCheck.getTrueStatements().add(new CodeMethodReturnStatement(expression));
				}

				CodeMethod runtimeCheckMethod = new CodeMethod(
					AccessModifierType.PRIVATE,
					Object.class,
					this.viewType.nextIdent("hybrid_"),
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "context"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					runtimeCheck).withThrows(IOException.class);

				this.viewType.add(runtimeCheckMethod);

				// ensure a break in the write stream
				this.flushBuffer();
				this.scopeStack.push(runtimeCheck.getFalseStatements());
				try {
					// defer blocks that cannot be fully processed server-side
					this.buildDeferredWrite(node.getClientCode(), node.getArgSize());
					this.flushBuffer();

				} finally {
					this.scopeStack.pop();
				}

				// return null if immediately writen
				runtimeCheck.getFalseStatements().add(new CodeMethodReturnStatement(CodePrimitiveExpression.NULL));

				// have the expression be a method invocation
				expression = new CodeMethodInvokeExpression(
					runtimeCheckMethod.getReturnType(),
					new CodeThisReferenceExpression(),
					runtimeCheckMethod.getName(),
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					new CodeVariableReferenceExpression(int.class, "index"),
					new CodeVariableReferenceExpression(int.class, "count"),
					new CodeVariableReferenceExpression(String.class, "key"));
			}
			return expression;

		} catch (ScriptTranslationException ex) {
			throw ex.adjustErrorStatistics(node);

		} catch (Exception ex) {
			String message = ex.getMessage();
			if (message == null) {
				message = ex.toString();
			}
			throw new InvalidNodeException(message, node, ex);
		}
	}

	private void buildDeferredWrite(String clientCode, int argSize)
			throws IOException {

		boolean prettyPrint = this.encoder.isPrettyPrint();
		CodeStatementCollection scope = this.scopeStack.peek();

		// use the script tag as its own replacement element
		this.formatter.writeOpenElementBeginTag(this.buffer, "script");
		this.formatter.writeAttribute(this.buffer, "type", "text/javascript");
		this.formatter.writeCloseElementBeginTag(this.buffer);
		this.ensureGlobalsEmitted(false);

		// emit patch function call which serializes attributes into object
		this.buffer.append("duel.write(");

		// emit client code directly
		this.buffer.append(clientCode);

		if (argSize > 0) {
			this.buffer.append(',');
			if (prettyPrint) {
				this.buffer.append(' ');
			}
			this.flushBuffer();

			// emit data var as literal
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(Object.class, "data"),
				CodePrimitiveExpression.ONE));

			if (argSize > 1) {
				this.buffer.append(',');
				if (prettyPrint) {
					this.buffer.append(' ');
				}
				this.flushBuffer();

				// emit index var as number
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE));

				if (argSize > 2) {
					this.buffer.append(',');
					if (prettyPrint) {
						this.buffer.append(' ');
					}
					this.flushBuffer();

					// emit count var as number
					scope.add(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count"),
						CodePrimitiveExpression.ONE));

					if (argSize > 3) {
						this.buffer.append(',');
						if (prettyPrint) {
							this.buffer.append(' ');
						}
						this.flushBuffer();

						// emit key var as String
						scope.add(new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"dataEncode",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeVariableReferenceExpression(String.class, "key"),
							CodePrimitiveExpression.ONE));
					}
				}
			}
		}
		this.buffer.append(");");

		// last parameter will be the current data
		this.formatter.writeElementEndTag(this.buffer, "script");
	}

	private void buildElement(ElementNode element)
		throws IOException {

		String tagName = element.getTagName();

		if ("script".equalsIgnoreCase(tagName)) {
			this.ensureGlobalsEmitted(true);
		}
		
		this.formatter.writeOpenElementBeginTag(this.buffer, tagName);

		int argSize = 0;
		Map<String, DataEncoder.Snippet> deferredAttrs = new LinkedHashMap<String, DataEncoder.Snippet>();
		for (String attrName : element.getAttributeNames()) {
			DuelNode attrVal = element.getAttribute(attrName);

			if (attrVal == null) {
				this.formatter.writeAttribute(this.buffer, attrName, null);

			} else if (attrVal instanceof LiteralNode) {
				this.formatter.writeAttribute(this.buffer, attrName, ((LiteralNode)attrVal).getValue());

			} else if (attrVal instanceof CodeBlockNode) {

				try {
					CodeStatement writeStatement = this.processCodeBlock((CodeBlockNode)attrVal);
					if (writeStatement != null) {
						this.formatter.writeOpenAttribute(this.buffer, attrName);
						this.flushBuffer();
						this.scopeStack.peek().add(writeStatement);
						this.formatter.writeCloseAttribute(this.buffer);
					} else {
						this.formatter.writeAttribute(this.buffer, attrName, null);
					}

				} catch (Exception ex) {
					
					// only defer attributes that cannot be processed server-side
					deferredAttrs.put(attrName, DataEncoder.snippet(((CodeBlockNode)attrVal).getClientCode()));
					argSize = Math.max(argSize, ((CodeBlockNode)attrVal).getArgSize());
				}

			} else {
				throw new InvalidNodeException("Invalid attribute node type: "+attrVal.getClass(), attrVal);
			}
		}

		CodeVariableDeclarationStatement idVar = null;
		String idValue = null;
		if (deferredAttrs.size() > 0) {
			DuelNode id = element.getAttribute("id");
			if (id == null) {
				this.formatter.writeOpenAttribute(this.buffer, "id");
				idVar = this.emitClientID();
				this.formatter.writeCloseAttribute(this.buffer);

			} else if (id instanceof LiteralNode) {
				idValue = ((LiteralNode)id).getValue();

			} else {
				// TODO: find cases where this may be legitimate
				throw new InvalidNodeException("Invalid ID attribute node type: "+id.getClass(), id);
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
			this.buildDeferredAttributeExecution(deferredAttrs, idVar, idValue, argSize);
		}
	}

	private void buildDeferredAttributeExecution(
		Map<String, DataEncoder.Snippet> deferredAttrs,
		CodeVariableDeclarationStatement idVar,
		String idValue,
		int argSize) throws IOException {

		boolean prettyPrint = this.encoder.isPrettyPrint();

		CodeStatementCollection scope = this.scopeStack.peek();

		// execute any deferred attributes using idVar
		this.formatter
			.writeOpenElementBeginTag(this.buffer, "script")
			.writeAttribute(this.buffer, "type", "text/javascript")
			.writeCloseElementBeginTag(this.buffer);
		this.ensureGlobalsEmitted(false);

		// emit patch function call which serializes attributes into object
		this.buffer.append("duel.attr(");

		// emit id var or known value
		if (idVar != null) {
			this.flushBuffer();
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(idVar),
				CodePrimitiveExpression.ONE));
		} else {
			this.encoder.write(this.buffer, idValue, 1);
		}
		this.buffer.append(',');
		if (prettyPrint) {
			this.buffer.append(' ');
		}

		// emit deferredAttrs as a JS Object
		this.encoder.write(this.buffer, deferredAttrs, 1);

		if (argSize > 0) {
			this.buffer.append(',');
			if (prettyPrint) {
				this.buffer.append(' ');
			}
			this.flushBuffer();

			// emit data var as literal
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(Object.class, "data"),
				CodePrimitiveExpression.ONE));

			if (argSize > 1) {
				this.buffer.append(',');
				if (prettyPrint) {
					this.buffer.append(' ');
				}
				this.flushBuffer();

				// emit index var as number
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE));

				if (argSize > 2) {
					this.buffer.append(',');
					if (prettyPrint) {
						this.buffer.append(' ');
					}
					this.flushBuffer();

					// emit count var as number
					scope.add(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count"),
						CodePrimitiveExpression.ONE));

					if (argSize > 3) {
						this.buffer.append(',');
						if (prettyPrint) {
							this.buffer.append(' ');
						}
						this.flushBuffer();

						// emit key var as String
						scope.add(new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"dataEncode",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeVariableReferenceExpression(String.class, "key"),
							CodePrimitiveExpression.ONE));
					}
				}
			}
		}
		this.buffer.append(");");

		// last parameter will be the current data
		this.formatter.writeElementEndTag(this.buffer, "script");
	}

	private void buildCodeBlock(CodeBlockNode node) throws IOException {
		try {
			CodeStatement writeStatement = this.processCodeBlock(node);
			if (writeStatement == null) {
				return;
			}

			this.flushBuffer();
			this.scopeStack.peek().add(writeStatement);
			return;

		} catch (Exception ex) {
			// only defer blocks that cannot be fully processed server-side
			this.buildDeferredCodeBlock(node.getClientCode(), node.getArgSize());
		}
	}

	private void buildDeferredCodeBlock(String clientCode, int argSize)
			throws IOException {

		boolean prettyPrint = this.encoder.isPrettyPrint();
		CodeStatementCollection scope = this.scopeStack.peek();

		// use the script tag as its own replacement element
		this.formatter.writeOpenElementBeginTag(this.buffer, "script");
		this.formatter.writeAttribute(this.buffer, "type", "text/javascript");
		this.formatter.writeOpenAttribute(this.buffer, "id");
		CodeVariableDeclarationStatement idVar = this.emitClientID();
		this.formatter.writeCloseAttribute(this.buffer);
		this.formatter.writeCloseElementBeginTag(this.buffer);
		this.ensureGlobalsEmitted(false);

		// emit patch function call which serializes attributes into object
		this.buffer.append("duel.replace(");

		// emit id var or known value
		this.flushBuffer();
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			new CodeVariableReferenceExpression(idVar),
			CodePrimitiveExpression.ONE));
		this.buffer.append(',');
		if (prettyPrint) {
			this.buffer.append(' ');
		}

		// emit client code directly
		this.buffer.append(clientCode);

		if (argSize > 0) {
			this.buffer.append(',');
			if (prettyPrint) {
				this.buffer.append(' ');
			}
			this.flushBuffer();

			// emit data var as literal
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(Object.class, "data"),
				CodePrimitiveExpression.ONE));

			if (argSize > 1) {
				this.buffer.append(',');
				if (prettyPrint) {
					this.buffer.append(' ');
				}
				this.flushBuffer();

				// emit index var as number
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE));

				if (argSize > 2) {
					this.buffer.append(',');
					if (prettyPrint) {
						this.buffer.append(' ');
					}
					this.flushBuffer();

					// emit count var as number
					scope.add(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count"),
						CodePrimitiveExpression.ONE));

					if (argSize > 3) {
						this.buffer.append(',');
						if (prettyPrint) {
							this.buffer.append(' ');
						}
						this.flushBuffer();

						// emit key var as String
						scope.add(new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"dataEncode",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeVariableReferenceExpression(String.class, "key"),
							CodePrimitiveExpression.ONE));
					}
				}
			}
		}
		this.buffer.append(");");

		// last parameter will be the current data
		this.formatter.writeElementEndTag(this.buffer, "script");
	}

	private CodeVariableDeclarationStatement emitClientID() {
		this.flushBuffer();
		CodeStatementCollection scope = this.scopeStack.peek();

		// the var contains a new unique ident
		CodeVariableDeclarationStatement localVar = CodeDOMUtility.nextID(scope);
		scope.add(localVar);

		String id = localVar.getName();

		// emit the value of the var
		CodeStatement emitVar = CodeDOMUtility.emitVarValue(localVar);
		scope.add(emitVar);

		return localVar;
	}

	/**
	 * @param block
	 * @return Code which emits the evaluated value of a code block
	 */
	private CodeStatement processCodeBlock(CodeBlockNode block) {
		boolean htmlEncode = true;
		if (block instanceof MarkupExpressionNode) {
			htmlEncode = false;
			block = new ExpressionNode(block.getValue(), block.getIndex(), block.getLine(), block.getColumn());
		}

		CodeExpression codeExpr = this.translateExpression(block, true);
		if (codeExpr == null) {
			return null;
		}

		return htmlEncode ?
			CodeDOMUtility.emitExpressionSafe(codeExpr) :
			CodeDOMUtility.emitExpression(codeExpr);
	}

	private void ensureGlobalsEmitted(boolean needsTags) {
		if (this.globalsEmitted) {
			return;
		}

		// emit check just inside first script block
		this.flushBuffer();

		this.scopeStack.peek().add(
			new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"writeGlobals",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodePrimitiveExpression(needsTags)));

		// check scope chain for any condition or iteration blocks that
		// might prevent this from being emitted. if found do not mark.
		// there is a runtime check as well which will prevent multiple.
		for (CodeStatementCollection scope : this.scopeStack) {
			// TODO: this is too naive. doesn't work with hybrid methods
			if (!(scope.getOwner() instanceof CodeMethod)) {
				return;
			}
		}

		this.globalsEmitted = true;
	}

	private CodeMethod ensureInitMethod() {
		for (CodeMember member : this.viewType.getMembers()) {
			if (member instanceof CodeMethod &&
				"init".equals(((CodeMethod)member).getName())) {
				return (CodeMethod)member;
			}
		}

		CodeMethod initMethod = new CodeMethod(
			AccessModifierType.PROTECTED,
			Void.class,
			"init",
			null).withOverride();
		this.viewType.add(initMethod);

		return initMethod;
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
