package org.duelengine.duel;

import static org.junit.Assert.*;
import java.util.Date;
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
	public void coerceBooleanOneTest() {
		Object input = 1.0;

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
}
