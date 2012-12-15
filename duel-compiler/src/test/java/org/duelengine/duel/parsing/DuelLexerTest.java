package org.duelengine.duel.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

public class DuelLexerTest {

	@Test
	public void literalTest() {

		String input = "This is just literal text.";

		Object[] expected = {
				DuelToken.literal("This is just literal text.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entitySimpleTest() {

		String input = "&lt;";

		Object[] expected = {
				DuelToken.literal("<")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityEuroTest() {

		String input = "&euro;";

		Object[] expected = {
				DuelToken.literal("\u20AC")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityOnlyTest() {

		String input = "&vert;&semi;&comma;";

		Object[] expected = {
				DuelToken.literal("|;,")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityLeadingTest() {

		String input = "leading&amp;";

		Object[] expected = {
				DuelToken.literal("leading&")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityTrailingTest() {

		String input = "&amp;trailing";

		Object[] expected = {
				DuelToken.literal("&trailing")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityBTest() {

		String input = "&#66;";

		Object[] expected = {
				DuelToken.literal("B")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityDecimalTest() {

		String input = "This is &#39;just &#60;literal te&#34;xt &#38; some entities&#62;.";

		Object[] expected = {
				DuelToken.literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityDecimalRecoveryTest() {

		String input = "This is &#39just &#60literal te&#34xt &#38 some entities&#62.";

		Object[] expected = {
				DuelToken.literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexLowerXTest() {

		String input = "&#x37;";

		Object[] expected = {
				DuelToken.literal("7")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexUpperXTest() {

		String input = "&#X38;";

		Object[] expected = {
				DuelToken.literal("8")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexUpperCaseTest() {

		String input = "&#xABCD;";

		Object[] expected = {
				DuelToken.literal("\uABCD")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexLowerCaseTest() {

		String input = "&#xabcd;";

		Object[] expected = {
				DuelToken.literal("\uabcd")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexMixedTest() {

		String input = "This is &#x27;just &#x3C;literal te&#X22;xt &#x26; some entities&#X3e;.";

		Object[] expected = {
				DuelToken.literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityHexRecoveryTest() {

		String input = "This is &#x27just &#x3Cliteral te&#X22xt &#x26 some entities&#X3e.";

		Object[] expected = {
				DuelToken.literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityMalformedTest() {

		String input = "there should &#xnot &Xltb&#gte decoded chars & inside this text";

		Object[] expected = {
				DuelToken.literal("there should &#xnot &Xltb&#gte decoded chars & inside this text")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedXmlTest() {

		String input = "This is &apos;just &lt;literal te&quot;xt &amp; some entities&gt;.";

		Object[] expected = {
				DuelToken.literal("This is 'just <literal te\"xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedXmlRecoveryTest() {

		String input = "This is &apos:just &lt.literal te&quot xt &AMP some entities&GT.";

		Object[] expected = {
				DuelToken.literal("This is ':just <.literal te\" xt & some entities>.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedCommonTest() {

		String input = "This is &copy;just &dot;literal te&trade;xt &middot; some entities&eacute;.";

		Object[] expected = {
				DuelToken.literal("This is \u00A9just \u02D9literal te\u2122xt \u00B7 some entities\u00E9.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedRareTest() {

		String input = "This is &semi;just &sol;literal te&Tab;xt &bsol; some entities&zwnj;.";

		Object[] expected = {
				DuelToken.literal("This is ;just /literal te\txt \\ some entities\u200C.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityNamedUnicodeTest() {

		String input = "This is &Sscr;just &smallsetminus;literal te&shy;xt &rfr; some entities&spades;.";

		Object[] expected = {
				DuelToken.literal("This is \u1D4AEjust \u2216literal te\u00ADxt \u1D52F some entities\u2660.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void entityInvalidRecoveryTest() {

		String input = "This is&amp just &THISISWRONG;literal te&123456;xt & some entities&grave.";

		Object[] expected = {
				DuelToken.literal("This is& just &THISISWRONG;literal te&123456;xt & some entities`.")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginTest() {

		String input = "<div>";

		Object[] expected = {
				DuelToken.elemBegin("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginWhitespaceTest() {

		String input = "<div  \t >";

		Object[] expected = {
				DuelToken.elemBegin("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemEndTest() {

		String input = "</div>";

		Object[] expected = {
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemEndWhitespaceTest() {

		String input = "</div\r\n\t\n>";

		Object[] expected = {
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemBeginEndTest() {

		String input = "<div></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemNestedTest() {

		String input = "<div><span><img></span></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("span"),
				DuelToken.elemBegin("img"),
				DuelToken.elemEnd("span"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemListTest() {

		String input = "<ul class='foo'>\r\t<li>one</li>\r\t<li>two</li>\r</ul>";

		Object[] expected = {
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

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemOverlappingTest() {

		String input = "<odd><auto-closed><even>plain text</odd></ignored></even>";

		Object[] expected = {
				DuelToken.elemBegin("odd"),
				DuelToken.elemBegin("auto-closed"),
				DuelToken.elemBegin("even"),
				DuelToken.literal("plain text"),
				DuelToken.elemEnd("odd"),
				DuelToken.elemEnd("ignored"),
				DuelToken.elemEnd("even")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemExternalWhitespaceTest() {

		String input = "\r\n<div>\n\t\t test \n</div>\r\n";

		Object[] expected = {
				DuelToken.literal("\n"),
				DuelToken.elemBegin("div"),
				DuelToken.literal("\n\t\t test \n"),
				DuelToken.elemEnd("div"),
				DuelToken.literal("\n")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemCasingTest() {

		String input = "</ALLCAPS><PascalCase><xml-style></CONST_STYLE></camelCase>";

		Object[] expected = {
				DuelToken.elemEnd("ALLCAPS"),
				DuelToken.elemBegin("PascalCase"),
				DuelToken.elemBegin("xml-style"),
				DuelToken.elemEnd("CONST_STYLE"),
				DuelToken.elemEnd("camelCase")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemVoidTest() {

		String input = "<div/>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void elemVoidWhitespaceTest() {

		String input = "<div\r\n\t\t \n />";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoValueTest() {

		String input = "<div noValue/>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("noValue"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoValueWhitespaceTest() {

		String input = "<div noValue      ></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("noValue"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrEmptyTest() {

		String input = "<div emptyValue=\"\" />";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("emptyValue"),
				DuelToken.attrValue(""),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrEmptyAltDelimTest() {

		String input = "<div emptyValue=''></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("emptyValue"),
				DuelToken.attrValue(""),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrSimpleTest() {

		String input = "<div simpleValue=\" this is the 'value' \"></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue(" this is the 'value' "),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrSimpleAltDelimTest() {

		String input = "<div simpleValue=' this is the \"value\" '></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue(" this is the \"value\" "),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();
		
		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoDelimTest() {

		String input = "<div simpleValue=this_is_the_value another></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue("this_is_the_value"),
				DuelToken.attrName("another"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrNoDelimAltTest() {

		String input = "<div another simpleValue=this_is_the_value></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("another"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue("this_is_the_value"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrOnlyEntitiesTest() {

		String input = "<div simpleValue='&vert;&semi;&comma;'></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue("|;,"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrQuotEntityTest() {

		String input = "<div simpleValue=\"the &quot;quote&quot;ed value &nothin\"></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue("the \"quote\"ed value &nothin"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrAposEntityTest() {

		String input = "<div simpleValue='the attribute&apos;s apos &nothin'></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("simpleValue"),
				DuelToken.attrValue("the attribute's apos &nothin"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrWhitespaceTest() {

		String input = "<test-elem\tsimple-value = \" this is the 'value' \"\r\n \t empty-value mixed-delims\r\n\t= ' foo ' ></test-elem>";

		Object[] expected = {
				DuelToken.elemBegin("test-elem"),
				DuelToken.attrName("simple-value"),
				DuelToken.attrValue(" this is the 'value' "),
				DuelToken.attrName("empty-value"),
				DuelToken.attrName("mixed-delims"),
				DuelToken.attrValue(" foo "),
				DuelToken.elemEnd("test-elem")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrMixedTest() {

		String input = "<root no-value whitespace=\" this contains whitespace \" anyQuotedText=\"/\\\uCAFE\uBABE\uAB98\uFCDE\ubcda\uef4A\r\n\t`1~!@#$%^&*()_+-=[]{}|;:',./<>?\"></root>";

		Object[] expected = {
				DuelToken.elemBegin("root"),
				DuelToken.attrName("no-value"),
				DuelToken.attrName("whitespace"),
				DuelToken.attrValue(" this contains whitespace "),
				DuelToken.attrName("anyQuotedText"),
				DuelToken.attrValue("/\\\uCAFE\uBABE\uAB98\uFCDE\ubcda\uef4A\n\t`1~!@#$%^&*()_+-=[]{}|;:',./<>?"),
				DuelToken.elemEnd("root")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrCallbackTest() {

		String input = "<div init=\"alert('$init');\"></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("init"),
				DuelToken.attrValue("alert('$init');"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrRawCallbackTest() {

		String input = "<div $init=\"alert('$init');\"></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("$init"),
				DuelToken.attrValue("alert('$init');"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void attrPrefixCallbackTest() {

		String input = "<div duel:oninit=\"alert('duel:oninit');\"></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("duel:oninit"),
				DuelToken.attrValue("alert('duel:oninit');"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void codeSimpleTest() {

		String input = "<% code block %>";

		Object[] expected = {
				DuelToken.block(new BlockValue("<%", "%>", " code block "))
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void codeAttributeTest() {

		String input = "<a href=\"<%= simple expr %>\">foo</a>";

		Object[] expected = {
				DuelToken.elemBegin("a"),
				DuelToken.attrName("href"),
				DuelToken.attrValue(new BlockValue("<%=", "%>", " simple expr ")),
				DuelToken.literal("foo"),
				DuelToken.elemEnd("a")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void codeCommentTest() {

		String input = "<span><%-- comment --%></span>";

		Object[] expected = {
				DuelToken.elemBegin("span"),
				DuelToken.block(new BlockValue("<%--", "--%>", " comment ")),
				DuelToken.elemEnd("span")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void codeCommentHTMLTest() {

		String input =
			"<%--\r\n<html>\r\n\t<body style=\"color:lime\">\r\n\t\t<!-- not much to say here -->\r\n\t</body>\r\n</html>\r\n--%>";

		Object[] expected = {
				DuelToken.block(new BlockValue("<%--", "--%>", "\n<html>\n\t<body style=\"color:lime\">\n\t\t<!-- not much to say here -->\n\t</body>\n</html>\n"))
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void htmlCommentTest() {

		String input = "<span><!-- comment --></span>";

		Object[] expected = {
				DuelToken.elemBegin("span"),
				DuelToken.block(new BlockValue("<!--", "-->", " comment ")),
				DuelToken.elemEnd("span")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();
	
		assertArrayEquals(expected, actual);
	}

	@Test
	public void doctypeHTML5Test() {

		String input = "<!DOCTYPE html>\r\n<html></html>";

		Object[] expected = {
				DuelToken.block(new BlockValue("<!DOCTYPE", ">", "html")),
				DuelToken.literal("\n"),
				DuelToken.elemBegin("html"),
				DuelToken.elemEnd("html")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();
	
		assertArrayEquals(expected, actual);
	}

	@Test
	public void doctypeXHTMLTest() {

		String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\r\n"+
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" />";

		Object[] expected = {
				DuelToken.block(new BlockValue("<!DOCTYPE", ">", "html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"")),
				DuelToken.literal("\n"),
				DuelToken.elemBegin("html"),
				DuelToken.attrName("xmlns"),
				DuelToken.attrValue("http://www.w3.org/1999/xhtml"),
				DuelToken.attrName("xml:lang"),
				DuelToken.attrValue("en"),
				DuelToken.elemEnd("html")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void doctypeEmptyTest() {

		String input = "<!DocType>";

		Object[] expected = {
				DuelToken.block(new BlockValue("<!DOCTYPE", ">", ""))
			};

		Object[] actual = new DuelLexer(input).toList().toArray();
	
		assertArrayEquals(expected, actual);
	}

	//@Test // Embedded DOCTYPE not supported
	public void doctypeLocalTest() {

		String input = "<!DOCTYPE doc [\r\n\t<!ATTLIST normId id ID #IMPLIED>\r\n\t<!ATTLIST normNames attr NMTOKENS #IMPLIED>\r\n]>";

		Object[] expected = {
				DuelToken.block(new BlockValue("<!", ">", "DOCTYPE doc [\r\n\t<!ATTLIST normId id ID #IMPLIED>\r\n\t<!ATTLIST normNames attr NMTOKENS #IMPLIED>\r\n]"))
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptBlockTest() {

		String input =
			"<script type=\"text/javascript\">" +
			"function foo() { for(var i=0, length=10; i<length; i++) { alert(i); } }" +
			"</script>";

		Object[] expected = {
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("function foo() { for(var i=0, length=10; i"),// breaks because tag suspected
				DuelToken.literal("<length; i++) { alert(i); } }"),
				DuelToken.elemEnd("script")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptMarkupTest() {

		String input =
			"<div class='content'>\r\n"+
			"\t<script type='text/javascript'>\r\n"+
			"\t\tvar text = '<strong>Lorem ipsum</strong> dolor sit amet, <i>consectetur</i> adipiscing elit.';\r\n"+
			"\t</script>\r\n"+
			"</div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("content"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("\n\t\tvar text = '"),// breaks because tag suspected
				DuelToken.literal("<strong>Lorem ipsum"),// breaks because tag suspected
				DuelToken.literal("</strong> dolor sit amet, "),// breaks because tag suspected
				DuelToken.literal("<i>consectetur"),// breaks because tag suspected
				DuelToken.literal("</i> adipiscing elit.';\n\t"),
				DuelToken.elemEnd("script"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptVoidTest() {

		String input =
			"<div class='content'>\r\n"+
			"\t<script type='text/javascript' src='foo.js' />\r\n"+
			"\t<script type='text/javascript' src='bar.js' />\r\n"+
			"\t<script type='text/javascript'>go();</script>\r\n"+
			"</div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("content"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.attrName("src"),
				DuelToken.attrValue("foo.js"),
				DuelToken.elemEnd("script"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.attrName("src"),
				DuelToken.attrValue("bar.js"),
				DuelToken.elemEnd("script"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("go();"),
				DuelToken.elemEnd("script"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptUnwrapMarkupTest() {

		String input =
			"<div class='content'>\r\n"+
			"\t<script type='text/javascript'><!--\r\n"+
			"\t\tvar text = '<strong>Lorem ipsum</strong> dolor sit amet, <i>consectetur</i> adipiscing elit.';\r\n"+
			"\t//--></script>\r\n"+
			"</div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("content"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("\n\t\tvar text = '<strong>Lorem ipsum</strong> dolor sit amet, <i>consectetur</i> adipiscing elit.';\n\t//"),
				DuelToken.elemEnd("script"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptCDATAMarkupTest() {

		String input =
			"<div class='content'>\r\n"+
			"\t<script type='text/javascript'><![CDATA[\r\n"+
			"\t\tvar text = '<strong>Lorem ipsum</strong> dolor sit amet, <i>consectetur</i> adipiscing elit.';\r\n"+
			"\t]]></script>\r\n"+
			"</div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.attrName("class"),
				DuelToken.attrValue("content"),
				DuelToken.literal("\n\t"),
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("\n\t\tvar text = '<strong>Lorem ipsum</strong> dolor sit amet, <i>consectetur</i> adipiscing elit.';\n\t"),
				DuelToken.elemEnd("script"),
				DuelToken.literal("\n"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void styleBlockTest() {

		String input =
			"<style type=\"text/css\">" +
			"invalid<selector { color:red; }" +
			"</style>";

		Object[] expected = {
				DuelToken.elemBegin("style"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/css"),
				DuelToken.literal("invalid"),
				DuelToken.literal("<selector { color:red; }"),
				DuelToken.elemEnd("style")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void styleCodeBlockTest() {

		String input =
			"<style type=\"text/css\">" +
			"#<%= \"hacky-technique\" %> { color:red; }" +
			"</style>";

		Object[] expected = {
				DuelToken.elemBegin("style"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/css"),
				DuelToken.literal("#"),
				DuelToken.block(new BlockValue("<%=", "%>", " \"hacky-technique\" ")),
				DuelToken.literal(" { color:red; }"),
				DuelToken.elemEnd("style")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void styleUnwrapCommentTest() {

		String input =
			"<style type=\"text/css\"><!--" +
			".my-class { color:red; }" +
			"--></style>";

		Object[] expected = {
				DuelToken.elemBegin("style"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/css"),
				DuelToken.literal(".my-class { color:red; }"),
				DuelToken.elemEnd("style")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void styleUnwrapCDATATest() {

		String input =
			"<style type=\"text/css\"><![CDATA[" +
			".my-class { color:red; }" +
			"]]></style>";

		Object[] expected = {
				DuelToken.elemBegin("style"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/css"),
				DuelToken.literal(".my-class { color:red; }"),
				DuelToken.elemEnd("style")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptUnwrapCommentTest() {

		String input =
			"<script type=\"text/javascript\"><!--" +
			"function foo() { for(var i=0, length=10; i<length; i++) { alert(i); } }" +
			"--></script>";

		Object[] expected = {
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("function foo() { for(var i=0, length=10; i<length; i++) { alert(i); } }"),
				DuelToken.elemEnd("script")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void scriptUnwrapCDATATest() {

		String input =
			"<script type=\"text/javascript\"><![CDATA[" +
			"function foo() { for(var i=0, length=10; i<length; i++) { alert(i); } }" +
			"]]></script>";

		Object[] expected = {
				DuelToken.elemBegin("script"),
				DuelToken.attrName("type"),
				DuelToken.attrValue("text/javascript"),
				DuelToken.literal("function foo() { for(var i=0, length=10; i<length; i++) { alert(i); } }"),
				DuelToken.elemEnd("script")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() {

		String input =
			"<div><if test='data===0'>zero<else if='data===1'>one<else>many</if></div>";

		Object[] expected = {
				DuelToken.elemBegin("div"),
				DuelToken.elemBegin("if"),
				DuelToken.attrName("test"),
				DuelToken.attrValue("data===0"),
				DuelToken.literal("zero"),
				DuelToken.elemBegin("else"),
				DuelToken.attrName("if"),
				DuelToken.attrValue("data===1"),
				DuelToken.literal("one"),
				DuelToken.elemBegin("else"),
				DuelToken.literal("many"),
				DuelToken.elemEnd("if"),
				DuelToken.elemEnd("div")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void conditionalBlockVoidElseTest() {

		String input =
			"<div><if test='data===0'>zero<else if='data===1'/>one<else />many</if></div>";

		Object[] expected = {
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

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void callLiteralTest() {

		String input =
				"<view name=\"foo\">\n"+
				"<call view=\"bar\" data=\"<%= { name: 'bar', items: [ 1, 'too' ] } %>\" />";

		Object[] expected = {
				DuelToken.elemBegin("view"),
				DuelToken.attrName("name"),
				DuelToken.attrValue("foo"),
				DuelToken.literal("\n"),
				DuelToken.elemBegin("call"),
				DuelToken.attrName("view"),
				DuelToken.attrValue("bar"),
				DuelToken.attrName("data"),
				DuelToken.attrValue(new BlockValue("<%=", "%>", " { name: 'bar', items: [ 1, 'too' ] } ")),
				DuelToken.elemEnd("call")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@Test
	public void callLiteralAltTest() {

		String input =
				"<view name=\"foo\">"+
				"<call view=\"bar\" data=\" { name: 'bar', items: [ 1, 'too' ] } \" />";

		Object[] expected = {
				DuelToken.elemBegin("view"),
				DuelToken.attrName("name"),
				DuelToken.attrValue("foo"),
				DuelToken.elemBegin("call"),
				DuelToken.attrName("view"),
				DuelToken.attrValue("bar"),
				DuelToken.attrName("data"),
				DuelToken.attrValue(" { name: 'bar', items: [ 1, 'too' ] } "),
				DuelToken.elemEnd("call")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();

		assertArrayEquals(expected, actual);
	}

	@SuppressWarnings("unused")
	private void dumpLists(Object[] expected, Object[] actual) {

		for (Object token : expected) {
			System.out.println(token.toString());
		}
		System.out.flush();

		for (Object token : actual) {
			System.err.println(token.toString());
		}
	}
}
