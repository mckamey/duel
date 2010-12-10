package org.duelengine.duel.codegen;

import java.util.*;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
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
		viewType.add(createCtor(new CodeParameterDeclarationExpression(DuelPart.class, "parts", true)));

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
			DuelPart.class);

		if (members != null) {
			for (CodeMember member : members) {
				partType.add(member);
			}
		}
		
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

	public static CodeVariableDeclarationStatement nextID(IdentifierScope scope) {
		// String id_XXX = output.nextID();
		return new CodeVariableDeclarationStatement(
			String.class,
			scope.nextIdent("id_"),
			new CodeMethodInvokeExpression(
				String.class,
				new CodeVariableReferenceExpression(DuelContext.class, "output"),
				"nextID"));
	}

	public static CodeStatement emitLiteralValue(String literal) {
		// output.append("literal");
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				Void.class,
				new CodeVariableReferenceExpression(DuelContext.class, "output"),
				"append",
				new CodePrimitiveExpression((literal.length() == 1) ? literal.charAt(0) : literal)));
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
				Void.class,
				new CodeThisReferenceExpression(),
				"htmlEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "output"),
				expression));
	}

	public static CodeStatement emitExpression(CodeExpression expression) {
		if (String.class.equals(expression.getResultType())) {
			// output.append(expression);
			return new CodeExpressionStatement(
				new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					expression));

		}

		// this.write(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"write",
				new CodeVariableReferenceExpression(DuelContext.class, "output"),
				expression));
	}

	/**
	 * Unwraps a simple expression if that's all the method returns.
	 * @param method the method to unwrap
	 * @return null if not able to be inlined
	 */
	public static CodeExpression inlineMethod(CodeMethod method) {
		List<CodeParameterDeclarationExpression> parameters = method.getParameters();
		if (parameters.size() != 5 || !DuelContext.class.equals(parameters.get(0).getType())) {
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
		return DuelData.isBoolean(expression.getResultType());
	}

	public static boolean isNumber(CodeExpression expression) {
		return DuelData.isNumber(expression.getResultType());
	}

	public static boolean isString(CodeExpression expression) {
		return DuelData.isString(expression.getResultType());
	}

	public static CodeExpression ensureBoolean(CodeExpression expression) {
		if (isBoolean(expression)) {
			return expression;
		}

		// DuelData.coerceBoolean(expression);
		return new CodeMethodInvokeExpression(
			boolean.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"coerceBoolean",
			expression);
	}

	public static CodeExpression ensureNumber(CodeExpression expression) {
		if (isNumber(expression)) {
			return expression;
		}

		// DuelData.coerceNumber(expression);
		return new CodeMethodInvokeExpression(
			double.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"coerceNumber",
			expression);
	}

	public static CodeExpression ensureString(CodeExpression expression) {
		if (isString(expression)) {
			return expression;
		}

		// DuelData.coerceString(expression);
		return new CodeMethodInvokeExpression(
			String.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"coerceString",
			expression);
	}

	public static CodeExpression ensureCollection(CodeExpression expression) {
		if (List.class.equals(expression.getResultType())) {
			return expression;
		}

		// DuelData.coerceCollection(expression);
		return new CodeMethodInvokeExpression(
			Collection.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"coerceCollection",
			expression);
	}

	public static CodeExpression ensureMap(CodeExpression expression) {
		if (Map.class.equals(expression.getResultType())) {
			return expression;
		}

		// DuelData.coerceMap(expression);
		return new CodeMethodInvokeExpression(
			Map.class,
			new CodeTypeReferenceExpression(DuelData.class),
			"coerceMap",
			expression);
	}

	public static CodeExpression equal(CodeExpression a, CodeExpression b) {
		return new CodeMethodInvokeExpression(
			boolean.class,
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
			boolean.class,
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
		return asAssignment(CodeBinaryOperatorType.ADD, i, ensureNumber(i), CodePrimitiveExpression.ONE);
	}

	public static CodeExpression safePreDecrement(CodeExpression i) {
		return asAssignment(CodeBinaryOperatorType.SUBTRACT, i, ensureNumber(i), CodePrimitiveExpression.ONE);
	}

	public static CodeExpression safePostIncrement(CodeExpression i) {
		return new CodeMethodInvokeExpression(
			double.class,
			new CodeThisReferenceExpression(),
			"echo",
			ensureNumber(i),
			asAssignment(CodeBinaryOperatorType.ADD, i, ensureNumber(i), CodePrimitiveExpression.ONE));
	}

	public static CodeExpression safePostDecrement(CodeExpression i) {
		return new CodeMethodInvokeExpression(
			double.class,
			new CodeThisReferenceExpression(),
			"echo",
			ensureNumber(i),
			asAssignment(CodeBinaryOperatorType.SUBTRACT, i, ensureNumber(i), CodePrimitiveExpression.ONE));
	}

	public static CodeExpression asAssignment(CodeBinaryOperatorType op, CodeExpression assign, CodeExpression left, CodeExpression right) {
		return new CodeBinaryOperatorExpression(
				CodeBinaryOperatorType.ASSIGN,
				assign,
				new CodeBinaryOperatorExpression(op, left, right).withParens()
			).withParens();
	}

	public static CodeExpression ensureType(Class<?> varType, CodeExpression expr) {
		Class<?> valueType = expr.getResultType();
		if (varType.isAssignableFrom(valueType)) {
			return expr;
		}
		if (DuelData.isNumber(varType)) {
			return ensureNumber(expr);
		}
		if (DuelData.isString(varType)) {
			return ensureString(expr);
		}
		if (DuelData.isBoolean(varType)) {
			return ensureBoolean(expr);
		}
		return expr;
	}

	/**
	 * A simplified version of ECMA-262 ToPrimitive definition (Section 9.1)
	 * @param exprType
	 * @return
	 */
	public static Class<?> toPrimitive(Class<?> exprType) {
		if (exprType.isPrimitive() || Number.class.isAssignableFrom(exprType) || Boolean.class.isAssignableFrom(exprType)) {
			return exprType;
		}

		// generalize others get coerced to String
		return String.class;
	}

	/**
	 * Implementation of common JS methods
	 * @param returnType
	 * @param target
	 * @param methodName
	 * @param args
	 * @return
	 */
	public static CodeObject translateMethodCall(Class<?> returnType, CodeExpression target, String methodName, CodeExpression... args) {
		if ("toString".equals(methodName)) {
			return ensureString(target);
		}

		if ("substring".equals(methodName)) {
			return new CodeMethodInvokeExpression(
				String.class,
				ensureString(target),
				"substring",
				args);
		}

		if ("substr".equals(methodName)) {
			if (args.length > 1) {
				args[1] = new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ADD,
					args[0],
					args[1]);
			}

			return new CodeMethodInvokeExpression(
				String.class,
				ensureString(target),
				"substring",
				args);
		}

		// implement others as needed
		return null;
	}

}
