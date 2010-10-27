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

		assertNotNull(actual.getDocType());
	}

	@Test
	public void loopTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("ul"),
				DuelToken.elemBegin("for"),
				DuelToken.attrName("each"),
				DuelToken.attrValue("model.items"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("li"),
				DuelToken.literal("item here"),
				DuelToken.elemEnd("li"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("for"),
				DuelToken.elemEnd("ul")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("ul", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("each", new ExpressionNode("model.items"))
						},
						new Node[] {
							new LiteralNode("\n\t"),
							new ElementNode("li", null,
									new Node[] {
										new LiteralNode("item here")
									}),
							new LiteralNode("\n"),
						})
				})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("if"),
				DuelToken.attrName("test"),
				DuelToken.attrValue("model == 0"),
				DuelToken.literal("\n\tzero\n"),
				DuelToken.elemBegin("else"),
				DuelToken.attrName("if"),
				DuelToken.attrValue("model == 1"),
				DuelToken.literal("\n\tone\n"),
				DuelToken.elemBegin("else"),
				DuelToken.literal("\n\tmany\n"),
				DuelToken.elemEnd("if"),
				DuelToken.elemEnd("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model == 0"))
									},
									new Node[] {
										new LiteralNode("\n\tzero\n")
									}),
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model == 1"))
									},
									new Node[] {
										new LiteralNode("\n\tone\n")
									}),
							new IFCommandNode(
									null,
									new Node[] {
										new LiteralNode("\n\tmany\n")
									}),
						})
				})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockVoidElseTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("if"),
				DuelToken.attrName("test"),
				DuelToken.attrValue("model===0"),
				DuelToken.literal("zero"),
				DuelToken.elemBegin("else"),
				DuelToken.attrName("if"),
				DuelToken.attrValue("model===1"),
				DuelToken.elemEnd("else"),
				DuelToken.literal("one"),
				DuelToken.elemBegin("else"),
				DuelToken.elemEnd("else"),
				DuelToken.literal("many"),
				DuelToken.elemEnd("if"),
				DuelToken.elemEnd("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model===0"))
									},
									new Node[] {
										new LiteralNode("zero")
									}),
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model===1"))
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

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalSinglesTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("if"),
				DuelToken.attrName("test"),
				DuelToken.attrValue("model == 0"),
				DuelToken.literal("\n\tzero\n"),
				DuelToken.elemEnd("if"),
				DuelToken.elemBegin("if"),
				DuelToken.attrName("test"),
				DuelToken.attrValue("model == 1"),
				DuelToken.literal("\n\tone\n"),
				DuelToken.elemEnd("if"),
				DuelToken.elemBegin("if"),
				DuelToken.literal("\n\tmany\n"),
				DuelToken.elemEnd("if"),
				DuelToken.elemEnd("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model == 0"))
									},
									new Node[] {
										new LiteralNode("\n\tzero\n")
									}),
						}),
					new XORCommandNode(null,
							new Node[] {
								new IFCommandNode(
										new AttributeNode[] {
											new AttributeNode("test", new ExpressionNode("model == 1"))
										},
										new Node[] {
											new LiteralNode("\n\tone\n")
										}),
							}),
					new XORCommandNode(null,
							new Node[] {
								new IFCommandNode(
										null,
										new Node[] {
											new LiteralNode("\n\tmany\n")
										}),
							})
				})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalAliasesTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("if"),
				DuelToken.attrName("if"),
				DuelToken.attrValue("model == 0"),
				DuelToken.literal("\n\tzero\n"),
				DuelToken.elemBegin("else"),
				DuelToken.attrName("test"),
				DuelToken.attrValue("model == 1"),
				DuelToken.literal("\n\tone\n"),
				DuelToken.elemBegin("else"),
				DuelToken.literal("\n\tmany\n"),
				DuelToken.elemEnd("if"),
				DuelToken.elemEnd("div")
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model == 0"))
									},
									new Node[] {
										new LiteralNode("\n\tzero\n")
									}),
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("model == 1"))
									},
									new Node[] {
										new LiteralNode("\n\tone\n")
									}),
							new IFCommandNode(
									null,
									new Node[] {
										new LiteralNode("\n\tmany\n")
									}),
						})
				})
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void callTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("call"),
				DuelToken.attrName("view"),
				DuelToken.attrValue("Foo.Other"),
				DuelToken.attrName("model"),
				DuelToken.attrValue("model.detail"),
				DuelToken.attrName("index"),
				DuelToken.attrValue("1"),
				DuelToken.attrName("count"),
				DuelToken.attrValue("42"),
				DuelToken.elemEnd("call"),
			};

		DocumentNode expected = new DocumentNode(new Node[] {
				new CALLCommandNode(
					new AttributeNode[] {
							new AttributeNode("view", new ExpressionNode("Foo.Other")),
							new AttributeNode("model", new ExpressionNode("model.detail")),
							new AttributeNode("index", new ExpressionNode("1")),
							new AttributeNode("count", new ExpressionNode("42"))
					},
					null)
			});

		DocumentNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}
}
