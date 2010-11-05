package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codedom.*;
import org.mozilla.javascript.ast.*;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.Token;

/**
 * Translates JavaScript source code into CodeDOM
 */
public class SourceTranslator {

	private final IdentifierScope scope;

	public SourceTranslator(IdentifierScope scope) {
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
			ex.printStackTrace();
		} finally {
			Context.exit();
		}

		if (root == null) {
			return null;
		}

		try {
			return this.visit(root);

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private CodeExpression visitExpression(AstNode node) {
		CodeObject value = this.visit(node);

		if (value instanceof CodeExpressionStatement) {
			value = ((CodeExpressionStatement)value).getExpression();

		} else if (value != null && !(value instanceof CodeExpression)) {
			throw new IllegalArgumentException("Expected an expression: "+value.getClass());
		}

		return (CodeExpression)value;
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
				return this.visitBlock((Block)node);
			case Token.GETPROP:
				return this.visitProperty((PropertyGet)node);
			case Token.GETELEM:
				return this.visitProperty((ElementGet)node);
			case Token.RETURN:
				return this.visitReturn((ReturnStatement)node);
			case Token.NAME:
				return this.visitVarRef((Name)node);
			case Token.LP:
				return this.visit(((ParenthesizedExpression)node).getExpression());
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
			case Token.CALL:
				return this.visitFunctionCall((FunctionCall)node);
			case Token.HOOK:
				return this.visitTernary((ConditionalExpression)node);
			case Token.THIS:
				// TODO: evaluate if will allow custom extensions
				throw new IllegalArgumentException("'this' not supported");
			default:
				CodeBinaryOperatorType binary = this.mapBinaryOperator(tokenType);
				if (binary != CodeBinaryOperatorType.NONE) {
					return this.visitBinaryOp((InfixExpression)node, binary);
				}
				CodeUnaryOperatorType unary = this.mapUnaryOperator(tokenType);
				if (unary != CodeUnaryOperatorType.NONE) {
					return this.visitUnaryOp((UnaryExpression)node, unary);
				}

				throw new IllegalArgumentException("Token not yet supported ("+node.getClass()+"):\n"+(node.debugPrint()));
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
			case Token.NEG:
				return CodeUnaryOperatorType.NEGATION;
			case Token.POS:
				return CodeUnaryOperatorType.POSITIVE;
			case Token.BITNOT:
				return CodeUnaryOperatorType.BITWISE_NEGATION;
			case Token.INC:
				return CodeUnaryOperatorType.PRE_INCREMENT;
			case Token.DEC:
				return CodeUnaryOperatorType.PRE_DECREMENT;
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

	private CodeObject visitVarRef(Name node) {
		String ident = node.getIdentifier();

		if (!node.isLocalName()) {
			if ("model".equals(ident) || "index".equals(ident) || "count".equals(ident)) {
				return new CodeVariableReferenceExpression(ident);
			}

			throw new IllegalArgumentException("Global references not supported");
		}

		// TODO: context may have added additional local vars
		throw new IllegalArgumentException("Unknown local references not supported");
	}

	private CodeMethod visitFunction(FunctionNode node) throws IllegalArgumentException {
		CodeMethod method = new CodeMethod();

		if (node.depth() == 1) {
			method.setName(this.scope.nextID());

			method.addParameter(Writer.class, "writer");
			method.addParameter(Object.class, "model");
			method.addParameter(Integer.class, "index");
			method.addParameter(Integer.class, "count");

		} else {
			// TODO: extract parameter names / types
			throw new IllegalArgumentException("Nested functions not yet supported.");
		}

		CodeObject body = this.visit(node.getBody());
		if (body instanceof CodeStatementBlock) {
			method.getStatements().addAll((CodeStatementBlock)body);

		} else if (body instanceof CodeStatement) {
			method.getStatements().add((CodeStatement)body);

		} else if (body instanceof CodeExpression) {
			method.getStatements().add((CodeExpression)body);

		} else if (body != null) {
			throw new IllegalArgumentException("Unexpected function body: "+body.getClass());
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
        		throw new IllegalArgumentException("Unexpected statement value: "+value.getClass());
            }
        }
        return statements;
	}

	private List<CodeMember> visit(AstRoot root) {
		List<CodeMember> members = new ArrayList<CodeMember>();
        for (Node node : root) {
            CodeObject member = this.visit((AstNode)node);

            if (member == null) {
            	continue;

            } else if (member instanceof CodeMember) {
            	members.add((CodeMember)member);

            } else {
        		throw new IllegalArgumentException("Unexpected member: "+member.getClass());
            }
        }
        return members;
	}
}
