package org.duelengine.duel.parsing;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class DuelParserTests {

	@Test
	public void literalSingleTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("This is just literal text."));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.literal("This is just literal text.")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void literalFoldingTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new LiteralNode("This is literal text\nwhich can all be folded."));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.literal("This is literal text"),
			DuelToken.literal("\n"),
			DuelToken.literal("which can all be folded.")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void leadingWhitespaceTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("br"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.literal("\n\n"),
			DuelToken.elemBegin("br")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void trailingWhitespaceTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("br"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("br"),
			DuelToken.literal("\n\n")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemBeginTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemBeginEndTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemAttribTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("class", new LiteralNode("foo"))
				}));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.attrName("class"),
			DuelToken.attrValue("foo")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemNestedTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new ElementNode("span", null,
					new ElementNode("img"))
			));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("span"),
			DuelToken.elemBegin("img"),
			DuelToken.elemEnd("span"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemListTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode(
				"ul",
				new AttributePair[] {
					new AttributePair("class", new LiteralNode("foo"))
				},
				new LiteralNode("\n\t"),
				new ElementNode("li", null,
					new LiteralNode("one")),
				new LiteralNode("\n\t"),
				new ElementNode("li", null,
					new LiteralNode("two")),
				new LiteralNode("\n")));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
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
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemOverlapTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new ElementNode("span")));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("span"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("span")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemAutoBalanceTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new ElementNode("img"),
				new ElementNode("span", null,
					new LiteralNode("plain text"))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("img"),
			DuelToken.elemBegin("span"),
			DuelToken.literal("plain text"),
			DuelToken.elemEnd("ignored"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("span")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void doctypeTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new DocTypeNode("html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""),
			new LiteralNode("\n"),
			new ElementNode("html",
				new AttributePair[] {
					new AttributePair("xmlns", new LiteralNode("http://www.w3.org/1999/xhtml")),
					new AttributePair("xml:lang", new LiteralNode("en"))
				},
				new LiteralNode("\n"),
				new ElementNode("body", null,
					new LiteralNode("Lorem ipsum dolor sit amet")),
				new LiteralNode("\n")));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.block(new BlockValue("<!doctype", ">", "html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"")),
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
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopArrayTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("ul", null,
				new FORCommandNode(
					new AttributePair[] {
						new AttributePair("each", new ExpressionNode("data.items"))
					},
					new LiteralNode("\n\t"),
					new ElementNode("li", null,
						new LiteralNode("item here")),
					new LiteralNode("\n"))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("ul"),
			DuelToken.elemBegin("for"),
			DuelToken.attrName("each"),
			DuelToken.attrValue("data.items"),
			DuelToken.literal("\n\t"),
			DuelToken.elemBegin("li"),
			DuelToken.literal("item here"),
			DuelToken.elemEnd("li"),
			DuelToken.literal("\n"),
			DuelToken.elemEnd("for"),
			DuelToken.elemEnd("ul")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopPropertiesTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("ul", null,
				new FORCommandNode(
					new AttributePair[] {
						new AttributePair("in", new ExpressionNode("data"))
					},
					new LiteralNode("\n\t"),
					new ElementNode("li", null,
						new LiteralNode("key-value here")),
					new LiteralNode("\n"))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("ul"),
			DuelToken.elemBegin("for"),
			DuelToken.attrName("in"),
			DuelToken.attrValue("data"),
			DuelToken.literal("\n\t"),
			DuelToken.elemBegin("li"),
			DuelToken.literal("key-value here"),
			DuelToken.elemEnd("li"),
			DuelToken.literal("\n"),
			DuelToken.elemEnd("for"),
			DuelToken.elemEnd("ul")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopCountTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("ul", null,
				new FORCommandNode(
					new AttributePair[] {
						new AttributePair("count", new ExpressionNode("4")),
						new AttributePair("data", new ExpressionNode("data.name"))
					},
					new LiteralNode("\n\t"),
					new ElementNode("li", null,
						new LiteralNode("item here")),
					new LiteralNode("\n"))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("ul"),
			DuelToken.elemBegin("for"),
			DuelToken.attrName("count"),
			DuelToken.attrValue("4"),
			DuelToken.attrName("data"),
			DuelToken.attrValue("data.name"),
			DuelToken.literal("\n\t"),
			DuelToken.elemBegin("li"),
			DuelToken.literal("item here"),
			DuelToken.elemEnd("li"),
			DuelToken.literal("\n"),
			DuelToken.elemEnd("for"),
			DuelToken.elemEnd("ul")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalBlockTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data == 0"))
						},
						new LiteralNode("\n\tzero\n")),
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data == 1"))
						},
						new LiteralNode("\n\tone\n")),
					new IFCommandNode(null,
						new LiteralNode("\n\tmany\n")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("if"),
			DuelToken.attrName("test"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("\n\tzero\n"),
			DuelToken.elemBegin("else"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 1"),
			DuelToken.literal("\n\tone\n"),
			DuelToken.elemBegin("else"),
			DuelToken.literal("\n\tmany\n"),
			DuelToken.elemEnd("if"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalBlockVoidElseTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data===0"))
						},
						new LiteralNode("zero")),
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data===1"))
						},
						new LiteralNode("one")),
					new IFCommandNode(null,
						new LiteralNode("many")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("if"),
			DuelToken.attrName("test"),
			DuelToken.attrValue("data===0"),
			DuelToken.literal("zero"),
			DuelToken.elemBegin("else"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data===1"),
			DuelToken.elemEnd("else"),
			DuelToken.literal("one"),
			DuelToken.elemBegin("else"),
			DuelToken.elemEnd("else"),
			DuelToken.literal("many"),
			DuelToken.elemEnd("if"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalSinglesTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data == 0"))
						},
						new LiteralNode("\n\tzero\n"))),
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data == 1"))
						},
						new LiteralNode("\n\tone\n"))),
				new XORCommandNode(null,
					new IFCommandNode(null,
						new LiteralNode("\n\tmany\n")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("if"),
			DuelToken.attrName("test"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("\n\tzero\n"),
			DuelToken.elemEnd("if"),
			DuelToken.elemBegin("if"),
			DuelToken.attrName("test"),
			DuelToken.attrValue("data == 1"),
			DuelToken.literal("\n\tone\n"),
			DuelToken.elemEnd("if"),
			DuelToken.elemBegin("if"),
			DuelToken.literal("\n\tmany\n"),
			DuelToken.elemEnd("if"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAliasesTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new XORCommandNode(null,
					new IFCommandNode(
						new AttributePair[] {
							new AttributePair("test", new ExpressionNode("data == 0"))
						},
						new LiteralNode("\n\tzero\n")),
						new IFCommandNode(
							new AttributePair[] {
								new AttributePair("test", new ExpressionNode("data == 1"))
							},
							new LiteralNode("\n\tone\n")),
						new IFCommandNode(null,
							new LiteralNode("\n\tmany\n")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("if"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("\n\tzero\n"),
			DuelToken.elemBegin("else"),
			DuelToken.attrName("test"),
			DuelToken.attrValue("data == 1"),
			DuelToken.literal("\n\tone\n"),
			DuelToken.elemBegin("else"),
			DuelToken.literal("\n\tmany\n"),
			DuelToken.elemEnd("if"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new IFCommandNode(
					new AttributePair[] {
						new AttributePair("test", new ExpressionNode("data == 0"))
					},
					new ElementNode("p", null,
						new LiteralNode("no items found")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("no items found"),
			DuelToken.elemEnd("p"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrUnclosedTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new ElementNode("div", null,
				new IFCommandNode(
					new AttributePair[] {
						new AttributePair("test", new ExpressionNode("data == 0"))
					},
					new ElementNode("p", null,
						new LiteralNode("no items found")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("no items found"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrVoidTagTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new IFCommandNode(
				new AttributePair[] {
					new AttributePair("test", new ExpressionNode("data.showHR"))
				},
				new ElementNode("hr")),
			new LiteralNode("always shown"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("hr"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data.showHR"),
			DuelToken.literal("always shown")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void callTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new CALLCommandNode(
				new AttributePair[] {
					new AttributePair("view", new ExpressionNode("Foo.Other")),
					new AttributePair("data", new ExpressionNode("data.detail")),
					new AttributePair("index", new ExpressionNode("1")),
					new AttributePair("count", new ExpressionNode("42"))
				}));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("call"),
			DuelToken.attrName("view"),
			DuelToken.attrValue("Foo.Other"),
			DuelToken.attrName("data"),
			DuelToken.attrValue("data.detail"),
			DuelToken.attrName("index"),
			DuelToken.attrValue("1"),
			DuelToken.attrName("count"),
			DuelToken.attrValue("42"),
			DuelToken.elemEnd("call")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void callPartTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo"))
			},
			new CALLCommandNode(
				new AttributePair[] {
					new AttributePair("view", new ExpressionNode("Foo.Other")),
					new AttributePair("data", new ExpressionNode("data.detail")),
					new AttributePair("index", new ExpressionNode("1")),
					new AttributePair("count", new ExpressionNode("42"))
				},
				new PARTCommandNode(
					new AttributePair[] {
							new AttributePair("name", new LiteralNode("title-area"))
					},
					new LiteralNode("And this would be the title.")),
				new PARTCommandNode(
					new AttributePair[] {
						new AttributePair("name", new LiteralNode("content-area"))
					},
					new LiteralNode("Content goes here!"))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("call"),
			DuelToken.attrName("view"),
			DuelToken.attrValue("Foo.Other"),
			DuelToken.attrName("data"),
			DuelToken.attrValue("data.detail"),
			DuelToken.attrName("index"),
			DuelToken.attrValue("1"),
			DuelToken.attrName("count"),
			DuelToken.attrValue("42"),
			DuelToken.elemBegin("part"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("title-area"),
			DuelToken.literal("And this would be the title."),
			DuelToken.elemEnd("part"),
			DuelToken.elemBegin("part"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("content-area"),
			DuelToken.literal("Content goes here!"),
			DuelToken.elemEnd("part"),
			DuelToken.elemEnd("call")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewPartTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("Foo.Other"))
			},
			new ElementNode("div",
				new AttributePair[] {
					new AttributePair("class", new LiteralNode("foo"))
				},
				new ElementNode("h3",
					new AttributePair[] {
						new AttributePair("class", new LiteralNode("head"))
					},
					new PARTCommandNode(
						new AttributePair[] {
								new AttributePair("name", new LiteralNode("title-area"))
						},
						new LiteralNode("Placeholder title."))),
				new ElementNode("p",
					new AttributePair[] {
						new AttributePair("class", new LiteralNode("body"))
					},
					new PARTCommandNode(
						new AttributePair[] {
							new AttributePair("name", new LiteralNode("content-area"))
						},
						new LiteralNode("Placeholder content.")))));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("Foo.Other"),
			DuelToken.elemBegin("div"),
			DuelToken.attrName("class"),
			DuelToken.attrValue("foo"),
			DuelToken.elemBegin("h3"),
			DuelToken.attrName("class"),
			DuelToken.attrValue("head"),
			DuelToken.elemBegin("part"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("title-area"),
			DuelToken.literal("Placeholder title."),
			DuelToken.elemEnd("part"),
			DuelToken.elemEnd("h3"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("class"),
			DuelToken.attrValue("body"),
			DuelToken.elemBegin("part"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("content-area"),
			DuelToken.literal("Placeholder content."),
			DuelToken.elemEnd("part"),
			DuelToken.elemEnd("p"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewMetadataUnclosedTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("Foo.Other"))
			},
			new ElementNode("div"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("Foo.Other"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewMetadataCloseTagTest() throws Exception {

		VIEWCommandNode expected = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.myView"))
			},
			new ElementNode("div"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo.myView"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("view")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void muliviewMetadataTest() throws Exception {

		VIEWCommandNode expected1 = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.myView"))
			},
			new ElementNode("div"));

		VIEWCommandNode expected2 = new VIEWCommandNode(
			new AttributePair[] {
				new AttributePair("name", new LiteralNode("foo.myOtherView"))
			},
			new ElementNode("p"));

		Iterable<VIEWCommandNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo.myView"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("view"),
			DuelToken.literal("\n\n"),
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo.myOtherView"),
			DuelToken.elemBegin("p"),
			DuelToken.elemEnd("p"),
			DuelToken.elemEnd("view")
		);

		Iterator<VIEWCommandNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(expected2, iterator.next());
		assertFalse(iterator.hasNext());
	}
}
