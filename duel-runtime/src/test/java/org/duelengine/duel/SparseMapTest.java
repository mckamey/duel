package org.duelengine.duel;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;

public class SparseMapTest {

	@Test
	public void putPrimitiveSingleTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"isDebug", false);

		String expected =
			"var isDebug = false;\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putPrimitivesTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested", DuelData.asMap(
				"foo", 42,
				"bar", true));

		String expected =
			"var simple = 'Hello';\n"+
			"var nested = {\n"+
			"\tfoo : 42,\n"+
			"\tbar : true\n"+
			"};\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putPrimitivesSparseTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", 42,
			"nested.bar", true);

		String expected =
			"var simple = 'Hello';\n"+
			"var nested = nested || {};\n"+
			"nested.foo = 42;\n"+
			"nested.bar = true;\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putExtendWithListTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", 42,
			"nested.bar", true);

		// extend
		input.putSparse("nested.baz", DuelData.asList(1, 2, 3));

		String expected =
			"var simple = 'Hello';\n"+
			"var nested = nested || {};\n"+
			"nested.foo = 42;\n"+
			"nested.bar = true;\n"+
			"nested.baz = [\n"+
			"\t1,\n"+
			"\t2,\n"+
			"\t3\n"+
			"];\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putOverwriteWithListTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", 42,
			"nested.bar", true);

		// overwrite
		input.putSparse("nested", DuelData.asList(1, 2, 3));

		String expected =
			"var simple = 'Hello';\n"+
			"var nested = [\n"+
			"\t1,\n"+
			"\t2,\n"+
			"\t3\n"+
			"];\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putObjectTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", null,
			"nested.bar", new Object());

		String expected =
			"var simple='Hello';"+
			"var nested=nested||{};"+
			"nested.foo=null;"+
			"nested.bar={};";

		StringBuilder output = new StringBuilder();
		new DataEncoder().writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putObjectPrettyPrintTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", null,
			"nested.bar", new Object());

		String expected =
			"var simple = 'Hello';\n"+
			"var nested = nested || {};\n"+
			"nested.foo = null;\n"+
			"nested.bar = {};\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putObjectExpandoTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested", new Object());

		// add expando properties
		input.putSparse("nested.foo", DuelData.asList(1, 2, 3));
		input.putSparse("nested.bar", true);

		String expected =
			"var simple = 'Hello';\n"+
			"var nested = {\n"+
			"\tfoo : [\n"+
			"\t\t1,\n"+
			"\t\t2,\n"+
			"\t\t3\n"+
			"\t],\n"+
			"\tbar : true\n"+
			"};\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putMapDeepNestTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"nested.many.$levels", DuelData.asMap("$", true, "", false));

		// add expando properties
		input.putSparse("nested.many.$levels.deep", DuelData.asList(1, 2, 3));

		String expected =
			"var nested=nested||{};"+
			"nested.many=nested.many||{};"+
			"nested.many.$levels={"+
			"$:true,"+
			"'':false,"+
			"deep:["+
			"1,"+
			"2,"+
			"3"+
			"]"+
			"};";

		StringBuilder output = new StringBuilder();
		new DataEncoder().writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putMapDeepNestPrettyPrintTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"nested.many.$levels", DuelData.asMap("$", true, "", false));

		// add expando properties
		input.putSparse("nested.many.$levels.deep", DuelData.asList(1, 2, 3));

		String expected =
			"var nested = nested || {};\n"+
			"nested.many = nested.many || {};\n"+
			"nested.many.$levels = {\n"+
			"\t$ : true,\n"+
			"\t'' : false,\n"+
			"\tdeep : [\n"+
			"\t\t1,\n"+
			"\t\t2,\n"+
			"\t\t3\n"+
			"\t]\n"+
			"};\n";

		StringBuilder output = new StringBuilder();
		new DataEncoder("\n", "\t").writeVars(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putInvalidExpandoStringTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested", "World");

		try {
			// add invalid expando properties
			input.putSparse("nested.foo", 42);

			fail("Expected IllegalStateException");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	@Test
	public void putInvalidExpandoNullTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested", null);

		try {
			// add invalid expando properties
			input.putSparse("nested.foo", 42);

			fail("Expected IllegalStateException");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}
}
