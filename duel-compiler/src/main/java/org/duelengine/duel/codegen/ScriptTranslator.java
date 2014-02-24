package org.duelengine.duel.codegen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.JSUtility;
import org.duelengine.duel.codedom.AccessModifierType;
import org.duelengine.duel.codedom.CodeArrayCreateExpression;
import org.duelengine.duel.codedom.CodeBinaryOperatorExpression;
import org.duelengine.duel.codedom.CodeBinaryOperatorType;
import org.duelengine.duel.codedom.CodeExpression;
import org.duelengine.duel.codedom.CodeExpressionStatement;
import org.duelengine.duel.codedom.CodeIterationStatement;
import org.duelengine.duel.codedom.CodeMember;
import org.duelengine.duel.codedom.CodeMethod;
import org.duelengine.duel.codedom.CodeMethodInvokeExpression;
import org.duelengine.duel.codedom.CodeMethodReturnStatement;
import org.duelengine.duel.codedom.CodeObject;
import org.duelengine.duel.codedom.CodePrimitiveExpression;
import org.duelengine.duel.codedom.CodePropertyReferenceExpression;
import org.duelengine.duel.codedom.CodeStatement;
import org.duelengine.duel.codedom.CodeStatementBlock;
import org.duelengine.duel.codedom.CodeTernaryOperatorExpression;
import org.duelengine.duel.codedom.CodeTypeDeclaration;
import org.duelengine.duel.codedom.CodeTypeReferenceExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorType;
import org.duelengine.duel.codedom.CodeVariableCompoundDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableReferenceExpression;
import org.duelengine.duel.codedom.IdentifierScope;
import org.duelengine.duel.codedom.ScriptExpression;
import org.duelengine.duel.codedom.ScriptVariableReferenceExpression;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

/**
 * Translates JavaScript source code into CodeDOM
 */
public class ScriptTranslator implements ErrorReporter {

	public static final String EXTRA_REFS = "ScriptTranslator.EXTRA_REFS";
	public static final String EXTRA_ASSIGN = "ScriptTranslator.EXTRA_ASSIGN";

	private final IdentifierScope scope;
	private Set<String> extraRefs;
	private boolean extraAssign;

	public ScriptTranslator() {
		this(new CodeTypeDeclaration());
	}

	public ScriptTranslator(IdentifierScope identScope) {
		if (identScope == null) {
			throw new NullPointerException("identScope");
		}
		scope = identScope;
	}

	/**
	 * @param jsSource JavaScript source code
	 * @return Equivalent translated CodeDOM
	 */
	public List<CodeMember> translate(String jsSource) {

		extraRefs = null;
		extraAssign = false;
		String jsFilename = "anonymous.js";
		ErrorReporter errorReporter = this;

		Context cx = Context.enter();
		cx.setLanguageVersion(Context.VERSION_1_5);
		CompilerEnvirons compEnv = new CompilerEnvirons();
		compEnv.initFromContext(cx);

		Parser parser = new Parser(compEnv, errorReporter);

		AstRoot root = null;
		try {
			root = parser.parse(jsSource, jsFilename, 1);

		} catch (EvaluatorException ex) {
			String message = ex.getMessage();
			if (message == null) {
				message = ex.toString();
			}
			throw new ScriptTranslationException(message, ex);

		} finally {
			Context.exit();
		}

		if (root == null) {
			return null;
		}

		List<CodeMember> members = visitRoot(root);
		if (members.size() > 0) {
			CodeMember method = members.get(0);
			if (extraRefs != null) {
				// store extra identifiers on the member to allow generation of a fallback block
				method.putMetaData(EXTRA_REFS, extraRefs.toArray());
			}
			if (extraAssign) {
				// flag as potentially modifying values
				method.putMetaData(EXTRA_ASSIGN, true);
			}
		}
		return members;
	}

	private CodeStatement visitStatement(AstNode node) {
		CodeObject value = visit(node);

		if (value instanceof CodeExpression) {
			return new CodeExpressionStatement((CodeExpression)value);

		} else if (value != null && !(value instanceof CodeStatement)) {
			throw new ScriptTranslationException("Expected a statement: "+value.getClass(), node);
		}

		return (CodeStatement)value;
	}

	private CodeExpression visitExpression(AstNode node) {
		CodeObject value = visit(node);

		if (value instanceof CodeExpressionStatement) {
			return ((CodeExpressionStatement)value).getExpression();

		} else if (value != null && !(value instanceof CodeExpression)) {
			throw new ScriptTranslationException("Expected an expression: "+value.getClass(), node);
		}

		return (CodeExpression)value;
	}

	private CodeExpression[] visitExpressionList(List<AstNode> nodes) {
		int length = nodes.size();
		if (length < 1) {
			return null;
		}

		CodeExpression[] expressions = new CodeExpression[length];
		for (int i=0; i<length; i++) {
			expressions[i] = visitExpression(nodes.get(i));
		}
		return expressions;
	}

	private CodeObject visit(AstNode node) throws IllegalArgumentException {
		if (node == null) {
			return null;
		}

		int tokenType = node.getType();
		switch (tokenType) {
			case Token.FUNCTION:
				return visitFunction((FunctionNode)node);
			case Token.BLOCK:
				if (node instanceof Block) {
					return visitBlock((Block)node);
				}
				if (node instanceof Scope) {
					return visitScope((Scope)node);
				}

				throw new ScriptTranslationException("Unexpected block token ("+node.getClass()+"):\n"+(node.debugPrint()), node);
			case Token.GETPROP:
				return visitProperty((PropertyGet)node);
			case Token.GETELEM:
				return visitProperty((ElementGet)node);
			case Token.RETURN:
				return visitReturn((ReturnStatement)node);
			case Token.NAME:
				return visitVarRef((Name)node, false);
			case Token.LP:
				CodeExpression expr = visitExpression(((ParenthesizedExpression)node).getExpression());
				if (expr != null) {
					expr.withParens();
				}
				return expr;
			case Token.STRING:
				return new CodePrimitiveExpression(((StringLiteral)node).getValue());
			case Token.NUMBER:
				// attempt to preserve intent of number literal
				double number = ((NumberLiteral)node).getNumber();
				if (number == ((double)((int)number))) {
					return new CodePrimitiveExpression((int)number);
				}
				if (number == ((double)((long)number))) {
					return new CodePrimitiveExpression((long)number);
				}
				return new CodePrimitiveExpression(number);
			case Token.FALSE:
				return CodePrimitiveExpression.FALSE;
			case Token.TRUE:
				return CodePrimitiveExpression.TRUE;
			case Token.NULL:
				return CodePrimitiveExpression.NULL;
			case Token.FOR:
				return visitForLoop((ForLoop)node);
			case Token.VAR:
				return visitVarDecl((VariableDeclaration)node);
			case Token.ARRAYLIT:
				return visitArrayLiteral((ArrayLiteral)node);
			case Token.OBJECTLIT:
				return visitObjectLiteral((ObjectLiteral)node);
			case Token.CALL:
				return visitFunctionCall((FunctionCall)node);
			case Token.HOOK:
				return visitTernary((ConditionalExpression)node);
			case Token.NEW:
				return visitNew((NewExpression)node);
			case Token.EXPR_VOID:
				ExpressionStatement voidExpr = (ExpressionStatement)node;
				if (voidExpr.hasSideEffects()) {
					// TODO: determine when this could occur
				}
				// unwrap expression node
				return visit(voidExpr.getExpression());
		    case Token.IN:
		    	return visitIn((InfixExpression)node);
		    case Token.INSTANCEOF:
		    	return visitInstanceOf((InfixExpression)node);
		    case Token.TYPEOF:
		    	return visitTypeOf((UnaryExpression)node);
			case Token.THIS:
			case Token.THISFN:
				// TODO: evaluate if should allow custom extensions via 'this'
				throw new ScriptTranslationException("'this' not legal in binding expressions", node);
		    case Token.ENTERWITH :
		    case Token.LEAVEWITH :
				throw new ScriptTranslationException("'with' not legal in binding expressions", node);
			default:
				CodeBinaryOperatorType binary = mapBinaryOperator(tokenType);
				if (binary != CodeBinaryOperatorType.NONE) {
					return visitBinaryOp((InfixExpression)node, binary);
				}
				CodeUnaryOperatorType unary = mapUnaryOperator(tokenType);
				if (unary != CodeUnaryOperatorType.NONE) {
					return visitUnaryOp((UnaryExpression)node, unary);
				}

				throw new ScriptTranslationException("Token not yet supported ("+node.getClass()+"):\n"+(node.debugPrint()), node);
		}
	}

	private CodeObject visitProperty(ElementGet node) {
		CodeExpression target = visitExpression(node.getTarget());
		CodeExpression property = visitExpression(node.getElement());

		return new CodePropertyReferenceExpression(target, property);
	}

	private CodeObject visitProperty(PropertyGet node) {
		CodeExpression target = visitExpression(node.getTarget());
		CodeExpression property = new CodePrimitiveExpression(node.getProperty().getIdentifier());

		return new CodePropertyReferenceExpression(target, property);
	}

	private CodeExpression visitIn(InfixExpression node) {
		CodeExpression key = visitExpression(node.getLeft());
		CodeExpression target = visitExpression(node.getRight());

		return new CodeMethodInvokeExpression(
			Boolean.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"containsKey",
			target,
			key);	
	}

	private CodeExpression visitInstanceOf(InfixExpression node) {
		CodeExpression target = visitExpression(node.getLeft());
		AstNode type = node.getRight();
		if (!(type instanceof Name)) {
			throw new ScriptTranslationException("Unexpected type expression ("+type.getClass()+")", node);
		}
		String method, typeIdent = ((Name)type).getIdentifier();
		if ("Array".equals(typeIdent)) {
			method = "isArray";
		} else if ("Date".equals(typeIdent)) {
			method = "isDate";
		} else {
			throw new ScriptTranslationException("Translation for 'instanceof' token currently only supports Array and Date ("+typeIdent+")", node);
		}

		return new CodeTernaryOperatorExpression(
			new CodeBinaryOperatorExpression(CodeBinaryOperatorType.IDENTITY_EQUALITY, target, CodePrimitiveExpression.NULL).withParens(),
			CodePrimitiveExpression.FALSE,
			new CodeMethodInvokeExpression(
				Boolean.class,
				new CodeTypeReferenceExpression(DuelData.class),
				method,
				new CodeMethodInvokeExpression(Class.class, target, "getClass")));
	}

	private CodeExpression visitTypeOf(UnaryExpression node) {
		CodeExpression operand = visitExpression(node.getOperand());

		return new CodeMethodInvokeExpression(
			String.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"typeOf",
			operand);	
	}

	private CodeExpression visitBinaryOp(InfixExpression node, CodeBinaryOperatorType operator) {
		CodeExpression left = visitExpression(node.getLeft());
		CodeExpression right = visitExpression(node.getRight());
		
		return new CodeBinaryOperatorExpression(operator, left, right);
	}

	private CodeBinaryOperatorType mapBinaryOperator(int tokenType) {
		switch (tokenType) {
			case Token.ASSIGN:
				return CodeBinaryOperatorType.ASSIGN;
			case Token.ADD:
				return CodeBinaryOperatorType.ADD;
			case Token.ASSIGN_ADD:
				return CodeBinaryOperatorType.ADD_ASSIGN;
			case Token.SUB:
				return CodeBinaryOperatorType.SUBTRACT;
			case Token.ASSIGN_SUB:
				return CodeBinaryOperatorType.SUBTRACT_ASSIGN;
			case Token.MUL:
				return CodeBinaryOperatorType.MULTIPLY;
			case Token.ASSIGN_MUL:
				return CodeBinaryOperatorType.MULTIPLY_ASSIGN;
			case Token.DIV:
				return CodeBinaryOperatorType.DIVIDE;
			case Token.ASSIGN_DIV:
				return CodeBinaryOperatorType.DIVIDE_ASSIGN;
			case Token.MOD:
				return CodeBinaryOperatorType.MODULUS;
			case Token.ASSIGN_MOD:
				return CodeBinaryOperatorType.MODULUS_ASSIGN;
			case Token.BITOR:
				return CodeBinaryOperatorType.BITWISE_OR;
			case Token.ASSIGN_BITOR:
				return CodeBinaryOperatorType.BITWISE_OR_ASSIGN;
			case Token.BITAND:
				return CodeBinaryOperatorType.BITWISE_AND;
			case Token.ASSIGN_BITAND:
				return CodeBinaryOperatorType.BITWISE_AND_ASSIGN;
			case Token.BITXOR:
				return CodeBinaryOperatorType.BITWISE_XOR;
			case Token.ASSIGN_BITXOR:
				return CodeBinaryOperatorType.BITWISE_XOR_ASSIGN;
			case Token.LSH:
				return CodeBinaryOperatorType.SHIFT_LEFT;
			case Token.ASSIGN_LSH:
				return CodeBinaryOperatorType.SHIFT_LEFT_ASSIGN;
			case Token.RSH:
				return CodeBinaryOperatorType.SHIFT_RIGHT;
			case Token.ASSIGN_RSH:
				return CodeBinaryOperatorType.SHIFT_RIGHT_ASSIGN;
			case Token.URSH:
				return CodeBinaryOperatorType.USHIFT_RIGHT;
			case Token.ASSIGN_URSH:
				return CodeBinaryOperatorType.USHIFT_RIGHT_ASSIGN;
			case Token.OR:
				return CodeBinaryOperatorType.BOOLEAN_OR;
			case Token.AND:
				return CodeBinaryOperatorType.BOOLEAN_AND;
			case Token.LT:
				return CodeBinaryOperatorType.LESS_THAN;
			case Token.LE:
				return CodeBinaryOperatorType.LESS_THAN_OR_EQUAL;
			case Token.GT:
				return CodeBinaryOperatorType.GREATER_THAN;
			case Token.GE:
				return CodeBinaryOperatorType.GREATER_THAN_OR_EQUAL;
			case Token.EQ:
				return CodeBinaryOperatorType.VALUE_EQUALITY;
			case Token.NE:
				return CodeBinaryOperatorType.VALUE_INEQUALITY;
			case Token.SHEQ:
				return CodeBinaryOperatorType.IDENTITY_EQUALITY;
			case Token.SHNE:
				return CodeBinaryOperatorType.IDENTITY_INEQUALITY;
			default:
				return CodeBinaryOperatorType.NONE;
		}
	}

	private CodeExpression visitUnaryOp(UnaryExpression node, CodeUnaryOperatorType operator) {

		if (node.isPostfix()) {
			switch (operator) {
				case PRE_DECREMENT:
					operator = CodeUnaryOperatorType.POST_DECREMENT;
					break;
				case PRE_INCREMENT:
					operator = CodeUnaryOperatorType.POST_INCREMENT;
					break;
				default:
					break;
			}
		}

		CodeExpression operand = visitExpression(node.getOperand());
		return new CodeUnaryOperatorExpression(operator, (CodeExpression)operand);
	}

	private CodeUnaryOperatorType mapUnaryOperator(int tokenType) {
		switch (tokenType) {
			case Token.NOT:
				return CodeUnaryOperatorType.LOGICAL_NEGATION;
			case Token.NEG:
				return CodeUnaryOperatorType.NEGATION;
			case Token.POS:
				return CodeUnaryOperatorType.POSITIVE;
			case Token.BITNOT:
				return CodeUnaryOperatorType.BITWISE_NEGATION;
			case Token.INC:
				// POST_INC will be differentiated later
				return CodeUnaryOperatorType.PRE_INCREMENT;
			case Token.DEC:
				// POST_DEC will be differentiated later
				return CodeUnaryOperatorType.PRE_DECREMENT;
			case Token.DELPROP:
			case Token.VOID:
			case Token.TYPEOF:
			default:
				return CodeUnaryOperatorType.NONE;
		}
	}

	private CodeObject visitTernary(ConditionalExpression node) {
		CodeExpression testExpr = visitExpression(node.getTestExpression());
		CodeExpression trueExpr = visitExpression(node.getTrueExpression());
		CodeExpression falseExpr = visitExpression(node.getFalseExpression());

		return new CodeTernaryOperatorExpression(testExpr, trueExpr, falseExpr);
	}

	private CodeObject visitVarDecl(VariableDeclaration node) {
		CodeVariableCompoundDeclarationStatement vars = new CodeVariableCompoundDeclarationStatement();

		for (VariableInitializer init : node.getVariables()) {
			Node nameNode = init.getTarget();
			if (!(nameNode instanceof Name)) {
				throw new ScriptTranslationException("Unexpected VAR node type ("+nameNode.getClass()+")", node);
			}
			CodeObject target = visitVarRef((Name)nameNode, true);
			if (!(target instanceof CodeVariableReferenceExpression)) {
				throw new ScriptTranslationException("Unexpected VAR type ("+target.getClass()+")", node);
			}

			CodeVariableReferenceExpression varRef = ((CodeVariableReferenceExpression)target);
			CodeVariableDeclarationStatement decl = new CodeVariableDeclarationStatement(
					varRef.getResultType(),
					varRef.getIdent(),
					visitExpression(init.getInitializer()));

			vars.addVar(decl);
		}

		if (vars.getVars().size() == 1) {
			return vars.getVars().get(0);
		}
		
		return vars;
	}

	private CodeObject visitVarRef(Name node, boolean declaration) {
		String ident = node.getIdentifier();

		if (JSUtility.isGlobalIdent(ident)) {
			if ("undefined".equals(ident)) {
				return ScriptExpression.UNDEFINED;
			}
			if ("NaN".equals(ident)) {
				return new CodePrimitiveExpression(Double.NaN);
			}
			if ("Infinity".equals(ident)) {
				return new CodePrimitiveExpression(Double.POSITIVE_INFINITY);
			}
			
			// fall through

		} else if ("data".equals(ident)) {
			return new CodeVariableReferenceExpression(Object.class, "data");

		} else if ("index".equals(ident) || "count".equals(ident)) {
			return new CodeVariableReferenceExpression(int.class, ident);

		} else if ("key".equals(ident)) {
			return new CodeVariableReferenceExpression(String.class, ident);

		} else if (declaration || scope.isLocalIdent(ident)) {
			// map to the unique server-side identifier
			ident = scope.uniqueIdent(ident);

			// TODO: can this surface result type?
			return new CodeVariableReferenceExpression(Object.class, ident);
		}

		// mark as potential to fail at runtime based upon data, unless
		// pure assignments which do not need to first check for existence
		if (!(node.getParent().getType() == Token.ASSIGN &&
			((InfixExpression)node.getParent()).getLeft() == node)) {

			if (extraRefs == null) {
				extraRefs = new HashSet<String>();
			}
			extraRefs.add(ident);
		} else {
			extraAssign = true;
		}

		return new ScriptVariableReferenceExpression(ident);
	}

	private CodeObject visitForLoop(ForLoop node) {
		CodeIterationStatement loop = new CodeIterationStatement(
			visitStatement(node.getInitializer()),
			visitExpression(node.getCondition()),
			visitStatement(node.getIncrement()));

		CodeObject body = visit(node.getBody());
		if (body instanceof CodeStatementBlock) {
			loop.getStatements().addAll((CodeStatementBlock)body);
		} else {
			throw new ScriptTranslationException("Expected statement block ("+body.getClass()+")", node.getBody());
		}

		return loop;
	}

	private CodeMethod visitFunction(FunctionNode node) throws IllegalArgumentException {
		CodeMethod method = new CodeMethod(
			AccessModifierType.PRIVATE,
			Object.class,
			scope.nextIdent("code_"),
			null);

		if (node.depth() == 1) {
			method.addParameter(DuelContext.class, "context");
			method.addParameter(Object.class, "data");
			method.addParameter(int.class, "index");
			method.addParameter(int.class, "count");
			method.addParameter(String.class, "key");

		} else {
			// TODO: extract parameter names / types
			throw new ScriptTranslationException("Nested functions not yet supported.", node);
		}

		CodeObject body = visit(node.getBody());
		if (body instanceof CodeStatementBlock) {
			method.getStatements().addAll((CodeStatementBlock)body);

		} else if (body instanceof CodeStatement) {
			method.getStatements().add((CodeStatement)body);

		} else if (body instanceof CodeExpression) {
			method.getStatements().add((CodeExpression)body);

		} else if (body != null) {
			throw new ScriptTranslationException("Unexpected function body: "+body.getClass(), node.getBody());
		}

		if ((node.depth() == 1) &&
			!(method.getStatements().getLastStatement() instanceof CodeMethodReturnStatement)) {
			// this is effectively an empty return in JavaScript
			method.getStatements().add(new CodeMethodReturnStatement(ScriptExpression.UNDEFINED));
		}

		/*
		// refine return type?
		for (CodeStatement statement : method.getStatements()) {
			if (statement instanceof CodeMethodReturnStatement &&
				((CodeMethodReturnStatement)statement).getExpression() != null) {
				method.setReturnType(((CodeMethodReturnStatement)statement).getExpression().getReturnType());
				break;
			}
		}
		*/

		return method;
	}

	private CodeMethodReturnStatement visitReturn(ReturnStatement node) {
		CodeExpression value = visitExpression(node.getReturnValue());

		// always return a value
		return new CodeMethodReturnStatement(value != null ? value : ScriptExpression.UNDEFINED);
	}

	private CodeObject visitFunctionCall(FunctionCall node) {
		CodeExpression target = visitExpression(node.getTarget());
		if (!(target instanceof CodePropertyReferenceExpression)) {
			throw new ScriptTranslationException("Unsupported function call ("+node.getClass()+"):\n"+(node.debugPrint()), node.getTarget());
		}

		CodePropertyReferenceExpression propertyRef = (CodePropertyReferenceExpression)target;
		CodeExpression nameExpr = propertyRef.getPropertyName();
		if (!(nameExpr instanceof CodePrimitiveExpression)) {
			throw new ScriptTranslationException("Unsupported function call ("+node.getClass()+"):\n"+(node.debugPrint()), node.getTarget());
		}
		String methodName = DuelData.coerceString(((CodePrimitiveExpression)nameExpr).getValue());
		CodeExpression[] args = visitExpressionList(node.getArguments());

		CodeObject methodCall = CodeDOMUtility.translateMethodCall(propertyRef.getResultType(), propertyRef.getTarget(), methodName, args);
		if (methodCall == null) {
			throw new ScriptTranslationException("Unsupported function call ("+node.getClass()+"):\n"+(node.debugPrint()), node.getTarget());
		}
		return methodCall;
	}

	private CodeObject visitNew(NewExpression node) {
		AstNode target = node.getTarget();
		if (target instanceof Name) {
			String ident = ((Name)target).getIdentifier();
			if ("Array".equals(ident)) {
				return visitArrayCtor(node);
			}

			// TODO: add other JS types 
		}

		throw new ScriptTranslationException("Create object type not yet supported ("+node.getClass()+"):\n"+(node.debugPrint()), node);
	}

	private CodeObject visitObjectLiteral(ObjectLiteral node) {
		List<ObjectProperty> properties = node.getElements();
		int length = properties.size();
		CodeExpression[] initializers = new CodeExpression[length*2];
		for (int i=0; i<length; i++) {
			ObjectProperty property = properties.get(i);

			AstNode key = property.getLeft();
			initializers[i*2] = (key.getType() == Token.NAME) ?
				new CodePrimitiveExpression(((Name)key).getIdentifier()) :
				visitExpression(key);

			initializers[i*2+1] = visitExpression(property.getRight());
		}

		return new CodeMethodInvokeExpression(
			Map.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"asMap",
			initializers);
	}

	private CodeObject visitArrayLiteral(ArrayLiteral node) {
		CodeExpression[] initializers = visitExpressionList(node.getElements());

		return new CodeArrayCreateExpression(Object.class, initializers);
	}

	private CodeObject visitArrayCtor(NewExpression node) {
		CodeExpression[] initializers = visitExpressionList(node.getArguments());
		if (initializers != null &&
			(initializers.length == 1) &&
			(initializers[0] instanceof CodePrimitiveExpression)) {

			CodePrimitiveExpression arg = (CodePrimitiveExpression)initializers[0];
			if (CodeDOMUtility.isNumber(arg)) {
				int size = ((Number)arg.getValue()).intValue();
				return new CodeArrayCreateExpression(Object.class, size);
			}
		}

		return new CodeArrayCreateExpression(Object.class, initializers);
	}

	private CodeObject visitScope(Scope scope) {
		CodeStatementBlock statements = new CodeStatementBlock();
        for (Node node : scope) {
            CodeObject value = visit((AstNode)node);

            if (value == null) {
            	continue;

            } else if (value instanceof CodeStatement) {
            	statements.add((CodeStatement)value);

            } else if (value instanceof CodeExpression) {
            	statements.add((CodeExpression)value);

            } else if (value instanceof CodeStatementBlock) {
            	statements.addAll((CodeStatementBlock)value);

            } else {
        		throw new ScriptTranslationException("Unexpected statement value: "+value.getClass(), (AstNode)node);
            }
        }
        return statements;
	}

	private CodeStatementBlock visitBlock(Block block) {
		CodeStatementBlock statements = new CodeStatementBlock();
        for (Node node : block) {
            CodeObject value = visit((AstNode)node);

            if (value == null) {
            	continue;

            } else if (value instanceof CodeStatement) {
            	statements.add((CodeStatement)value);

            } else if (value instanceof CodeExpression) {
            	statements.add((CodeExpression)value);

            } else if (value instanceof CodeStatementBlock) {
            	statements.addAll((CodeStatementBlock)value);

            } else {
        		throw new ScriptTranslationException("Unexpected statement value: "+value.getClass(), (AstNode)node);
            }
        }
        return statements;
	}

	private List<CodeMember> visitRoot(AstRoot root) {
		List<CodeMember> members = new ArrayList<CodeMember>();
        for (Node node : root) {
            CodeObject member = visit((AstNode)node);

            if (member == null) {
            	continue;

            } else if (member instanceof CodeMember) {
            	members.add((CodeMember)member);

            } else {
        		throw new ScriptTranslationException("Unexpected member: "+member.getClass(), (AstNode)node);
            }
        }
        return members;
	}

	@Override
	public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
		// do nothing with warnings for now
	}

	@Override
	public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
		throw runtimeError(message, sourceName, line, lineSource, lineOffset);
	}

	@Override
	public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
	}
}
