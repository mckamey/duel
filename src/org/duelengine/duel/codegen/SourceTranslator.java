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

	private final UniqueNameGenerator nameGen;

	public SourceTranslator(UniqueNameGenerator nameGen) {
		if (nameGen == null) {
			throw new NullPointerException("nameGen");
		}
		this.nameGen = nameGen;
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
			method.setName(this.nameGen.nextID());

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
