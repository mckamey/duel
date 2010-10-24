package org.duelengine.duel.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

public class DuelLexerTests {

	@Test
	public void literalTest() {

		String input = "This is just literal text.";

		Object[] expected = {
				DuelToken.Literal("This is just literal text."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityOnlyTest() {

		String input = "&vert;&semi;&comma;";

		Object[] expected = {
				DuelToken.Literal("|;,"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityDecimalTest() {

		String input = "This is &#39;just &#60;literal te&#34;xt &#38; some entities&#62;.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityDecimalRecoveryTest() {

		String input = "This is &#39just &#60literal te&#34xt &#38 some entities&#62.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexTest() {

		String input = "This is &#x27;just &#x3C;literal te&#X22;xt &#x26; some entities&#X3e;.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexRecoveryTest() {

		String input = "This is &#x27just &#x3Cliteral te&#X22xt &#x26 some entities&#X3e.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedXmlTest() {

		String input = "This is &apos;just &lt;literal te&quot;xt &amp; some entities&gt;.";

		Object[] expected = {
				DuelToken.Literal("This is 'just <literal te\"xt & some entities>."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedXmlRecoveryTest() {

		String input = "This is &apos:just &lt.literal te&quot xt &AMP some entities&GT.";

		Object[] expected = {
				DuelToken.Literal("This is ':just <.literal te\" xt & some entities>."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedCommonTest() {

		String input = "This is &copy;just &dot;literal te&trade;xt &middot; some entities&eacute;.";

		Object[] expected = {
				DuelToken.Literal("This is \u00A9just \u02D9literal te\u2122xt \u00B7 some entities\u00E9."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedRareTest() {

		String input = "This is &semi;just &sol;literal te&Tab;xt &bsol; some entities&zwnj;.";

		Object[] expected = {
				DuelToken.Literal("This is ;just /literal te\txt \\ some entities\u200C."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedUnicodeTest() {

		String input = "This is &Sscr;just &smallsetminus;literal te&shy;xt &rfr; some entities&spades;.";

		Object[] expected = {
				DuelToken.Literal("This is \u1D4AEjust \u2216literal te\u00ADxt \u1D52F some entities\u2660."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityInvalidRecoveryTest() {

		String input = "This is&amp just &THISISWRONG;literal te&123456;xt & some entities&grave.";

		Object[] expected = {
				DuelToken.Literal("This is& just &THISISWRONG;literal te&123456;xt & some entities`."),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginTest() {

		String input = "<div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginWhitespaceTest() {

		String input = "<div  \t >";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemEndTest() {

		String input = "</div>";

		Object[] expected = {
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemEndWhitespaceTest() {

		String input = "</div\r\n\t\n>";

		Object[] expected = {
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginEndTest() {

		String input = "<div></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemVoidTest() {

		String input = "<div/>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemVoidWhitespaceTest() {

		String input = "<div\r\n\t\t \n />";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoValueTest() {

		String input = "<div noValue/>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("noValue"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoValueWhitespaceTest() {

		String input = "<div noValue      ></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("noValue"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrEmptyTest() {

		String input = "<div emptyValue=\"\" />";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("emptyValue"),
				DuelToken.AttrValue(""),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrEmptyAltDelimTest() {

		String input = "<div emptyValue=''></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("emptyValue"),
				DuelToken.AttrValue(""),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrSimpleTest() {

		String input = "<div simpleValue=\" this is the 'value' \"></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("simpleValue"),
				DuelToken.AttrValue(" this is the 'value' "),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrSimpleAltDelimTest() {

		String input = "<div simpleValue=' this is the \"value\" '></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("simpleValue"),
				DuelToken.AttrValue(" this is the \"value\" "),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();
		
		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoDelimTest() {

		String input = "<div simpleValue=this_is_the_value another></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("simpleValue"),
				DuelToken.AttrValue("this_is_the_value"),
				DuelToken.AttrName("another"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrOnlyEntitiesTest() {

		String input = "<div simpleValue='&vert;&semi;&comma;'></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("simpleValue"),
				DuelToken.AttrValue("|;,"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrQuotEntityTest() {

		String input = "<div simpleValue=\"the &quot;quote&quot;ed value &nothin\"></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("simpleValue"),
				DuelToken.AttrValue("the \"quote\"ed value &nothin"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrAposEntityTest() {

		String input = "<div simpleValue='the attribute&apos;s apos &nothin'></div>";

		Object[] expected = {
				DuelToken.ElemBegin("div"),
				DuelToken.AttrName("simpleValue"),
				DuelToken.AttrValue("the attribute's apos &nothin"),
				DuelToken.ElemEnd("div"),
				DuelToken.End
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	private void dumpList(String label, Object[] tokens) {
		System.out.println();
		System.out.print(label+":");
		for (Object token : tokens) {
			System.out.print("\n\t"+token);
		}
		System.out.println();
	}
}
