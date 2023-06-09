package org.duelengine.duel.codegen;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.DuelPart;
import org.duelengine.duel.DuelView;
import org.duelengine.duel.HTMLFormatter;
import org.duelengine.duel.codedom.AccessModifierType;
import org.duelengine.duel.codedom.CodeBinaryOperatorExpression;
import org.duelengine.duel.codedom.CodeBinaryOperatorType;
import org.duelengine.duel.codedom.CodeConstructor;
import org.duelengine.duel.codedom.CodeExpression;
import org.duelengine.duel.codedom.CodeExpressionStatement;
import org.duelengine.duel.codedom.CodeMember;
import org.duelengine.duel.codedom.CodeMethod;
import org.duelengine.duel.codedom.CodeMethodInvokeExpression;
import org.duelengine.duel.codedom.CodeMethodReturnStatement;
import org.duelengine.duel.codedom.CodeObject;
import org.duelengine.duel.codedom.CodeParameterDeclarationExpression;
import org.duelengine.duel.codedom.CodePrimitiveExpression;
import org.duelengine.duel.codedom.CodeStatement;
import org.duelengine.duel.codedom.CodeStatementCollection;
import org.duelengine.duel.codedom.CodeThisReferenceExpression;
import org.duelengine.duel.codedom.CodeTypeDeclaration;
import org.duelengine.duel.codedom.CodeTypeReferenceExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorType;
import org.duelengine.duel.codedom.CodeVariableDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableReferenceExpression;
import org.duelengine.duel.codedom.IdentifierScope;

final class CodeDOMUtility {

	// static class
	private CodeDOMUtility() {}

	public static CodeTypeDeclaration createViewType(String typeNS, String typeName, CodeMember... members) {
		CodeTypeDeclaration viewType = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			typeNS,
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
		// String id_XXX = nextID(context);
		return new CodeVariableDeclarationStatement(
			String.class,
			scope.nextIdent("id_"),
			new CodeMethodInvokeExpression(
				String.class,
				new CodeThisReferenceExpression(),
				"nextID",
				new CodeVariableReferenceExpression(DuelContext.class, "context")));
	}

	public static CodeStatement emitLiteralValue(String literal) {
		// write(context, "literal");
		return emitExpression(new CodePrimitiveExpression((literal.length() == 1) ? literal.charAt(0) : literal));
	}

	public static CodeStatement emitVarValue(CodeVariableDeclarationStatement localVar) {
		// write(context, varName);
		return emitExpression(new CodeVariableReferenceExpression(localVar));
	}

	public static CodeStatement emitExpressionSafe(CodeExpression expression, HTMLFormatter formatter, CodeGenSettings settings) {
		Class<?> exprType = expression.getResultType();
		if (Boolean.class.equals(exprType) ||
			Number.class.isAssignableFrom(exprType) ||
			(exprType != null && exprType.isPrimitive() && !char.class.equals(exprType))) {

			// can optimize based on static analysis
			return emitExpression(expression);
		}

		if (expression instanceof CodePrimitiveExpression) {
			try {
				// perform HTML encoding at compile time
				Object value = ((CodePrimitiveExpression)expression).getValue();
				if (!(value instanceof Boolean) && !(value instanceof Number)) {
					StringBuilder output = new StringBuilder();
					formatter.writeLiteral(output, DuelData.coerceString(value), settings.getEncodeNonASCII());
					value = output.toString();
				}

				expression = new CodePrimitiveExpression(value);
				return emitExpression(expression);

			} catch (IOException ex) {
				// ignore and encode at runtime
			}
		}
		
		// htmlEncode(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"htmlEncode",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
				expression));
	}

	public static CodeStatement emitExpression(CodeExpression expression) {
		// write(output, expression);
		return new CodeExpressionStatement(
			new CodeMethodInvokeExpression(
				Void.class,
				new CodeThisReferenceExpression(),
				"write",
				new CodeVariableReferenceExpression(DuelContext.class, "context"),
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

	public static CodeExpression lookupExtraVar(String ident) {
		return new CodeMethodInvokeExpression(
			Object.class,
			new CodeThisReferenceExpression(),
			"getExtra",
			new CodeVariableReferenceExpression(DuelContext.class, "context"),
			new CodePrimitiveExpression(ident));
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
