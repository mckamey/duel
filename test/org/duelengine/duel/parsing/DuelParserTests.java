package org.duelengine.duel.parsing;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class DuelParserTests {

	@Test
	public void literalSingleTest() throws Exception {

		DuelToken[] input = {
				DuelToken.literal("This is just literal text.")
			};

		ContainerNode expected = new ContainerNode (new Node[] {
				new LiteralNode("This is just literal text.")
			});

		ContainerNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void literalFoldingTest() throws Exception {

		DuelToken[] input = {
				DuelToken.literal("This is literal text"),
				DuelToken.literal("\n"),
				DuelToken.literal("which can all be folded.")
			};

		ContainerNode expected = new ContainerNode (new Node[] {
				new LiteralNode("This is literal text\nwhich can all be folded.")
			});

		ContainerNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemBeginTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div")
			};

		ContainerNode expected = new ContainerNode (new Node[] {
				new ElementNode("div")
			});

		ContainerNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemBeginEndTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemEnd("div")
			};

		ContainerNode expected = new ContainerNode (new Node[] {
				new ElementNode("div")
			});

		ContainerNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemAttribTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("foo")
			};

		ContainerNode expected = new ContainerNode (new Node[] {
				new ElementNode("div", new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo"))
					})
			});

		ContainerNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}

	@Test
	public void elemNestedTest() throws Exception {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("span"),
				DuelToken.elemEnd("span"),
				DuelToken.elemEnd("div")
			};

		ContainerNode expected = new ContainerNode (new Node[] {
				new ElementNode("div", null, new Node[] {
						new ElementNode("span")
					})
			});

		ContainerNode actual = new DuelParser().parse(input);

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

		ContainerNode expected = new ContainerNode (new Node[] {
				new ElementNode("div", null, new Node[] {
						new ElementNode("span")
					})
			});

		ContainerNode actual = new DuelParser().parse(input);

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

		ContainerNode expected = new ContainerNode (new Node[] {
				new ElementNode("div", null, new Node[] {
						new ElementNode("img"),
						new ElementNode("span", null, new Node[] {
								new LiteralNode("plain text")
							})
					})
			});

		ContainerNode actual = new DuelParser().parse(input);

		assertEquals(expected, actual);
	}
}
