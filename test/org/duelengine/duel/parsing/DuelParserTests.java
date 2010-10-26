package org.duelengine.duel.parsing;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;

public class DuelParserTests {

	@Test
	public void literalSingleTest() {

		DuelToken[] input = {
				DuelToken.literal("This is just literal text.")
			};

		Object[] expected = {
				new LiteralNode("This is just literal text.")
			};

		Object[] actual = new DuelParser().parse(Arrays.asList(input).iterator()).toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void literalFoldingTest() {

		DuelToken[] input = {
				DuelToken.literal("This is literal text"),
				DuelToken.literal("\n"),
				DuelToken.literal("which can all be folded.")
			};

		Object[] expected = {
				new LiteralNode("This is literal text\nwhich can all be folded.")
			};

		Object[] actual = new DuelParser().parse(Arrays.asList(input).iterator()).toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginTest() {

		DuelToken[] input = {
				DuelToken.elemBegin("div")
			};

		Object[] expected = {
				new ElementNode("div")
			};

		Object[] actual = new DuelParser().parse(Arrays.asList(input).iterator()).toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginEndTest() {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemEnd("div")
			};

		Object[] expected = {
				new ElementNode("div")
			};

		Object[] actual = new DuelParser().parse(Arrays.asList(input).iterator()).toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemAttribTest() {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("foo")
			};

		Object[] expected = {
				new ElementNode("div", new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo"))
					})
			};

		Object[] actual = new DuelParser().parse(Arrays.asList(input).iterator()).toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemNestedTest() {

		DuelToken[] input = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("span"),
				DuelToken.elemEnd("span"),
				DuelToken.elemEnd("div")
			};

		Object[] expected = {
				new ElementNode("div", null, new Node[] {
						new ElementNode("span")
					})
			};

		Object[] actual = new DuelParser().parse(Arrays.asList(input).iterator()).toArray();

		assertArrayEquals(expected, actual);
	}
}
