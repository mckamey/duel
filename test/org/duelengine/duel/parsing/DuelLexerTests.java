package org.duelengine.duel.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

public class DuelLexerTests {

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
	public void entityOnlyTest() {

		String input = "&vert;&semi;&comma;";

		Object[] expected = {
				DuelToken.literal("|;,")
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
	public void entityHexTest() {

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
	public void html5DocTypeTest() {

		String input = "<!doctype html><html />";

		Object[] expected = {
				DuelToken.block(new BlockValue("<!", ">", "doctype html")),
				DuelToken.elemBegin("html"),
				DuelToken.elemEnd("html")
			};

		Object[] actual = new DuelLexer(input).toList().toArray();
	
		assertArrayEquals(expected, actual);
	}

	@Test
	public void xhtmlDocTypeTest() {

		String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" />";

		Object[] expected = {
				DuelToken.block(new BlockValue("<!", ">", "DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\"")),
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
				DuelToken.literal("</i> adipiscing elit.';\n\t"),// breaks because tag suspected
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

	private void dumpList(String label, Object[] tokens) {
		System.out.println();
		System.out.print(label+":");
		for (Object token : tokens) {
			System.out.print("\n\t"+token);
		}
		System.out.println();
	}
}
