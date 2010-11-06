package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codedom.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SourceTranslatorTests {

	@Test
	public void translateVarRefTest() {
		String input = "function(model) { return model; }";

		CodeMethod expected =
			new CodeMethod(
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

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateBinaryOpStringsTest() {
		String input = "function(model) { return model === \"Lorem ipsum\"; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.IDENTITY_EQUALITY,
							new CodeVariableReferenceExpression("model"),
							new CodePrimitiveExpression("Lorem ipsum")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateConcatStringsTest() {
		String input = "function(model) { return \"Lorem ipsum\" + model; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new CodePrimitiveExpression("Lorem ipsum"),
							new CodeVariableReferenceExpression("model")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateParensBinaryOpNumbersTest() {
		String input = "function(model, index, count) { return (count >= 1); }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.GREATER_THAN_OR_EQUAL,
							new CodeVariableReferenceExpression("count"),
							new CodePrimitiveExpression(1.0)))
				});

		// need to signal that a parens was absorbed
		((CodeMethodReturnStatement)expected.getStatements().getLastStatement()).getExpression().setHasParens(true);

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateObjectPropertyAccessTest() {
		String input = "function(model) { return model.foo; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression("model"),
							new CodePrimitiveExpression("foo")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateObjectMethodCallTest() {
		String input = "function(model) { return model.substr(5, 2); }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeMethodInvokeExpression(
							new CodePropertyReferenceExpression(
								new CodeVariableReferenceExpression("model"),
								new CodePrimitiveExpression("substr")), null, new CodeExpression[] {
									new CodePrimitiveExpression(5.0),
									new CodePrimitiveExpression(2.0)
								}))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateMapValueAccessTest() {
		String input = "function(model) { return model['foo']; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression("model"),
							new CodePrimitiveExpression("foo")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayAccessTest() {
		String input = "function(model) { return model[3]; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression("model"),
							new CodePrimitiveExpression(3.0)))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateUnaryOpTest() {
		String input = "function(model) { return -(42); }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.NEGATION,
							new CodePrimitiveExpression(42.0)))
				});

		// need to signal that a parens was absorbed
		((CodeUnaryOperatorExpression)((CodeMethodReturnStatement)expected.getStatements().getLastStatement()).getExpression()).getExpression().setHasParens(true);

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translatePostIncTest() {
		String input = "function(model) { return model--; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_DECREMENT,
							new CodeVariableReferenceExpression("model")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translatePreIncTest() {
		String input = "function(model) { return ++model; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.PRE_INCREMENT,
							new CodeVariableReferenceExpression("model")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateTernaryTest() {
		String input = "function(model) { return model ? 1 : 2; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeTernaryOperatorExpression(
							new CodeVariableReferenceExpression("model"),
							new CodePrimitiveExpression(1.0),
							new CodePrimitiveExpression(2.0)))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}
}
