package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
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
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									new CodeVariableReferenceExpression(int.class, "count")
								}))
					})
			});

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void expressionDataTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new ExpressionNode("data")
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
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
								"htmlEncode",
								new CodeExpression[] {
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									new CodeVariableReferenceExpression(Object.class, "data")
								}))
					})
			});

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void markupExpressionDataTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo"))
			},
			new Node[] {
				new MarkupExpressionNode("data")
			});


		CodeTypeDeclaration expected = new CodeTypeDeclaration(
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
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									new CodeVariableReferenceExpression(Object.class, "data")
								}))
					})
			});

		// flag the expression as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(0)).getExpression()).getArguments().get(1).setHasParens(true);

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
								"htmlEncode",
								new CodeExpression[] {
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									new CodePropertyReferenceExpression(
										new ScriptVariableReferenceExpression("Math"),
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
									new AttributeNode("test", new ExpressionNode("data === 0"))
								},
								new Node[] {
									new LiteralNode("zero")
								}),
							new IFCommandNode(
								new AttributeNode[] {
									new AttributeNode("test", new ExpressionNode("data === 1"))
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.IDENTITY_EQUALITY,
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression(0.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										"append",
										new CodeExpression[] {
											new CodePrimitiveExpression("zero")
										}))
							},
							new CodeStatement[] {
								new CodeConditionStatement(
									new CodeBinaryOperatorExpression(
										CodeBinaryOperatorType.IDENTITY_EQUALITY,
										new CodeVariableReferenceExpression(Object.class, "data"),
										new CodePrimitiveExpression(1.0)),
									new CodeStatement[] {
										new CodeExpressionStatement(
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression(Appendable.class, "output"),
												"append",
												new CodeExpression[] {
													new CodePrimitiveExpression("one")
												}))
									},
									new CodeStatement[] {
										new CodeExpressionStatement(
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression(Appendable.class, "output"),
												"append",
												new CodeExpression[] {
													new CodePrimitiveExpression("many")
												}))
									})
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					})
			});

		// flag the conditions as having had parens
		((CodeConditionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(1)).getCondition().setHasParens(true);
		((CodeConditionStatement)((CodeConditionStatement)((CodeMethod)expected.getMembers().get(3)).getStatements().get(1)).getFalseStatements().getLastStatement()).getCondition().setHasParens(true);

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
							new AttributeNode("test", new StatementNode("return data == 0;"))
						},
						new Node[] {
							new LiteralNode("zero")
						}),
					new IFCommandNode(
						new AttributeNode[] {
							new AttributeNode("test", new StatementNode("return data == 1;"))
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.VALUE_EQUALITY,
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression(0.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										"append",
										new CodeExpression[] {
											new CodePrimitiveExpression("zero")
										}))
							},
							null),
						new CodeConditionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.VALUE_EQUALITY,
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression(1.0)),
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										"append",
										new CodeExpression[] {
											new CodePrimitiveExpression("one")
										}))
							},
							null),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("many")
								})),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("</div>")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationArrayTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("each", new ExpressionNode("data.items"))
						},
						new Node[] {
							new LiteralNode("item "),
							new ExpressionNode("index")
						})
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeVariableDeclarationStatement(
							Collection.class,
							"items_1",// collection
							new CodeMethodInvokeExpression(
								new CodeThisReferenceExpression(),
								"asArray",
								new CodeExpression[] {
									new CodePropertyReferenceExpression(
										new CodeVariableReferenceExpression(Object.class, "data"),
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
										new CodeVariableReferenceExpression(Collection.class, "items_1"),
										"size",
										null)),
							}),
						new CodeIterationStatement(
							new CodeVariableDeclarationStatement(
								Iterator.class,
								"iterator_4",
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression(Collection.class, "items_1"),
									"iterator",
									null)),// initStatement
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
								"hasNext",
								null),// testExpression
							new CodeExpressionStatement(
								new CodeUnaryOperatorExpression(
									CodeUnaryOperatorType.POST_INCREMENT,
									new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeThisReferenceExpression(),
										"render_2",
										new CodeExpression[] {
											new CodeVariableReferenceExpression(Appendable.class, "output"),
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
												"next",
												null),
											new CodeVariableReferenceExpression(int.class, "index_2"),
											new CodeVariableReferenceExpression(int.class, "count_3"),
											new CodePrimitiveExpression(null)
										}))
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
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
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									"append",
									new CodeExpression[] {
										new CodePrimitiveExpression("item ")
									})),
							new CodeExpressionStatement(
								new CodeMethodInvokeExpression(
									new CodeThisReferenceExpression(),
									"write",
									new CodeExpression[] {
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										new CodeVariableReferenceExpression(int.class, "index")
									}))
					})
			});

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationObjectTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("in", new ExpressionNode("data.foo"))
						},
						new Node[] {
							new LiteralNode("item "),
							new ExpressionNode("index")
						})
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeVariableDeclarationStatement(
							Collection.class,
							"items_1",// collection
							new CodeMethodInvokeExpression(
								new CodeThisReferenceExpression(),
								"asObject",
								new CodeExpression[] {
									new CodePropertyReferenceExpression(
										new CodeVariableReferenceExpression(Object.class, "data"),
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
										new CodeVariableReferenceExpression(Collection.class, "items_1"),
										"size",
										null)),
							}),
						new CodeIterationStatement(
							new CodeVariableDeclarationStatement(
								Iterator.class,
								"iterator_4",
								new CodeMethodInvokeExpression(
									new CodeVariableReferenceExpression(Collection.class, "items_1"),
									"iterator",
									null)),// initStatement
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
								"hasNext",
								null),// testExpression
							new CodeExpressionStatement(
								new CodeUnaryOperatorExpression(
									CodeUnaryOperatorType.POST_INCREMENT,
									new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
							new CodeStatement[] {
								new CodeVariableDeclarationStatement(
									Map.Entry.class,
									"entry_5",
									new CodeMethodInvokeExpression(
										new CodeVariableReferenceExpression(Iterator.class, "iterator_4"),
										"next",
										null)),
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeThisReferenceExpression(),
										"render_2",
										new CodeExpression[] {
											new CodeVariableReferenceExpression(Appendable.class, "output"),
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
												"getValue",
												null),
											new CodeVariableReferenceExpression(int.class, "index_2"),
											new CodeVariableReferenceExpression(int.class, "count_3"),
											new CodeMethodInvokeExpression(
												new CodeVariableReferenceExpression(Map.Entry.class, "entry_5"),
												"getKey",
												null)
										}))
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
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
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									"append",
									new CodeExpression[] {
										new CodePrimitiveExpression("item ")
									})),
							new CodeExpressionStatement(
								new CodeMethodInvokeExpression(
									new CodeThisReferenceExpression(),
									"write",
									new CodeExpression[] {
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										new CodeVariableReferenceExpression(int.class, "index")
									}))
					})
			});

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void iterationCountTest() throws IOException {

		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("example"))
			},
			new Node[] {
				new ElementNode("div", null, new Node[] {
					new FORCommandNode(
						new AttributeNode[] {
							new AttributeNode("count", new ExpressionNode("4")),
							new AttributeNode("data", new ExpressionNode("data.name")),
						},
						new Node[] {
							new LiteralNode("item "),
							new ExpressionNode("index")
						})
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div>")
								})),
						new CodeVariableDeclarationStatement(
							Object.class,
							"data_1",// data
							new CodePropertyReferenceExpression(
								new CodeVariableReferenceExpression(Object.class, "data"),
								new CodePrimitiveExpression("name"))),
						new CodeIterationStatement(
							new CodeVariableCompoundDeclarationStatement(
								new CodeVariableDeclarationStatement[] {
									new CodeVariableDeclarationStatement(
										int.class,
										"index_2",// index
										new CodePrimitiveExpression(0)),
									new CodeVariableDeclarationStatement(
										int.class,
										"count_3",// count
										new CodePrimitiveExpression(4))
								}),// initStatement
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.LESS_THAN,
								new CodeVariableReferenceExpression(int.class, "index_2"),
								new CodeVariableReferenceExpression(int.class, "count_3")),// testExpression
							new CodeExpressionStatement(
								new CodeUnaryOperatorExpression(
									CodeUnaryOperatorType.POST_INCREMENT,
									new CodeVariableReferenceExpression(int.class, "index_2"))),// incrementStatement
							new CodeStatement[] {
								new CodeExpressionStatement(
									new CodeMethodInvokeExpression(
										new CodeThisReferenceExpression(),
										"render_2",
										new CodeExpression[] {
											new CodeVariableReferenceExpression(Appendable.class, "output"),
											new CodeVariableReferenceExpression(Object.class, "data_1"),
											new CodeVariableReferenceExpression(int.class, "index_2"),
											new CodeVariableReferenceExpression(int.class, "count_3"),
											new CodePrimitiveExpression(null)
										}))
							}),
						new CodeExpressionStatement(
							new CodeMethodInvokeExpression(
								new CodeVariableReferenceExpression(Appendable.class, "output"),
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
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									"append",
									new CodeExpression[] {
										new CodePrimitiveExpression("item ")
									})),
							new CodeExpressionStatement(
								new CodeMethodInvokeExpression(
									new CodeThisReferenceExpression(),
									"write",
									new CodeExpression[] {
										new CodeVariableReferenceExpression(Appendable.class, "output"),
										new CodeVariableReferenceExpression(int.class, "index")
									}))
					})
			});

		// mark as having had parens
		((CodeMethodInvokeExpression)((CodeExpressionStatement)((CodeMethod)expected.getMembers().get(4)).getStatements().get(1)).getExpression()).getArguments().get(1).setHasParens(true);

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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
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
			AccessModifierType.PUBLIC,
			"foo.bar",
			"Blah",
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
								new CodeVariableReferenceExpression(Appendable.class, "output"),
								"append",
								new CodeExpression[] {
									new CodePrimitiveExpression("<div></div>")
								}))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}

	@Test
	public void callViewTest() throws IOException {
		ViewRootNode input = new ViewRootNode(
			new AttributeNode[] {
				new AttributeNode("name", new LiteralNode("foo.bar.Blah"))
			},
			new Node[] {
				new CALLCommandNode(new AttributeNode[] {
					new AttributeNode("view", new LiteralNode("foo.bar.Yada")),
					new AttributeNode("data", new ExpressionNode("data"))
				})
			});

		CodeTypeDeclaration expected = new CodeTypeDeclaration(
			AccessModifierType.PUBLIC,
			"foo.bar",
			"Blah",
			new CodeMember[] {
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
								new CodeFieldReferenceExpression(
									new CodeThisReferenceExpression(),
									org.duelengine.duel.DuelView.class,
									"view_2"),
								"render",
								new CodeExpression[] {
									new CodeVariableReferenceExpression(Appendable.class, "output"),
									new CodeVariableReferenceExpression(Object.class, "data"),
									new CodeVariableReferenceExpression(int.class, "index"),
									new CodeVariableReferenceExpression(int.class, "count"),
									new CodeVariableReferenceExpression(String.class, "key")
								}))
					}),
				new CodeField(
						AccessModifierType.PRIVATE,
						org.duelengine.duel.DuelView.class,
						"view_2",
						null
					),
				new CodeMethod(
					AccessModifierType.PROTECTED,
					Void.class,
					"init",
					null,
					new CodeStatement[] {
						new CodeExpressionStatement(
							new CodeBinaryOperatorExpression(
								CodeBinaryOperatorType.ASSIGN,
								new CodeFieldReferenceExpression(
									new CodeThisReferenceExpression(),
									org.duelengine.duel.DuelView.class,
									"view_2"),
								new CodeObjectCreateExpression(
									"foo.bar.Yada",
									new CodeExpression[] {
										new CodeThisReferenceExpression()
									})))
					})
			});

		CodeTypeDeclaration actual = new CodeDOMBuilder().build(input);

		assertEquals(expected, actual);
	}
}
