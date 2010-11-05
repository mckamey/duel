package org.duelengine.duel.codegen;

import java.io.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class CodeDOMBuilderTests {

	@Test
	public void stringSimpleTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("A JSON payload should be an object or array, not a string.")
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("A JSON payload should be an object or array, not a string.")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void stringEscapeTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new LiteralNode("\\\b\f\n\r\t\u0123\u4567\u89AB\uCDEF\uabcd\uef4A\"")
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("\\&#x0008;&#x000C;\n\r\t&#x0123;&#x4567;&#x89AB;&#xCDEF;&#xABCD;&#xEF4A;\"")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void expressionCountTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ExpressionNode("count")
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeThisReferenceExpression(),
								"htmlEncode",
								new CodeExpression[] {
									new CodeVariableReferenceExpression("writer"),
									new CodeVariableReferenceExpression("count")
								}))
					})
			});

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(0)).getStatements().getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void markupExpressionModelTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new MarkupExpressionNode("model")
			});


		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodeVariableReferenceExpression("model")
								}))
					})
			});

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(0)).getStatements().getStatements().get(0)).getExpression()).getArguments().get(0).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void statementNoneTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new StatementNode("return Math.PI;")
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeThisReferenceExpression(),
								"htmlEncode",
								new CodeExpression[] {
									new CodeVariableReferenceExpression("writer"),
									new CodePropertyReferenceExpression(
										new CodeVariableReferenceExpression("Math"),
										new CodePrimitiveExpression("PI"))
									
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalBlockTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example.foo"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new XORCommandNode(null,
						new Node[] {
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("model === 0"))
								},
								new Node[] {
									new LiteralNode("zero")
								}),
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("model === 1"))
								},
								new Node[] {
									new LiteralNode("one")
								}),
							new IFCommandNode(
								null,
								new Node[] {
									new LiteralNode("many")
								}),
						})
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			"example",
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.IDENTITY_EQUALITY,
								new CodeVariableReferenceExpression("model"),
								new CodePrimitiveExpression(0.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("writer"),
										"write",
										new CodeExpression[] {
											new CodePrimitiveExpression("zero")
										}))
							},
							new CodeStatement[] {
								new CodeConditionStatement(
									new CodeBinaryOperatorExpression(
										CodeBinaryOperatorType.IDENTITY_EQUALITY,
										new CodeVariableReferenceExpression("model"),
										new CodePrimitiveExpression(1.0)),
									new CodeStatement[] {
										new CodeExpressionStatement(
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression("writer"),
												"write",
												new CodeExpression[] {
													new CodePrimitiveExpression("one")
												}))
									},
									new CodeStatement[] {
										new CodeExpressionStatement(
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression("writer"),
												"write",
												new CodeExpression[] {
													new CodePrimitiveExpression("many")
												}))
									})
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					})
			});

		// flag the conditions as having had parens
		((CodeConditionStatement)((CodeMethod)expected.getMembers().get(0)).getStatements().getStatements().get(1)).getCondition().setHasParens(true);
		((CodeConditionStatement)((CodeConditionStatement)((CodeMethod)expected.getMembers().get(0)).getStatements().getStatements().get(1)).getFalseStatements().getLastStatement()).getCondition().setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void conditionalSinglesTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example.foo2"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new IFCommandNode(
						new AttributeNode[] {
							new AttributeNode("test", new StatementNode("return model == 0;"))
						},
						new Node[] {
							new LiteralNode("zero")
						}),
					new IFCommandNode(
						new AttributeNode[] {
							new AttributeNode("test", new StatementNode("return model == 1;"))
						},
						new Node[] {
							new LiteralNode("one")
						}),
					new IFCommandNode(
						null,
						new Node[] {
							new LiteralNode("many")
						}),
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			"example",
			"foo2",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.VALUE_EQUALITY,
								new CodeVariableReferenceExpression("model"),
								new CodePrimitiveExpression(0.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("writer"),
										"write",
										new CodeExpression[] {
											new CodePrimitiveExpression("zero")
										}))
							},
							null),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.VALUE_EQUALITY,
								new CodeVariableReferenceExpression("model"),
								new CodePrimitiveExpression(1.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression("writer"),
										"write",
										new CodeExpression[] {
											new CodePrimitiveExpression("one")
										}))
							},
							null),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("many")
								})),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void attributesTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ElementNode("div",
					new AttributeNode[] {
						new AttributeNode("class", new LiteralNode("foo")),
						new AttributeNode("style", new LiteralNode("color:red"))
					},
					new Node[] {
					new ElementNode("ul",
						new AttributeNode[] {
							new AttributeNode("class", new LiteralNode("bar"))
						},
						new Node[] {
							new ElementNode("li",
									null,
									new Node[] {
										new LiteralNode("one")
									}),
							new ElementNode("li",
									null,
									new Node[] {
										new LiteralNode("two")
									}),
							new ElementNode("li",
									null,
									new Node[] {
										new LiteralNode("three")
									}),
						})
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			null,
			"foo",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div class=\"foo\" style=\"color:red\"><ul class=\"bar\"><li>one</li><li>two</li><li>three</li></ul></div>")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void namespaceTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new Node[] {
				new ElementNode("div")
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			"foo.bar",
			"Blah",
			new CodeMethod[] {
				new CodeMethod(Void.class, "t_1",
					new CodeParameterDeclarationExpression[] {
						new CodeParameterDeclarationExpression(Writer.class, "writer"),
						new CodeParameterDeclarationExpression(Object.class, "model"),
						new CodeParameterDeclarationExpression(Integer.class, "index"),
						new CodeParameterDeclarationExpression(Integer.class, "count")
					},
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression("writer"),
								"write",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div></div>")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}
}
