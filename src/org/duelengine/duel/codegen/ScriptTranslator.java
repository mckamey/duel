package org.duelengine.duel.codegen;

import java.util.*;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.JSUtility;
import org.duelengine.duel.codedom.*;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;

/**
 * Translates JavaScript source code into CodeDOM
 */
public class ScriptTranslator implements ErrorReporter {

	public static final String EXTERNAL_REFS = "EXTERNAL_REFS";

	private final IdentifierScope scope;
	private List<String> externalRefs;

	public ScriptTranslator() {
		this(new CodeTypeDeclaration());
	}

	public ScriptTranslator(IdentifierScope scope) {
		if (scope == null) {
			throw new NullPointerException("scope");
		}
		this.scope = scope;
	}

	/**
	 * @param jsSource JavaScript source code
	 * @return Equivalent translated CodeDOM
	 */
	public List<CodeMember> translate(String jsSource) {

		this.externalRefs = null;
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

		List<CodeMember> members = this.visitRoot(root);
		if (this.externalRefs != null && members.size() > 0) {
			// store external identifiers on the member to allow generation of a fallback block
			members.get(0).withUserData(EXTERNAL_REFS, this.externalRefs.toArray());
		}
		return members;
	}

	private CodeStatement visitStatement(AstNode node) {
		CodeObject value = this.visit(node);

		if (value instanceof CodeExpression) {
			return new CodeExpressionStatement((CodeExpression)value);

		} else if (value != null && !(value instanceof CodeStatement)) {
			throw new ScriptTranslationException("Expected a statement: "+value.getClass(), node);
		}

		return (CodeStatement)value;
	}

	private CodeExpression visitExpression(AstNode node) {
		CodeObject value = this.visit(node);

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
			expressions[i] = this.visitExpression(nodes.get(i));
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
				return this.visitFunction((FunctionNode)node);
			case Token.BLOCK:
				if (node instanceof Block) {
					return this.visitBlock((Block)node);
				}
				if (node instanceof Scope) {
					return this.visitScope((Scope)node);
				}

				throw new ScriptTranslationException("Unexpected block token ("+node.getClass()+"):\n"+(node.debugPrint()), node);
			case Token.GETPROP:
				return this.visitProperty((PropertyGet)node);
			case Token.GETELEM:
				return this.visitProperty((ElementGet)node);
			case Token.RETURN:
				return this.visitReturn((ReturnStatement)node);
			case Token.NAME:
				return this.visitVarRef((Name)node, false);
			case Token.LP:
				CodeExpression expr = this.visitExpression(((ParenthesizedExpression)node).getExpression());
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
				return this.visitForLoop((ForLoop)node);
			case Token.VAR:
				return this.visitVarDecl((VariableDeclaration)node);
			case Token.ARRAYLIT:
				return this.visitArrayLiteral((ArrayLiteral)node);
			case Token.OBJECTLIT:
				return this.visitObjectLiteral((ObjectLiteral)node);
			case Token.CALL:
				return this.visitFunctionCall((FunctionCall)node);
			case Token.HOOK:
				return this.visitTernary((ConditionalExpression)node);
			case Token.NEW:
				return this.visitNew((NewExpression)node);
			case Token.EXPR_VOID:
				ExpressionStatement voidExpr = (ExpressionStatement)node;
				if (voidExpr.hasSideEffects()) {
					// TODO: determine when this could occur
				}
				// unwrap expression node
				return this.visit(voidExpr.getExpression());
			case Token.THIS:
			case Token.THISFN:
				// TODO: evaluate if should allow custom extensions via 'this'
				throw new ScriptTranslationException("'this' not legal in binding expressions", node);
		    case Token.ENTERWITH :
		    case Token.LEAVEWITH :
				throw new ScriptTranslationException("'with' not legal in binding expressions", node);
			default:
				CodeBinaryOperatorType binary = this.mapBinaryOperator(tokenType);
				if (binary != CodeBinaryOperatorType.NONE) {
					return this.visitBinaryOp((InfixExpression)node, binary);
				}
				CodeUnaryOperatorType unary = this.mapUnaryOperator(tokenType);
				if (unary != CodeUnaryOperatorType.NONE) {
					return this.visitUnaryOp((UnaryExpression)node, unary);
				}

				throw new ScriptTranslationException("Token not yet supported ("+node.getClass()+"):\n"+(node.debugPrint()), node);
		}
	}

	private CodeObject visitProperty(ElementGet node) {
		CodeExpression target = this.visitExpression(node.getTarget());
		CodeExpression property = this.visitExpression(node.getElement());

		return new CodePropertyReferenceExpression(target, property);
	}

	private CodeObject visitProperty(PropertyGet node) {
		CodeExpression target = this.visitExpression(node.getTarget());
		CodeExpression property = new CodePrimitiveExpression(node.getProperty().getIdentifier());

		return new CodePropertyReferenceExpression(target, property);
	}

	private CodeExpression visitBinaryOp(InfixExpression node, CodeBinaryOperatorType operator) {
		CodeExpression left = this.visitExpression(node.getLeft());
		CodeExpression right = this.visitExpression(node.getRight());
		
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
			}
		}

		CodeExpression operand = this.visitExpression(node.getOperand());
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
		CodeExpression testExpr = this.visitExpression(node.getTestExpression());
		CodeExpression trueExpr = this.visitExpression(node.getTrueExpression());
		CodeExpression falseExpr = this.visitExpression(node.getFalseExpression());

		return new CodeTernaryOperatorExpression(testExpr, trueExpr, falseExpr);
	}

	private CodeObject visitVarDecl(VariableDeclaration node) {
		CodeVariableCompoundDeclarationStatement vars = new CodeVariableCompoundDeclarationStatement();

		for (VariableInitializer init : node.getVariables()) {
			Node nameNode = init.getTarget();
			if (!(nameNode instanceof Name)) {
				throw new ScriptTranslationException("Unexpected VAR node type ("+nameNode.getClass()+")", node);
			}
			CodeObject target = this.visitVarRef((Name)nameNode, true);
			if (!(target instanceof CodeVariableReferenceExpression)) {
				throw new ScriptTranslationException("Unexpected VAR type ("+target.getClass()+")", node);
			}

			CodeVariableReferenceExpression varRef = ((CodeVariableReferenceExpression)target);
			CodeVariableDeclarationStatement decl = new CodeVariableDeclarationStatement(
					varRef.getResultType(),
					varRef.getIdent(),
					this.visitExpression(init.getInitializer()));

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
				return CodePrimitiveExpression.NULL;
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

		} else if (declaration || this.scope.isLocalIdent(ident)) {
			// map to the unique server-side identifier
			ident = this.scope.uniqueIdent(ident);

			// TODO: can this surface result type?
			return new CodeVariableReferenceExpression(Object.class, ident);
		}

		// mark as potential to fail at runtime based upon data but
		// pure assignments do not need to first check for existence
		if (!(node.getParent().getType() == Token.ASSIGN &&
			((InfixExpression)node.getParent()).getLeft() == node)) {

			if (this.externalRefs == null) {
				this.externalRefs = new ArrayList<String>();
			}
			this.externalRefs.add(ident);
		}

		return new ScriptVariableReferenceExpression(ident);
	}

	private CodeObject visitForLoop(ForLoop node) {
		CodeIterationStatement loop = new CodeIterationStatement(
			this.visitStatement(node.getInitializer()),
			this.visitExpression(node.getCondition()),
			this.visitStatement(node.getIncrement()));

		CodeObject body = this.visit(node.getBody());
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
			this.scope.nextIdent("code_"),
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

		CodeObject body = this.visit(node.getBody());
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
			method.getStatements().add(new CodeMethodReturnStatement(CodePrimitiveExpression.NULL));
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
		CodeExpression value = this.visitExpression(node.getReturnValue());

		// always return a value
		return new CodeMethodReturnStatement(value != null ? value : CodePrimitiveExpression.NULL);
	}

	private CodeObject visitFunctionCall(FunctionCall node) {
		CodeExpression target = this.visitExpression(node.getTarget());
		if (!(target instanceof CodePropertyReferenceExpression)) {
			throw new ScriptTranslationException("Unsupported function call ("+node.getClass()+"):\n"+(node.debugPrint()), node.getTarget());
		}

		CodePropertyReferenceExpression propertyRef = (CodePropertyReferenceExpression)target;
		CodeExpression nameExpr = propertyRef.getPropertyName();
		if (!(nameExpr instanceof CodePrimitiveExpression)) {
			throw new ScriptTranslationException("Unsupported function call ("+node.getClass()+"):\n"+(node.debugPrint()), node.getTarget());
		}
		String methodName = DuelData.coerceString(((CodePrimitiveExpression)nameExpr).getValue());
		CodeExpression[] args = this.visitExpressionList(node.getArguments());

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
				return this.visitArrayCtor(node);
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
				this.visitExpression(key);

			initializers[i*2+1] = this.visitExpression(property.getRight());
		}

		return new CodeMethodInvokeExpression(
			Map.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"asMap",
			initializers);
	}

	private CodeObject visitArrayLiteral(ArrayLiteral node) {
		CodeExpression[] initializers = this.visitExpressionList(node.getElements());

		return new CodeArrayCreateExpression(Object.class, initializers);
	}

	private CodeObject visitArrayCtor(NewExpression node) {
		CodeExpression[] initializers = this.visitExpressionList(node.getArguments());
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
            CodeObject value = this.visit((AstNode)node);

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
            CodeObject value = this.visit((AstNode)node);

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
            CodeObject member = this.visit((AstNode)node);

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
		throw this.runtimeError(message, sourceName, line, lineSource, lineOffset);
	}

	@Override
	public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
	}
}
