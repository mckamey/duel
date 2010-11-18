package org.duelengine.duel.parsing;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class DuelParserTests {

	@Test
	public void literalSingleTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new LiteralNode("This is just literal text."));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.literal("This is just literal text.")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void literalFoldingTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new LiteralNode("This is literal text\nwhich can all be folded."));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.literal("This is literal text"),
			DuelToken.literal("\n"),
			DuelToken.literal("which can all be folded.")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemBeginTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("div"));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemBeginEndTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("div"));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemAttribTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo"))
					}));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.attrName("class"),
			DuelToken.attrValue("foo")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemNestedTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("div", null,
				new ElementNode("span", null,
					new ElementNode("img"))
			));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("span"),
			DuelToken.elemBegin("img"),
			DuelToken.elemEnd("span"),
			DuelToken.elemEnd("div")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemListTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode(
				"ul",
				new AttributeNode[] {
					new AttributeNode("class", new LiteralNode("foo"))
				},
				new LiteralNode("\n\t"),
				new ElementNode("li", null,
					new LiteralNode("one")),
				new LiteralNode("\n\t"),
				new ElementNode("li", null,
					new LiteralNode("two")),
				new LiteralNode("\n")));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemOverlapTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new ElementNode("span")));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("span"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("span")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemAutoBalanceTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new ElementNode("img"),
					new ElementNode("span", null,
						new LiteralNode("plain text"))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("img"),
			DuelToken.elemBegin("span"),
			DuelToken.literal("plain text"),
			DuelToken.elemEnd("ignored"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("span")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void doctypeTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new DocTypeNode("html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""),
				new LiteralNode("\n"),
				new ElementNode("html",
					new AttributeNode[] {
						new AttributeNode("xmlns", new LiteralNode("http://www.w3.org/1999/xhtml")),
						new AttributeNode("xml:lang", new LiteralNode("en"))
					},
					new LiteralNode("\n"),
					new ElementNode("body", null,
						new LiteralNode("Lorem ipsum dolor sit amet")),
					new LiteralNode("\n")));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopArrayTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("ul", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("each", new ExpressionNode("data.items"))
					},
					new LiteralNode("\n\t"),
					new ElementNode("li", null,
						new LiteralNode("item here")),
					new LiteralNode("\n"))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopPropertiesTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("ul", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("in", new ExpressionNode("data"))
					},
					new LiteralNode("\n\t"),
					new ElementNode("li", null,
						new LiteralNode("key-value here")),
					new LiteralNode("\n"))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopCountTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("ul", null,
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("count", new ExpressionNode("4")),
						new AttributeNode("data", new ExpressionNode("data.name"))
					},
					new LiteralNode("\n\t"),
					new ElementNode("li", null,
						new LiteralNode("item here")),
					new LiteralNode("\n"))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalBlockTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new XORCommandNode(null,
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 0"))
							},
							new LiteralNode("\n\tzero\n")),
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 1"))
							},
							new LiteralNode("\n\tone\n")),
						new IFCommandNode(null,
							new LiteralNode("\n\tmany\n")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalBlockVoidElseTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new XORCommandNode(null,
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data===0"))
							},
							new LiteralNode("zero")),
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data===1"))
							},
							new LiteralNode("one")),
						new IFCommandNode(null,
							new LiteralNode("many")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalSinglesTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new XORCommandNode(null,
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 0"))
							},
							new LiteralNode("\n\tzero\n"))),
					new XORCommandNode(null,
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 1"))
							},
							new LiteralNode("\n\tone\n"))),
					new XORCommandNode(null,
						new IFCommandNode(null,
							new LiteralNode("\n\tmany\n")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAliasesTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new XORCommandNode(null,
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 0"))
							},
							new LiteralNode("\n\tzero\n")),
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("data == 1"))
								},
								new LiteralNode("\n\tone\n")),
							new IFCommandNode(null,
								new LiteralNode("\n\tmany\n")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new ElementNode("div", null,
					new IFCommandNode(
						new AttributeNode[] {
							new AttributeNode("test", new ExpressionNode("data == 0"))
						},
						new ElementNode("p", null,
							new LiteralNode("no items found")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("no items found"),
			DuelToken.elemEnd("p"),
			DuelToken.elemEnd("div")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrUnclosedTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new ElementNode("div", null,
				new IFCommandNode(
					new AttributeNode[] {
						new AttributeNode("test", new ExpressionNode("data == 0"))
					},
					new ElementNode("p", null,
						new LiteralNode("no items found")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("no items found"),
			DuelToken.elemEnd("div")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrVoidTagTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(null,
			new IFCommandNode(
				new AttributeNode[] {
					new AttributeNode("test", new ExpressionNode("data.showHR"))
				},
				new ElementNode("hr")),
			new LiteralNode("always shown"));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("hr"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data.showHR"),
			DuelToken.literal("always shown")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void callTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new CALLCommandNode(
					new AttributeNode[] {
						new AttributeNode("view", new ExpressionNode("Foo.Other")),
						new AttributeNode("data", new ExpressionNode("data.detail")),
						new AttributeNode("index", new ExpressionNode("1")),
						new AttributeNode("count", new ExpressionNode("42"))
					}));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void callPartTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(null,
				new CALLCommandNode(
					new AttributeNode[] {
						new AttributeNode("view", new ExpressionNode("Foo.Other")),
						new AttributeNode("data", new ExpressionNode("data.detail")),
						new AttributeNode("index", new ExpressionNode("1")),
						new AttributeNode("count", new ExpressionNode("42"))
					},
					new PARTCommandNode(
						new AttributeNode[] {
								new AttributeNode("name", new LiteralNode("title-area"))
						},
						new LiteralNode("And this would be the title.")),
					new PARTCommandNode(
						new AttributeNode[] {
							new AttributeNode("name", new LiteralNode("content-area"))
						},
						new LiteralNode("Content goes here!"))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewPartTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("Foo.Other"))
			},
			new ElementNode("div",
				new AttributeNode[] {
					new AttributeNode("class", new LiteralNode("foo"))
				},
				new ElementNode("h3",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("head"))
					},
					new PARTCommandNode(
						new AttributeNode[] {
								new AttributeNode("name", new LiteralNode("title-area"))
						},
						new LiteralNode("Placeholder title."))),
				new ElementNode("p",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("body"))
					},
					new PARTCommandNode(
						new AttributeNode[] {
							new AttributeNode("name", new LiteralNode("content-area"))
						},
						new LiteralNode("Placeholder content.")))));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewMetadataUnclosedTest() throws Exception {

		ViewRootNode expected =
			new ViewRootNode(
				new AttributeNode[] {
					new AttributeNode("name", new LiteralNode("Foo.Other"))
				},
				new ElementNode("div"));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("Foo.Other"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewMetadataCloseTagTest() throws Exception {

		ViewRootNode expected = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.myView"))
			},
			new ElementNode("div"));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo.myView"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("view")
		);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void muliviewMetadataTest() throws Exception {

		ViewRootNode expected1 = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.myView"))
			},
			new ElementNode("div"));

		ViewRootNode expected2 = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.myOtherView"))
			},
			new ElementNode("p"));

		Iterable<ViewRootNode> actual = new DuelParser().parse(
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

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(expected2, iterator.next());
		assertFalse(iterator.hasNext());
	}
}
