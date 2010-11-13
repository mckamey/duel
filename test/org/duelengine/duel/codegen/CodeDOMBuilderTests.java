package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class CodeDOMBuilderTests {

	@Test
	public void stringSimpleTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")))
				)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void stringEscapeTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("\\&#x0008;&#x000C;\n\r\t&#x0123;&#x4567;&#x89AB;&#xCDEF;&#xABCD;&#xEF4A;\"")))
				)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodeVariableReferenceExpression(int.class, "count")))
				)
			);

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void expressionDataTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data")))
				)
			);

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void markupExpressionDataTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodeVariableReferenceExpression(Object.class, "data")))
				)
			);

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void statementNoneTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"htmlEncode",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodePropertyReferenceExpression(
							new ScriptVariableReferenceExpression("Math"),
							new CodePrimitiveExpression("PI"))))
				)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example.foo"))
			},
			new ElementNode("div", null,
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributeNode[] {
							new AttributeNode("test", new ExpressionNode("data === 0"))
						},
						new LiteralNode("zero")),
					new IFCommandNode(
						new AttributeNode[] {
							new AttributeNode("test", new ExpressionNode("data === 1"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
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
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										"append",
										new CodePrimitiveExpression("one")))
							},
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										"append",
										new CodePrimitiveExpression("many")))
							})
					}),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
				)
			);

		// flag the conditions as having had parens
		((CodeConditionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(1)).getCondition().setHasParens(true);
		((CodeConditionStatement)((CodeConditionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(1)).getFalseStatements().getLastStatement()).getCondition().setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalSinglesTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example.foo2"))
			},
			new ElementNode("div", null,
				new IFCommandNode(
					new AttributeNode[] {
						new AttributeNode("test", new StatementNode("return data == 0;"))
					},
					new LiteralNode("zero")),
				new IFCommandNode(
					new AttributeNode[] {
						new AttributeNode("test", new StatementNode("return data == 1;"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodePrimitiveExpression("zero")))
					},
					null),
				new CodeConditionStatement(
					new CodeBinaryOperatorExpression(
						CodeBinaryOperatorType.VALUE_EQUALITY,
						new CodeVariableReferenceExpression(Object.class, "data"),
						new CodePrimitiveExpression(1.0)),
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodePrimitiveExpression("one")))
					},
					null),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("many"))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
				)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationArrayTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("each", new ExpressionNode("data.items"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
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
							new CodeVariableReferenceExpression(Appendable.class, "output"),
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
								"next"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index")))
			));

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationObjectTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("in", new ExpressionNode("data.foo"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
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
							new CodeVariableReferenceExpression(Appendable.class, "output"),
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
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index")))
				));

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationCountTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("count", new ExpressionNode("4")),
						new AttributeNode("data", new ExpressionNode("data.name")),
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
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
							new CodeVariableReferenceExpression(Appendable.class, "output"),
							new CodeVariableReferenceExpression(Object.class, "data_1"),
							new CodeVariableReferenceExpression(int.class, "index_2"),
							new CodeVariableReferenceExpression(int.class, "count_3"),
							CodePrimitiveExpression.NULL))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("</div>")))
			),
			new CodeMethod(
				AccessModifierType.PRIVATE,
				Void.class,
				"render_2",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("item "))),
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeThisReferenceExpression(),
						"write",
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodeVariableReferenceExpression(int.class, "index")))
			));

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void attributesTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new ElementNode("div",
				new AttributeNode[] {
					new AttributeNode("class", new LiteralNode("foo")),
					new AttributeNode("style", new LiteralNode("color:red"))
				},
				new ElementNode("ul",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("bar"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("<div class=\"foo\" style=\"color:red\"><ul class=\"bar\"><li>one</li><li>two</li><li>three</li></ul></div>")))
				)
			);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void namespaceTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
					new CodeParameterDeclarationExpression(Object.class, "data"),
					new CodeParameterDeclarationExpression(int.class, "index"),
					new CodeParameterDeclarationExpression(int.class, "count"),
					new CodeParameterDeclarationExpression(String.class, "key")
				},
				new CodeExpressionStatement(
					new CodeMethodInvokeExpression(
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						"append",
						new CodePrimitiveExpression("<div></div>")))
				));

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void callViewTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new CALLCommandNode(
				new AttributeNode[] {
					new AttributeNode("view", new LiteralNode("foo.bar.Yada")),
					new AttributeNode("data", new ExpressionNode("data.foo"))
				}));

		CodeTypeDeclaration expected = CodeDOMUtility.createViewType(
			"foo.bar",
			"Blah",
			new CodeMethod(
				AccessModifierType.PROTECTED,
				Void.class,
				"render",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
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
						new CodeVariableReferenceExpression(Appendable.class, "output"),
						new CodePropertyReferenceExpression(
							new CodeVariableReferenceExpression(Object.class, "data"),
							new CodePrimitiveExpression("foo")),
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
						new CodeObjectCreateExpression(
							"foo.bar.Yada",
							new CodeThisReferenceExpression())))
				));

		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);
		((CodeMethod)expected.getMembers().get(5)).setOverride(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}

	@Test
	public void callWrapperTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new CALLCommandNode(
				new AttributeNode[] {
					new AttributeNode("view", new LiteralNode("foo.bar.Yada")),
					new AttributeNode("data", new ExpressionNode("data"))
				},
				new PARTCommandNode(
					new AttributeNode[] {
						new AttributeNode("name", new LiteralNode("header"))
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
					new CodeParameterDeclarationExpression(Appendable.class, "output"),
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
						new CodeVariableReferenceExpression(Appendable.class, "output"),
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
						new CodeParameterDeclarationExpression(Appendable.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeExpressionStatement(
						new CodeMethodInvokeExpression(
							new CodeVariableReferenceExpression(Appendable.class, "output"),
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
							new CodeObjectCreateExpression(
								"part_3",
								new CodeThisReferenceExpression()))))
				));

		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);
		((CodeMethod)((CodeTypeDeclaration)expected.getMembers().get(5)).getMembers().get(1)).setOverride(true);
		((CodeMethod)expected.getMembers().get(6)).setOverride(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().buildView(input);

		assertEquals(expected, actual);
	}
}
