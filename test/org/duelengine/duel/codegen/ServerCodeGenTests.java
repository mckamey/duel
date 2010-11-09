package org.duelengine.duel.codegen;

import java.io.*;

import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeGenTests {

	@Test
	public void stringSimpleTest() throws IOException {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			"com.example",
			"Foo",
			new CodeMethod[] {
				new CodeMethod(
					AccessModifierType.PRIVATE,
					Void.class,
					"bind_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Appendable.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")
								}))
					})
			});

		String expected =
			"package com.example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n\n"+
			"public class Foo extends org.duelengine.duel.View {\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"A JSON payload should be an object or array, not a string.\");\n"+
			"\t}\n"+
			"\t\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws IOException {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(
					AccessModifierType.PRIVATE,
					Void.class,
					"bind_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Appendable.class, "output"),
						new CodeParameterDeclarationExpression(Object.class, "data"),
						new CodeParameterDeclarationExpression(int.class, "index"),
						new CodeParameterDeclarationExpression(int.class, "count"),
						new CodeParameterDeclarationExpression(String.class, "key")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeThisReferenceExpression(),
								"write",
								new CodeExpression[] {
									new CodeVariableReferenceExpression("output"),
									new CodeVariableReferenceExpression("count")
								}))
					})
			});

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n\n"+
			"public class foo extends org.duelengine.duel.View {\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, count);\n"+
			"\t}\n"+
			"\t\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}
}
