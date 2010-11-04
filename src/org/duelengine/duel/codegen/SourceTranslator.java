package org.duelengine.duel.codegen;

import org.duelengine.duel.codedom.*;
import org.mozilla.javascript.ast.*;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Node;

/**
 * Translates JavaScript source code into CodeDOM
 */
public class SourceTranslator {

	/**
	 * @param jsSource JavaScript source code
	 * @return Equivalent translated Java source code
	 */
	public CodeObject translate(String jsSource) {

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

		switch (node.getType()) {
			case Token.FUNCTION:
				return this.visitFunction((FunctionNode)node);
			case Token.BLOCK:
				return this.visitBlock((Block)node);
			case Token.RETURN:
				return this.visitReturn((ReturnStatement)node);
			case Token.NAME:
				return this.visitVarRef((Name)node);
			case Token.THIS:
				// TODO: evaluate if will allow custom extensions
				throw new IllegalArgumentException("'this' not supported");
			default:
				throw new IllegalArgumentException("Not yet supported: "+Token.name(node.getType()));
		}
	}

	private CodeObject visitVarRef(Name node) {
		if (!node.isLocalName()) {
			throw new IllegalArgumentException("Global references not supported");
		}

		String ident = node.getIdentifier();

		if ("model".equals(ident) || "index".equals(ident) || "count".equals(ident)) {
			return new CodeVariableReferenceExpression(ident);
		}

		// TODO: context may have added additional local vars
		throw new IllegalArgumentException("Unknown local references not supported");
	}

	private CodeObject visitFunction(FunctionNode node) throws IllegalArgumentException {
		if (node.depth() != 1) {
			throw new IllegalArgumentException("Nested functions not yet supported.");
		}

		// inline method body
		return this.visit(node.getBody());
	}

	private CodeStatement visitReturn(ReturnStatement node) {
		CodeObject value = this.visit(node.getReturnValue());

		// TODO: evaluate situations where not inlining methods

		if (value == null) {
			return null;
		}

		if (value instanceof CodeExpression) {
			return CodeDomFactory.emitExpression((CodeExpression)value);
		}

		if (value instanceof CodeExpressionStatement) {
			return CodeDomFactory.emitExpression(((CodeExpressionStatement)value).getExpression());
		}

		throw new IllegalArgumentException("Unexpected return value: "+value.getClass());
	}

	private CodeObject visitBlock(Block block) {
		CodeStatementBlock statements = new CodeStatementBlock();
        for (Node node : block) {
            CodeObject value = this.visit((AstNode)node);
            if (value instanceof CodeStatement) {
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

	private CodeObject visit(AstRoot root) {
        for (Node node : root) {
            this.visit((AstNode)node);
        }
        return null;
	}
}
