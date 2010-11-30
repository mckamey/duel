package org.duelengine.duel.codegen;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.codedom.*;

public class ScriptTranslatorTests {

	@Test
	public void translateVarRefTest() {
		String input = "function(data) { return data; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(new CodeVariableReferenceExpression(Object.class, "data")));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("Lorem ipsum"))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ADD,
						new CodePrimitiveExpression("Lorem ipsum"),
						new CodeVariableReferenceExpression(Object.class, "data"))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.GREATER_THAN_OR_EQUAL,
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodePrimitiveExpression(1.0))));

		// need to signal that a parens was absorbed
		((CodeMethodReturnStatement)expected.getStatements().getLastStatement()).getExpression().setHasParens(true);

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("foo"))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeMethodInvokeExpression(
						Object.class,
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("substr")),
							null,
							new CodePrimitiveExpression(5.0),
							new CodePrimitiveExpression(2.0))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("foo"))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(3.0))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.NEGATION,
						new CodePrimitiveExpression(42.0))));

		// need to signal that a parens was absorbed
		((CodeUnaryOperatorExpression)((CodeMethodReturnStatement)expected.getStatements().getLastStatement()).getExpression()).getExpression().setHasParens(true);

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.POST_DECREMENT,
						new CodeVariableReferenceExpression(Object.class, "data"))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.PRE_INCREMENT,
						new CodeVariableReferenceExpression(Object.class, "data"))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeTernaryOperatorExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1.0),
						new CodePrimitiveExpression(2.0))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeIterationStatement(
					new CodeVariableCompoundDeclarationStatement(
						new CodeVariableDeclarationStatement(Object.class, "i2",
							new CodePrimitiveExpression(0.0)),
						new CodeVariableDeclarationStatement(Object.class, "length4",
							new CodePropertyReferenceExpression(
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression("length4")))),
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.LESS_THAN,
						new CodeVariableReferenceExpression(Object.class, "i2"),
						new CodeVariableReferenceExpression(Object.class, "length4")),
					new CodeExpressionStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(Object.class, "i2"))),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Object.class,
							new CodePropertyReferenceExpression(
								new CodePropertyReferenceExpression(
									new CodeVariableReferenceExpression(Object.class, "data"),
									new CodeVariableReferenceExpression(Object.class, "i2")),
								new CodePrimitiveExpression("toString")),
							null))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateObjectLiteralEmptyTest() {
		String input = "function(data) { return {}; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeMethodInvokeExpression(
						Map.class,
						new CodeThisReferenceExpression(),
						"asMap")));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateObjectLiteralMixedTest() {
		String input = "function(data, index, count) { return { 'a': -2, count: data, \"\": null, $: false }; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeMethodInvokeExpression(
						Map.class,
						new CodeThisReferenceExpression(),
						"asMap",
						new CodePrimitiveExpression("a"), new CodeUnaryOperatorExpression(CodeUnaryOperatorType.NEGATION, new CodePrimitiveExpression(2.0)),
						new CodePrimitiveExpression("count"), new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(""), new CodePrimitiveExpression(null),
						new CodePrimitiveExpression("$"), new CodePrimitiveExpression(false))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);

		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayLiteralEmptyTest() {
		String input = "function(data) { return []; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression()));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayLiteralMixedTest() {
		String input = "function(data) { return [\"a\", 42, data, null, true]; }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression(
						Object.class,
						new CodePrimitiveExpression("a"),
						new CodePrimitiveExpression(42.0),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(null),
						new CodePrimitiveExpression(true))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayCtorEmptyTest() {
		String input = "function(data) { return new Array(); }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression()));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayCtorSizeTest() {
		String input = "function(data) { return new Array(42); }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression(Object.class, 42)));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateArrayCtorMixedTest() {
		String input = "function(data) { return new Array(\"a\", 42, data, null, true); }";

		CodeMethod expected =
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression(
						Object.class,
						new CodePrimitiveExpression("a"),
						new CodePrimitiveExpression(42.0),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(null),
						new CodePrimitiveExpression(true))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateEmptyTest() {
		String input = "function(data) { return ( /*...*/ ); }";

		try {
			new ScriptTranslator().translate(input);

			fail("Expected to throw a ScriptTranslationException");

		} catch (ScriptTranslationException ex) {
		
			assertEquals(35, ex.getColumn());
		}
	}
}
