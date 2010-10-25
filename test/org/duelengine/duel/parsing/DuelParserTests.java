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
}
