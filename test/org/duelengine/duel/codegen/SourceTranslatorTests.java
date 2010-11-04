package org.duelengine.duel.codegen;

import org.duelengine.duel.codedom.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SourceTranslatorTests {

	@Test
	public void stringSimpleTest() throws Exception {
		String input = "function(model) { return model; }";

		CodeObject expected = null;

		CodeObject actual = new SourceTranslator().translate(input);

		assertEquals(expected, actual);
	}
}
