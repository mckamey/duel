package org.duelengine.duel.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

public class DuelLexerTests {

	@Test
	public void literalTest() {

		String input = "This is just literal text.";

		Object[] expected = {
				DuelToken.Literal("This is just literal text.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityDecimalTest() {

		String input = "This is &#39;just &#60;literal te&#34;xt &#38; some entities&#62;.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityDecimalRecoveryTest() {

		String input = "This is &#39just &#60literal te&#34xt &#38 some entities&#62.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexTest() {

		String input = "This is &#x27;just &#x3C;literal te&#X22;xt &#x26; some entities&#X3e;.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexRecoveryTest() {

		String input = "This is &#x27just &#x3Cliteral te&#X22xt &#x26 some entities&#X3e.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedXmlTest() {

		String input = "This is &apos;just &lt;literal te&quot;xt &amp; some entities&gt;.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedXmlRecoveryTest() {

		String input = "This is &apos:just &lt.literal te&quot xt &AMP some entities&GT.";

		Object[] expected = {
				DuelToken.Literal("This is ':just <.literal te\" xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedCommonTest() {

		String input = "This is &copy;just &dot;literal te&trade;xt &middot; some entities&eacute;.";

		Object[] expected = {
				DuelToken.Literal("This is \u00A9just \u02D9literal te\u2122xt \u00B7 some entities\u00E9.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedRareTest() {

		String input = "This is &semi;just &sol;literal te&Tab;xt &bsol; some entities&zwnj;.";

		Object[] expected = {
				DuelToken.Literal("This is ;just /literal te\txt \\ some entities\u200C.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedUnicodeTest() {

		String input = "This is &Sscr;just &smallsetminus;literal te&shy;xt &rfr; some entities&spades;.";

		Object[] expected = {
				DuelToken.Literal("This is \u1D4AEjust \u2216literal te\u00ADxt \u1D52F some entities\u2660.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityInvalidRecoveryTest() {

		String input = "This is&amp just &THISISWRONG;literal te&123456;xt & some entities&grave.";

		Object[] expected = {
				DuelToken.Literal("This is& just &THISISWRONG;literal te&123456;xt & some entities`.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}
}
