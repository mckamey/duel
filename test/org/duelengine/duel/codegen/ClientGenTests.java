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

		String expected = "/*global duel */\n\nvar foo;\nif (typeof foo === \"undefined\") {\n\tfoo = {};\n}\nif (typeof foo.bar === \"undefined\") {\n\tfoo.bar = {};\n}\n\nfoo.bar.Blah = duel([\"div\"]);";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = ScrubActual(writer);

	
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

		String expected = "/*global duel */\n\nvar foo = duel(\"A JSON payload should be an object or array, not a string.\");";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = ScrubActual(writer);
		
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

		String expected = "/*global duel */\n\nvar foo = duel(\"\\\\\\b\\f\\n\\r\\t\\u0123\\u4567\\u89AB\\uCDEF\\uABCD\\uEF4A\\\"\");";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = ScrubActual(writer);
		
		assertEquals(expected, actual);
	}

	private String ScrubActual(StringWriter writer) {
		return writer.toString().replaceAll("\r\n", "\n");
	}
}
