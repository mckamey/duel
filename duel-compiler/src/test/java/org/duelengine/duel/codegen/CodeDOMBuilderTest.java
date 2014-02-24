package org.duelengine.duel.codegen;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.duelengine.duel.DuelContext;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.ast.AttributePair;
import org.duelengine.duel.ast.CALLCommandNode;
import org.duelengine.duel.ast.CodeCommentNode;
import org.duelengine.duel.ast.CommentNode;
import org.duelengine.duel.ast.DocTypeNode;
import org.duelengine.duel.ast.ElementNode;
import org.duelengine.duel.ast.ExpressionNode;
import org.duelengine.duel.ast.FORCommandNode;
import org.duelengine.duel.ast.IFCommandNode;
import org.duelengine.duel.ast.LiteralNode;
import org.duelengine.duel.ast.MarkupExpressionNode;
import org.duelengine.duel.ast.PARTCommandNode;
import org.duelengine.duel.ast.StatementNode;
import org.duelengine.duel.ast.VIEWCommandNode;
import org.duelengine.duel.ast.XORCommandNode;
import org.duelengine.duel.codedom.AccessModifierType;
import org.duelengine.duel.codedom.CodeArrayCreateExpression;
import org.duelengine.duel.codedom.CodeBinaryOperatorExpression;
import org.duelengine.duel.codedom.CodeBinaryOperatorType;
import org.duelengine.duel.codedom.CodeCastExpression;
import org.duelengine.duel.codedom.CodeCommentStatement;
import org.duelengine.duel.codedom.CodeConditionStatement;
import org.duelengine.duel.codedom.CodeExpressionStatement;
import org.duelengine.duel.codedom.CodeField;
import org.duelengine.duel.codedom.CodeFieldReferenceExpression;
import org.duelengine.duel.codedom.CodeIterationStatement;
import org.duelengine.duel.codedom.CodeMethod;
import org.duelengine.duel.codedom.CodeMethodInvokeExpression;
import org.duelengine.duel.codedom.CodeMethodReturnStatement;
import org.duelengine.duel.codedom.CodeObjectCreateExpression;
import org.duelengine.duel.codedom.CodeParameterDeclarationExpression;
import org.duelengine.duel.codedom.CodePrimitiveExpression;
import org.duelengine.duel.codedom.CodePropertyReferenceExpression;
import org.duelengine.duel.codedom.CodeStatement;
import org.duelengine.duel.codedom.CodeThisReferenceExpression;
import org.duelengine.duel.codedom.CodeTypeDeclaration;
import org.duelengine.duel.codedom.CodeTypeReferenceExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorExpression;
import org.duelengine.duel.codedom.CodeUnaryOperatorType;
import org.duelengine.duel.codedom.CodeVariableCompoundDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableDeclarationStatement;
import org.duelengine.duel.codedom.CodeVariableReferenceExpression;
import org.duelengine.duel.codedom.ScriptExpression;
import org.duelengine.duel.codedom.ScriptVariableReferenceExpression;
import org.junit.Test;

public class CodeDOMBuilderTest {

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
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
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
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
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
						new CodePrimitiveExpression(0)),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("zero")))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1)),
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							Void.class,
							new CodeThisReferenceExpression(),
							"write",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodePrimitiveExpression("one")))),
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
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
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
						new CodePrimitiveExpression("<div foo=\"&amp;&lt;&gt;&quot;\">&amp;&lt;&gt;\""))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeExtras",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(true))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("<script type=\"text/javascript\">&<>\"</script>&amp;&lt;&gt;\"</div>")))
				).withOverride().withThrows(IOException.class)
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
			new LiteralNode("BEFORE"),
			new CALLCommandNode(
				new AttributePair[] {
					new AttributePair("view", new LiteralNode("foo.bar.Yada")),
					new AttributePair("data", new ExpressionNode("data.foo"))
				}),
			new LiteralNode("AFTER"));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						new CodePrimitiveExpression("BEFORE"))),
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
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("foo")).withParens(),
						new CodeVariableReferenceExpression(int.class, "index"),
						new CodeVariableReferenceExpression(int.class, "count"),
						new CodeVariableReferenceExpression(String.class, "key"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("AFTER")))
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
	public void callLiteralTest() throws IOException {
		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.bar.Blah"))
			},
			new CALLCommandNode(
				new AttributePair[] {
					new AttributePair("view", new ExpressionNode("foo.bar.Yada")),
					new AttributePair("data", new ExpressionNode(" { name: 'bar', items: [ 1, 'too' ] } "))
				}));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
						new CodeMethodInvokeExpression(
							Map.class,
							new CodeTypeReferenceExpression(DuelData.class),
							"asMap",
							new CodePrimitiveExpression("name"),
							new CodePrimitiveExpression("bar"),
							new CodePrimitiveExpression("items"),
							new CodeArrayCreateExpression(
								Object.class,
								new CodePrimitiveExpression(1),
								new CodePrimitiveExpression("too"))).withParens(),
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
					"getPartName",
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
					"getPartName",
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
					"getPartName",
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
						new CodePrimitiveExpression("<!DOCTYPE html><html><head><title>The head.</title></head><body><h1>The body.</h1></body></html>")))
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
					new CodePrimitiveExpression("<div class=\""))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("foo"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\" style=\""))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new CodePrimitiveExpression("color:"),
							new CodeVariableReferenceExpression(Object.class, "data")).withParens()
						)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\"><ul class=\"bar\"><li>one</li><li>two</li><li>three</li></ul></div>")))
				).withOverride().withThrows(IOException.class)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void attributesBooleanExpressionsTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("form",
				null,
				new ElementNode("input",
					new AttributePair[] {
						new AttributePair("type", new LiteralNode("checkbox")),
						new AttributePair("checked", new ExpressionNode(" data.isChecked "))
					}),
				new ElementNode("input",
					new AttributePair[] {
						new AttributePair("type", new LiteralNode("checkbox")),
						new AttributePair("disabled", new ExpressionNode(" data.isDisabled "))
					})
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
					new CodePrimitiveExpression("<form><input type=\"checkbox\""))),
				new CodeConditionStatement(
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("isChecked")).withParens(),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(" checked=\"checked\"")))
				),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(" /><input type=\"checkbox\""))),
				new CodeConditionStatement(
					new CodePropertyReferenceExpression(
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression("isDisabled")).withParens(),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(" disabled=\"disabled\"")))
				),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(" /></form>")))
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
					new CodePrimitiveExpression("<div id=\""))),
				new CodeVariableDeclarationStatement(
					String.class,
					"id_1",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeThisReferenceExpression(),
						"nextID",
						new CodeVariableReferenceExpression(DuelContext.class, "context"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_1"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\"><p id=\""))),
				new CodeVariableDeclarationStatement(
					String.class,
					"id_2",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeThisReferenceExpression(),
						"nextID",
						new CodeVariableReferenceExpression(DuelContext.class, "context"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_2"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\">Lorem ipsum.</p><script>"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeExtras",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(false))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("duel({'class':function(data, index, count, key){return(barCSS(key));}})("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(','))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(','))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "count"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(','))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "key"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(").toDOM("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_2"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(",1);</script></div><script>duel({'class':function(data, index){return(fooCSS(index));},style:function(data){return(customStyle(data));}})("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(','))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(").toDOM("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_1"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(",1);</script>")))
			).withOverride().withThrows(IOException.class)
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
					new CodePrimitiveExpression("<div><script id=\""))),
				new CodeVariableDeclarationStatement(
					String.class,
					"id_1",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeThisReferenceExpression(),
						"nextID",
						new CodeVariableReferenceExpression(DuelContext.class, "context"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_1"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\">"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeExtras",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(false))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("duel(function(data, index){return(foo(index));})("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(Object.class, "data"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(','))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(int.class, "index"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(").toDOM("))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"dataEncode",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_1"),
					CodePrimitiveExpression.ONE)),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(");</script></div>")))
			).withOverride().withThrows(IOException.class)
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}

	@Test
	public void hybridDeferredExecutionAttributesTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("Foo"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("onmouseover", new ExpressionNode(" foo.bar.onhover ")),
					new AttributePair("onclick", new ExpressionNode(" foo.onclick ")),
					new AttributePair("class", new LiteralNode("foo-bar"))
				},
				new ElementNode("p",
					new AttributePair[] {
						new AttributePair("onclick", new ExpressionNode(" foo.baz.onclick "))
					},
					new LiteralNode("Lorem ipsum."))
			));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
			null,
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
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("<div"))),
				new CodeVariableDeclarationStatement(Object.class, "val_1",
					new CodePropertyReferenceExpression(
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("foo"),
							new CodePrimitiveExpression("bar")),
							new CodePrimitiveExpression("onhover")).withParens()),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_INEQUALITY,
						new CodeVariableReferenceExpression(Object.class, "val_1"),
						ScriptExpression.UNDEFINED),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(" onmouseover=\""))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(Object.class, "val_1"))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression('"')))),
				new CodeVariableDeclarationStatement(Object.class, "val_2",
					new CodePropertyReferenceExpression(
						new ScriptVariableReferenceExpression("foo"),
						new CodePrimitiveExpression("onclick")).withParens()),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_INEQUALITY,
						new CodeVariableReferenceExpression(Object.class, "val_2"),
						ScriptExpression.UNDEFINED),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(" onclick=\""))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(Object.class, "val_2"))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression('"')))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(" class=\"foo-bar\" id=\""))),
				new CodeVariableDeclarationStatement(String.class, "id_3",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeThisReferenceExpression(),
						"nextID",
						new CodeVariableReferenceExpression(DuelContext.class, "context"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_3"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\"><p"))),
				new CodeVariableDeclarationStatement(Object.class, "val_4",
					new CodePropertyReferenceExpression(
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("foo"),
							new CodePrimitiveExpression("baz")),
							new CodePrimitiveExpression("onclick")).withParens()),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_INEQUALITY,
						new CodeVariableReferenceExpression(Object.class, "val_4"),
						ScriptExpression.UNDEFINED),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(" onclick=\""))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(Object.class, "val_4"))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression('"')))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression(" id=\""))),
				new CodeVariableDeclarationStatement(String.class, "id_5",
					new CodeMethodInvokeExpression(
						String.class,
						new CodeThisReferenceExpression(),
						"nextID",
						new CodeVariableReferenceExpression(DuelContext.class, "context"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodeVariableReferenceExpression(String.class, "id_5"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("\">Lorem ipsum.</p><script>"))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"writeExtras",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					CodePrimitiveExpression.FALSE)),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "val_4"),
						ScriptExpression.UNDEFINED),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("duel({onclick:function(){return(foo.baz.onclick);}})().toDOM("))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(String.class, "id_5"),
						CodePrimitiveExpression.ONE)),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(",1);")))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("</script></div><script>"))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "val_1"),
						ScriptExpression.UNDEFINED),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("duel({onmouseover:function(){return(foo.bar.onhover);}})().toDOM("))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(String.class, "id_3"),
						CodePrimitiveExpression.ONE)),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(",1);")))),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.IDENTITY_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "val_2"),
						ScriptExpression.UNDEFINED),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression("duel({onclick:function(){return(foo.onclick);}})().toDOM("))),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"dataEncode",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodeVariableReferenceExpression(String.class, "id_3"),
						CodePrimitiveExpression.ONE)),
					new CodeExpressionStatement(new CodeMethodInvokeExpression(
						Void.class,
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(",1);")))),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("</script>")))
			).withOverride().withThrows(IOException.class)
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
					new CodeVariableDeclarationStatement(Object.class, "val_1",
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("foo"),
							new CodePrimitiveExpression("bar")).withParens()),
					new CodeConditionStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.IDENTITY_INEQUALITY,
							new CodeVariableReferenceExpression(Object.class, "val_1"),
							ScriptExpression.UNDEFINED),
						new CodeStatement[] {
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"htmlEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(Object.class, "val_1")))
						},
						new CodeStatement[] {
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("<script id=\""))),
							new CodeVariableDeclarationStatement(String.class, "id_1",
								new CodeMethodInvokeExpression(
									String.class,
									new CodeThisReferenceExpression(),
									"nextID",
									new CodeVariableReferenceExpression(DuelContext.class, "context"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(String.class, "id_1"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("\">"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"writeExtras",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								CodePrimitiveExpression.FALSE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("duel(function(){return(foo.bar);})().toDOM("))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"dataEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(String.class, "id_1"),
								CodePrimitiveExpression.ONE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression(");</script>"))),
						}),
				new CodeExpressionStatement(new CodeMethodInvokeExpression(
					Void.class,
					new CodeThisReferenceExpression(),
					"write",
					new CodeVariableReferenceExpression(DuelContext.class, "context"),
					new CodePrimitiveExpression("</div>")))
			).withOverride().withThrows(IOException.class)
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
					new CodeVariableDeclarationStatement(Object.class, "val_1",
						new CodeMethodInvokeExpression(
							Object.class,
							new CodeThisReferenceExpression(),
							"code_2",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodeVariableReferenceExpression(int.class, "index"),
							new CodeVariableReferenceExpression(int.class, "count"),
							new CodeVariableReferenceExpression(String.class, "key"))),
					new CodeConditionStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.IDENTITY_INEQUALITY,
							new CodeVariableReferenceExpression(Object.class, "val_1"),
							ScriptExpression.UNDEFINED),
						new CodeStatement[] {
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"htmlEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(Object.class, "val_1")))
						},
						new CodeStatement[] {
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("<script id=\""))),
							new CodeVariableDeclarationStatement(String.class, "id_1",
								new CodeMethodInvokeExpression(
									String.class,
									new CodeThisReferenceExpression(),
									"nextID",
									new CodeVariableReferenceExpression(DuelContext.class, "context"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(String.class, "id_1"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("\">"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"writeExtras",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								CodePrimitiveExpression.FALSE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("duel(function(data){foo.bar = (baz+data);})("))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"dataEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(Object.class, "data"),
								CodePrimitiveExpression.ONE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression(").toDOM("))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"dataEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(String.class, "id_1"),
								CodePrimitiveExpression.ONE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression(");</script>"))),
						}),
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
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("foo"),
							new CodePrimitiveExpression("bar")),
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.ADD,
							new ScriptVariableReferenceExpression("baz"),
							new CodeVariableReferenceExpression(Object.class, "data")).withParens())),
				new CodeMethodReturnStatement(ScriptExpression.UNDEFINED)
			)
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}

	@Test
	public void propertyAssignmentTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new StatementNode("data.foo = 42;")));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
				new CodeMethodReturnStatement(ScriptExpression.UNDEFINED)
			)
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}

	@Test
	public void extraRefAssignmentTest() throws IOException {

		VIEWCommandNode input = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new StatementNode("foo = data;")));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
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
					new CodeVariableDeclarationStatement(Object.class, "val_1",
						new CodeMethodInvokeExpression(
							Object.class,
							new CodeThisReferenceExpression(),
							"code_2",
							new CodeVariableReferenceExpression(DuelContext.class, "context"),
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodeVariableReferenceExpression(int.class, "index"),
							new CodeVariableReferenceExpression(int.class, "count"),
							new CodeVariableReferenceExpression(String.class, "key"))),
					new CodeConditionStatement(
						new CodeBinaryOperatorExpression(
							CodeBinaryOperatorType.IDENTITY_INEQUALITY,
							new CodeVariableReferenceExpression(Object.class, "val_1"),
							ScriptExpression.UNDEFINED),
						new CodeStatement[] {
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"htmlEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(Object.class, "val_1")))
						},
						new CodeStatement[] {
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("<script id=\""))),
							new CodeVariableDeclarationStatement(String.class, "id_1",
								new CodeMethodInvokeExpression(
									String.class,
									new CodeThisReferenceExpression(),
									"nextID",
									new CodeVariableReferenceExpression(DuelContext.class, "context"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(String.class, "id_1"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("\">"))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"writeExtras",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								CodePrimitiveExpression.FALSE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression("duel(function(data){foo = data;})("))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"dataEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(Object.class, "data"),
								CodePrimitiveExpression.ONE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression(").toDOM("))),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"dataEncode",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodeVariableReferenceExpression(String.class, "id_1"),
								CodePrimitiveExpression.ONE)),
							new CodeExpressionStatement(new CodeMethodInvokeExpression(
								Void.class,
								new CodeThisReferenceExpression(),
								"write",
								new CodeVariableReferenceExpression(DuelContext.class, "context"),
								new CodePrimitiveExpression(");</script>"))),
						}),
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
				new CodeMethodReturnStatement(ScriptExpression.UNDEFINED)
			)
		);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);
		assertEquals(expected, actual);
	}
}