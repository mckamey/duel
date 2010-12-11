package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import org.duelengine.duel.DataEncoder;
import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class CodeDOMBuilderTests {

	@Test
	public void stringSimpleTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("A JSON payload should be an object or array, not a string."));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void stringEscapeTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("\\\b\f\n\r\t\u0123\u4567\u89AB\uCDEF\uabcd\uef4A\""));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("\\&#x0008;&#x000C;\n\r\t&#x0123;&#x4567;&#x89AB;&#xCDEF;&#xABCD;&#xEF4A;\"")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ExpressionNode("count"));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "count").withParens()))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void expressionDataTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ExpressionNode("data"));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data").withParens()))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void markupExpressionDataTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new MarkupExpressionNode("data"));


		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data").withParens()))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

//	@Test
	public void statementNoneTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new StatementNode("return Math.PI;"));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("Math"),
							new CodePrimitiveExpression("PI"))))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("example.foo"))
			},
			new ElementNode("div", null,
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data === 0"))
						},
						new LiteralNode("zero")),
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data === 1"))
						},
						new LiteralNode("one")),
					new IFCommandNode(null,
						new LiteralNode("many")))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
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
								new CodeVariableReferenceExpression(DuelContext.class, "output"),
								"append",
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
										new CodeVariableReferenceExpression(DuelContext.class, "output"),
										"append",
										new CodePrimitiveExpression("one")))
							},
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										Void.class,
										new CodeVariableReferenceExpression(DuelContext.class, "output"),
										"append",
										new CodePrimitiveExpression("many")))
							})
					}),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalSinglesTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("example.foo2"))
			},
			new ElementNode("div", null,
				new IFCommandNode(
					new AttributePair[] {
						new AttributePair("test", new StatementNode("return data == 0;"))
					},
					new LiteralNode("zero")),
				new IFCommandNode(
					new AttributePair[] {
						new AttributePair("test", new StatementNode("return data == 1;"))
					},
					new LiteralNode("one")),
				new IFCommandNode(null,
					new LiteralNode("many"))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(0)),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("zero")))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1)),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("one")))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("many"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationArrayTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("example"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributePair[] {
						new AttributePair("each", new ExpressionNode("data.items"))
					},
					new LiteralNode("item "),
					new ExpressionNode("index"))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Collection.class,
					"items_1",// collection
					new CodeMethodInvokeExpression(
						Collection.class,
						new CodeTypeReferenceExpression(DuelData.class),
						"coerceCollection",
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("items")).withParens())),
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
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
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
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index").withParens()))
			).withThrows(IOException.class));

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationObjectTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("example"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributePair[] {
						new AttributePair("in", new ExpressionNode("data.foo"))
					},
					new LiteralNode("item "),
					new ExpressionNode("index"))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Collection.class,
					"items_1",// collection
					new CodeMethodInvokeExpression(
						Set.class,
						new CodeMethodInvokeExpression(
							Map.class,
							new CodeTypeReferenceExpression(DuelData.class),
							"coerceMap",
							new CodePropertyReferenceExpression(
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression("foo")).withParens()),
						"entrySet")),
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
						new CodeCastExpression(
							Map.Entry.class,
							new CodeMethodInvokeExpression(
								Object.class,
								new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
								"next"))),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"render_2",
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeMethodInvokeExpression(
								Object.class,
								new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
								"getValue"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							new CodeMethodInvokeExpression(
								String.class,
								new CodeTypeReferenceExpression(DuelData.class),
								"coerceString",
								new CodeMethodInvokeExpression(
									Object.class,
									new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
									"getKey"))))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index").withParens()))
				).withThrows(IOException.class));

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationCountTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("example"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributePair[] {
						new AttributePair("count", new ExpressionNode("4")),
						new AttributePair("data", new ExpressionNode("data.name")),
					},
					new LiteralNode("item "),
					new ExpressionNode("index"))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div>"))),
				new CodeVariableDeclarationStatement(
					Object.class,
					"data_1",// data
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("name")).withParens()),
				new CodeIterationStatement(
					new CodeVariableCompoundDeclarationStatement(
						new CodeVariableDeclarationStatement(
							int.class,
							"index_2",// index
							CodePrimitiveExpression.ZERO),
						new CodeVariableDeclarationStatement(
							int.class,
							"count_3",// count
							new CodePrimitiveExpression(4).withParens())),// initStatement
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
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeVariableReferenceExpression(Object.class, "data_1"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index").withParens()))
			).withThrows(IOException.class));

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void suspendModeTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("foo", new LiteralNode("&<>\""))
				},
				new LiteralNode("&<>\""),
				new ElementNode("script",
					new AttributePair[] {
						new AttributePair("type", new LiteralNode("text/javascript"))
					},
					new LiteralNode("&<>\"")),
				new LiteralNode("&<>\"")
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div foo=\"&amp;&lt;&gt;&quot;\">&amp;&lt;&gt;\""))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeGlobalData",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_2"),
					new CodePrimitiveExpression(true))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<script type=\"text/javascript\">&<>\"</script>&amp;&lt;&gt;\"</div>")))
				).withOverride().withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				DataEncoder.class,
				"encoder_2"),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ASSIGN,
					new CodeFieldReferenceExpression(
						new CodeThisReferenceExpression(),
						DataEncoder.class,
						"encoder_2"),
					new CodeObjectCreateExpression(
						DataEncoder.class.getSimpleName(),
						new CodePrimitiveExpression("\n"),
						new CodePrimitiveExpression("\t"))))).withOverride()
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void namespaceTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.bar.Blah"))
			},
			new ElementNode("div"));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div></div>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void callViewTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.bar.Blah"))
			},
			new CALLCommandNode(
				new AttributePair[] {
					new AttributePair("view", new LiteralNode("foo.bar.Yada")),
					new AttributePair("data", new ExpressionNode("data.foo"))
				}));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeThisReferenceExpression(),
						"renderView",
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("foo")).withParens(),
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

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void callWrapperTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.bar.Blah"))
			},
			new CALLCommandNode(
				new AttributePair[] {
					new AttributePair("view", new LiteralNode("foo.bar.Yada")),
					new AttributePair("data", new ExpressionNode("data"))
				},
				new PARTCommandNode(
					new AttributePair[] {
						new AttributePair("name", new LiteralNode("header"))
					},
					new ElementNode("div", null,
						new LiteralNode("Lorem ipsum.")))));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeThisReferenceExpression(),
						"renderView",
						new CodeFieldReferenceExpression(
							new CodeThisReferenceExpression(),
							org.duelengine.duel.DuelView.class,
							"view_2"),
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
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
					"getPartName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("header"))).withOverride(),
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
							Void.class,
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
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
							new CodeObjectCreateExpression("part_3"))))
				).withOverride());

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void wrapperViewTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.bar.Blah"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("class", new LiteralNode("dialog"))
				},
				new PARTCommandNode(
						new AttributePair[] {
							new AttributePair("name", new LiteralNode("header"))
						},
						new ElementNode("h2", null,
							new LiteralNode("Warning"))),
				new ElementNode("hr"),
				new PARTCommandNode(
						new AttributePair[] {
							new AttributePair("name", new LiteralNode("body"))
						},
						new ElementNode("div", null,
							new LiteralNode("Lorem ipsum.")))));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div class=\"dialog\">"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<hr />"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			CodeDOMUtility.createPartType(
				"part_2",
				new CodeMethod(
					AccessModifierType.PUBLIC,
					String.class,
					"getPartName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("header"))).withOverride(),
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
							Void.class,
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
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
					"getPartName",
					null,
					new CodeMethodReturnStatement(new CodePrimitiveExpression("body"))).withOverride(),
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
							Void.class,
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							"append",
							new CodePrimitiveExpression("<div>Lorem ipsum.</div>")))
					).withOverride().withThrows(IOException.class)));

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		
		assertEquals(expected, actual);
	}

	@Test
	public void commentTest() throws Exception {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("Hello world."),
			new CommentNode("Comment Here"),
			new LiteralNode("Lorem ipsum."));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("Hello world.<!--Comment Here-->Lorem ipsum.")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void commentMarkupTest() throws Exception {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("Hello world."),
			new CommentNode("Comment<br>with<hr>some-->markup"),
			new LiteralNode("Lorem ipsum."));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("Hello world.<!--Comment<br>with<hr>some--&gt;markup-->Lorem ipsum.")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void codeCommentTest() throws Exception {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("Hello world."),
			new CodeCommentNode("Code Comment Here"),
			new LiteralNode("Lorem ipsum."));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("Hello world."))),
				new CodeCommentStatement("Code Comment Here"),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("Lorem ipsum.")))
			).withOverride().withThrows(IOException.class));

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void docTypeTest() throws Exception {
		VIEWCommandNode input = new VIEWCommandNode(
				new AttributePair[] {
					new AttributePair("name", new LiteralNode("foo"))
				},
				new DocTypeNode("html"),
				new ElementNode("html", null,
					new ElementNode("head", null,
						new ElementNode("title", null,
							new LiteralNode("The head."))),
					new ElementNode("body", null,
						new ElementNode("h1", null,
							new LiteralNode("The body."))))
				);

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<!doctype html><html><head><title>The head.</title></head><body><h1>The body.</h1></body></html>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void attributesTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("class", new LiteralNode("foo")),
					new AttributePair("style", new LiteralNode("color:red"))
				},
				new ElementNode("ul",
					new AttributePair[] {
						new AttributePair("class", new LiteralNode("bar"))
					},
					new ElementNode("li", null,
						new LiteralNode("one")),
					new ElementNode("li", null,
						new LiteralNode("two")),
					new ElementNode("li", null,
						new LiteralNode("three")))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						Void.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"append",
						new CodePrimitiveExpression("<div class=\"foo\" style=\"color:red\"><ul class=\"bar\"><li>one</li><li>two</li><li>three</li></ul></div>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void attributesExpressionsTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("class", new ExpressionNode("\"foo\"")),
					new AttributePair("style", new ExpressionNode("\"color:\"+data"))
				},
				new ElementNode("ul",
					new AttributePair[] {
						new AttributePair("class", new LiteralNode("bar"))
					},
					new ElementNode("li", null,
						new LiteralNode("one")),
					new ElementNode("li", null,
						new LiteralNode("two")),
					new ElementNode("li", null,
						new LiteralNode("three")))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<div class=\""))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodePrimitiveExpression("foo").withParens())),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("\" style=\""))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new CodePrimitiveExpression("color:"),
							new CodeVariableReferenceExpression(Object.class, "data")).withParens()
						)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("\"><ul class=\"bar\"><li>one</li><li>two</li><li>three</li></ul></div>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void deferredExecutionAttributesTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("class", new ExpressionNode("fooCSS(index)")),
					new AttributePair("style", new ExpressionNode("customStyle(data)"))
				},
				new ElementNode("p",
					new AttributePair[] {
						new AttributePair("class", new ExpressionNode("barCSS(key)"))
					},
					new LiteralNode("Lorem ipsum."))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<div id=\""))),
				new CodeVariableDeclarationStatement(
					String.class,
					"id_1",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"nextID")),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodeVariableReferenceExpression(String.class, "id_1"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("\"><p id=\""))),
				new CodeVariableDeclarationStatement(
					String.class,
					"id_2",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"nextID")),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodeVariableReferenceExpression(String.class, "id_2"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("\">Lorem ipsum.</p><script type=\"text/javascript\">"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeGlobalData",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					new CodePrimitiveExpression(false))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("duel.attr("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(String.class, "id_2"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", { \"class\" : function(data, index, count, key) { return (barCSS(key)); } }, "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(int.class, "count"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(String.class, "key"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(");</script></div><script type=\"text/javascript\">duel.attr("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(String.class, "id_1"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", {\n\t\t\"class\" : function(data, index) { return (fooCSS(index)); },\n\t\tstyle : function(data) { return (customStyle(data)); }\n\t}, "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_5"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(");</script>")))
			).withOverride().withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				DataEncoder.class,
				"encoder_5"),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ASSIGN,
					new CodeFieldReferenceExpression(
						new CodeThisReferenceExpression(),
						DataEncoder.class,
						"encoder_5"),
					new CodeObjectCreateExpression(
						DataEncoder.class.getSimpleName(),
						new CodePrimitiveExpression("\n"),
						new CodePrimitiveExpression("\t"))))).withOverride()
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}

	@Test
	public void deferredExecutionExpressionTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new ExpressionNode("foo(index)")));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<div><script type=\"text/javascript\" id=\""))),
				new CodeVariableDeclarationStatement(
					String.class,
					"id_1",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"nextID")),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodeVariableReferenceExpression(String.class, "id_1"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("\">"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeGlobalData",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_3"),
					new CodePrimitiveExpression(false))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("duel.replace("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_3"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(String.class, "id_1"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", function(data, index) { return (foo(index)); }, "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_3"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(", "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_3"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(");</script></div>")))
			).withOverride().withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				DataEncoder.class,
				"encoder_3"),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ASSIGN,
					new CodeFieldReferenceExpression(
						new CodeThisReferenceExpression(),
						DataEncoder.class,
						"encoder_3"),
					new CodeObjectCreateExpression(
						DataEncoder.class.getSimpleName(),
						new CodePrimitiveExpression("\n"),
						new CodePrimitiveExpression("\t"))))).withOverride()
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}

	@Test
	public void hybridDeferredExecutionExpressionTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new ExpressionNode("foo.bar")));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<div>"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"htmlEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeMethodInvokeExpression(
						Object.class,
						new CodeThisReferenceExpression(),
						"hybrid_3",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"hybrid_3",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeConditionStatement(
					new CodeMethodInvokeExpression(
						boolean.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"hasGlobalData",
						new CodePrimitiveExpression("foo")),
					new CodeMethodReturnStatement(
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("foo"),
							new CodePrimitiveExpression("bar")).withParens())
				),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<script type=\"text/javascript\">"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeGlobalData",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_4"),
					new CodePrimitiveExpression(false))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("duel.write(function() { return (foo.bar); });</script>"))),
				new CodeMethodReturnStatement(CodePrimitiveExpression.NULL)
			).withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				DataEncoder.class,
				"encoder_4"),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ASSIGN,
					new CodeFieldReferenceExpression(
						new CodeThisReferenceExpression(),
						DataEncoder.class,
						"encoder_4"),
					new CodeObjectCreateExpression(
						DataEncoder.class.getSimpleName(),
						new CodePrimitiveExpression("\n"),
						new CodePrimitiveExpression("\t"))))).withOverride()
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}

	@Test
	public void hybridDeferredExecutionStatementTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new StatementNode("foo.bar = (baz+data);")));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<div>"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"htmlEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeMethodInvokeExpression(
						Object.class,
						new CodeThisReferenceExpression(),
						"hybrid_3",
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key")))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"code_2",
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
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("foo"),
							new CodePrimitiveExpression("bar")),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new ScriptVariableReferenceExpression("baz"),
							new CodeVariableReferenceExpression(Object.class, "data")).withParens())),
				new CodeMethodReturnStatement(CodePrimitiveExpression.NULL)
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Object.class,
				"hybrid_3",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(DuelContext.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeConditionStatement(
					new CodeMethodInvokeExpression(
						boolean.class,
						new CodeVariableReferenceExpression(DuelContext.class, "output"),
						"hasGlobalData",
						new CodePrimitiveExpression("foo"),
						new CodePrimitiveExpression("baz")),
					new CodeMethodReturnStatement(
						new CodeMethodInvokeExpression(
							Object.class,
							new CodeThisReferenceExpression(),
							"code_2",
							new CodeVariableReferenceExpression(DuelContext.class, "output"),
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodeVariableReferenceExpression(int.class, "index"),
							new CodeVariableReferenceExpression(int.class, "count"),
							new CodeVariableReferenceExpression(String.class, "key")))
				),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("<script type=\"text/javascript\">"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeGlobalData",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_4"),
					new CodePrimitiveExpression(false))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression("duel.write(function(data) { foo.bar = (baz+data); }, "))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeFieldReferenceExpression(new CodeThisReferenceExpression(), DataEncoder.class, "encoder_4"),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeVariableReferenceExpression(DuelContext.class, "output"),
					"append",
					new CodePrimitiveExpression(");</script>"))),
				new CodeMethodReturnStatement(CodePrimitiveExpression.NULL)
			).withThrows(IOException.class),
			new CodeField(
				AccessModifierType.PRIVATE,
				DataEncoder.class,
				"encoder_4"),
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"init",
				null,
				new CodeExpressionStatement(new CodeBinaryOperatorExpression(
					CodeBinaryOperatorType.ASSIGN,
					new CodeFieldReferenceExpression(
						new CodeThisReferenceExpression(),
						DataEncoder.class,
						"encoder_4"),
					new CodeObjectCreateExpression(
						DataEncoder.class.getSimpleName(),
						new CodePrimitiveExpression("\n"),
						new CodePrimitiveExpression("\t"))))).withOverride()
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}
}