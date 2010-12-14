package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.codedom.*;

public class ServerCodeGenTests {

	@Test
	public void stringSimpleTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"A JSON payload should be an object or array, not a string.\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			null,
			"foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "count")))
				).withOverride().withThrows(IOException.class)
			);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo() {\n"+
			"\t}\n\n"+
			"\tpublic foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, count);\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"example",
			"foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<div>"))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(0)).withParens(),
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("zero")))
					},
					new CodeStatement[] {
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.IDENTITY_EQUALITY,
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression(1)).withParens(),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										Void.class,
										new CodeThisReferenceExpression(),
										"write",
										new CodeVariableReferenceExpression(DuelContext.class, "context"),
										new CodePrimitiveExpression("one")))
							},
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										Void.class,
										new CodeThisReferenceExpression(),
										"write",
										new CodeVariableReferenceExpression(DuelContext.class, "context"),
										new CodePrimitiveExpression("many")))
							})
					}),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("</div>")))
				).withOverride().withThrows(IOException.class)
			);

		String expected =
			"package example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo() {\n"+
			"\t}\n\n"+
			"\tpublic foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tif (this.equal(data, 0)) {\n"+
			"\t\t\tthis.write(context, \"zero\");\n"+
			"\t\t} else if (this.equal(data, 1)) {\n"+
			"\t\t\tthis.write(context, \"one\");\n"+
			"\t\t} else {\n"+
			"\t\t\tthis.write(context, \"many\");\n"+
			"\t\t}\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalSinglesTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"example",
			"foo2",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<div>"))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(0.0)),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("zero")))
					),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1.0)),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("one")))
					),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("many"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("</div>")))
				).withOverride().withThrows(IOException.class)
			);

		String expected =
			"package example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo2 extends DuelView {\n\n"+
			"\tpublic foo2() {\n"+
			"\t}\n\n"+
			"\tpublic foo2(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tif (this.coerceEqual(data, 0.0)) {\n"+
			"\t\t\tthis.write(context, \"zero\");\n"+
			"\t\t}\n"+
			"\t\tif (this.coerceEqual(data, 1.0)) {\n"+
			"\t\t\tthis.write(context, \"one\");\n"+
			"\t\t}\n"+
			"\t\tthis.write(context, \"many\");\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void iterationArrayTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			null,
			"example",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Collection.class,
					"items_1",// collection
					new CodeMethodInvokeExpression(
						List.class,
						new CodeTypeReferenceExpression(DuelData.class),
						"coerceCollection",
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("items")))),
				new CodeVariableCompoundDeclarationStatement(
					new CodeVariableDeclarationStatement(
						int.class,
						"index_2",// index
						CodePrimitiveExpression.ZERO),
					new CodeVariableDeclarationStatement(
						int.class,
						"count_3",// count
						new CodeMethodInvokeExpression(
							int.class,
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"size"))),
				new CodeIterationStatement(
					new CodeVariableDeclarationStatement(
						Iterator.class,
						"iterator_4",
						new CodeMethodInvokeExpression(
							Iterator.class,
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"iterator")),// initStatement
					new CodeMethodInvokeExpression(
						boolean.class,
						new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
						"hasNext"),// testExpression
					new CodeExpressionStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeMethodInvokeExpression(
								Map.Entry.class,
								new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
								"next"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "index").withParens()))
			).withThrows(IOException.class));

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example() {\n"+
			"\t}\n\n"+
			"\tpublic example(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tCollection items_1 = DuelData.coerceCollection(this.getProperty(data, \"items\"));\n"+
			"\t\tint index_2 = 0,\n" +
			"\t\t\tcount_3 = items_1.size();\n"+
			"\t\tfor (Iterator iterator_4=items_1.iterator(); iterator_4.hasNext(); index_2++) {\n"+
			"\t\t\tthis.render_2(context, iterator_4.next(), index_2, count_3, null);\n"+
			"\t\t}\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"item \");\n"+
			"\t\tthis.write(context, index);\n"+
			"\t}\n}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void iterationObjectTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			null,
			"example",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Collection.class,
					"items_1",// collection
					new CodeMethodInvokeExpression(
						Map.class,
						new CodeTypeReferenceExpression(DuelData.class),
						"coerceMap",
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("foo")))),
				new CodeVariableCompoundDeclarationStatement(
					new CodeVariableDeclarationStatement(
						int.class,
						"index_2",// index
						CodePrimitiveExpression.ZERO),
					new CodeVariableDeclarationStatement(
						int.class,
						"count_3",// count
						new CodeMethodInvokeExpression(
							int.class,
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"size"))),
				new CodeIterationStatement(
					new CodeVariableDeclarationStatement(
						Iterator.class,
						"iterator_4",
						new CodeMethodInvokeExpression(
							Iterator.class,
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"iterator")),// initStatement
					new CodeMethodInvokeExpression(
						boolean.class,
						new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
						"hasNext"),// testExpression
					new CodeExpressionStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
					new CodeVariableDeclarationStatement(
						Map.Entry.class,
						"entry_5",
						new CodeMethodInvokeExpression(
							Map.Entry.class,
							new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
							"next")),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeMethodInvokeExpression(
								Object.class,
								new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
								"getValue"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							new CodeMethodInvokeExpression(
								String.class,
								new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
								"getKey")))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "index").withParens()))
			).withThrows(IOException.class));

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example() {\n"+
			"\t}\n\n"+
			"\tpublic example(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tCollection items_1 = DuelData.coerceMap(this.getProperty(data, \"foo\"));\n"+
			"\t\tint index_2 = 0,\n"+
			"\t\t\tcount_3 = items_1.size();\n"+
			"\t\tfor (Iterator iterator_4=items_1.iterator(); iterator_4.hasNext(); index_2++) {\n"+
			"\t\t\tEntry entry_5 = iterator_4.next();\n"+
			"\t\t\tthis.render_2(context, entry_5.getValue(), index_2, count_3, entry_5.getKey());\n"+
			"\t\t}\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"item \");\n"+
			"\t\tthis.write(context, index);\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void iterationCountTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			null,
			"example",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Object.class,
					"data_1",// data
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("name"))),
				new CodeIterationStatement(
					new CodeVariableCompoundDeclarationStatement(
						new CodeVariableDeclarationStatement(
							int.class,
							"index_2",// index
							CodePrimitiveExpression.ZERO),
						new CodeVariableDeclarationStatement(
							int.class,
							"count_3",// count
							new CodePrimitiveExpression(4))),// initStatement
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.LESS_THAN,
						new CodeVariableReferenceExpression(int.class, "index_2"),
						new CodeVariableReferenceExpression(int.class, "count_3")),// testExpression
					new CodeExpressionStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeVariableReferenceExpression(Object.class, "data_1"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(int.class, "index").withParens()))
			).withThrows(IOException.class));

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example() {\n"+
			"\t}\n\n"+
			"\tpublic example(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tObject data_1 = this.getProperty(data, \"name\");\n"+
			"\t\tfor (int index_2=0, count_3=4; index_2 < count_3; index_2++) {\n"+
			"\t\t\tthis.render_2(context, data_1, index_2, count_3, null);\n"+
			"\t\t}\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"item \");\n"+
			"\t\tthis.write(context, index);\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void ternaryOpTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeTernaryOperatorExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression(1.0),
							new CodePrimitiveExpression(2.0))))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, DuelData.coerceBoolean(data) ? 1.0 : 2.0);\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void unaryPostIncTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(Object.class, "data"))))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, this.echo(DuelData.coerceNumber(data), (data = (DuelData.coerceNumber(data) + 1))));\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void unaryPreDecTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.PRE_DECREMENT,
							new CodeVariableReferenceExpression(Object.class, "data"))))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, (data = (DuelData.coerceNumber(data) - 1)));\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void binaryModAssignTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.MODULUS_ASSIGN,
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression(5.0))))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, (data = (DuelData.coerceNumber(data) % 5.0)));\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void binaryAssignTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ASSIGN,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(String.class, "key")))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tdata = key;\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void binaryAssignCastTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ASSIGN,
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(Object.class, "data")))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tcount = DuelData.coerceNumber(data);\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void callViewTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"foo.bar",
			"Blah",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"renderView",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))
				).withOverride().withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				org.duelengine.duel.DuelView.class,
				"view_2"),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ASSIGN,
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						new CodeObjectCreateExpression("foo.bar.Yada")))
				).withOverride());

		String expected =
			"package foo.bar;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Blah extends DuelView {\n\n"+
			"\tpublic Blah() {\n"+
			"\t}\n\n"+
			"\tpublic Blah(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.renderView(context, this.view_2, data, index, count, key);\n"+
			"\t}\n\n"+
			"\tprivate DuelView view_2;\n\n"+
			"\t@Override\n"+
			"\tprotected void init() {\n"+
			"\t\tthis.view_2 = new foo.bar.Yada();\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void callWrapperTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"foo.bar",
			"Blah",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"renderView",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						new CodeVariableReferenceExpression(Object.class, "data").withParens(),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))
				).withOverride().withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				org.duelengine.duel.DuelView.class,
				"view_2"),
			CodeDOMUtility.createPartType(
				"part_3",
				new CodeMethod(
					AccessModifierType.PUBLIC,
					String.class,
					"getName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("header"))).withOverride(),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "context"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("<div>Lorem ipsum.</div>")))
					).withOverride().withThrows(IOException.class)),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ASSIGN,
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						new CodeObjectCreateExpression(
							"foo.bar.Yada",
							new CodeThisReferenceExpression(),
							new CodeObjectCreateExpression("part_3"))))
				).withOverride());

		String expected =
			"package foo.bar;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Blah extends DuelView {\n\n"+
			"\tpublic Blah() {\n"+
			"\t}\n\n"+
			"\tpublic Blah(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.renderView(context, this.view_2, data, index, count, key);\n"+
			"\t}\n\n"+
			"\tprivate DuelView view_2;\n\n"+
			"\tprivate class part_3 extends DuelPart {\n\n"+
			"\t\t@Override\n"+
			"\t\tpublic String getName() {\n"+
			"\t\t\treturn \"header\";\n"+
			"\t\t}\n\n"+
			"\t\t@Override\n"+
			"\t\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\t\tthis.write(context, \"<div>Lorem ipsum.</div>\");\n"+
			"\t\t}\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void init() {\n"+
			"\t\tthis.view_2 = new foo.bar.Yada(this, new part_3());\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void wrapperViewTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"foo.bar",
			"Blah",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<div class=\"dialog\">"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"renderPart",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("header"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("<hr />"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"renderPart",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("body"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			CodeDOMUtility.createPartType(
				"part_2",
				new CodeMethod(
					AccessModifierType.PUBLIC,
					String.class,
					"getName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("header"))).withOverride(),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "context"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("<h2>Warning</h2>")))
					).withOverride().withThrows(IOException.class)),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"addPart",
						new CodeObjectCreateExpression("part_2"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"addPart",
						new CodeObjectCreateExpression("part_3")))).withOverride(),
			CodeDOMUtility.createPartType(
				"part_3",
				new CodeMethod(
					AccessModifierType.PUBLIC,
					String.class,
					"getName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("body"))).withOverride(),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "context"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("<div>Lorem ipsum.</div>")))
					).withOverride().withThrows(IOException.class)));

		String expected =
			"package foo.bar;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Blah extends DuelView {\n\n"+
			"\tpublic Blah() {\n"+
			"\t}\n\n"+
			"\tpublic Blah(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div class=\\\"dialog\\\">\");\n"+
			"\t\tthis.renderPart(context, \"header\", data, index, count, key);\n"+
			"\t\tthis.write(context, \"<hr />\");\n"+
			"\t\tthis.renderPart(context, \"body\", data, index, count, key);\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate class part_2 extends DuelPart {\n\n"+
			"\t\t@Override\n"+
			"\t\tpublic String getName() {\n"+
			"\t\t\treturn \"header\";\n"+
			"\t\t}\n\n"+
			"\t\t@Override\n"+
			"\t\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\t\tthis.write(context, \"<h2>Warning</h2>\");\n"+
			"\t\t}\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void init() {\n"+
			"\t\tthis.addPart(new part_2());\n"+
			"\t\tthis.addPart(new part_3());\n"+
			"\t}\n\n"+
			"\tprivate class part_3 extends DuelPart {\n\n"+
			"\t\t@Override\n"+
			"\t\tpublic String getName() {\n"+
			"\t\t\treturn \"body\";\n"+
			"\t\t}\n\n"+
			"\t\t@Override\n"+
			"\t\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\t\tthis.write(context, \"<div>Lorem ipsum.</div>\");\n"+
			"\t\t}\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void commentTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("Hello world.<!--Comment Here-->Lorem ipsum.")))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"Hello world.<!--Comment Here-->Lorem ipsum.\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void codeCommentTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			"com.example",
			"Foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("Hello world."))),
				new CodeCommentStatement("Code Comment Here"),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("Lorem ipsum.")))
				).withOverride().withThrows(IOException.class));

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"Hello world.\");\n"+
			"\t\t/*Code Comment Here*/\n"+
			"\t\tthis.write(context, \"Lorem ipsum.\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void propertyAssignmentTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			null,
			"foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("<div>"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"htmlEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeMethodInvokeExpression(
						Object.class,
						new CodeThisReferenceExpression(),
						"code_2",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ASSIGN,
						new CodePropertyReferenceExpression(new CodeVariableReferenceExpression(Object.class, "data"), new CodePrimitiveExpression("foo")),
						new CodePrimitiveExpression(42))),
				new CodeMethodReturnStatement(CodePrimitiveExpression.NULL)
			)
		);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo() {\n"+
			"\t}\n\n"+
			"\tpublic foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n"+
			"\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tthis.htmlEncode(context, this.code_2(context, data, index, count, key));\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate Object code_2(DuelContext context, Object data, int index, int count, String key) {\n"+
			"\t\tthis.setProperty(data, \"foo\", 42);\n"+
			"\t\treturn null;\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void externalRefAssignmentTest() throws IOException {

		CodeTypeDeclaration input = CodeDOMUtility.createViewType(
			null,
			"foo",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("<div>"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"htmlEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeMethodInvokeExpression(
						Object.class,
						new CodeThisReferenceExpression(),
						"code_2",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "context"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.ASSIGN,
						new ScriptVariableReferenceExpression("foo"),
						new CodeVariableReferenceExpression(Object.class, "data"))),
				new CodeMethodReturnStatement(CodePrimitiveExpression.NULL)
			)
		);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import java.util.Map.Entry;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo() {\n"+
			"\t}\n\n"+
			"\tpublic foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext context, Object data, int index, int count, String key) throws IOException {\n"+
			"\t\tthis.write(context, \"<div>\");\n"+
			"\t\tthis.htmlEncode(context, this.code_2(context, data, index, count, key));\n"+
			"\t\tthis.write(context, \"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate Object code_2(DuelContext context, Object data, int index, int count, String key) {\n"+
			"\t\tthis.putExternal(context, \"foo\", data);\n"+
			"\t\treturn null;\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
