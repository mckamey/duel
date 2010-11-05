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
			case Token.THIS:
				// TODO: evaluate if will allow custom extensions
				throw new IllegalArgumentException("'this' not supported");
			default:
				CodeBinaryOperatorType operator = this.mapBinaryOperator(tokenType);
				if (operator != CodeBinaryOperatorType.NONE) {
					return this.visitBinaryOp((InfixExpression)node, operator);
				}

				throw new IllegalArgumentException("Token not yet supported ("+node.getClass()+"):\n"+(node.debugPrint()));
		}
	}

	private CodeObject visitBinaryOp(InfixExpression node, CodeBinaryOperatorType operator) {

		CodeObject left = this.visit(node.getLeft());

		if (left instanceof CodeExpressionStatement) {
			left = ((CodeExpressionStatement)left).getExpression();
		} else if (left != null && !(left instanceof CodeExpression)) {
			throw new IllegalArgumentException("Unexpected binary expression: "+left.getClass());
		}

		CodeObject right = this.visit(node.getRight());

		if (right instanceof CodeExpressionStatement) {
			right = ((CodeExpressionStatement)right).getExpression();
		} else if (right != null && !(right instanceof CodeExpression)) {
			throw new IllegalArgumentException("Unexpected binary expression: "+right.getClass());
		}

		return new CodeBinaryOperatorExpression(operator, (CodeExpression)left, (CodeExpression)right);
	}

	private CodeBinaryOperatorType mapBinaryOperator(int tokenType) {
		switch (tokenType) {
			case Token.ADD:
				return CodeBinaryOperatorType.ADD;
			case Token.GT:
				return CodeBinaryOperatorType.GREATER_THAN;
			case Token.GE:
				return CodeBinaryOperatorType.GREATER_THAN_OR_EQUAL;
			case Token.LT:
				return CodeBinaryOperatorType.LESS_THAN;
			case Token.LE:
				return CodeBinaryOperatorType.LESS_THAN_OR_EQUAL;
			case Token.SHEQ:
				return CodeBinaryOperatorType.IDENTITY_EQUALITY;
			case Token.SHNE:
				return CodeBinaryOperatorType.IDENTITY_INEQUALITY;
			default:
				return CodeBinaryOperatorType.NONE;
		}
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
		CodeObject value = this.visit(node.getReturnValue());

		// TODO: evaluate situations where not inlining methods

		if (value == null) {
			return null;
		}

		if (value instanceof CodeExpression) {
			return new CodeMethodReturnStatement((CodeExpression)value);
		}

		if (value instanceof CodeExpressionStatement) {
			return new CodeMethodReturnStatement(((CodeExpressionStatement)value).getExpression());
		}

		throw new IllegalArgumentException("Unexpected return value: "+value.getClass());
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
