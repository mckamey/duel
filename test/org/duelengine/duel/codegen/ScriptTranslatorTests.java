package org.duelengine.duel.codegen;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.codedom.*;

public class ScriptTranslatorTests {

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

	@Test
	public void translateVarRefTest() {
		String input = "function(data) { return data; }";

		CodeMethod expected =
			new CodeMethod(
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.GREATER_THAN_OR_EQUAL,
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodePrimitiveExpression(1)).withParens()));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeMethodInvokeExpression(
						String.class,
						new CodeMethodInvokeExpression(
							String.class,
							new CodeTypeReferenceExpression(DuelData.class),
							"coerceString",
							new CodeVariableReferenceExpression(Object.class, "data")),
						"substring",
						new CodePrimitiveExpression(5),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new CodePrimitiveExpression(5),
							new CodePrimitiveExpression(2)))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateExternalMethodCallTest() {
		String input = "function(data) { return foo(5, 2); }";

		try {
			new ScriptTranslator().translate(input);
			fail("Expected to throw a ScriptTranslationException");

		} catch (ScriptTranslationException ex) {
			assertEquals(24, ex.getIndex());
		}
	}

	@Test
	public void translateExternalVarRefTest() {
		String input = "function(data) { return foo.bar; }";

		CodeMethod expected =
			new CodeMethod(
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
				new CodeMethodReturnStatement(
					new CodePropertyReferenceExpression(
						new ScriptVariableReferenceExpression("foo"),
						new CodePrimitiveExpression("bar"))));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(3))));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.NEGATION,
						new CodePrimitiveExpression(42).withParens())));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeTernaryOperatorExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1),
						new CodePrimitiveExpression(2))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateForLoopTest() {
		String input =
			"function (data) {"+
			"  var str;"+
			"  for (var i=0, length=data.length; i<length; i++) {"+
			"    str += data[i].toString();"+
			"  }"+
			"  return str;"+
			"}";

		CodeMethod expected =
			new CodeMethod(
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
				new CodeVariableDeclarationStatement(Object.class, "str2", null),
				new CodeIterationStatement(
					new CodeVariableCompoundDeclarationStatement(
						new CodeVariableDeclarationStatement(Object.class, "i3",
							new CodePrimitiveExpression(0)),
						new CodeVariableDeclarationStatement(Object.class, "length4",
							new CodePropertyReferenceExpression(
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression("length")))),
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.LESS_THAN,
						new CodeVariableReferenceExpression(Object.class, "i3"),
						new CodeVariableReferenceExpression(Object.class, "length4")),
					new CodeExpressionStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(Object.class, "i3"))),
					new CodeExpressionStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD_ASSIGN,
							new CodeVariableReferenceExpression(Object.class, "str2"),
							new CodeMethodInvokeExpression(
								String.class,
								new CodeTypeReferenceExpression(DuelData.class),
								"coerceString",
								new CodePropertyReferenceExpression(
									new CodeVariableReferenceExpression(Object.class, "data"),
									new CodeVariableReferenceExpression(Object.class, "i3")))))),
					new CodeMethodReturnStatement(
						new CodeVariableReferenceExpression(Object.class, "str2")));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
						new CodePrimitiveExpression("a"), new CodeUnaryOperatorExpression(CodeUnaryOperatorType.NEGATION, new CodePrimitiveExpression(2)),
						new CodePrimitiveExpression("count"), new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(""), CodePrimitiveExpression.NULL,
						new CodePrimitiveExpression("$"), CodePrimitiveExpression.FALSE)));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression(
						Object.class,
						new CodePrimitiveExpression("a"),
						new CodePrimitiveExpression(42),
						new CodeVariableReferenceExpression(Object.class, "data"),
						CodePrimitiveExpression.NULL,
						CodePrimitiveExpression.TRUE)));

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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
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
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeMethodReturnStatement(
					new CodeArrayCreateExpression(
						Object.class,
						new CodePrimitiveExpression("a"),
						new CodePrimitiveExpression(42),
						new CodeVariableReferenceExpression(Object.class, "data"),
						CodePrimitiveExpression.NULL,
						CodePrimitiveExpression.TRUE)));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateUndefinedTest() {
		String input = "function(data) { return undefined; }";

		CodeMethod expected =
			new CodeMethod(
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
				new CodeMethodReturnStatement(
					CodePrimitiveExpression.NULL));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateNaNTest() {
		String input = "function(data) { return NaN; }";

		CodeMethod expected =
			new CodeMethod(
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
				new CodeMethodReturnStatement(
					new CodePrimitiveExpression(Double.NaN)));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}

	@Test
	public void translateNegInfinityTest() {
		String input = "function(data) { return -Infinity; }";

		CodeMethod expected =
			new CodeMethod(
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
				new CodeMethodReturnStatement(
					new CodeUnaryOperatorExpression(
						CodeUnaryOperatorType.NEGATION,
						new CodePrimitiveExpression(Double.POSITIVE_INFINITY))));

		List<CodeMember> actual = new ScriptTranslator().translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}
}
