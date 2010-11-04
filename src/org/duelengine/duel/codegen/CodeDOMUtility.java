package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codedom.*;

final class CodeDOMUtility {

	// static class
	private CodeDOMUtility() {}

	public static CodeVariableDeclarationStatement nextID(UniqueNameGenerator varGen) {
		return new CodeVariableDeclarationStatement(
			String.class,
			varGen.nextID(),
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"nextID",
				null));
	}

	public static CodeStatement emitLiteralValue(String literal) {
		// writer.write("literal");
		return emitExpression(new CodePrimitiveExpression(literal));
	}

	public static CodeStatement emitVarValue(String varName) {
		// writer.write(varName);
		return emitExpression(new CodeVariableReferenceExpression(varName));
	}

	public static CodeStatement emitExpression(CodeExpression expression) {
		// writer.write(expression);
		return new CodeExpressionStatement( 
			new CodeMethodInvokeExpression(
				new CodeVariableReferenceExpression("writer"),
				"write",
				new CodeExpression[] { expression }));
	}

	/**
	 * Unwraps a simple expression if that's all the method returns.
	 * @param method the method to unwrap
	 * @return null if not able to be inlined
	 */
	public static CodeExpression inlineMethod(CodeMethod method) {
		List<CodeParameterDeclarationExpression> parameters = method.getParameters();
		if (parameters.size() != 4 || !parameters.get(0).getType().equals(Writer.class)) {
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
