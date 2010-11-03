package org.duelengine.duel.codegen;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeBuilderTests {

	@Test
	public void stringSimpleTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("A JSON payload should be an object or array, not a string.")
			});

		CodeType expected = new CodeType(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(new CodeStatement[] {
					new CodeEmitLiteralStatement("A JSON payload should be an object or array, not a string.")
				})
			});

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void stringEscapeTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("\\\b\f\n\r\t\u0123\u4567\u89AB\uCDEF\uabcd\uef4A\"")
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);
		
		assertEquals(expected, actual);
	}

	//@Test
	public void expressionCountTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ExpressionNode("count")
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void markupExpressionModelTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new MarkupExpressionNode("model")
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void statementNoneTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new StatementNode("bar();")
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void statementIndexTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new StatementNode("bar(index);")
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void conditionalBlockTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("model === 0"))
								},
								new Node[] {
									new LiteralNode("zero")
								}),
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("model === 1"))
								},
								new Node[] {
									new LiteralNode("one")
								}),
							new IFCommandNode(
								null,
								new Node[] {
									new LiteralNode("many")
								}),
						})
				})
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void attributesTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ElementNode("div",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo")),
						new AttributeNode("style", new LiteralNode("color:red"))
					},
					new Node[] {
					new ElementNode("ul",
						new AttributeNode[] {
							new AttributeNode("class", new LiteralNode("foo"))
						},
						new Node[] {
							new ElementNode("li",
									null,
									new Node[] {
										new LiteralNode("one")
									}),
							new ElementNode("li",
									null,
									new Node[] {
										new LiteralNode("two")
									}),
							new ElementNode("li",
									null,
									new Node[] {
										new LiteralNode("three")
									}),
						})
				})
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}

	//@Test
	public void namespaceTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new Node[] {
				new ElementNode("div")
			});

		CodeType expected = null;

		CodeType actual = new ServerCodeBuilder().build(input);

		assertEquals(expected, actual);
	}
}
