package org.duelengine.duel.codegen;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.duelengine.duel.DataEncoder;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.DuelView;
import org.duelengine.duel.HTMLFormatter;
import org.duelengine.duel.ast.CALLCommandNode;
import org.duelengine.duel.ast.CodeBlockNode;
import org.duelengine.duel.ast.CodeCommentNode;
import org.duelengine.duel.ast.CommandNode;
import org.duelengine.duel.ast.CommentNode;
import org.duelengine.duel.ast.DocTypeNode;
import org.duelengine.duel.ast.DuelNode;
import org.duelengine.duel.ast.ElementNode;
import org.duelengine.duel.ast.ExpressionNode;
import org.duelengine.duel.ast.FORCommandNode;
import org.duelengine.duel.ast.IFCommandNode;
import org.duelengine.duel.ast.LiteralNode;
import org.duelengine.duel.ast.MarkupExpressionNode;
import org.duelengine.duel.ast.PARTCommandNode;
import org.duelengine.duel.ast.UnknownNode;
import org.duelengine.duel.ast.VIEWCommandNode;
import org.duelengine.duel.ast.XORCommandNode;
import org.duelengine.duel.codedom.AccessModifierType;
import org.duelengine.duel.codedom.CodeBinaryOperatorExpression;
import org.duelengine.duel.codedom.CodeBinaryOperatorType;
import org.duelengine.duel.codedom.CodeCastExpression;
import org.duelengine.duel.codedom.CodeCommentStatement;
import org.duelengine.duel.codedom.CodeConditionStatement;
import org.duelengine.duel.codedom.CodeExpression;
import org.duelengine.duel.codedom.CodeExpressionStatement;
import org.duelengine.duel.codedom.CodeField;
import org.duelengine.duel.codedom.CodeFieldReferenceExpression;
import org.duelengine.duel.codedom.CodeIterationStatement;
import org.duelengine.duel.codedom.CodeMember;
import org.duelengine.duel.codedom.CodeMethod;
import org.duelengine.duel.codedom.CodeMethodInvokeExpression;
import org.duelengine.duel.codedom.CodeMethodReturnStatement;
import org.duelengine.duel.codedom.CodeObjectCreateExpression;
import org.duelengine.duel.codedom.CodeParameterDeclarationExpression;
import org.duelengine.duel.codedom.CodePrimitiveExpression;
import org.duelengine.duel.codedom.CodeStatement;
import org.duelengine.duel.codedom.CodeStatementCollection;
import org.duelengine.duel.codedom.CodeThisReferenceExpression;
import org.duelengine.duel.codedom.CodeTypeDeclaration;
import org.duelengine.duel.codedom.CodeUnaryOperatorExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorType;
import org.duelengine.duel.codedom.CodeVariableCompoundDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableReferenceExpression;
import org.duelengine.duel.parsing.InvalidNodeException;

/**
 * Translates the view AST to CodeDOM tree
 */
public class CodeDOMBuilder {

	private enum TagMode {
		NONE,

		PRE_MODE,

		SUSPEND_MODE
	}

	private final CodeGenSettings settings;
	private final HTMLFormatter formatter;
	private final DataEncoder encoder;
	private final StringBuilder buffer;
	private final Stack<CodeStatementCollection> scopeStack = new Stack<CodeStatementCollection>();
	private CodeTypeDeclaration viewType;
	private TagMode tagMode;
	private boolean needsExtrasEmitted;
	private boolean hasScripts;

	public CodeDOMBuilder() {
		this(null);
	}

	public CodeDOMBuilder(CodeGenSettings codeGenSettings) {
		settings = (codeGenSettings != null) ? codeGenSettings : new CodeGenSettings();
		buffer = new StringBuilder();
		formatter = new HTMLFormatter();
		encoder = new DataEncoder(settings.getNewline(), settings.getIndent());
	}

	public CodeTypeDeclaration buildView(VIEWCommandNode viewNode) throws IOException {
		try {
			// prepend the server-side prefix
			String fullName = settings.getServerName(viewNode.getName());
			int lastDot = fullName.lastIndexOf('.');
			String name = fullName.substring(lastDot+1);
			String ns = (lastDot > 0) ? fullName.substring(0, lastDot) : null;

			scopeStack.clear();
			tagMode = TagMode.NONE;
			hasScripts = false;
			needsExtrasEmitted = true;
			viewType = CodeDOMUtility.createViewType(ns, name);

			CodeMethod method = buildRenderMethod(viewNode.getChildren()).withOverride();

			method.setName("render");
			method.setAccess(AccessModifierType.PROTECTED);

			return viewType;

		} finally {
			viewType = null;
		}
	}

	private CodeMethod buildRenderMethod(List<DuelNode> content)
		throws IOException {

		CodeMethod method = new CodeMethod(
			AccessModifierType.PRIVATE,
			Void.class,
			viewType.nextIdent("render_"),
			new CodeParameterDeclarationExpression[] {
				new CodeParameterDeclarationExpression(DuelContext.class, "context"),
				new CodeParameterDeclarationExpression(Object.class, "data"),
				new CodeParameterDeclarationExpression(int.class, "index"),
				new CodeParameterDeclarationExpression(int.class, "count"),
				new CodeParameterDeclarationExpression(String.class, "key")
			}).withThrows(IOException.class);

		viewType.add(method);

		flushBuffer();
		scopeStack.push(method.getStatements());
		for (DuelNode node : content) {
			buildNode(node);
		}
		flushBuffer();
		scopeStack.pop();

		return method;
	}

	private void buildNode(DuelNode node) throws IOException {
		if (node instanceof LiteralNode) {
			if (node instanceof UnknownNode) {
				// IE conditional comments get parsed as an unknown literal
				// this ensures if a script is within a conditional comment
				// that extras are emitted before the opening of the comment 
				ensureExtrasEmitted(true);
			}
			String literal = ((LiteralNode)node).getValue();
			if (tagMode == TagMode.SUSPEND_MODE || node instanceof UnknownNode) {
				buffer.append(literal);

			} else {
				if (literal != null &&
					literal.length() > 0 &&
					tagMode != TagMode.PRE_MODE &&
					settings.getNormalizeWhitespace()) {

					// not very efficient but allows simple normalization
					literal = literal.replaceAll("^[\\r\\n]+", "").replaceAll("[\\r\\n]+$", "").replaceAll("\\s+", " ");
					if (literal.isEmpty()) {
						literal = " ";
					}
				}

				formatter.writeLiteral(buffer, literal, settings.getEncodeNonASCII());
			}

		} else if (node instanceof CommandNode) {
			CommandNode command = (CommandNode)node;
			switch (command.getCommand()) {
				case XOR:
					buildConditional((XORCommandNode)node);
					break;
				case IF:
					buildConditional((IFCommandNode)node, scopeStack.peek());
					break;
				case FOR:
					buildIteration((FORCommandNode)node);
					break;
				case CALL:
					buildCall((CALLCommandNode)node);
					break;
				case PART:
					buildPartPlaceholder((PARTCommandNode)node);
					break;
				default:
					throw new InvalidNodeException("Invalid command node type: "+command.getCommand(), command);
			}

		} else if (node instanceof ElementNode) {
			ElementNode element = (ElementNode)node;
			buildElement(element);

		} else if (node instanceof CodeBlockNode) {
			buildCodeBlock((CodeBlockNode)node);

		} else if (node instanceof CommentNode) {
			// emit comment markup
			CommentNode comment = (CommentNode)node;
			formatter.writeComment(buffer, comment.getValue());

		} else if (node instanceof DocTypeNode) {
			// emit doctype
			DocTypeNode doctype = (DocTypeNode)node;
			formatter.writeDocType(buffer, doctype.getValue());

		} else if (node instanceof CodeCommentNode) {
			buildComment((CodeCommentNode)node);
		}
	}

	private void buildCall(CALLCommandNode node)
		throws IOException {

		if (node.isDefer()) {
			buildDeferredCall(node);
			return;
		}

		// generate a field to hold the child template
		CodeField field = new CodeField(
				AccessModifierType.PRIVATE,
				DuelView.class,
				viewType.nextIdent("view_"));
		viewType.add(field);

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
		viewName = settings.getServerName(viewName);

		CodeExpression[] ctorArgs = new CodeExpression[node.getChildren().size()];

		int i = 0;
		for (DuelNode child : node.getChildren()) {
			ctorArgs[i++] = buildPart((PARTCommandNode)child);
		}

		// insert an initialization statement into the init method
		CodeMethod initMethod = ensureInitMethod();
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
			dataExpr = translateExpression((CodeBlockNode)callData, false);
		} else {
			dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
		}

		CodeExpression indexExpr;
		DuelNode callIndex = node.getAttribute(CALLCommandNode.INDEX);
		if (callIndex instanceof CodeBlockNode) {
			indexExpr = translateExpression((CodeBlockNode)callIndex, false);
		} else {
			indexExpr = new CodeVariableReferenceExpression(int.class, "index");
		}

		CodeExpression countExpr;
		DuelNode callCount = node.getAttribute(CALLCommandNode.COUNT);
		if (callCount instanceof CodeBlockNode) {
			countExpr = translateExpression((CodeBlockNode)callCount, false);
		} else {
			countExpr = new CodeVariableReferenceExpression(int.class, "count");
		}

		CodeExpression keyExpr;
		DuelNode callKey = node.getAttribute(CALLCommandNode.KEY);
		if (callKey instanceof CodeBlockNode) {
			keyExpr = translateExpression((CodeBlockNode)callKey, false);
		} else {
			keyExpr = new CodeVariableReferenceExpression(String.class, "key");
		}

		flushBuffer();
		CodeStatementCollection scope = scopeStack.peek();
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

	private void buildDeferredCall(CALLCommandNode node)
		throws IOException {

		boolean prettyPrint = encoder.isPrettyPrint();
		CodeStatementCollection scope = scopeStack.peek();

		// use the script tag as its own replacement element
		hasScripts = true;
		formatter.writeOpenElementBeginTag(buffer, "script");
		if (settings.getScriptTypeAttr()) {
			formatter.writeAttribute(buffer, "type", "text/javascript");
		}
		formatter.writeOpenAttribute(buffer, "id");
		CodeVariableDeclarationStatement idVar = emitClientID();
		formatter
			.writeCloseAttribute(buffer)
			.writeCloseElementBeginTag(buffer);
		ensureExtrasEmitted(false);

		// determine the name of the template
		String viewName = null;
		DuelNode callView = node.getAttribute(CALLCommandNode.VIEW);
		if (callView instanceof LiteralNode) {
			viewName = ((LiteralNode)callView).getValue();
		} else if (callView instanceof ExpressionNode) {
			viewName = ((ExpressionNode)callView).getValue();
		}

		if (viewName != null) {
			// prepend the server-side prefix
			viewName = settings.getClientName(viewName);
			buffer.append(viewName);

		} else {
			// wrap the code block as an anonymous DUEL view
			buffer.append("duel(");
			if (callView instanceof CodeBlockNode) {
				CodeExpression viewExpr = translateExpression((CodeBlockNode)callView, false);

				// emit view expression as a literal result of the expression
				flushBuffer();
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					viewExpr,
					CodePrimitiveExpression.ONE));

			} else {
				// TODO: how to handle switcher method cases?
				throw new InvalidNodeException("Unexpected Call command view attribute: "+callView, callView);
			}
			buffer.append(")");
		}

		// bind the view
		buffer.append("(");
		flushBuffer();

		CodeExpression dataExpr;
		DuelNode callData = node.getAttribute(CALLCommandNode.DATA);
		if (callData instanceof CodeBlockNode) {
			dataExpr = translateExpression((CodeBlockNode)callData, false);
		} else {
			dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
		}

		// emit data expression as a literal
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			dataExpr,
			CodePrimitiveExpression.ONE));

		buffer.append(',');
		if (prettyPrint) {
			buffer.append(' ');
		}
		flushBuffer();

		CodeExpression indexExpr;
		DuelNode callIndex = node.getAttribute(CALLCommandNode.INDEX);
		if (callIndex instanceof CodeBlockNode) {
			indexExpr = translateExpression((CodeBlockNode)callIndex, false);
		} else {
			indexExpr = new CodeVariableReferenceExpression(int.class, "index");
		}

		// emit index expression as a literal
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			indexExpr,
			CodePrimitiveExpression.ONE));

		buffer.append(',');
		if (prettyPrint) {
			buffer.append(' ');
		}
		flushBuffer();

		CodeExpression countExpr;
		DuelNode callCount = node.getAttribute(CALLCommandNode.COUNT);
		if (callCount instanceof CodeBlockNode) {
			countExpr = translateExpression((CodeBlockNode)callCount, false);
		} else {
			countExpr = new CodeVariableReferenceExpression(int.class, "count");
		}

		// emit count expression as a literal
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			countExpr,
			CodePrimitiveExpression.ONE));

		buffer.append(',');
		if (prettyPrint) {
			buffer.append(' ');
		}
		flushBuffer();

		CodeExpression keyExpr;
		DuelNode callKey = node.getAttribute(CALLCommandNode.KEY);
		if (callKey instanceof CodeBlockNode) {
			keyExpr = translateExpression((CodeBlockNode)callKey, false);
		} else {
			keyExpr = new CodeVariableReferenceExpression(String.class, "key");
		}

		// emit key expression as a literal
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			keyExpr,
			CodePrimitiveExpression.ONE));

		// emit patch function call which replaces the DOM node with the result
		buffer.append(").toDOM(");

		// emit id var or known value
		flushBuffer();
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			new CodeVariableReferenceExpression(idVar),
			CodePrimitiveExpression.ONE));

		buffer.append(");");

		// last parameter will be the current data
		formatter.writeElementEndTag(buffer, "script");
	}

	private CodeObjectCreateExpression buildPart(PARTCommandNode node)
		throws IOException {

		CodeTypeDeclaration part = CodeDOMUtility.createPartType(
				viewType.nextIdent("part_"));
		viewType.add(part);

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

		CodeTypeDeclaration parentView = viewType;
		try {
			viewType = part;

			CodeMethod renderMethod = buildRenderMethod(node.getChildren()).withOverride();

			renderMethod.setName("render");
			renderMethod.setAccess(AccessModifierType.PROTECTED);

		} finally {
			viewType = parentView;
		}

		return new CodeObjectCreateExpression(part.getTypeName());
	}

	private void buildPartPlaceholder(PARTCommandNode part)
		throws IOException {

		CodeObjectCreateExpression createPart = buildPart(part);

		// insert an initialization statement into the init method
		CodeMethod initMethod = ensureInitMethod();
		initMethod.getStatements().add(
			new CodeExpressionStatement(
				new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"addPart",
					createPart)));

		// parent scope
		CodeStatementCollection scope = scopeStack.peek();

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
		CodeStatementCollection scope = scopeStack.peek();

		// build a helper method to hold the inner content
		CodeMethod innerBind = buildRenderMethod(node.getChildren());

		CodeExpression dataExpr;
		DuelNode loopCount = node.getAttribute(FORCommandNode.COUNT);
		if (loopCount instanceof CodeBlockNode) {
			CodeExpression countExpr = translateExpression((CodeBlockNode)loopCount, false);

			DuelNode loopData = node.getAttribute(FORCommandNode.DATA);
			if (loopData instanceof CodeBlockNode) {
				dataExpr = translateExpression((CodeBlockNode)loopData, false);
			} else {
				dataExpr = new CodeVariableReferenceExpression(Object.class, "data");
			}

			buildIterationCount(scope, countExpr, dataExpr, innerBind);

		} else {
			DuelNode loopObj = node.getAttribute(FORCommandNode.IN);
			if (loopObj instanceof CodeBlockNode) {
				CodeExpression objExpr = translateExpression((CodeBlockNode)loopObj, false);
				buildIterationObject(scope, objExpr, innerBind);

			} else {
				DuelNode loopArray = node.getAttribute(FORCommandNode.EACH);
				if (!(loopArray instanceof CodeBlockNode)) {
					throw new InvalidNodeException("FOR loop missing arguments", loopArray);
				}

				CodeExpression arrayExpr = translateExpression((CodeBlockNode)loopArray, false);
				buildIterationArray(scope, arrayExpr, innerBind);
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

		CodeStatementCollection scope = scopeStack.peek();

		for (DuelNode conditional : node.getChildren()) {
			if (conditional instanceof IFCommandNode) {
				scope = buildConditional((IFCommandNode)conditional, scope);
			}
		}
	}

	private CodeStatementCollection buildConditional(IFCommandNode node, CodeStatementCollection scope)
		throws IOException {

		flushBuffer();

		CodeBlockNode testNode = node.getTest();

		if (testNode == null) {
			// no condition block needed
			if (node.hasChildren()) {
				scopeStack.push(scope);
				for (DuelNode child : node.getChildren()) {
					buildNode(child);
				}
				flushBuffer();
				scopeStack.pop();
			}
			return scope;
		}

		CodeConditionStatement condition = new CodeConditionStatement();
		scope.add(condition);

		condition.setCondition(translateExpression(testNode, false));

		if (node.hasChildren()) {
			scopeStack.push(condition.getTrueStatements());
			for (DuelNode child : node.getChildren()) {
				buildNode(child);
			}
			flushBuffer();
			scopeStack.pop();
		}
		
		return condition.getFalseStatements();
	}

	private CodeExpression translateExpression(CodeBlockNode node, boolean canWrite) {

		try {
			// convert from JavaScript source to CodeDOM
			List<CodeMember> members = new ScriptTranslator(viewType).translate(node.getClientCode());
			boolean firstIsMethod = (members.size() > 0) && members.get(0) instanceof CodeMethod;
			CodeMethod method = firstIsMethod ? (CodeMethod)members.get(0) : null;

			CodeExpression expression = null;
			if (firstIsMethod) {
				// attempt to extract single expression (inline the return expression)
				expression = CodeDOMUtility.inlineMethod(method);
			}

			if (expression != null) {
				// add remaining CodeDOM members to viewType
				for (int i=1, length=members.size(); i<length; i++) {
					viewType.add(members.get(i));
				}

			} else if (firstIsMethod) {
				// add all CodeDOM members to viewType
				viewType.addAll(members);

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

			if (method.getUserData(ScriptTranslator.EXTRA_ASSIGN) != null) {
				// flag as potentially modifying extra values
				needsExtrasEmitted = true;
			}

			if (canWrite && (method != null) &&
				method.getUserData(ScriptTranslator.EXTRA_REFS) instanceof Object[]) {

				// flag as potentially modifying extra values
				needsExtrasEmitted = true;

				Object[] refs = (Object[])members.get(0).getUserData(ScriptTranslator.EXTRA_REFS);
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
						"hasExtras",
						args));

				if (firstIsMethod && expression instanceof CodeMethodInvokeExpression) {
					runtimeCheck.getTrueStatements().addAll(method.getStatements());
					viewType.getMembers().remove(method);
				} else {
					runtimeCheck.getTrueStatements().add(new CodeMethodReturnStatement(expression));
				}

				CodeMethod runtimeCheckMethod = new CodeMethod(
					AccessModifierType.PRIVATE,
					Object.class,
					viewType.nextIdent("hybrid_"),
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "context"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					runtimeCheck).withThrows(IOException.class);

				viewType.add(runtimeCheckMethod);

				// ensure a break in the write stream
				flushBuffer();
				scopeStack.push(runtimeCheck.getFalseStatements());
				try {
					// defer blocks that cannot be fully processed server-side
					buildDeferredWrite(node.getClientCode(), node.getArgSize());
					flushBuffer();

				} finally {
					scopeStack.pop();
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

		boolean prettyPrint = encoder.isPrettyPrint();
		CodeStatementCollection scope = scopeStack.peek();

		// use the script tag as its own replacement element
		hasScripts = true;
		formatter.writeOpenElementBeginTag(buffer, "script");
		if (settings.getScriptTypeAttr()) {
			formatter.writeAttribute(buffer, "type", "text/javascript");
		}
		formatter.writeCloseElementBeginTag(buffer);
		ensureExtrasEmitted(false);

		// wrap client code as an anonymous DUEL view
		buffer.append("duel(");

		// emit client code directly
		buffer.append(clientCode);

		// immediately invoke anonymous view
		buffer.append(")(");

		if (argSize > 0) {
			flushBuffer();

			// emit data var as literal
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(Object.class, "data"),
				CodePrimitiveExpression.ONE));

			if (argSize > 1) {
				buffer.append(',');
				if (prettyPrint) {
					buffer.append(' ');
				}
				flushBuffer();

				// emit index var as number
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE));

				if (argSize > 2) {
					buffer.append(',');
					if (prettyPrint) {
						buffer.append(' ');
					}
					flushBuffer();

					// emit count var as number
					scope.add(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count"),
						CodePrimitiveExpression.ONE));

					if (argSize > 3) {
						buffer.append(',');
						if (prettyPrint) {
							buffer.append(' ');
						}
						flushBuffer();

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
		// emit write function call which writes result directly to the current document
		buffer.append(").write();");

		// last parameter will be the current data
		formatter.writeElementEndTag(buffer, "script");
	}

	private void buildElement(ElementNode element)
		throws IOException {

		String tagName = element.getTagName();

		if ("script".equalsIgnoreCase(tagName)) {
			hasScripts = true;
			ensureExtrasEmitted(true);
		}
		
		formatter.writeOpenElementBeginTag(buffer, tagName);

		int argSize = 0;
		Map<String, DataEncoder.Snippet> deferredAttrs = new LinkedHashMap<String, DataEncoder.Snippet>();
		for (String attrName : element.getAttributeNames()) {
			DuelNode attrVal = element.getAttribute(attrName);
			
			if (attrVal == null) {
				formatter.writeAttribute(buffer, attrName, null);

			} else if (element.isBoolAttribute(attrName)) {
				if (attrVal instanceof LiteralNode) {
					if (DuelData.coerceBoolean(((LiteralNode)attrVal).getValue())) {
						formatter.writeAttribute(buffer, attrName, attrName);
					}

				} else if (attrVal instanceof CodeBlockNode) {
					try {
						flushBuffer();
						CodeConditionStatement condition = new CodeConditionStatement();
						scopeStack.peek().add(condition);

						condition.setCondition(translateExpression((CodeBlockNode)attrVal, false));

						// write attribute if truthy
						scopeStack.push(condition.getTrueStatements());
						formatter.writeAttribute(buffer, attrName, attrName);
						flushBuffer();
						scopeStack.pop();

					} catch (Exception ex) {
						
						// only defer attributes that cannot be processed server-side
						deferredAttrs.put(attrName, DataEncoder.asSnippet(((CodeBlockNode)attrVal).getClientCode()));
						argSize = Math.max(argSize, ((CodeBlockNode)attrVal).getArgSize());
						continue;
					}
				}

			} else if (element.isLinkAttribute(attrName)) {
				formatter.writeOpenAttribute(buffer, attrName);
				flushBuffer();

				CodeStatement writeStatement;
				if (attrVal instanceof LiteralNode) {
					writeStatement = buildLinkIntercept(((LiteralNode)attrVal).getValue());

				} else if (attrVal instanceof CodeBlockNode) {
					try {
						writeStatement = buildLinkIntercept((CodeBlockNode)attrVal);

					} catch (Exception ex) {
						
						// only defer attributes that cannot be processed server-side
						deferredAttrs.put(attrName, DataEncoder.asSnippet(((CodeBlockNode)attrVal).getClientCode()));
						argSize = Math.max(argSize, ((CodeBlockNode)attrVal).getArgSize());
						continue;
					}

				} else {
					throw new InvalidNodeException("Invalid attribute node type: "+attrVal.getClass(), attrVal);
				}

				scopeStack.peek().add(writeStatement);

				formatter.writeCloseAttribute(buffer);

			} else if (attrVal instanceof LiteralNode) {
				formatter.writeAttribute(buffer, attrName, ((LiteralNode)attrVal).getValue());

			} else if (attrVal instanceof CodeBlockNode) {

				try {
					CodeStatement writeStatement = processCodeBlock((CodeBlockNode)attrVal);
					if (writeStatement != null) {
						formatter.writeOpenAttribute(buffer, attrName);
						flushBuffer();
						scopeStack.peek().add(writeStatement);
						formatter.writeCloseAttribute(buffer);
					} else {
						formatter.writeAttribute(buffer, attrName, null);
					}

				} catch (Exception ex) {
					
					// only defer attributes that cannot be processed server-side
					deferredAttrs.put(attrName, DataEncoder.asSnippet(((CodeBlockNode)attrVal).getClientCode()));
					argSize = Math.max(argSize, ((CodeBlockNode)attrVal).getArgSize());
					continue;
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
				formatter.writeOpenAttribute(buffer, "id");
				idVar = emitClientID();
				formatter.writeCloseAttribute(buffer);

			} else if (id instanceof LiteralNode) {
				idValue = ((LiteralNode)id).getValue();

			} else {
				// TODO: find cases where this may be legitimate
				throw new InvalidNodeException("Invalid ID attribute node type: "+id.getClass(), id);
			}
		}

		if (element.canHaveChildren()) {
			formatter.writeCloseElementBeginTag(buffer);

			TagMode prevMode = tagMode;
			if ("script".equalsIgnoreCase(tagName) || "style".equalsIgnoreCase(tagName)) {
				tagMode = TagMode.SUSPEND_MODE;

			} else if ("pre".equalsIgnoreCase(tagName)) {
				tagMode = TagMode.PRE_MODE;
			}

			try {
				for (DuelNode child : element.getChildren()) {
					if (settings.getNormalizeWhitespace() &&
						tagMode == TagMode.NONE &&
						child instanceof LiteralNode &&
						(child == element.getFirstChild() || child == element.getLastChild())) {

						String lit = ((LiteralNode)child).getValue();
						if (lit == null || lit.matches("^[\\r\\n]*$")) {
							// skip literals which will be normalized away 
							continue;
						}
					}
					buildNode(child);
				}
			} finally {
				tagMode = prevMode;
			}

			// ensure changes emitted, if no scripts then no need
			if (hasScripts && "body".equalsIgnoreCase(tagName)) {
				ensureExtrasEmitted(true);
				buffer.append(settings.getNewline());
			}
			formatter.writeElementEndTag(buffer, tagName);

		} else {
			formatter.writeCloseElementVoidTag(buffer);
		}

		if (deferredAttrs.size() > 0) {
			buildDeferredAttributeExecution(deferredAttrs, idVar, idValue, argSize);
		}
	}

	private void buildDeferredAttributeExecution(
		Map<String, DataEncoder.Snippet> deferredAttrs,
		CodeVariableDeclarationStatement idVar,
		String idValue,
		int argSize) throws IOException {

		boolean prettyPrint = encoder.isPrettyPrint();

		CodeStatementCollection scope = scopeStack.peek();

		// execute any deferred attributes using idVar
		hasScripts = true;
		formatter.writeOpenElementBeginTag(buffer, "script");
		if (settings.getScriptTypeAttr()) {
			formatter.writeAttribute(buffer, "type", "text/javascript");
		}
		formatter.writeCloseElementBeginTag(buffer);
		ensureExtrasEmitted(false);

		// wrap attributes object as an anonymous DUEL view
		buffer.append("duel(");

		// emit deferredAttrs as a JS Object
		encoder.write(buffer, deferredAttrs, 1);

		// immediately invoke anonymous view
		buffer.append(")(");

		if (argSize > 0) {
			flushBuffer();

			// emit data var as literal
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(Object.class, "data"),
				CodePrimitiveExpression.ONE));

			if (argSize > 1) {
				buffer.append(',');
				if (prettyPrint) {
					buffer.append(' ');
				}
				flushBuffer();

				// emit index var as number
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE));

				if (argSize > 2) {
					buffer.append(',');
					if (prettyPrint) {
						buffer.append(' ');
					}
					flushBuffer();

					// emit count var as number
					scope.add(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count"),
						CodePrimitiveExpression.ONE));

					if (argSize > 3) {
						buffer.append(',');
						if (prettyPrint) {
							buffer.append(' ');
						}
						flushBuffer();

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

		// emit patch function call which merges attributes into DOM node
		buffer.append(").toDOM(");

		// emit id var or known value
		if (idVar != null) {
			flushBuffer();
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(idVar),
				CodePrimitiveExpression.ONE));
		} else {
			encoder.write(buffer, idValue, 1);
		}

		buffer.append(',');
		if (prettyPrint) {
			buffer.append(" true");
		} else {
			buffer.append('1');
		}
		buffer.append(");");

		// last parameter will be the current data
		formatter.writeElementEndTag(buffer, "script");
	}

	private void buildCodeBlock(CodeBlockNode node) throws IOException {
		try {
			CodeStatement writeStatement = processCodeBlock(node);
			if (writeStatement == null) {
				return;
			}

			flushBuffer();
			scopeStack.peek().add(writeStatement);
			return;

		} catch (Exception ex) {
			// only defer blocks that cannot be fully processed server-side
			buildDeferredCodeBlock(node.getClientCode(), node.getArgSize());
		}
	}

	private void buildDeferredCodeBlock(String clientCode, int argSize)
			throws IOException {

		boolean prettyPrint = encoder.isPrettyPrint();
		CodeStatementCollection scope = scopeStack.peek();

		// use the script tag as its own replacement element
		hasScripts = true;
		formatter.writeOpenElementBeginTag(buffer, "script");
		if (settings.getScriptTypeAttr()) {
			formatter.writeAttribute(buffer, "type", "text/javascript");
		}
		formatter.writeOpenAttribute(buffer, "id");
		CodeVariableDeclarationStatement idVar = emitClientID();
		formatter
			.writeCloseAttribute(buffer)
			.writeCloseElementBeginTag(buffer);
		ensureExtrasEmitted(false);

		// wrap client code as an anonymous DUEL view
		buffer.append("duel(");

		// emit client code directly
		buffer.append(clientCode);

		// immediately invoke anonymous view
		buffer.append(")(");

		if (argSize > 0) {
			flushBuffer();

			// emit data var as literal
			scope.add(new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"dataEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodeVariableReferenceExpression(Object.class, "data"),
				CodePrimitiveExpression.ONE));

			if (argSize > 1) {
				buffer.append(',');
				if (prettyPrint) {
					buffer.append(' ');
				}
				flushBuffer();

				// emit index var as number
				scope.add(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE));

				if (argSize > 2) {
					buffer.append(',');
					if (prettyPrint) {
						buffer.append(' ');
					}
					flushBuffer();

					// emit count var as number
					scope.add(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count"),
						CodePrimitiveExpression.ONE));

					if (argSize > 3) {
						buffer.append(',');
						if (prettyPrint) {
							buffer.append(' ');
						}
						flushBuffer();

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

		// emit patch function call which replaces DOM node with result
		buffer.append(").toDOM(");

		// emit id var or known value
		flushBuffer();
		scope.add(new CodeMethodInvokeExpression(
			Void.class,
			new CodeThisReferenceExpression(),
			"dataEncode",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			new CodeVariableReferenceExpression(idVar),
			CodePrimitiveExpression.ONE));

		buffer.append(");");

		// last parameter will be the current data
		formatter.writeElementEndTag(buffer, "script");
	}

	private CodeVariableDeclarationStatement emitClientID() {
		flushBuffer();
		CodeStatementCollection scope = scopeStack.peek();

		// the var contains a new unique ident
		CodeVariableDeclarationStatement localVar = CodeDOMUtility.nextID(scope);
		scope.add(localVar);

		// emit the value of the var
		CodeStatement emitVar = CodeDOMUtility.emitVarValue(localVar);
		scope.add(emitVar);

		return localVar;
	}

	/**
	 * @param literal
	 * @return Code which emits the evaluated value of a link intercept
	 */
	private CodeStatement buildLinkIntercept(Object literal) {
		CodeExpression codeExpr = new CodeMethodInvokeExpression(
			String.class,
			new CodeThisReferenceExpression(),
			"transformURL",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			CodeDOMUtility.ensureString(new CodePrimitiveExpression(literal)));

		return CodeDOMUtility.emitExpressionSafe(codeExpr);
	}

	/**
	 * @param block
	 * @return Code which emits the evaluated value of a link intercept
	 */
	private CodeStatement buildLinkIntercept(CodeBlockNode block) {
		boolean htmlEncode = true;
		if (block instanceof MarkupExpressionNode) {
			htmlEncode = false;
			block = new ExpressionNode(block.getValue(), block.getIndex(), block.getLine(), block.getColumn());
		}

		CodeExpression codeExpr = translateExpression(block, true);
		if (codeExpr == null) {
			return null;
		}

		codeExpr = new CodeMethodInvokeExpression(
			String.class,
			new CodeThisReferenceExpression(),
			"transformURL",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			CodeDOMUtility.ensureString(codeExpr));

		return htmlEncode ?
			CodeDOMUtility.emitExpressionSafe(codeExpr) :
			CodeDOMUtility.emitExpression(codeExpr);
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

		CodeExpression codeExpr = translateExpression(block, true);
		if (codeExpr == null) {
			return null;
		}

		return htmlEncode ?
			CodeDOMUtility.emitExpressionSafe(codeExpr) :
			CodeDOMUtility.emitExpression(codeExpr);
	}

	private void ensureExtrasEmitted(boolean needsTags) {
		if (!needsExtrasEmitted) {
			return;
		}

		// emit check just inside first script block
		flushBuffer();

		scopeStack.peek().add(
			new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"writeExtras",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				new CodePrimitiveExpression(needsTags)));

		// check scope chain for any condition or iteration blocks that
		// might prevent this from being emitted. if found do not mark.
		// there is a runtime check as well which will prevent multiple.
		for (CodeStatementCollection scope : scopeStack) {
			// this is too naive. may not work with inlined methods
			if (!(scope.getOwner() instanceof CodeMethod)) {
				return;
			}
		}

		needsExtrasEmitted = false;
	}

	private CodeMethod ensureInitMethod() {
		for (CodeMember member : viewType.getMembers()) {
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
		viewType.add(initMethod);

		return initMethod;
	}
	
	private void buildComment(CodeCommentNode comment) {
		flushBuffer();

		CodeStatementCollection scope = scopeStack.peek();
		scope.add(new CodeCommentStatement(comment.getValue()));
	}

	/**
	 * Resets the buffer returning the accumulated value
	 * @return
	 */
	private void flushBuffer() {
		if (buffer.length() < 1) {
			return;
		}

		// get the accumulated value
		CodeStatement emitLit = CodeDOMUtility.emitLiteralValue(buffer.toString());
		scopeStack.peek().add(emitLit);

		// clear the buffer
		buffer.setLength(0);
	}
}
