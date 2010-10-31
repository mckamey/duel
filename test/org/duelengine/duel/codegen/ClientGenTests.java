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

		String expected = "/*global duel */\n\n/* namespace */\nvar foo;\nif (typeof foo === \"undefined\") {\n\tfoo = {};\n}\nif (typeof foo.bar === \"undefined\") {\n\tfoo.bar = {};\n}\n\nfoo.bar.Blah = duel([\"div\"]);";

		StringWriter writer = new StringWriter();
		new ClientGen().write(writer, new ViewRootNode[] { input });
		String actual = writer.toString().replaceAll("\r\n", "\n");

		System.out.println(actual);
		
		assertEquals(expected, actual);
	}
}
