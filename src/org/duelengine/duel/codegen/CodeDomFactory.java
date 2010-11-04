package org.duelengine.duel.codegen;

import org.duelengine.duel.codedom.*;

final class CodeDomFactory {

	// static class
	private CodeDomFactory() {}

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
}
