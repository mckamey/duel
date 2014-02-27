package org.duelengine.duel;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

public class DuelDataTest {

	@Test
	public void coerceBooleanNullTest() {
		Object input = null;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanUndefinedTest() {
		Object input = JSUtility.UNDEFINED;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanFalseTest() {
		Object input = false;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanEmptyStringTest() {
		Object input = "";

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanZeroIntTest() {
		Object input = 0;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanZeroFloatTest() {
		Object input = 0.0f;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanZeroDoubleTest() {
		Object input = 0.0;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanZeroLongTest() {
		Object input = 0L;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanNaNTest() {
		Object input = Double.NaN;

		boolean expected = false;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanTrueTest() {
		Object input = true;

		boolean expected = true;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanOneIntTest() {
		Object input = 1;

		boolean expected = true;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanOneFloatTest() {
		Object input = 1.0;

		boolean expected = true;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanPITest() {
		Object input = Math.PI;

		boolean expected = true;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceBooleanDateTest() {
		Object input = new Date();

		boolean expected = true;

		boolean actual = DuelData.coerceBoolean(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceStringNullTest() {
		Object input = null;

		String expected = "null";

		String actual = DuelData.coerceString(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceStringUndefinedTest() {
		Object input = JSUtility.UNDEFINED;

		String expected = "undefined";

		String actual = DuelData.coerceString(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceStringFalseTest() {
		Object input = false;

		String expected = "false";

		String actual = DuelData.coerceString(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceStringTrueTest() {
		Object input = true;

		String expected = "true";

		String actual = DuelData.coerceString(input);

		assertEquals(expected, actual);
	}

	@Test
	public void coerceNumberNullTest() {
		Object input = null;

		double expected = 0.0;

		double actual = DuelData.coerceNumber(input);

		assertEquals(expected, actual, Double.MIN_VALUE);
	}

	@Test
	public void coerceNumberUndefinedTest() {
		Object input = JSUtility.UNDEFINED;

		double expected = Double.NaN;

		double actual = DuelData.coerceNumber(input);

		assertEquals(expected, actual, Double.MIN_VALUE);
	}

	@Test
	public void typeOfNullTest() {
		Object input = null;

		String expected = "object";

		String actual = DuelData.typeOf(input);

		assertEquals(expected, actual);
	}

	@Test
	public void typeOfUndefinedTest() {
		Object input = JSUtility.UNDEFINED;

		String expected = "undefined";

		String actual = DuelData.typeOf(input);

		assertEquals(expected, actual);
	}

	@Test
	public void asMapNullTest() {
		Object input = null;

		Map<String, Object> expected = Collections.emptyMap();

		Map<String, Object> actual = DuelData.asMap(input);

		assertEquals(expected.size(), actual.size());
		for (String key : expected.keySet()) {
			assertEquals(expected.get(key), actual.get(key));
		}
	}

	@Test
	public void asMapUndefinedTest() {
		Object input = JSUtility.UNDEFINED;

		Map<String, Object> expected = Collections.emptyMap();

		Map<String, Object> actual = DuelData.asMap(input);

		assertEquals(expected.size(), actual.size());
		for (String key : expected.keySet()) {
			assertEquals(expected.get(key), actual.get(key));
		}
	}
}
