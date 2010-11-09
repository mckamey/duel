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
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class Foo extends DuelView {\n\n"+
			"\tpublic Foo() {\n"+
			"\t}\n\n"+
			"\tpublic Foo(ClientIDStrategy clientID) {\n"+
			"\t\tsuper(clientID);\n"+
			"\t}\n\n"+
			"\tpublic Foo(DuelView view) {\n"+
			"\t\tsuper(view);\n"+
			"\t}\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"A JSON payload should be an object or array, not a string.\");\n"+
			"\t}\n"+
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
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo() {\n"+
			"\t}\n\n"+
			"\tpublic foo(ClientIDStrategy clientID) {\n"+
			"\t\tsuper(clientID);\n"+
			"\t}\n\n"+
			"\tpublic foo(DuelView view) {\n"+
			"\t\tsuper(view);\n"+
			"\t}\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\tthis.write(output, count);\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws IOException {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			"example",
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
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.IDENTITY_EQUALITY,
								new CodeVariableReferenceExpression("data"),
								new CodePrimitiveExpression(0.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("output"),
										"append",
										new CodeExpression[] {
											new CodePrimitiveExpression("zero")
										}))
							},
							new CodeStatement[] {
								new CodeConditionStatement(
									new CodeBinaryOperatorExpression(
										CodeBinaryOperatorType.IDENTITY_EQUALITY,
										new CodeVariableReferenceExpression("data"),
										new CodePrimitiveExpression(1.0)),
									new CodeStatement[] {
										new CodeExpressionStatement(
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression("output"),
												"append",
												new CodeExpression[] {
													new CodePrimitiveExpression("one")
												}))
									},
									new CodeStatement[] {
										new CodeExpressionStatement(
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression("output"),
												"append",
												new CodeExpression[] {
													new CodePrimitiveExpression("many")
												}))
									})
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					})
			});

		// flag the conditions as having had parens
		((CodeConditionStatement)((CodeMethod)input.getMembers().get(3)).getStatements().getStatements().get(1)).getCondition().setHasParens(true);
		((CodeConditionStatement)((CodeConditionStatement)((CodeMethod)input.getMembers().get(3)).getStatements().getStatements().get(1)).getFalseStatements().getLastStatement()).getCondition().setHasParens(true);

		String expected =
			"package example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo extends DuelView {\n\n"+
			"\tpublic foo() {\n"+
			"\t}\n\n"+
			"\tpublic foo(ClientIDStrategy clientID) {\n"+
			"\t\tsuper(clientID);\n"+
			"\t}\n\n"+
			"\tpublic foo(DuelView view) {\n"+
			"\t\tsuper(view);\n"+
			"\t}\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tif (data == 0.0) {\n"+
			"\t\t\toutput.append(\"zero\");\n"+
			"\t\t} else if (data == 1.0) {\n"+
			"\t\t\toutput.append(\"one\");\n"+
			"\t\t} else {\n"+
			"\t\t\toutput.append(\"many\");\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalSinglesTest() throws IOException {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			"example",
			"foo2",
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
									new CodePrimitiveExpression("<div>")
								})),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.VALUE_EQUALITY,
								new CodeVariableReferenceExpression("data"),
								new CodePrimitiveExpression(0.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("output"),
										"append",
										new CodeExpression[] {
											new CodePrimitiveExpression("zero")
										}))
							},
							null),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.VALUE_EQUALITY,
								new CodeVariableReferenceExpression("data"),
								new CodePrimitiveExpression(1.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("output"),
										"append",
										new CodeExpression[] {
											new CodePrimitiveExpression("one")
										}))
							},
							null),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("many")
								})),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					})
			});

		String expected =
			"package example;\n\n"+
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class foo2 extends DuelView {\n\n"+
			"\tpublic foo2() {\n"+
			"\t}\n\n"+
			"\tpublic foo2(ClientIDStrategy clientID) {\n"+
			"\t\tsuper(clientID);\n"+
			"\t}\n\n"+
			"\tpublic foo2(DuelView view) {\n"+
			"\t\tsuper(view);\n"+
			"\t}\n\n"+
			"\tprivate void bind_1(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tif (data == 0.0) {\n"+
			"\t\t\toutput.append(\"zero\");\n"+
			"\t\t}\n"+
			"\t\tif (data == 1.0) {\n"+
			"\t\t\toutput.append(\"one\");\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"many\");\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}
}
