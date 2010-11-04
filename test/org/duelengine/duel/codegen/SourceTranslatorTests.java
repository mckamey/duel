package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codedom.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SourceTranslatorTests {

	@Test
	public void stringSimpleTest() throws Exception {
		String input = "function(model) { return model; }";

		CodeMethod expected =
			new CodeMethod(
				Object.class,
				"t_1",
				new CodeParameterDeclarationExpression[] {
					new CodeParameterDeclarationExpression(Writer.class, "writer"),
					new CodeParameterDeclarationExpression(Object.class, "model"),
					new CodeParameterDeclarationExpression(Integer.class, "index"),
					new CodeParameterDeclarationExpression(Integer.class, "count")
				},
				new CodeStatement[] {
					new CodeMethodReturnStatement(new CodeVariableReferenceExpression("model"))
				});

		List<CodeMember> actual = new SourceTranslator(new CodeTypeDeclaration()).translate(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
	}
}
