package org.duelengine.duel.codegen;

import java.io.*;

import org.junit.Test;
import static org.junit.Assert.*;

import org.duelengine.duel.ast.*;

public class ServerCodeGenTests {

	@Test
	public void stringSimpleTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("A JSON payload should be an object or array, not a string.")
			});

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();
		
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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();
		
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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

		assertEquals(expected, actual);
	}

	//@Test
	public void multiViewTest() throws Exception {
		ViewRootNode[] input = new ViewRootNode[] { 
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo"))
				},
				new Node[] {
					new LiteralNode("First View")
				}),
			new ViewRootNode(
					new AttributeNode[] {
						new AttributeNode("name", new LiteralNode("bar"))
					},
					new Node[] {
						new LiteralNode("Second View")
					})
		};

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, input);
		String actual = writer.toString();

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

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

		assertEquals(expected, actual);
	}

	//@Test
	public void namespaceRepeatedTest() throws Exception {
		ViewRootNode[] input = new ViewRootNode[] { 
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
				},
				new Node[] {
					new LiteralNode("First View")
				}),
			new ViewRootNode(
					new AttributeNode[] {
						new AttributeNode("name", new LiteralNode("foo.bar.Yada"))
					},
					new Node[] {
						new LiteralNode("Second View")
					})
		};

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, input);
		String actual = writer.toString();

		assertEquals(expected, actual);
	}

	//@Test
	public void namespacesOverlappingTest() throws Exception {
		ViewRootNode[] input = new ViewRootNode[] { 
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.one.Blah"))
				},
				new Node[] {
					new LiteralNode("First View")
				}),
			new ViewRootNode(
					new AttributeNode[] {
						new AttributeNode("name", new LiteralNode("foo.bar.two.Yada"))
					},
					new Node[] {
						new LiteralNode("Second View")
					})
		};

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, input);
		String actual = writer.toString();

		assertEquals(expected, actual);
	}

	//@Test
	public void namespacesDistinctTest() throws Exception {
		ViewRootNode[] input = new ViewRootNode[] { 
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
				},
				new Node[] {
					new LiteralNode("First View")
				}),
			new ViewRootNode(
					new AttributeNode[] {
						new AttributeNode("name", new LiteralNode("com.example.Yada"))
					},
					new Node[] {
						new LiteralNode("Second View")
					})
		};

		String expected = "";

		StringWriter writer = new StringWriter();
		new ServerCodeGen().write(writer, input);
		String actual = writer.toString();

		assertEquals(expected, actual);
	}
}
