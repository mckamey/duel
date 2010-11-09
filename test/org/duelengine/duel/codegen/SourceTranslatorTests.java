package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.duelengine.duel.ast.CodeBlockNode;
import org.duelengine.duel.codedom.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SourceTranslatorTests {

	@Test
	public void translateVarRefTest() {
		String input = "function(data) { return data; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(new CodeVariableReferenceExpression("data"))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateBinaryOpStringsTest() {
		String input = "function(data) { return data === \"Lorem ipsum\"; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.IDENTITY_EQUALITY,
							new CodeVariableReferenceExpression("data"),
							new CodePrimitiveExpression("Lorem ipsum")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateConcatStringsTest() {
		String input = "function(data) { return \"Lorem ipsum\" + data; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new CodePrimitiveExpression("Lorem ipsum"),
							new CodeVariableReferenceExpression("data")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateParensBinaryOpNumbersTest() {
		String input = "function(data, index, count) { return (count >= 1); }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
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
		String input = "function(data) { return data.foo; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression("data"),
							new CodePrimitiveExpression("foo")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateObjectMethodCallTest() {
		String input = "function(data) { return data.substr(5, 2); }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeMethodInvokeExpression(
							new CodePropertyReferenceExpression(
								new CodeVariableReferenceExpression("data"),
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
		String input = "function(data) { return data['foo']; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression("data"),
							new CodePrimitiveExpression("foo")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayAccessTest() {
		String input = "function(data) { return data[3]; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression("data"),
							new CodePrimitiveExpression(3.0)))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateUnaryOpTest() {
		String input = "function(data) { return -(42); }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
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
		String input = "function(data) { return data--; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_DECREMENT,
							new CodeVariableReferenceExpression("data")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translatePreIncTest() {
		String input = "function(data) { return ++data; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.PRE_INCREMENT,
							new CodeVariableReferenceExpression("data")))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateTernaryTest() {
		String input = "function(data) { return data ? 1 : 2; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(
						new CodeTernaryOperatorExpression(
							new CodeVariableReferenceExpression("data"),
							new CodePrimitiveExpression(1.0),
							new CodePrimitiveExpression(2.0)))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateForLoopTest() {
		String input = "function(data) { for (var i=0, length=data.length; i<length; i++) { data[i].toString(); } }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count")
				},
				new CodeStatement[] {
					new CodeIterationStatement(
						new CodeVariableCompoundDeclarationStatement(new CodeVariableDeclarationStatement[] {
							new CodeVariableDeclarationStatement(null, "i2",
								new CodePrimitiveExpression(0.0)),
							new CodeVariableDeclarationStatement(null, "length4",
								new CodePropertyReferenceExpression(
									new CodeVariableReferenceExpression("data"),
									new CodePrimitiveExpression("length4"))),
						}),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.LESS_THAN,
							new CodeVariableReferenceExpression("i2"),
							new CodeVariableReferenceExpression("length4")),
						new CodeExpressionStatement(
							new CodeUnaryOperatorExpression(
								CodeUnaryOperatorType.POST_INCREMENT,
								new CodeVariableReferenceExpression("i2"))),
						new CodeStatement[] {
							new CodeExpressionStatement(
								new CodeMethodInvokeExpression(
									new CodePropertyReferenceExpression(
										new CodePropertyReferenceExpression(
											new CodeVariableReferenceExpression("data"),
											new CodeVariableReferenceExpression("i2")),
										new CodePrimitiveExpression("toString")),
									null,
									null))
						})
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}
}
