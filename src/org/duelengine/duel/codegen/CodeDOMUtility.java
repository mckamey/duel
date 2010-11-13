package org.duelengine.duel.codegen;

import java.util.*;
import org.duelengine.duel.ClientIDStrategy;
import org.duelengine.duel.DuelPart;
import org.duelengine.duel.DuelView;
import org.duelengine.duel.codedom.*;

final class CodeDOMUtility {

	// static class
	private CodeDOMUtility() {}

	public static CodeTypeDeclaration createViewType(String namespace, String typeName, CodeMember... members) {
		CodeTypeDeclaration viewType = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			namespace,
			typeName,
			DuelView.class);

		viewType.add(createCtor());
		viewType.add(createCtor(
			new CodeParameterDeclarationExpression(ClientIDStrategy.class, "clientID")));
		viewType.add(createCtor(
			new CodeParameterDeclarationExpression(DuelView.class, "view"),
			new CodeParameterDeclarationExpression(DuelPart.class, "parts", true)));

		if (members != null) {
			for (CodeMember member : members) {
				viewType.add(member);
			}
		}
		
		return viewType;
	}

	public static CodeTypeDeclaration createPartType(String typeName, CodeMember... members) {
		CodeTypeDeclaration partType = new CodeTypeDeclaration(
			AccessModifierType.PRIVATE,
			null,
			typeName,
			DuelPart.class,
			members);

		partType.add(createCtor(new CodeParameterDeclarationExpression(DuelView.class, "view")));
		
		return partType;
	}
	
	public static CodeConstructor createCtor(CodeParameterDeclarationExpression... parameters) {
		CodeConstructor ctor = new CodeConstructor();
		ctor.setAccess(AccessModifierType.PUBLIC);

		for (CodeParameterDeclarationExpression parameter : parameters) {
			ctor.addParameter(parameter);
			ctor.getBaseCtorArgs().add(new CodeVariableReferenceExpression(parameter));
		}

		return ctor;
	}

	public static CodeVariableDeclarationStatement nextID(IdentifierScope varGen) {
		return new CodeVariableDeclarationStatement(
			String.class,
			varGen.nextIdent("id_"),
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"nextID"));
	}

	public static CodeStatement emitLiteralValue(String literal) {
		// output.append("literal");
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeVariableReferenceExpression(Appendable.class, "output"),
				"append",
				new CodePrimitiveExpression(literal)));
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
				new CodeVariableReferenceExpression(Appendable.class, "output"),
				expression));
	}

	public static CodeStatement emitExpression(CodeExpression expression) {
		if (String.class.equals(expression.getResultType())) {
			// output.append(expression);
			return new CodeExpressionStatement(
				new CodeMethodInvokeExpression(
					new CodeVariableReferenceExpression(Appendable.class, "output"),
					"append",
					expression));

		}

		// this.write(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				new CodeThisReferenceExpression(),
				"write",
				new CodeVariableReferenceExpression(Appendable.class, "output"),
				expression));
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

	public static boolean isBoolean(CodeExpression expression) {
		return isBoolean(expression.getResultType());
	}

	public static boolean isBoolean(Class<?> exprType) {
		return (boolean.class.equals(exprType) ||
			Boolean.class.equals(exprType));
	}

	public static boolean isNumber(CodeExpression expression) {
		return isNumber(expression.getResultType());
	}

	public static boolean isNumber(Class<?> exprType) {
		return (Number.class.isAssignableFrom(exprType) ||
			int.class.isAssignableFrom(exprType) ||
			double.class.isAssignableFrom(exprType) ||
			float.class.isAssignableFrom(exprType) ||
			long.class.isAssignableFrom(exprType) ||
			short.class.isAssignableFrom(exprType) ||
			byte.class.isAssignableFrom(exprType));
	}

	public static boolean isString(CodeExpression expression) {
		return isString(expression.getResultType());
	}

	public static boolean isString(Class<?> exprType) {
		return String.class.equals(exprType);
	}

	public static CodeExpression ensureBoolean(CodeExpression expression) {
		if (isBoolean(expression)) {
			return expression;
		}

		// this.asBoolean(expression);
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"asBoolean",
			expression);
	}

	public static CodeExpression ensureNumber(CodeExpression expression) {
		if (isNumber(expression)) {
			return expression;
		}

		// this.asNumber(expression);
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"asNumber",
			expression);
	}

	public static CodeExpression ensureString(CodeExpression expression) {
		if (isString(expression)) {
			return expression;
		}

		// this.asString(expression);
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"asString",
			expression);
	}

	public static CodeExpression equal(CodeExpression a, CodeExpression b) {
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"equal",
			a,
			b);
	}

	public static CodeExpression notEqual(CodeExpression a, CodeExpression b) {
		return new CodeUnaryOperatorExpression(
			CodeUnaryOperatorType.LOGICAL_NEGATION,
			equal(a, b));
	}

	public static CodeExpression coerceEqual(CodeExpression a, CodeExpression b) {
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"coerceEqual",
			a,
			b);
	}

	public static CodeExpression coerceNotEqual(CodeExpression a, CodeExpression b) {
		return new CodeUnaryOperatorExpression(
			CodeUnaryOperatorType.LOGICAL_NEGATION,
			coerceEqual(a, b));
	}

	public static CodeExpression safePreIncrement(CodeExpression i) {
		return asAssignment(CodeBinaryOperatorType.ADD, i, CodePrimitiveExpression.ONE);
	}

	public static CodeExpression safePreDecrement(CodeExpression i) {
		return asAssignment(CodeBinaryOperatorType.SUBTRACT, i, CodePrimitiveExpression.ONE);
	}

	public static CodeExpression safePostIncrement(CodeExpression i) {
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"echo",
			ensureNumber(i),
			asAssignment(CodeBinaryOperatorType.ADD, i, CodePrimitiveExpression.ONE));
	}

	public static CodeExpression safePostDecrement(CodeExpression i) {
		return new CodeMethodInvokeExpression(
			new CodeThisReferenceExpression(),
			"echo",
			ensureNumber(i),
			asAssignment(CodeBinaryOperatorType.SUBTRACT, i, CodePrimitiveExpression.ONE));
	}

	public static CodeExpression asAssignment(CodeBinaryOperatorType op, CodeExpression a, CodeExpression b) {
		CodeBinaryOperatorExpression expr = new CodeBinaryOperatorExpression(op, a, b);
		expr.setHasParens(true);

		expr = new CodeBinaryOperatorExpression(CodeBinaryOperatorType.ASSIGN, a, expr);
		expr.setHasParens(true);
		return expr;
	}

	public static CodeExpression ensureType(Class<?> varType, CodeExpression expr) {
		Class<?> valueType = expr.getResultType();
		if (varType.isAssignableFrom(valueType)) {
			return expr;
		}
		if (isNumber(varType)) {
			return ensureNumber(expr);
		}
		if (isString(varType)) {
			return ensureString(expr);
		}
		if (isBoolean(varType)) {
			return ensureBoolean(expr);
		}
		return expr;
	}
}
