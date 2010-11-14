package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.DuelContext;
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"A JSON payload should be an object or array, not a string.\");\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "count")))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, count);\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(0.0)),
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(DuelContext.class, "output"),
								"append",
								new CodePrimitiveExpression("zero")))
					},
					new CodeStatement[] {
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.IDENTITY_EQUALITY,
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression(1.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(DuelContext.class, "output"),
										"append",
										new CodePrimitiveExpression("one")))
							},
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(DuelContext.class, "output"),
										"append",
										new CodePrimitiveExpression("many")))
							})
					}),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);
		((CodeConditionStatement)((CodeMethod)input.getMembers().get(1)).getStatements().get(1)).getCondition().setHasParens(true);
		((CodeConditionStatement)((CodeConditionStatement)((CodeMethod)input.getMembers().get(1)).getStatements().get(1)).getFalseStatements().getLastStatement()).getCondition().setHasParens(true);

		String expected =
			"package example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tif (this.equal(data, 0.0)) {\n"+
			"\t\t\toutput.append(\"zero\");\n"+
			"\t\t} else if (this.equal(data, 1.0)) {\n"+
			"\t\t\toutput.append(\"one\");\n"+
			"\t\t} else {\n"+
			"\t\t\toutput.append(\"many\");\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(0.0)),
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(DuelContext.class, "output"),
								"append",
								new CodePrimitiveExpression("zero")))
					}),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1.0)),
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(DuelContext.class, "output"),
								"append",
								new CodePrimitiveExpression("one")))
					}),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("many"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo2 extends DuelView {\n\n"+
			"\tpublic foo2(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tif (this.coerceEqual(data, 0.0)) {\n"+
			"\t\t\toutput.append(\"zero\");\n"+
			"\t\t}\n"+
			"\t\tif (this.coerceEqual(data, 1.0)) {\n"+
			"\t\t\toutput.append(\"one\");\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"many\");\n"+
			"\t\toutput.append(\"</div>\");\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Collection.class,
					"items_1",// collection
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"asArray",
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
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"size"))),
				new CodeIterationStatement(
					new CodeVariableDeclarationStatement(
						Iterator.class,
						"iterator_4",
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"iterator")),// initStatement
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
						"hasNext"),// testExpression
					new CodeExpressionStatement(
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
								"next"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index")))
			));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)input.getMembers().get(2)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tCollection items_1 = this.asArray(this.getProperty(data, \"items\"));\n"+
			"\t\tint index_2 = 0,\n" +
			"\t\t\tcount_3 = items_1.size();\n"+
			"\t\tfor (Iterator iterator_4=items_1.iterator(); iterator_4.hasNext(); index_2++) {\n"+
			"\t\t\tthis.render_2(output, iterator_4.next(), index_2, count_3, null);\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"item \");\n"+
			"\t\tthis.write(output, (index));\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Collection.class,
					"items_1",// collection
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"asObject",
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
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"size"))),
				new CodeIterationStatement(
					new CodeVariableDeclarationStatement(
						Iterator.class,
						"iterator_4",
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(Collection.class, "items_1"),
							"iterator")),// initStatement
					new CodeMethodInvokeExpression(
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
							new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
							"next")),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
								"getValue"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
								"getKey")))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("item "))),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeVariableReferenceExpression(int.class, "index")))
			));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)input.getMembers().get(2)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tCollection items_1 = this.asObject(this.getProperty(data, \"foo\"));\n"+
			"\t\tint index_2 = 0,\n"+
			"\t\t\tcount_3 = items_1.size();\n"+
			"\t\tfor (Iterator iterator_4=items_1.iterator(); iterator_4.hasNext(); index_2++) {\n"+
			"\t\t\tEntry entry_5 = iterator_4.next();\n"+
			"\t\t\tthis.render_2(output, entry_5.getValue(), index_2, count_3, entry_5.getKey());\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"item \");\n"+
			"\t\tthis.write(output, (index));\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
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
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeVariableReferenceExpression(Object.class, "data_1"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index")))
			));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)input.getMembers().get(2)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tObject data_1 = this.getProperty(data, \"name\");\n"+
			"\t\tfor (int index_2=0, count_3=4; index_2 < count_3; index_2++) {\n"+
			"\t\t\tthis.render_2(output, data_1, index_2, count_3, null);\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"item \");\n"+
			"\t\tthis.write(output, (index));\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeTernaryOperatorExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression(1.0),
							new CodePrimitiveExpression(2.0))))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, this.asBoolean(data) ? 1.0 : 2.0);\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.POST_INCREMENT,
							new CodeVariableReferenceExpression(Object.class, "data"))))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, this.echo(this.asNumber(data), (data = (this.asNumber(data) + 1))));\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeUnaryOperatorExpression(
							CodeUnaryOperatorType.PRE_DECREMENT,
							new CodeVariableReferenceExpression(Object.class, "data"))))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, (data = (this.asNumber(data) - 1)));\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.MODULUS_ASSIGN,
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression(5.0))))
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, (data = (this.asNumber(data) % 5.0)));\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
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
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
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
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tcount = this.asNumber(data);\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						"render",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))
				),
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
				));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);

		String expected =
			"package foo.bar;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Blah extends DuelView {\n\n"+
			"\tpublic Blah(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.view_2.render(output, data, index, count, key);\n"+
			"\t}\n\n"+
			"\tprivate DuelView view_2;\n\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						"render",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))
				),
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
					new CodeMethodReturnStatement(new CodePrimitiveExpression("header"))),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("<div>Lorem ipsum.</div>")))
					)),
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
				));
		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);
		((CodeMethod)input.getMembers().get(4)).setOverride(true);
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)input.getMembers().get(1)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);
		((CodeMethod)((CodeTypeDeclaration)input.getMembers().get(3)).getMembers().get(0)).setOverride(true);
		((CodeMethod)((CodeTypeDeclaration)input.getMembers().get(3)).getMembers().get(1)).setOverride(true);

		String expected =
			"package foo.bar;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Blah extends DuelView {\n\n"+
			"\tpublic Blah(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.view_2.render(output, (data), index, count, key);\n"+
			"\t}\n\n"+
			"\tprivate DuelView view_2;\n\n"+
			"\tprivate class part_3 extends DuelPart {\n\n"+
			"\t\t@Override\n"+
			"\t\tpublic String getName() {\n"+
			"\t\t\treturn \"header\";\n"+
			"\t\t}\n\n"+
			"\t\t@Override\n"+
			"\t\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\t\toutput.append(\"<div>Lorem ipsum.</div>\");\n"+
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
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div class=\"dialog\">"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"renderPart",
						new CodePrimitiveExpression("header"),
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<hr />"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"renderPart",
						new CodePrimitiveExpression("body"),
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			CodeDOMUtility.createPartType(
				"part_2",
				new CodeMethod(
					AccessModifierType.PUBLIC,
					String.class,
					"getName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("header"))),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("<h2>Warning</h2>")))
					)),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"addPart",
						new CodeObjectCreateExpression("part_2"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"addPart",
						new CodeObjectCreateExpression("part_3")))),
			CodeDOMUtility.createPartType(
				"part_3",
				new CodeMethod(
					AccessModifierType.PUBLIC,
					String.class,
					"getName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("body"))),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(DuelContext.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("<div>Lorem ipsum.</div>")))
					)));

		// mark override and parens
		((CodeMethod)input.getMembers().get(1)).setOverride(true);
		((CodeMethod)input.getMembers().get(3)).setOverride(true);
		((CodeMethod)((CodeTypeDeclaration)input.getMembers().get(2)).getMembers().get(0)).setOverride(true);
		((CodeMethod)((CodeTypeDeclaration)input.getMembers().get(2)).getMembers().get(1)).setOverride(true);
		((CodeMethod)((CodeTypeDeclaration)input.getMembers().get(4)).getMembers().get(0)).setOverride(true);
		((CodeMethod)((CodeTypeDeclaration)input.getMembers().get(4)).getMembers().get(1)).setOverride(true);

		String expected =
			"package foo.bar;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Blah extends DuelView {\n\n"+
			"\tpublic Blah(DuelPart... parts) {\n"+
			"\t\tsuper(parts);\n"+
			"\t}\n\n"+
			"\t@Override\n"+
			"\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div class=\\\"dialog\\\">\");\n"+
			"\t\tthis.renderPart(\"header\", output, data, index, count, key);\n"+
			"\t\toutput.append(\"<hr />\");\n"+
			"\t\tthis.renderPart(\"body\", output, data, index, count, key);\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate class part_2 extends DuelPart {\n\n"+
			"\t\t@Override\n"+
			"\t\tpublic String getName() {\n"+
			"\t\t\treturn \"header\";\n"+
			"\t\t}\n\n"+
			"\t\t@Override\n"+
			"\t\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\t\toutput.append(\"<h2>Warning</h2>\");\n"+
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
			"\t\tprotected void render(DuelContext output, Object data, int index, int count, String key) {\n"+
			"\t\t\toutput.append(\"<div>Lorem ipsum.</div>\");\n"+
			"\t\t}\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().writeCode(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
