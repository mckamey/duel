package org.duelengine.duel.codegen;

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
				new CodeVariableReferenceExpression(Appendable.class, "output"),
				"append",
				new CodeExpression[] {
					new CodePrimitiveExpression(literal)
				}));
	}

	public static CodeStatement emitVarValue(CodeVariableDeclarationStatement localVar) {
		// output.append(varName);
		return emitExpression(new CodeVariableReferenceExpression(localVar));
	}

	public static CodeStatement emitExpressionSafe(CodeExpression expression) {
		Class<?> exprType = expression.getResultType();
		if (Boolean.class.equals(exprType) ||
			Number.class.isAssignableFrom(exprType) ||
			(exprType != null && exprType.isPrimitive() && !char.class.equals(exprType))) {

			// can optimize based on static analysis
			return emitExpression(expression);
		}
			
		// this.htmlEncode(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"htmlEncode",
				new CodeExpression[] {
					new CodeVariableReferenceExpression(Appendable.class, "output"),
					expression
				}));
	}

	public static CodeStatement emitExpression(CodeExpression expression) {
		if (String.class.equals(expression.getResultType())) {
			// output.append(expression);
			return new CodeExpressionStatement(
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(Appendable.class, "output"),
					"append",
					new CodeExpression[] {
						expression
					}));

		}

		// this.write(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"write",
				new CodeExpression[] {
					new CodeVariableReferenceExpression(Appendable.class, "output"),
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
		if (parameters.size() != 5 || !Appendable.class.equals(parameters.get(0).getType())) {
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

	public static CodeExpression ensureBoolean(CodeExpression expression) {
		Class<?> exprType = expression.getResultType();
		if (boolean.class.equals(exprType) ||
			Boolean.class.equals(exprType)) {

			return expression;
		}

		// this.asBoolean(expression);
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"asBoolean",
			new CodeExpression[] {
				expression
			});
	}

	public static CodeExpression ensureNumber(CodeExpression expression) {
		Class<?> exprType = expression.getResultType();
		if (Number.class.isAssignableFrom(exprType) ||
			int.class.isAssignableFrom(exprType) ||
			double.class.isAssignableFrom(exprType) ||
			float.class.isAssignableFrom(exprType) ||
			long.class.isAssignableFrom(exprType) ||
			short.class.isAssignableFrom(exprType) ||
			byte.class.isAssignableFrom(exprType)) {

			return expression;
		}

		// this.asNumber(expression);
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"asNumber",
			new CodeExpression[] {
				expression
			});
	}

	public static CodeExpression ensureString(CodeExpression expression) {
		if (String.class.equals(expression.getResultType())) {
			return expression;
		}

		// this.asString(expression);
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"asString",
			new CodeExpression[] {
				expression
			});
	}
}
