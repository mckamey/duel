package org.duelengine.duel.codegen;

import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class ClientCodeGenTests {

	@Test
	public void stringSimpleTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("A JSON payload should be an object or array, not a string.")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\"A JSON payload should be an object or array, not a string.\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void stringEscapeTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("\\\b\f\n\r\t\u0123\u4567\u89AB\uCDEF\uabcd\uef4A\"")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\"\\\\\\b\\f\\n\\r\\t\\u0123\\u4567\\u89AB\\uCDEF\\uABCD\\uEF4A\\\"\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ExpressionNode("count")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function(model, index, count) { return (count); });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void markupExpressionModelTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new MarkupExpressionNode("model")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function(model) { return duel.raw(model); });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void statementNoneTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new StatementNode("return Math.PI;")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function() { return Math.PI; });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void statementIndexTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new StatementNode("bar(index);")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function(model, index) { bar(index); });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
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

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$xor\",\n"+
			"\t\t\t[\"$if\", { \"test\" : function(model) { return (model === 0); } },\n"+
			"\t\t\t\t\"zero\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"$if\", { \"test\" : function(model) { return (model === 1); } },\n"+
			"\t\t\t\t\"one\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"$if\",\n"+
			"\t\t\t\t\"many\"\n"+
			"\t\t\t]\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void loopArrayTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("each", new ExpressionNode("model.items"))
						},
						new Node[] {
							new LiteralNode("item "),
							new ExpressionNode("index")
						})
				})
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$for\", { \"each\" : function(model) { return (model.items); } },\n"+
			"\t\t\t\"item \",\n"+
			"\t\t\tfunction(model, index) { return (index); }\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void loopPropertiesTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("in", new ExpressionNode("model"))
						},
						new Node[] {
							new LiteralNode("property "),
							new ExpressionNode("index")
						})
				})
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$for\", { \"in\" : function(model) { return (model); } },\n"+
			"\t\t\t\"property \",\n"+
			"\t\t\tfunction(model, index) { return (index); }\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void loopCountTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("count", new ExpressionNode("4")),
							new AttributeNode("model", new ExpressionNode("model"))
						},
						new Node[] {
							new LiteralNode("item "),
							new ExpressionNode("index")
						})
				})
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$for\", {\n\t\t\t\t\"count\" : function() { return (4); },\n\t\t\t\t\"model\" : function(model) { return (model); }\n\t\t\t},\n"+
			"\t\t\t\"item \",\n"+
			"\t\t\tfunction(model, index) { return (index); }\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
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

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\", {\n"+
			"\t\t\t\"class\" : \"foo\",\n"+
			"\t\t\t\"style\" : \"color:red\"\n"+
			"\t\t},\n"+
			"\t\t[\"ul\", { \"class\" : \"foo\" },\n"+
			"\t\t\t[\"li\",\n"+
			"\t\t\t\t\"one\"\n\t\t\t],\n"+
			"\t\t\t[\"li\",\n"+
			"\t\t\t\t\"two\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"li\",\n"+
			"\t\t\t\t\"three\"\n"+
			"\t\t\t]\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
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

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\"First View\");\n\n"+
			"var bar = duel(\"Second View\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void namespaceTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new Node[] {
				new ElementNode("div")
			});

		String expected =
			"/*global duel */\n\n"+
			"var foo;\n"+
			"if (typeof foo === \"undefined\") {\n"+
			"\tfoo = {};\n"+
			"}\n"+
			"if (typeof foo.bar === \"undefined\") {\n"+
			"\tfoo.bar = {};\n"+
			"}\n\n"+
			"foo.bar.Blah = duel([\"div\"]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, new ViewRootNode[] { input });
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
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

		String expected =
			"/*global duel */\n\n"+
			"var foo;\n"+
			"if (typeof foo === \"undefined\") {\n"+
			"\tfoo = {};\n"+
			"}\n"+
			"if (typeof foo.bar === \"undefined\") {\n"+
			"\tfoo.bar = {};\n"+
			"}\n\n"+
			"foo.bar.Blah = duel(\"First View\");\n\n"+
			"foo.bar.Yada = duel(\"Second View\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
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

		String expected =
			"/*global duel */\n\n"+
			"var foo;\n"+
			"if (typeof foo === \"undefined\") {\n"+
			"\tfoo = {};\n"+
			"}\n"+
			"if (typeof foo.bar === \"undefined\") {\n"+
			"\tfoo.bar = {};\n"+
			"}\n"+
			"if (typeof foo.bar.one === \"undefined\") {\n"+
			"\tfoo.bar.one = {};\n"+
			"}\n\n"+
			"foo.bar.one.Blah = duel(\"First View\");\n\n"+
			"if (typeof foo.bar.two === \"undefined\") {\n"+
			"\tfoo.bar.two = {};\n"+
			"}\n\n"+
			"foo.bar.two.Yada = duel(\"Second View\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
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

		String expected =
			"/*global duel */\n\n"+
			"var foo;\n"+
			"if (typeof foo === \"undefined\") {\n"+
			"\tfoo = {};\n"+
			"}\n"+
			"if (typeof foo.bar === \"undefined\") {\n"+
			"\tfoo.bar = {};\n"+
			"}\n\n"+
			"foo.bar.Blah = duel(\"First View\");\n\n"+
			"var com;\n"+
			"if (typeof com === \"undefined\") {\n"+
			"\tcom = {};\n"+
			"}\n"+
			"if (typeof com.example === \"undefined\") {\n"+
			"\tcom.example = {};\n"+
			"}\n\n"+
			"com.example.Yada = duel(\"Second View\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
