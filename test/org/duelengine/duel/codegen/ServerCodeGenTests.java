package org.duelengine.duel.codegen;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
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
			"\tprotected void render(Appendable output, Object data, int index, int count, String key) {\n"+
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
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
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
			"\tprotected void render(Appendable output, Object data, int index, int count, String key) {\n"+
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
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
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
		((CodeConditionStatement)((CodeMethod)input.getMembers().get(3)).getStatements().get(1)).getCondition().setHasParens(true);
		((CodeConditionStatement)((CodeConditionStatement)((CodeMethod)input.getMembers().get(3)).getStatements().get(1)).getFalseStatements().getLastStatement()).getCondition().setHasParens(true);

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
			"\tprotected void render(Appendable output, Object data, int index, int count, String key) {\n"+
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
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
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
			"\tprotected void render(Appendable output, Object data, int index, int count, String key) {\n"+
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

	@Test
	public void iterationArrayTest() throws IOException {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			null,
			"example",
			new CodeMethod[] {
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"render",
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
						new CodeVariableDeclarationStatement(
							Collection.class,
							"items_1",// collection
							new CodeMethodInvokeExpression(
								new CodeThisReferenceExpression(),
								"asItems",
								new CodeExpression[] {
									new CodePropertyReferenceExpression(
										new CodeVariableReferenceExpression("data"),
										new CodePrimitiveExpression("items"))
								})),
						new CodeVariableCompoundDeclarationStatement(
							new CodeVariableDeclarationStatement[]{
								new CodeVariableDeclarationStatement(
									int.class,
									"index_2",// index
									new CodePrimitiveExpression(0)),
								new CodeVariableDeclarationStatement(
									int.class,
									"count_3",// count
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("items_1"),
										"size",
										null)),
							}),
						new CodeIterationStatement(
							new CodeVariableDeclarationStatement(
								Iterator.class,
								"iterator_4",
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression("items_1"),
									"iterator",
									null)),// initStatement
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("iterator_4"),
								"hasNext",
								null),// testExpression
							new CodeExpressionStatement(
								new CodeUnaryOperatorExpression(
									CodeUnaryOperatorType.POST_INCREMENT,
									new CodeVariableReferenceExpression("index_2"))),// incrementStatement
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeThisReferenceExpression(),
										"render_2",
										new CodeExpression[] {
											new CodeVariableReferenceExpression("output"),
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression("iterator_4"),
												"next",
												null),
											new CodeVariableReferenceExpression("index_2"),
											new CodeVariableReferenceExpression("count_3"),
											new CodePrimitiveExpression(null)
										}))
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					}),
					new CodeMethod(
						AccessModifierType.PRIVATE,
						Void.class,
						"render_2",
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
										new CodePrimitiveExpression("item ")
									})),
							new CodeExpressionStatement(
								new CodeMethodInvokeExpression(
									new CodeThisReferenceExpression(),
									"write",
									new CodeExpression[] {
										new CodeVariableReferenceExpression("output"),
										new CodeVariableReferenceExpression("index")
									}))
					})
			});

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)input.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example() {\n"+
			"\t}\n\n"+
			"\tpublic example(ClientIDStrategy clientID) {\n"+
			"\t\tsuper(clientID);\n"+
			"\t}\n\n"+
			"\tpublic example(DuelView view) {\n"+
			"\t\tsuper(view);\n"+
			"\t}\n\n"+
			"\tprotected void render(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tCollection items_1 = this.asItems(this.getProperty(data, \"items\"));\n"+
			"\t\tint index_2 = 0,\n" +
			"\t\t\tcount_3 = items_1.size();\n"+
			"\t\tfor (Iterator iterator_4 = items_1.iterator(); iterator_4.hasNext(); index_2++) {\n"+
			"\t\t\tthis.render_2(output, iterator_4.next(), index_2, count_3, null);\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"item \");\n"+
			"\t\tthis.write(output, (index));\n"+
			"\t}\n}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();
		
		assertEquals(expected, actual);
	}

	@Test
	public void iterationObjectTest() throws IOException {

		CodeTypeDeclaration input = new CodeTypeDeclaration(
				AccessModifierType.PUBLIC,
				null,
				"example",
				new CodeMethod[] {
					new CodeMethod(
						AccessModifierType.PROTECTED,
						Void.class,
						"render",
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
							new CodeVariableDeclarationStatement(
								Collection.class,
								"items_1",// collection
								new CodeMethodInvokeExpression(
									new CodeThisReferenceExpression(),
									"asEntries",
									new CodeExpression[] {
										new CodePropertyReferenceExpression(
											new CodeVariableReferenceExpression("data"),
											new CodePrimitiveExpression("foo"))
									})),
							new CodeVariableCompoundDeclarationStatement(
								new CodeVariableDeclarationStatement[]{
									new CodeVariableDeclarationStatement(
										int.class,
										"index_2",// index
										new CodePrimitiveExpression(0)),
									new CodeVariableDeclarationStatement(
										int.class,
										"count_3",// count
										new CodeMethodInvokeExpression(
											new CodeVariableReferenceExpression("items_1"),
											"size",
											null)),
								}),
							new CodeIterationStatement(
								new CodeVariableDeclarationStatement(
									Iterator.class,
									"iterator_4",
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("items_1"),
										"iterator",
										null)),// initStatement
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression("iterator_4"),
									"hasNext",
									null),// testExpression
								new CodeExpressionStatement(
									new CodeUnaryOperatorExpression(
										CodeUnaryOperatorType.POST_INCREMENT,
										new CodeVariableReferenceExpression("index_2"))),// incrementStatement
								new CodeStatement[] {
									new CodeVariableDeclarationStatement(
										Map.Entry.class,
										"entry_5",
										new CodeMethodInvokeExpression(
											new CodeVariableReferenceExpression("iterator_4"),
											"next",
											null)),
									new CodeExpressionStatement(
										new CodeMethodInvokeExpression(
											new CodeThisReferenceExpression(),
											"render_2",
											new CodeExpression[] {
												new CodeVariableReferenceExpression("output"),
												new CodeMethodInvokeExpression(
													new CodeVariableReferenceExpression("entry_5"),
													"getValue",
													null),
												new CodeVariableReferenceExpression("index_2"),
												new CodeVariableReferenceExpression("count_3"),
												new CodeMethodInvokeExpression(
													new CodeVariableReferenceExpression("entry_5"),
													"getKey",
													null)
											}))
								}),
							new CodeExpressionStatement(
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression("output"),
									"append",
									new CodeExpression[] {
										new CodePrimitiveExpression("</div>")
									}))
						}),
						new CodeMethod(
							AccessModifierType.PRIVATE,
							Void.class,
							"render_2",
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
											new CodePrimitiveExpression("item ")
										})),
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeThisReferenceExpression(),
										"write",
										new CodeExpression[] {
											new CodeVariableReferenceExpression("output"),
											new CodeVariableReferenceExpression("index")
										}))
						})
				});

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)input.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		String expected =
			"import java.io.*;\n"+
			"import java.util.*;\n"+
			"import org.duelengine.duel.*;\n\n"+
			"public class example extends DuelView {\n\n"+
			"\tpublic example() {\n"+
			"\t}\n\n"+
			"\tpublic example(ClientIDStrategy clientID) {\n"+
			"\t\tsuper(clientID);\n"+
			"\t}\n\n"+
			"\tpublic example(DuelView view) {\n"+
			"\t\tsuper(view);\n"+
			"\t}\n\n"+
			"\tprotected void render(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"<div>\");\n"+
			"\t\tCollection items_1 = this.asEntries(this.getProperty(data, \"foo\"));\n"+
			"\t\tint index_2 = 0,\n"+
			"\t\t\tcount_3 = items_1.size();\n"+
			"\t\tfor (Iterator iterator_4 = items_1.iterator(); iterator_4.hasNext(); index_2++) {\n"+
			"\t\t\tEntry entry_5 = iterator_4.next();\n"+
			"\t\t\tthis.render_2(output, entry_5.getValue(), index_2, count_3, entry_5.getKey());\n"+
			"\t\t}\n"+
			"\t\toutput.append(\"</div>\");\n"+
			"\t}\n\n"+
			"\tprivate void render_2(Appendable output, Object data, int index, int count, String key) {\n"+
			"\t\toutput.append(\"item \");\n"+
			"\t\tthis.write(output, (index));\n"+
			"\t}\n"+
			"}\n";

		StringBuilder output = new StringBuilder();
		new ServerCodeGen().write(output, input);
		String actual = output.toString();

		assertEquals(expected, actual);
	}
}
