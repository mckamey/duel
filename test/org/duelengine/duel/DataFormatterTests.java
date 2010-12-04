package org.duelengine.duel;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import org.junit.Test;

public class DataFormatterTests {

	@Test
	public void writeNullTest() throws IOException {
		Object input = null;

		String expected = "null";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeFalseTest() throws IOException {
		Object input = false;

		String expected = "false";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeTrueTest() throws IOException {
		Object input = true;

		String expected = "true";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberZeroTest() throws IOException {
		Object input = 0.0;

		String expected = "0";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberDecimalTest() throws IOException {
		Object input = 26.2;

		String expected = "26.2";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberNegIntegerTest() throws IOException {
		Object input = -10.0;

		String expected = "-10";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberPITest() throws IOException {
		Object input = Math.PI;

		String expected = "3.141592653589793";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberExpTest() throws IOException {
		Object input = 7.6543e21;

		String expected = "7.6543E21";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberNegExpTest() throws IOException {
		Object input = -7.6543e-21;

		String expected = "-7.6543E-21";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberOverflowLongTest() throws IOException {
		Object input = Long.MIN_VALUE;

		String expected = "\"-9223372036854775808\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberOverflowLongMaxTest() throws IOException {
		Object input = Long.MAX_VALUE;

		String expected = "\"9223372036854775807\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberOverflowLongBigTest() throws IOException {
		Object input = 9223372036854775799L;

		String expected = "\"9223372036854775799\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberOverflowLongSmallTest() throws IOException {
		Object input = -9223372036854775799L;

		String expected = "\"-9223372036854775799\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberNaNTest() throws IOException {
		Object input = Double.NaN;

		String expected = "NaN";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberPositiveInfinityTest() throws IOException {
		Object input = Double.POSITIVE_INFINITY;

		String expected = "Infinity";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeNumberNegativeInfinityTest() throws IOException {
		Object input = Double.NEGATIVE_INFINITY;

		String expected = "-Infinity";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeDate2010UTCTest() throws IOException {
		Object input = new Date(1291422018285L);

		String expected = "new Date(1291422018285)";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeDate1968UTCTest() throws IOException {
		Object input = new Date(-34963200000L);

		String expected = "new Date(-34963200000)";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeStringEmptyTest() throws IOException {
		Object input = "";

		String expected = "\"\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeStringSimpleTest() throws IOException {
		Object input = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

		String expected = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeStringEscapedTest() throws IOException {
		Object input = "\\\b\f\n\r\t\u0123\u4567\u89AB\uCDEF\uabcd\uef4A\"";

		String expected = "\"\\\\\\b\\f\\n\\r\\t\\u0123\\u4567\\u89AB\\uCDEF\\uABCD\\uEF4A\\\"\"";

		StringBuilder output = new StringBuilder();
		new DataFormatter().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeArrayEmptyTest() throws IOException {
		Object input = new Object[0];

		String expected = "[]";

		StringBuilder output = new StringBuilder();
		new DataFormatter(false).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeArrayEmptyPrettyPrintTest() throws IOException {
		Object input = Collections.EMPTY_SET;

		String expected = "[]";

		StringBuilder output = new StringBuilder();
		new DataFormatter(true).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeArraySingleTest() throws IOException {
		Object input = new String[] { "Test." };

		String expected = "[\"Test.\"]";

		StringBuilder output = new StringBuilder();
		new DataFormatter(false).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeArraySinglePrettyPrintTest() throws IOException {
		Object input = new String[] { "Test." };

		String expected = "[ \"Test.\" ]";

		StringBuilder output = new StringBuilder();
		new DataFormatter(true).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void writeArrayMultipleTest() throws IOException {
		Object input = Arrays.asList(false, null, true, 42, "Test");

		String expected = "[false,null,true,42,\"Test\"]";

		StringBuilder output = new StringBuilder();
		new DataFormatter(false).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void writeArrayMultiplePrettyPrintTest() throws IOException {
		Object input = Arrays.asList(false, null, true, 42, "Test");

		String expected =
			"[\n"+
			"\tfalse,\n"+
			"\tnull,\n"+
			"\ttrue,\n"+
			"\t42,\n"+
			"\t\"Test\"\n"+
			"]";

		StringBuilder output = new StringBuilder();
		new DataFormatter(true).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeObjectEmptyTest() throws IOException {
		Object input = Collections.EMPTY_MAP;

		String expected = "{}";

		StringBuilder output = new StringBuilder();
		new DataFormatter(false).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeObjectEmptyPrettyPrintTest() throws IOException {
		Object input = new Object();

		String expected = "{}";

		StringBuilder output = new StringBuilder();
		new DataFormatter(true).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeObjectSingleTest() throws IOException {
		Object input = DuelData.asMap(
				"One", 1
			);

		String expected = "{One:1}";

		StringBuilder output = new StringBuilder();
		new DataFormatter(false).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeObjectSinglePrettyPrintTest() throws IOException {
		Object input = DuelData.asMap(
				"One", 1
			);

		String expected = "{ One : 1 }";

		StringBuilder output = new StringBuilder();
		new DataFormatter(true).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeObjectMultipleTest() throws IOException {
		Object input = DuelData.asMap(
				"", "",
				"One", 1,
				2, "Too",
				".T.H.R.E.E.", Math.PI,
				"$", null,
				" white space ", true,
				false, false
			);

		String expected = "{\"\":\"\",One:1,\"2\":\"Too\",\".T.H.R.E.E.\":3.141592653589793,$:null,\" white space \":true,\"false\":false}";

		StringBuilder output = new StringBuilder();
		new DataFormatter(false).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void writeObjectMultiplePrettyPrintTest() throws IOException {
		Object input = DuelData.asMap(
				"", "",
				"One", 1,
				2, "Too",
				".T.H.R.E.E.", Math.PI,
				"$", null,
				" white space ", true,
				false, false
			);

		String expected =
			"{\n"+
			"\t\"\" : \"\",\n"+
			"\tOne : 1,\n"+
			"\t\"2\" : \"Too\",\n"+
			"\t\".T.H.R.E.E.\" : 3.141592653589793,\n"+
			"\t$ : null,\n"+
			"\t\" white space \" : true,\n"+
			"\t\"false\" : false\n"+
			"}";

		StringBuilder output = new StringBuilder();
		new DataFormatter(true).write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
