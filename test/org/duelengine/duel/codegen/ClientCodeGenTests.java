package org.duelengine.duel.codegen;

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
			new LiteralNode("A JSON payload should be an object or array, not a string."));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\"A JSON payload should be an object or array, not a string.\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void stringEscapeTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new LiteralNode("\\\b\f\n\r\t\u0123\u4567\u89AB\uCDEF\uabcd\uef4A\""));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\"\\\\\\b\\f\\n\\r\\t\\u0123\\u4567\\u89AB\\uCDEF\\uABCD\\uEF4A\\\"\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new ExpressionNode("count"));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function(data, index, count) { return (count); });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void markupExpressionDataTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new MarkupExpressionNode("data"));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function(data) { return duel.raw(data); });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void statementNoneTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new StatementNode("return Math.PI;"));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function() { return Math.PI; });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void statementIndexTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new StatementNode("bar(index);"));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(function(data, index) { bar(index); });\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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
					new IFCommandNode(
						null,
						new LiteralNode("many")))
			));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$xor\",\n"+
			"\t\t\t[\"$if\", { \"test\" : function(data) { return (data === 0); } },\n"+
			"\t\t\t\t\"zero\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"$if\", { \"test\" : function(data) { return (data === 1); } },\n"+
			"\t\t\t\t\"one\"\n"+
			"\t\t\t],\n"+
			"\t\t\t[\"$if\",\n"+
			"\t\t\t\t\"many\"\n"+
			"\t\t\t]\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void loopArrayTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("each", new ExpressionNode("data.items"))
					},
					new LiteralNode("item "),
					new ExpressionNode("index"))
			));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$for\", { \"each\" : function(data) { return (data.items); } },\n"+
			"\t\t\t\"item \",\n"+
			"\t\t\tfunction(data, index) { return (index); }\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void loopPropertiesTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("in", new ExpressionNode("data"))
					},
					new LiteralNode("property "),
					new ExpressionNode("index"))
			));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$for\", { \"in\" : function(data) { return (data); } },\n"+
			"\t\t\t\"property \",\n"+
			"\t\t\tfunction(data, index) { return (index); }\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void loopCountTest() throws Exception {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("count", new ExpressionNode("4")),
						new AttributeNode("data", new ExpressionNode("data"))
					},
					new LiteralNode("item "),
					new ExpressionNode("index"))
			));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\n"+
			"\t[\"div\",\n"+
			"\t\t[\"$for\", {\n\t\t\t\t\"count\" : function() { return (4); },\n\t\t\t\t\"data\" : function(data) { return (data); }\n\t\t\t},\n"+
			"\t\t\t\"item \",\n"+
			"\t\t\tfunction(data, index) { return (index); }\n"+
			"\t\t]\n"+
			"\t]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void attributesTest() throws Exception {

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
					new AttributeNode("class", new LiteralNode("foo"))
				},
				new ElementNode("li", null,
					new LiteralNode("one")),
				new ElementNode("li", null,
					new LiteralNode("two")),
				new ElementNode("li", null,
					new LiteralNode("three"))
				)));

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
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void multiViewTest() throws Exception {
		String expected =
			"/*global duel */\n\n"+
			"var foo = duel(\"First View\");\n\n"+
			"var bar = duel(\"Second View\");\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, 
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo"))
				},
				new LiteralNode("First View")),
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("bar"))
				},
				new LiteralNode("Second View")));
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void namespaceTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new ElementNode("div"));

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
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void namespaceRepeatedTest() throws Exception {
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
		new ClientCodeGen().write(output,
			new ViewRootNode(
					new AttributeNode[] {
						new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
					},
					new LiteralNode("First View")),
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.Yada"))
				},
				new LiteralNode("Second View")));
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void namespacesOverlappingTest() throws Exception {
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
		new ClientCodeGen().write(output,
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.one.Blah"))
				},
				new LiteralNode("First View")),
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.two.Yada"))
				},
				new LiteralNode("Second View")));
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void namespacesDistinctTest() throws Exception {
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
		new ClientCodeGen().write(output,
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
				},
				new LiteralNode("First View")),
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("com.example.Yada"))
				},
				new LiteralNode("Second View")));
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void commentTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new LiteralNode("Hello world."),
			new CommentNode("Comment Here"),
			new LiteralNode("Lorem ipsum."));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel([\"\",\n"+
			"\t\"Hello world.\",\n"+
			"\t[\"!\",\n"+
			"\t\t\"Comment Here\"\n"+
			"\t],\n"+
			"\t\"Lorem ipsum.\"\n"+
			"]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		System.out.println(actual);

		assertEquals(expected, actual);
	}

	@Test
	public void codeCommentTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new LiteralNode("Hello world."),
			new CodeCommentNode("Code Comment Here"),
			new LiteralNode("Lorem ipsum."));

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel([\"\",\n"+
			"\t\"Hello world.\" /*Code Comment Here*/,\n"+
			"\t\"Lorem ipsum.\"\n"+
			"]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		System.out.println(actual);
		
		assertEquals(expected, actual);
	}

	@Test
	public void documentRootTest() throws Exception {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
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

		String expected =
			"/*global duel */\n\n"+
			"var foo = duel([\"\",\n"+
			"\t[\"!doctype\",\n"+
			"\t\t\"html\"\n"+
			"\t],\n"+
			"\t[\"html\",\n"+
			"\t\t[\"head\",\n"+
			"\t\t\t[\"title\",\n"+
			"\t\t\t\t\"The head.\"\n"+
			"\t\t\t]\n"+
			"\t\t],\n"+
			"\t\t[\"body\",\n"+
			"\t\t\t[\"h1\",\n"+
			"\t\t\t\t\"The body.\"\n"+
			"\t\t\t]\n"+
			"\t\t]\n"+
			"\t]\n"+
			"]);\n";

		StringBuilder output = new StringBuilder();
		new ClientCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
