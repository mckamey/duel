package org.duelengine.duel.codegen;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.codedom.*;
import org.duelengine.duel.DuelContext;

public class CodeDOMUtilityTests {

	@Test
	public void inlineMethodVarRefTest() throws Exception {

		CodeMethod input = new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(new CodeVariableReferenceExpression(Object.class, "data")));

		CodeExpression expected = new CodeVariableReferenceExpression(Object.class, "data");
		
		CodeExpression actual = CodeDOMUtility.inlineMethod(input);
		assertEquals(expected, actual);
	}

	@Test
	public void inlineMethodPrimitiveTest() throws Exception {

		CodeMethod input = new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(new CodePrimitiveExpression("hello world.")));

		CodeExpression expected = new CodePrimitiveExpression("hello world.");
		
		CodeExpression actual = CodeDOMUtility.inlineMethod(input);
		assertEquals(expected, actual);
	}

	@Test
	public void inlineMethodMultiLineTest() throws Exception {

		CodeMethod input = new CodeMethod(
			AccessModifierType.PRIVATE,
			Object.class,
			"code_1",
			new CodeParameterDeclarationExpression[] {
				new CodeParameterDeclarationExpression(DuelContext.class, "context"),
				new CodeParameterDeclarationExpression(Object.class, "data"),
				new CodeParameterDeclarationExpression(int.class, "index"),
				new CodeParameterDeclarationExpression(int.class, "count"),
				new CodeParameterDeclarationExpression(String.class, "key")
			},
			new CodeVariableDeclarationStatement(
				Object.class,
				"foo",
				new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.LESS_THAN_OR_EQUAL,
					new CodeVariableReferenceExpression(int.class, "index"),
					new CodeVariableReferenceExpression(int.class, "count"))),
			new CodeMethodReturnStatement(new CodeVariableReferenceExpression(Object.class, "foo")));

		CodeExpression expected = null;
		
		CodeExpression actual = CodeDOMUtility.inlineMethod(input);
		assertEquals(expected, actual);
	}
}
