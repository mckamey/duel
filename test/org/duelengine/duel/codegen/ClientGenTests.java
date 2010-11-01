package org.duelengine.duel.codegen;

import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class ClientGenTests {

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
			"foo.bar.Blah = duel([\"div\"]);";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

		assertEquals(expected, actual);
	}

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
			"var foo = duel(\"A JSON payload should be an object or array, not a string.\");";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();
		
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
			"var foo = duel(\"\\\\\\b\\f\\n\\r\\t\\u0123\\u4567\\u89AB\\uCDEF\\uABCD\\uEF4A\\\"\");";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();
		
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
			"\t\t\t[\"$if\", { \"test\" : function(model, index, count) { return (model === 0); } },\n"+
			"\t\t\t\t\"zero\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"$if\", { \"test\" : function(model, index, count) { return (model === 1); } },\n"+
			"\t\t\t\t\"one\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"$if\",\n"+
			"\t\t\t\t\"many\"\n"+
			"\t\t\t]\n"+
			"\t\t]\n"+
			"\t]);";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void nestedElementsTest() throws Exception {

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
			"\t]);";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString();

		assertEquals(expected, actual);
	}
}
