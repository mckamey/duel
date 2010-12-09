package org.duelengine.duel;

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.Test;

public class SparseMapTests {

	@Test
	public void putPrimitivesTest() throws IOException {
		Object input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", 42,
			"nested.bar", true);

		String expected = "{simple:\"Hello\",nested:{foo:42,bar:true}}";

		StringBuilder output = new StringBuilder();
		new DataEncoder().write(output, input);
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

		String expected = "{simple:\"Hello\",nested:{foo:42,bar:true,baz:[1,2,3]}}";

		StringBuilder output = new StringBuilder();
		new DataEncoder().write(output, input);
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

		String expected = "{simple:\"Hello\",nested:[1,2,3]}";

		StringBuilder output = new StringBuilder();
		new DataEncoder().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void putObjectTest() throws IOException {
		SparseMap input = SparseMap.asSparseMap(
			"simple", "Hello",
			"nested.foo", null,
			"nested.bar", new Object());

		String expected = "{simple:\"Hello\",nested:{foo:null,bar:{}}}";

		StringBuilder output = new StringBuilder();
		new DataEncoder().write(output, input);
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

		String expected = "{simple:\"Hello\",nested:{foo:[1,2,3],bar:true}}";

		StringBuilder output = new StringBuilder();
		new DataEncoder().write(output, input);
		String actual = output.toString();
System.out.println(expected);
System.err.println(actual);
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
			
		}
	}
}