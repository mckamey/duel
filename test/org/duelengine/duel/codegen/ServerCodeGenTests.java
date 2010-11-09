package org.duelengine.duel.codegen;

import org.junit.Test;
import static org.junit.Assert.*;

import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeGenTests {

	@Test
	public void stringSimpleTest() throws Exception {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			"com.example",
			"Foo",
			new CodeMethod[] {
				new CodeMethod(
					AccessModifierType.PRIVATE,
					Void.class,
					"bind_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Appendable.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")
								}))
					})
			});

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n\n"+
			"public class Foo extends org.duelengine.duel.View {\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count) {\n"+
			"\t\toutput.append(\"A JSON payload should be an object or array, not a string.\");\n"+
			"\t}\n"+
			"\t\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();
		
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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		System.out.append(actual);

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	//@Test
	public void markupExpressionDataTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new MarkupExpressionNode("data")
			});

		String expected = "";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

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
									new AttributeNode("test", new ExpressionNode("data === 0"))
								},
								new Node[] {
									new LiteralNode("zero")
								}),
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("data === 1"))
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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();

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

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
