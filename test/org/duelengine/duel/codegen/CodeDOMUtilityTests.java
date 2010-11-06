package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codedom.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeDOMUtilityTests {

	@Test
	public void inlineMethodVarRefTest() throws Exception {

		CodeMethod input = new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(new CodeVariableReferenceExpression("model"))
				});

		CodeExpression expected = new CodeVariableReferenceExpression("model");
		
		CodeExpression actual = CodeDOMUtility.inlineMethod(input);
		assertEquals(expected, actual);
	}

	@Test
	public void inlineMethodPrimitiveTest() throws Exception {

		CodeMethod input = new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(new CodePrimitiveExpression("hello world."))
				});

		CodeExpression expected = new CodePrimitiveExpression("hello world.");
		
		CodeExpression actual = CodeDOMUtility.inlineMethod(input);
		assertEquals(expected, actual);
	}

	@Test
	public void inlineMethodMultiLineTest() throws Exception {

		CodeMethod input = new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeVariableDeclarationStatement(
						Object.class,
						"foo",
						new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.LESS_THAN_OR_EQUAL,
								new CodeVariableReferenceExpression("index"),
								new CodeVariableReferenceExpression("count"))),
					new CodeMethodReturnStatement(new CodeVariableReferenceExpression("foo"))
				});

		CodeExpression expected = null;
		
		CodeExpression actual = CodeDOMUtility.inlineMethod(input);
		assertEquals(expected, actual);
	}
}
