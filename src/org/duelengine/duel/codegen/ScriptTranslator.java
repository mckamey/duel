package org.duelengine.duel.codegen;

import java.util.*;
import org.duelengine.duel.DuelContext;
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
public class ScriptTranslator {

	private final IdentifierScope scope;

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
	 * @return Equivalent translated Java source code
	 */
	public List<CodeMember> translate(String jsSource) {

		String jsFilename = "anonymous.js";
		ErrorReporter errorReporter = null;

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

		return this.visitRoot(root);
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

	private CodeStatement visitStatement(AstNode node) {
		CodeObject value = this.visit(node);

		if (value instanceof CodeExpression) {
			
			return new CodeExpressionStatement((CodeExpression)value);

		} else if (value != null && !(value instanceof CodeStatement)) {
			throw new ScriptTranslationException("Expected a statement: "+value.getClass(), node);
		}

		return (CodeStatement)value;
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
				return this.visitVarRef((Name)node);
			case Token.LP:
				CodeExpression expr = this.visitExpression(((ParenthesizedExpression)node).getExpression());
				if (expr != null) {
					expr.setHasParens(true);
				}
				return expr;
			case Token.STRING:
				return new CodePrimitiveExpression(((StringLiteral)node).getValue());
			case Token.NUMBER:
				return new CodePrimitiveExpression(((NumberLiteral)node).getNumber());
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
			case Token.CALL:
				return this.visitFunctionCall((FunctionCall)node);
			case Token.HOOK:
				return this.visitTernary((ConditionalExpression)node);
			case Token.EXPR_VOID:
				ExpressionStatement voidExpr = (ExpressionStatement)node;
				if (voidExpr.hasSideEffects()) {
					// TODO: determine when this could occur
				}
				// unwrap expression node
				return this.visit(voidExpr.getExpression());
			case Token.THIS:
				// TODO: evaluate if will allow custom extensions via 'this'
				throw new ScriptTranslationException("'this' not legal in binding expressions", node);
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

		return new CodePropertyReferenceExpression((CodeExpression)target, property);
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
			CodeObject target = this.visit(init.getTarget());
			if (!(target instanceof CodeVariableReferenceExpression)) {
				throw new ScriptTranslationException("Unexpected VAR type ("+target.getClass()+")", node);
			}

			// TODO: can this surface result type?
			String ident = this.scope.uniqueIdent(((CodeVariableReferenceExpression)target).getIdent());
			CodeVariableDeclarationStatement decl = new CodeVariableDeclarationStatement(
				Object.class, ident, this.visitExpression(init.getInitializer()));

			vars.addVar(decl);
		}

		return vars;
	}

	private CodeObject visitVarRef(Name node) {
		String ident = node.getIdentifier();

		if (JSUtility.isGlobalIdent(ident)) {
			return new ScriptVariableReferenceExpression(ident);
		}

		if ("data".equals(ident)) {
			return new CodeVariableReferenceExpression(Object.class, "data");
		}

		if ("index".equals(ident) || "count".equals(ident)) {
			return new CodeVariableReferenceExpression(int.class, ident);
		}

		if ("key".equals(ident)) {
			return new CodeVariableReferenceExpression(String.class, ident);
		}

		// map to the unique server-side identifier
		ident = this.scope.uniqueIdent(ident);

		// TODO: can this surface result type?
		return new CodeVariableReferenceExpression(Object.class, ident);
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
			method.setReturnType(Void.class);
			method.addParameter(DuelContext.class, "output");
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

		for (CodeStatement statement : method.getStatements()) {
			if (statement instanceof CodeMethodReturnStatement &&
				((CodeMethodReturnStatement)statement).getExpression() != null) {
				// TODO: refine return type
				method.setReturnType(Object.class);
				break;
			}
		}
		
		return method;
	}

	private CodeMethodReturnStatement visitReturn(ReturnStatement node) {
		CodeExpression value = this.visitExpression(node.getReturnValue());

		return new CodeMethodReturnStatement(value);
	}

	private CodeObject visitFunctionCall(FunctionCall node) {
		List<AstNode> argNodes = node.getArguments();
		CodeExpression[] args = new CodeExpression[argNodes.size()];
		for (int i=0, length=args.length; i<length; i++) {
			args[i] = this.visitExpression(argNodes.get(i));
		}

		return new CodeMethodInvokeExpression(this.visitExpression(node.getTarget()), null, args);
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
}
