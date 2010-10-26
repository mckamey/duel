package org.duelengine.duel.parsing;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class DuelParserTests {

	@Test
	public void literalSingleTest() throws Exception {

		DuelToken[] input = {
				DuelToken.literal("This is just literal text.")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new LiteralNode("This is just literal text.")
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void literalFoldingTest() throws Exception {

		DuelToken[] input = {
				DuelToken.literal("This is literal text"),
				DuelToken.literal("\n"),
				DuelToken.literal("which can all be folded.")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new LiteralNode("This is literal text\nwhich can all be folded.")
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemBeginTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div")
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemBeginEndTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemEnd("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div")
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemAttribTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("foo")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo"))
					})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemNestedTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("span"),
				DuelToken.elemBegin("img"),
				DuelToken.elemEnd("span"),
				DuelToken.elemEnd("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
						new ElementNode("span", null, new Node[] {
								new ElementNode("img")
							})
					})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemListTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("ul"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("foo"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("li"),
				DuelToken.literal("one"),
				DuelToken.elemEnd("li"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("li"),
				DuelToken.literal("two"),
				DuelToken.elemEnd("li"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("ul")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode(
					"ul",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo"))
					},
					new Node[] {
						new LiteralNode("\n\t"),
						new ElementNode("li", null, new Node[] {
								new LiteralNode("one")
							}),
						new LiteralNode("\n\t"),
						new ElementNode("li", null, new Node[] {
								new LiteralNode("two")
							}),
						new LiteralNode("\n")
					})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemOverlapTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("span"),
				DuelToken.elemEnd("div"),
				DuelToken.elemEnd("span")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
						new ElementNode("span")
					})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemAutoBalanceTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("img"),
				DuelToken.elemBegin("span"),
				DuelToken.literal("plain text"),
				DuelToken.elemEnd("ignored"),
				DuelToken.elemEnd("div"),
				DuelToken.elemEnd("span")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
						new ElementNode("img"),
						new ElementNode("span", null, new Node[] {
								new LiteralNode("plain text")
							})
					})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void doctypeTest() throws Exception {

		DuelToken[] input = {
				DuelToken.block(new BlockValue("<!doctype", ">", " html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"")),
				DuelToken.literal("\n"),
				DuelToken.elemBegin("html"),
				DuelToken.attrName("xmlns"),
				DuelToken.attrValue("http://www.w3.org/1999/xhtml"),
				DuelToken.attrName("xml:lang"),
				DuelToken.attrValue("en"),
				DuelToken.literal("\n"),
				DuelToken.elemBegin("body"),
				DuelToken.literal("Lorem ipsum dolor sit amet"),
				DuelToken.elemEnd("body"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("html")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new DocTypeNode(" html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""),
				new LiteralNode("\n"),
				new ElementNode("html",
					new AttributeNode[] {
						new AttributeNode("xmlns", new LiteralNode("http://www.w3.org/1999/xhtml")),
						new AttributeNode("xml:lang", new LiteralNode("en"))
					},
					new Node[] {
						new LiteralNode("\n"),
						new ElementNode("body", null, new Node[] {
							new LiteralNode("Lorem ipsum dolor sit amet")
						}),
						new LiteralNode("\n")
					})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}
}
