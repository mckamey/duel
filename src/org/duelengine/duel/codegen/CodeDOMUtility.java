package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codedom.*;

final class CodeDOMUtility {

	// static class
	private CodeDOMUtility() {}

	public static CodeVariableDeclarationStatement nextID(IdentifierScope varGen) {
		return new CodeVariableDeclarationStatement(
			String.class,
			varGen.nextIdent("id_"),
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"nextID",
				null));
	}

	public static CodeStatement emitLiteralValue(String literal) {
		// output.append("literal");
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeVariableReferenceExpression("output"),
				"append",
				new CodeExpression[] {
					new CodePrimitiveExpression(literal)
				}));
	}

	public static CodeStatement emitVarValue(String varName) {
		// output.append(varName);
		return emitExpression(new CodeVariableReferenceExpression(varName));
	}

	public static CodeStatement emitExpressionSafe(CodeExpression expression) {
		Class<?> exprType = expression.getResultType();
		if (exprType.equals(Boolean.class) ||
			Number.class.isAssignableFrom(exprType) ||
			(exprType.isPrimitive() && !exprType.equals(char.class))) {

			// can optimize based on static analysis
			return emitExpression(expression);
		}
			
		// this.htmlEncode(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"htmlEncode",
				new CodeExpression[] {
					new CodeVariableReferenceExpression("output"),
					expression
				}));
	}

	public static CodeStatement emitExpression(CodeExpression expression) {
		// this.write(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"write",
				new CodeExpression[] {
					new CodeVariableReferenceExpression("output"),
					expression
				}));
	}

	/**
	 * Unwraps a simple expression if that's all the method returns.
	 * @param method the method to unwrap
	 * @return null if not able to be inlined
	 */
	public static CodeExpression inlineMethod(CodeMethod method) {
		List<CodeParameterDeclarationExpression> parameters = method.getParameters();
		if (parameters.size() != 5 || !parameters.get(0).getType().equals(Appendable.class)) {
			// incompatible method signature
			return null;
		}

		CodeStatementCollection statements = method.getStatements();
		if (statements.size() != 1) {
			// incompatible number of statements
			return null;
		}

		CodeStatement last = statements.getLastStatement();
		if (last instanceof CodeMethodReturnStatement) {
			return ((CodeMethodReturnStatement)last).getExpression();
		}

		return null;
	}
}
