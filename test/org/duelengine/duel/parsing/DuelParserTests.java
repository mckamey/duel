package org.duelengine.duel.parsing;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class DuelParserTests {

	@Test
	public void literalSingleTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.literal("This is just literal text.")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
				new LiteralNode("This is just literal text.")
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void literalFoldingTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.literal("This is literal text"),
			DuelToken.literal("\n"),
			DuelToken.literal("which can all be folded.")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new LiteralNode("This is literal text\nwhich can all be folded.")
		});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemBeginTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div")
		});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemBeginEndTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div")
		});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemAttribTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("view"),
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("foo")
			};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", new AttributeNode[] {
				new AttributeNode("class", new LiteralNode("foo"))
			})
		});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemNestedTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("span"),
			DuelToken.elemBegin("img"),
			DuelToken.elemEnd("span"),
			DuelToken.elemEnd("div")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null, new Node[] {
				new ElementNode("span", null, new Node[] {
						new ElementNode("img")
				})
			})
		});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemListTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
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

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemOverlapTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("span"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("span")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null, new Node[] {
					new ElementNode("span")
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void elemAutoBalanceTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("img"),
			DuelToken.elemBegin("span"),
			DuelToken.literal("plain text"),
			DuelToken.elemEnd("ignored"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("span")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null,
				new Node[] {
					new ElementNode("img"),
					new ElementNode("span", null, new Node[] {
							new LiteralNode("plain text")
					})
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void doctypeTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
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

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
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

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopArrayTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("ul", null, new Node[] {
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("each", new ExpressionNode("data.items"))
					},
					new Node[] {
						new LiteralNode("\n\t"),
						new ElementNode("li",
							null,
							new Node[] {
								new LiteralNode("item here")
							}),
						new LiteralNode("\n"),
					})
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopPropertiesTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("ul", null, new Node[] {
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("in", new ExpressionNode("data"))
					},
					new Node[] {
						new LiteralNode("\n\t"),
						new ElementNode("li",
							null,
							new Node[] {
								new LiteralNode("key-value here")
							}),
						new LiteralNode("\n"),
					})
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void loopCountTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("ul", null, new Node[] {
				new FORCommandNode(
					new AttributeNode[] {
						new AttributeNode("count", new ExpressionNode("4")),
						new AttributeNode("data", new ExpressionNode("data.name"))
					},
					new Node[] {
						new LiteralNode("\n\t"),
						new ElementNode("li",
							null,
							new Node[] {
								new LiteralNode("item here")
							}),
						new LiteralNode("\n"),
					})
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalBlockTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("data == 0"))
									},
									new Node[] {
										new LiteralNode("\n\tzero\n")
									}),
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("data == 1"))
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

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalBlockVoidElseTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null, new Node[] {
				new XORCommandNode(null,
					new Node[] {
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data===0"))
							},
							new Node[] {
								new LiteralNode("zero")
							}),
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data===1"))
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

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalSinglesTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null, new Node[] {
				new XORCommandNode(null,
					new Node[] {
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 0"))
							},
							new Node[] {
								new LiteralNode("\n\tzero\n")
							}),
					}),
				new XORCommandNode(null,
					new Node[] {
						new IFCommandNode(
							new AttributeNode[] {
								new AttributeNode("test", new ExpressionNode("data == 1"))
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

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAliasesTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("data == 0"))
									},
									new Node[] {
										new LiteralNode("\n\tzero\n")
									}),
							new IFCommandNode(
									new AttributeNode[] {
										new AttributeNode("test", new ExpressionNode("data == 1"))
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

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("no items found"),
			DuelToken.elemEnd("p"),
			DuelToken.elemEnd("div")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null, new Node[] {
				new IFCommandNode(
					new AttributeNode[] {
						new AttributeNode("test", new ExpressionNode("data == 0"))
					},
					new Node[] {
						new ElementNode("p", null, new Node[] {
							new LiteralNode("no items found")
						})
					}),
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrUnclosedTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("div"),
			DuelToken.elemBegin("p"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data == 0"),
			DuelToken.literal("no items found"),
			DuelToken.elemEnd("div")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new ElementNode("div", null, new Node[] {
				new IFCommandNode(
					new AttributeNode[] {
						new AttributeNode("test", new ExpressionNode("data == 0"))
					},
					new Node[] {
						new ElementNode("p", null, new Node[] {
							new LiteralNode("no items found")
						})
					}),
				})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void conditionalAttrVoidTagTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.elemBegin("hr"),
			DuelToken.attrName("if"),
			DuelToken.attrValue("data.showHR"),
			DuelToken.literal("always shown")
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new IFCommandNode(
				new AttributeNode[] {
					new AttributeNode("test", new ExpressionNode("data.showHR"))
				},
				new Node[] {
					new ElementNode("hr")
				}),
			new LiteralNode("always shown")
		});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void callTest() throws Exception {

		DuelToken[] input = {
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
			DuelToken.elemEnd("call"),
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new CALLCommandNode(
				new AttributeNode[] {
					new AttributeNode("view", new ExpressionNode("Foo.Other")),
					new AttributeNode("data", new ExpressionNode("data.detail")),
					new AttributeNode("index", new ExpressionNode("1")),
					new AttributeNode("count", new ExpressionNode("42"))
				},
				null)
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void callPartTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(null, new Node[] {
			new CALLCommandNode(
				new AttributeNode[] {
					new AttributeNode("view", new ExpressionNode("Foo.Other")),
					new AttributeNode("data", new ExpressionNode("data.detail")),
					new AttributeNode("index", new ExpressionNode("1")),
					new AttributeNode("count", new ExpressionNode("42"))
				},
				new Node[] {
					new PARTCommandNode(
						new AttributeNode[] {
								new AttributeNode("name", new LiteralNode("title-area"))
						},
						new Node[] {
							new LiteralNode("And this would be the title.")
						}),
					new PARTCommandNode(
						new AttributeNode[] {
							new AttributeNode("name", new LiteralNode("content-area"))
						},
						new Node[] {
							new LiteralNode("Content goes here!")
						}),
					})
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewPartTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("Foo.Other"))
			},
			new Node[] {
				new ElementNode("div",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo"))
					},
					new Node[] {
						new ElementNode("h3",
							new AttributeNode[] {
								new AttributeNode("class", new LiteralNode("head"))
							},
							new Node[] {
							new PARTCommandNode(
								new AttributeNode[] {
										new AttributeNode("name", new LiteralNode("title-area"))
								},
								new Node[] {
									new LiteralNode("Placeholder title.")
								}),
							}),
						new ElementNode("p",
							new AttributeNode[] {
								new AttributeNode("class", new LiteralNode("body"))
							},
							new Node[] {
								new PARTCommandNode(
									new AttributeNode[] {
										new AttributeNode("name", new LiteralNode("content-area"))
									},
									new Node[] {
										new LiteralNode("Placeholder content.")
								}),
							})
					})
				});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewMetadataUnclosedTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("Foo.Other"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div")
		};

		ViewRootNode expected = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("Foo.Other"))
			},
			new Node[] {
				new ElementNode("div")
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void viewMetadataCloseTagTest() throws Exception {

		DuelToken[] input = {
			DuelToken.elemBegin("view"),
			DuelToken.attrName("name"),
			DuelToken.attrValue("foo.myView"),
			DuelToken.elemBegin("div"),
			DuelToken.elemEnd("div"),
			DuelToken.elemEnd("view")
		};

		ViewRootNode expected = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.myView"))
			},
			new Node[] {
				new ElementNode("div")
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void muliviewMetadataTest() throws Exception {

		DuelToken[] input = {
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
		};

		ViewRootNode expected1 = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.myView"))
			},
			new Node[] {
				new ElementNode("div")
			});

		ViewRootNode expected2 = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.myOtherView"))
			},
			new Node[] {
				new ElementNode("p")
			});

		Iterable<ViewRootNode> actual = new DuelParser().parse(input);

		Iterator<ViewRootNode> iterator = actual.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(expected1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(expected2, iterator.next());
		assertFalse(iterator.hasNext());
	}
}
