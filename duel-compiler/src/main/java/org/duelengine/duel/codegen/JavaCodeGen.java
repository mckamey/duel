package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

/**
 * Code generator which emits Java source from CodeDOM classes
 * Inherently thread-safe as contains no mutable instance data.
 */
public class JavaCodeGen implements CodeGenerator {

	private enum ParensSetting {
		AUTO,
		FORCE,
		SUPPRESS
	}

	private static final String DUEL_PACKAGE = DuelView.class.getPackage().getName();
	private final CodeGenSettings settings;

	public JavaCodeGen() {
		this(null);
	}

	public JavaCodeGen(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
	}

	@Override
	public String getFileExtension() {
		return ".java";
	}

	/**
	 * Generates server-side code for the given views
	 * @param output
	 * @param views
	 * @throws IOException 
	 */
	@Override
	public void write(Appendable output, VIEWCommandNode... views)
		throws IOException {

		this.write(output, views != null ? Arrays.asList(views) : null);
	}

	/**
	 * Generates server-side code for the given views
	 * @param output
	 * @param views
	 * @throws IOException
	 */
	@Override
	public void write(Appendable output, Iterable<VIEWCommandNode> views)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}
		if (views == null) {
			throw new NullPointerException("views");
		}

		boolean importsWritten = false;
		for (VIEWCommandNode view : views) {
			if (view != null) {
				CodeTypeDeclaration viewType = new CodeDOMBuilder(this.settings).buildView(view);

				if (importsWritten) {
					this.writeln(output, 0);
				} else {
					this.writePackage(output, viewType.getNamespace());
					importsWritten = true;
				}

				this.writeTypeDeclaration(output, viewType, 0);
				this.writeln(output, 0);
			}
		}
	}

	public void writeCode(Appendable output, CodeObject code)
		throws IOException {

		if (code instanceof CodeTypeDeclaration) {
			CodeTypeDeclaration viewType = (CodeTypeDeclaration)code;
			this.writePackage(output, viewType.getNamespace());
			this.writeTypeDeclaration(output, viewType, 0);
			this.writeln(output, 0);

		} else if (code instanceof CodeStatement) {
			this.writeStatement(output, (CodeStatement)code, 0);

		} else if (code instanceof CodeExpression) {
			this.writeExpression(output, (CodeExpression)code);

		} else if (code instanceof CodeMember) {
			this.writeMember(output, "Ctor", (CodeMember)code, 0);

		} else if (code instanceof CodeStatementBlock) {
			output.append('{');
			for (CodeStatement statement : ((CodeStatementBlock)code).getStatements()) {
				this.writeStatement(output, statement, 1);
			}
			this.writeln(output, 0);
			output.append('}');

		} else if (code != null) {
			throw new UnsupportedOperationException("Not implemented: "+code.getClass());
		}
	}

	private void writeTypeDeclaration(Appendable output, CodeTypeDeclaration type, int depth)
		throws IOException {

		// write intro
		this.writeAccessModifier(output, type.getAccess());
		output.append("class ").append(type.getTypeName());
		Class<?> baseType = type.getBaseType();
		if (baseType != null) {
			output.append(" extends ");
			this.writeTypeName(output, baseType);
		}
		output.append(" {");
		depth++;

		String typeName = type.getTypeName();
		for (CodeMember member : type.getMembers()) {
			this.writeln(output, depth, 2);
			this.writeMember(output, typeName, member, depth);
		}

		// write outro
		depth--;
		this.writeln(output, depth);
		output.append('}');
	}

	private void writeMember(Appendable output, String typeName, CodeMember member, int depth)
		throws IOException {

		if (member instanceof CodeConstructor) {
			this.writeConstructor(output, (CodeConstructor)member, typeName, depth);

		} else if (member instanceof CodeMethod) {
			this.writeMethod(output, (CodeMethod)member, depth);

		} else if (member instanceof CodeField) {
			this.writeField(output, (CodeField)member);

		} else if (member instanceof CodeTypeDeclaration) {
			this.writeTypeDeclaration(output, (CodeTypeDeclaration)member, depth);

		} else if (member != null) {
			throw new UnsupportedOperationException("Not implemented: "+member.getClass());
		}
	}

	private void writeConstructor(Appendable output, CodeConstructor ctor, String typeName, int depth)
		throws IOException {

		this.writeAccessModifier(output, ctor.getAccess());
		output.append(typeName).append("(");

		boolean needsDelim = false;
		for (CodeParameterDeclarationExpression param : ctor.getParameters()) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			writeParameterDeclaration(output, param);
		}
		output.append(") {");
		depth++;
		List<CodeExpression> args = ctor.getBaseCtorArgs();
		if (args.size() > 0) {
			this.writeln(output, depth);
			output.append("super(");
			needsDelim = false;
			for (CodeExpression expression : args) {
				if (needsDelim) {
					output.append(", ");
				} else {
					needsDelim = true;
				}
				this.writeExpression(output, expression);
			}
			output.append(");");
		} else {
			args = ctor.getChainedCtorArgs();
			if (args.size() > 0) {
				this.writeln(output, depth);
				output.append("this(");
				needsDelim = false;
				for (CodeExpression expression : args) {
					if (needsDelim) {
						output.append(", ");
					} else {
						needsDelim = true;
					}
					this.writeExpression(output, expression);
				}
				output.append(");");
			}
		}

		for (CodeStatement statement : ctor.getStatements()) {
			this.writeStatement(output, statement, depth);
		}
		depth--;
		this.writeln(output, depth);
		output.append("}");
	}

	private void writeMethod(Appendable output, CodeMethod method, int depth)
		throws IOException {

		if (method.isOverride()) {
			output.append("@Override");
			this.writeln(output, depth);
		}
		this.writeAccessModifier(output, method.getAccess());
		this.writeTypeName(output, method.getReturnType());
		output.append(' ').append(method.getName()).append('(');

		boolean needsDelim = false;
		for (CodeParameterDeclarationExpression param : method.getParameters()) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			this.writeParameterDeclaration(output, param);
		}
		output.append(')');
		needsDelim = false;
		for (Class<?> exception : method.getThrows()) {
			if (needsDelim) {
				output.append(", ");
			} else {
				output.append(" throws ");
				needsDelim = true;
			}
			this.writeTypeName(output, exception);
		}
		output.append(" {");
		depth++;
		for (CodeStatement statement : method.getStatements()) {
			this.writeStatement(output, statement, depth);
		}
		depth--;
		this.writeln(output, depth);
		output.append("}");
	}

	private void writeField(Appendable output, CodeField field)
		throws IOException {

		this.writeAccessModifier(output, field.getAccess());
		this.writeTypeName(output, field.getType());
		output.append(' ').append(field.getName());
		
		CodeExpression initExpression = field.getInitExpression();
		if (initExpression != null) {
			output.append(" = ");
			this.writeExpression(output, initExpression);
		}

		output.append(';');
	}

	private void writeParameterDeclaration(Appendable output, CodeParameterDeclarationExpression param) throws IOException {

		this.writeTypeName(output, param.getResultType());
		if (param.isVarArgs()) {
			output.append("... ");
		} else {
			output.append(' ');
		}
		output.append(param.getName());
	}

	private void writeStatement(Appendable output, CodeStatement statement, int depth)
		throws IOException {

		this.writeStatement(output, statement, depth, false);
	}

	private void writeStatement(Appendable output, CodeStatement statement, int depth, boolean inline)
		throws IOException {

		if (statement == null) {
			return;
		}

		if (!inline) {
			this.writeln(output, depth);
		}

		boolean needsSemicolon; 
		if (statement instanceof CodeExpressionStatement) {
			this.writeExpression(output, ((CodeExpressionStatement)statement).getExpression(), ParensSetting.SUPPRESS);
			needsSemicolon = true;

		} else if (statement instanceof CodeConditionStatement) {
			needsSemicolon = this.writeConditionStatement(output, (CodeConditionStatement)statement, depth);

		} else if (statement instanceof CodeVariableDeclarationStatement) {
			needsSemicolon = this.writeVariableDeclarationStatement(output, (CodeVariableDeclarationStatement)statement, inline);

		} else if (statement instanceof CodeIterationStatement) {
			needsSemicolon = this.writeIterationStatement(output, (CodeIterationStatement)statement, depth);

		} else if (statement instanceof CodeVariableCompoundDeclarationStatement) {
			needsSemicolon = this.writeVariableCompoundDeclarationStatement(output, (CodeVariableCompoundDeclarationStatement)statement, depth, inline);

		} else if (statement instanceof CodeMethodReturnStatement) {
			needsSemicolon = this.writeMethodReturn(output, (CodeMethodReturnStatement)statement);

		} else if (statement instanceof CodeCommentStatement) {
			needsSemicolon = this.writeComment(output, (CodeCommentStatement)statement);

		} else {
			throw new UnsupportedOperationException("Statement not yet supported: "+statement.getClass());
		}

		if (needsSemicolon && !inline) {
			output.append(';');
		}
	}

	private boolean writeComment(Appendable output, CodeCommentStatement statement)
		throws IOException {

		String comment = statement.getValue();
		comment = (comment != null) ? comment.replace("*/", "*\\/") : "";
		output.append("/*").append(comment).append("*/");
		
		return false;
	}

	private boolean writeMethodReturn(Appendable output, CodeMethodReturnStatement statement)
		throws IOException {

		output.append("return");
		CodeExpression expr = statement.getExpression();
		if (expr != null) {
			output.append(' ');
			this.writeExpression(output, expr);
		}
			
		return true;
	}

	private void writeExpression(Appendable output, CodeExpression expression)
		throws IOException {

		this.writeExpression(output, expression, ParensSetting.AUTO);
	}

	private void writeExpression(Appendable output, CodeExpression expression, ParensSetting parens)
		throws IOException {

		if (expression == null) {
			return;
		}

		boolean needsParens;
		switch (parens) {
			case FORCE:
				needsParens = true;
				break;
			case SUPPRESS:
				needsParens = false;
				break;
			default:
				needsParens = expression.hasParens();
				break;
		}

		if (expression instanceof CodePrimitiveExpression) {
			this.writePrimitive(output, ((CodePrimitiveExpression)expression).getValue());

		} else if (expression instanceof CodeVariableReferenceExpression) {
			output.append(((CodeVariableReferenceExpression)expression).getIdent());

		} else if (expression instanceof CodeBinaryOperatorExpression) {
			if (needsParens) {
				output.append('(');
			}
			this.writeBinaryOperator(output, (CodeBinaryOperatorExpression)expression);
			if (needsParens) {
				output.append(')');
			}

		} else if (expression instanceof CodeUnaryOperatorExpression) {
			if (needsParens) {
				output.append('(');
			}
			this.writeUnaryOperator(output, (CodeUnaryOperatorExpression)expression);
			if (needsParens) {
				output.append(')');
			}

		} else if (expression instanceof CodePropertyReferenceExpression) {
			this.writePropertyReference(output, (CodePropertyReferenceExpression)expression);

		} else if (expression instanceof CodeTernaryOperatorExpression) {
			if (needsParens) {
				output.append('(');
			}
			this.writeTernaryOperator(output, (CodeTernaryOperatorExpression)expression);
			if (needsParens) {
				output.append(')');
			}

		} else if (expression instanceof CodeArrayCreateExpression) {
			this.writeArrayCreate(output, (CodeArrayCreateExpression)expression);

		} else if (expression instanceof CodeMethodInvokeExpression) {
			this.writeMethodInvoke(output, (CodeMethodInvokeExpression)expression);

		} else if (expression instanceof CodeFieldReferenceExpression) {
			this.writeFieldReference(output, (CodeFieldReferenceExpression)expression);

		} else if (expression instanceof CodeTypeReferenceExpression) {
			this.writeTypeName(output, ((CodeTypeReferenceExpression)expression).getResultType());

		} else if (expression instanceof CodeThisReferenceExpression) {
			output.append("this");

		} else if (expression instanceof CodeParameterDeclarationExpression) {
			this.writeParameterDeclaration(output, (CodeParameterDeclarationExpression)expression);

		} else if (expression instanceof CodeObjectCreateExpression) {
			this.writeObjectCreate(output, (CodeObjectCreateExpression)expression);

		} else if (expression instanceof CodeCastExpression) {
			if (needsParens) {
				output.append('(');
			}
			this.writeCast(output, (CodeCastExpression)expression);
			if (needsParens) {
				output.append(')');
			}

		} else if (expression instanceof ScriptVariableReferenceExpression) {
			this.writeExtraDataReference(output, (ScriptVariableReferenceExpression)expression);
			
		} else if (expression != null) {
			throw new UnsupportedOperationException("Unexpected expression: "+expression.getClass());
		}
	}

	private void writeFieldReference(Appendable output, CodeFieldReferenceExpression expression)
		throws IOException {

		this.writeExpression(output, expression.getTarget());
		output.append('.').append(expression.getFieldName());
	}

	private void writePropertyReference(Appendable output, CodePropertyReferenceExpression expression)
		throws IOException {

		// translate into dynamic helper method call
		this.writeExpression(
			output,
			new CodeMethodInvokeExpression(
				expression.getResultType(),
				new CodeThisReferenceExpression(),
				"getProperty",
				expression.getTarget(),
				expression.getPropertyName()));
	}

	private void writeBinaryOperator(Appendable output, CodeBinaryOperatorExpression expression)
		throws IOException {

		boolean asNumber = true,
			asString = false,
			isAssign = false;

		CodeExpression left = expression.getLeft();
		CodeExpression right = expression.getRight();

		boolean leftIsPropertyRef = (left instanceof CodePropertyReferenceExpression);
		boolean leftIsExtraRef = (left instanceof ScriptVariableReferenceExpression);

		String operator;
		switch (expression.getOperator()) {
			case IDENTITY_EQUALITY:
				if (!CodePrimitiveExpression.NULL.equals(left) &&
					!CodePrimitiveExpression.NULL.equals(right)) {
					this.writeExpression(output, CodeDOMUtility.equal(left, right));
					return;
				}
				operator = " == ";
				asNumber = false;
				break;
			case IDENTITY_INEQUALITY:
				if (!CodePrimitiveExpression.NULL.equals(left) &&
					!CodePrimitiveExpression.NULL.equals(right)) {

					this.writeExpression(output, CodeDOMUtility.notEqual(left, right));
					return;
				}
				operator = " != ";
				asNumber = false;
				break;
			case VALUE_EQUALITY:
				this.writeExpression(output, CodeDOMUtility.coerceEqual(left, right));
				return;
			case VALUE_INEQUALITY:
				this.writeExpression(output, CodeDOMUtility.coerceNotEqual(left, right));
				return;
			case GREATER_THAN:
				operator = " > ";
				break;
			case GREATER_THAN_OR_EQUAL:
				operator = " >= ";
				break;
			case LESS_THAN:
				operator = " < ";
				break;
			case LESS_THAN_OR_EQUAL:
				operator = " <= ";
				break;
			case ASSIGN:
				operator = " = ";
				isAssign = true;
				break;
			case ADD:
				operator = " + ";
				Class<?> leftType = CodeDOMUtility.toPrimitive(left.getResultType());
				Class<?> rightType = CodeDOMUtility.toPrimitive(right.getResultType());

				// asString trumps asNumber
				asString = DuelData.isString(leftType) || DuelData.isString(rightType);
				asNumber = !asString;
				break;
			case ADD_ASSIGN:
				// asString trumps asNumber
				asString = DuelData.isString(CodeDOMUtility.toPrimitive(left.getResultType()));
				this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.ADD, left,
					asString ? CodeDOMUtility.ensureString(left) : CodeDOMUtility.ensureNumber(left),
					asString ? CodeDOMUtility.ensureString(right) : CodeDOMUtility.ensureNumber(right)));
				return;
			case SUBTRACT:
				operator = " - ";
				break;
			case SUBTRACT_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.SUBTRACT,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " -= ";
				isAssign = true;
				break;
			case MULTIPLY:
				operator = " * ";
				break;
			case MULTIPLY_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.MULTIPLY,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " *= ";
				isAssign = true;
				break;
			case DIVIDE:
				operator = " / ";
				break;
			case DIVIDE_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.DIVIDE,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " /= ";
				isAssign = true;
				break;
			case MODULUS:
				operator = " % ";
				break;
			case MODULUS_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.MODULUS,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " %= ";
				isAssign = true;
				break;
			case BITWISE_AND:
				operator = " & ";
				break;
			case BITWISE_AND_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.BITWISE_AND,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " &= ";
				isAssign = true;
				break;
			case BITWISE_OR:
				operator = " | ";
				break;
			case BITWISE_OR_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.BITWISE_OR,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " |= ";
				isAssign = true;
				break;
			case BITWISE_XOR:
				operator = " ^ ";
				break;
			case BITWISE_XOR_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.BITWISE_XOR,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " ^= ";
				isAssign = true;
				break;
			case BOOLEAN_AND:
				if (!DuelData.isBoolean(left.getResultType()) || !DuelData.isBoolean(right.getResultType())) {
					// convert to JavaScript semantics for boolean AND
					this.writeExpression(output,
						new CodeMethodInvokeExpression(
							Object.class,
							new CodeThisReferenceExpression(),
							"LogicalAND",
							left,
							right));
					return;
				}
				operator = " && ";
				asNumber = false;
				break;
			case BOOLEAN_OR:
				if (!DuelData.isBoolean(left.getResultType()) || !DuelData.isBoolean(right.getResultType())) {
					// convert to JavaScript semantics for boolean OR
					this.writeExpression(output,
						new CodeMethodInvokeExpression(
							Object.class,
							new CodeThisReferenceExpression(),
							"LogicalOR",
							left,
							right));
					return;
				}
				operator = " || ";
				asNumber = false;
				break;
			case SHIFT_LEFT:
				operator = " << ";
				break;
			case SHIFT_LEFT_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.SHIFT_LEFT,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " <<= ";
				isAssign = true;
				break;
			case SHIFT_RIGHT:
				operator = " >> ";
				break;
			case SHIFT_RIGHT_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.SHIFT_RIGHT,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " >>= ";
				isAssign = true;
				break;
			case USHIFT_RIGHT:
				operator = " >>> ";
				break;
			case USHIFT_RIGHT_ASSIGN:
				if (leftIsPropertyRef || leftIsExtraRef || !CodeDOMUtility.isNumber(left)) {
					this.writeExpression(output, CodeDOMUtility.asAssignment(CodeBinaryOperatorType.USHIFT_RIGHT,
						left, CodeDOMUtility.ensureNumber(left), CodeDOMUtility.ensureNumber(right)));
					return;
				}
				operator = " >>>= ";
				isAssign = true;
				break;
			default:
				throw new UnsupportedOperationException("Unknown binary operator: "+expression.getOperator());
		}

		if (isAssign) {
			if (leftIsPropertyRef) {
				// translate into dynamic helper method call
				CodePropertyReferenceExpression leftPropRef = (CodePropertyReferenceExpression)left; 
				this.writeExpression(
					output,
					new CodeMethodInvokeExpression(
						expression.getResultType(),
						new CodeThisReferenceExpression(),
						"setProperty",
						leftPropRef.getTarget(),
						leftPropRef.getPropertyName(),
						right));
				return;
			} else if (leftIsExtraRef) {
				// translate into dynamic helper method call
				ScriptVariableReferenceExpression leftVarRef = (ScriptVariableReferenceExpression)left; 
				this.writeExpression(
					output,
					new CodeMethodInvokeExpression(
						expression.getResultType(),
						new CodeThisReferenceExpression(),
						"putExtra",
						new CodeVariableReferenceExpression(DuelContext.class, "context"),
						new CodePrimitiveExpression(leftVarRef.getIdent()),
						right));
				return;
			}

			this.writeExpression(output, left);

		} else if (asString) {
			this.writeExpression(output, CodeDOMUtility.ensureString(left));
		} else if (asNumber) {
			this.writeExpression(output, CodeDOMUtility.ensureNumber(left));
		} else {
			this.writeExpression(output, left);
		}

		output.append(operator);

		if (isAssign) {
			this.writeExpression(output, CodeDOMUtility.ensureType(left.getResultType(), right));
		} else if (asString) {
			this.writeExpression(output, CodeDOMUtility.ensureString(right));
		} else if (asNumber) {
			this.writeExpression(output, CodeDOMUtility.ensureNumber(right));
		} else {
			this.writeExpression(output, right);
		}
	}

	private void writeUnaryOperator(Appendable output, CodeUnaryOperatorExpression expression)
		throws IOException {

		CodeExpression expr = expression.getExpression(); 

		String operator;
		boolean isPost = false;
		switch (expression.getOperator()) {
			case LOGICAL_NEGATION:
				operator = "!";
				expr = CodeDOMUtility.ensureBoolean(expr);
				break;
			case BITWISE_NEGATION:
				operator = "~";
				expr = CodeDOMUtility.ensureNumber(expr);
				break;
			case NEGATION:
				operator = "-";
				expr = CodeDOMUtility.ensureNumber(expr);
				break;
			case POSITIVE:
				operator = "+";
				expr = CodeDOMUtility.ensureNumber(expr);
				break;
			case POST_DECREMENT:
				if (!CodeDOMUtility.isNumber(expr)) {
					// convert into type safe expression
					// data-- => echo(asNumber(data), (data = asNumber(data)-1))
					this.writeExpression(output, CodeDOMUtility.safePostDecrement(expr));
					return;
				}

				// statically type safe
				operator = "--";
				isPost = true;
				break;
			case POST_INCREMENT:
				if (!CodeDOMUtility.isNumber(expr)) {
					// convert into type safe expression
					// data-- => echo(asNumber(data), (data = asNumber(data)+1))
					this.writeExpression(output, CodeDOMUtility.safePostIncrement(expr));
					return;
				}

				// statically type safe
				operator = "++";
				isPost = true;
				break;
			case PRE_DECREMENT:
				if (!CodeDOMUtility.isNumber(expr)) {
					// convert into type safe expression
					// --data => (data = (asNumber(data)-1))
					this.writeExpression(output, CodeDOMUtility.safePreDecrement(expr));
					return;
				}

				// statically type safe
				operator = "--";
				break;
			case PRE_INCREMENT:
				if (!CodeDOMUtility.isNumber(expr)) {
					// convert into type safe expression
					// ++data => (data = (asNumber(data)+1))
					this.writeExpression(output, CodeDOMUtility.safePreIncrement(expr));
					return;
				}

				// statically type safe
				operator = "++";
				break;
			default:
				throw new UnsupportedOperationException("Unary operator not yet supported: "+expression.getOperator());
		}

		if (isPost) {
			this.writeExpression(output, expr);
			output.append(operator);
		} else {
			output.append(operator);
			this.writeExpression(output, expr);
		}
	}

	private void writeTernaryOperator(Appendable output, CodeTernaryOperatorExpression expression)
		throws IOException {

		this.writeExpression(output, CodeDOMUtility.ensureBoolean(expression.getTestExpression()));
		output.append(" ? ");
		this.writeExpression(output, expression.getTrueExpression());
		output.append(" : ");
		this.writeExpression(output, expression.getFalseExpression());
	}

	private boolean writeVariableDeclarationStatement(Appendable output, CodeVariableDeclarationStatement statement, boolean inline)
		throws IOException {

		this.writeTypeName(output, statement.getType());
		output.append(' ').append(statement.getName());
		CodeExpression initExpr = statement.getInitExpression();
		if (initExpr != null) {
			if (inline) {
				output.append('=');
			} else {
				output.append(" = ");
			}
			this.writeExpression(output, initExpr);
		}

		return true;
	}

	private boolean writeVariableCompoundDeclarationStatement(Appendable output, CodeVariableCompoundDeclarationStatement statement, int depth, boolean inline)
		throws IOException {

		List<CodeVariableDeclarationStatement> vars = statement.getVars();
		if (vars.size() < 1) {
			return false;
		}

		this.writeTypeName(output, vars.get(0).getType());
		output.append(' ');
		depth++;
		boolean needsDelim = false;
		for (CodeVariableDeclarationStatement varRef : vars) {
			if (needsDelim) {
				output.append(',');
				if (inline) {
					output.append(' ');
				} else {
					this.writeln(output, depth);
				}
			} else {
				needsDelim = true;
			}
			output.append(varRef.getName());
			CodeExpression initExpr = varRef.getInitExpression();
			if (initExpr != null) {
				if (inline) {
					output.append('=');
				} else {
					output.append(" = ");
				}
				this.writeExpression(output, initExpr);
			}
		}
		depth--;
		return true;
	}

	private void writeExtraDataReference(Appendable output, ScriptVariableReferenceExpression expression)
		throws IOException {

		this.writeExpression(output, CodeDOMUtility.lookupExtraVar(expression.getIdent()));
	}

	private boolean writeConditionStatement(Appendable output, CodeConditionStatement statement, int depth)
		throws IOException {

		output.append("if (");
		this.writeExpression(output, CodeDOMUtility.ensureBoolean(statement.getCondition()), ParensSetting.SUPPRESS);
		output.append(") {");
		depth++;

		for (CodeStatement trueStatement : statement.getTrueStatements()) {
			this.writeStatement(output, trueStatement, depth);
		}

		depth--;
		this.writeln(output, depth);
		output.append('}');

		if (statement.getFalseStatements().size() > 0) {
			boolean nestedConditionals =
				(statement.getFalseStatements().size() == 1) &&
				(statement.getFalseStatements().getLastStatement() instanceof CodeConditionStatement);

			output.append(" else ");
			if (!nestedConditionals) {
				output.append('{');
				depth++;
				for (CodeStatement falseStatement : statement.getFalseStatements()) {
					this.writeStatement(output, falseStatement, depth);
				}
				depth--;
				this.writeln(output, depth);
				output.append('}');
			} else {
				this.writeConditionStatement(output, (CodeConditionStatement)statement.getFalseStatements().getLastStatement(), depth);
			}
		}

		return false;
	}

	private boolean writeIterationStatement(Appendable output, CodeIterationStatement statement, int depth)
		throws IOException {

		output.append("for (");

		this.writeStatement(output, statement.getInitStatement(), depth, true);
		output.append("; ");
		this.writeExpression(output, CodeDOMUtility.ensureBoolean(statement.getTestExpression()));
		output.append("; ");
		this.writeStatement(output, statement.getIncrementStatement(), depth, true);

		output.append(") {");
		depth++;
		for (CodeStatement childStatement : statement.getStatements()) {
			this.writeStatement(output, childStatement, depth);
		}
		depth--;
		this.writeln(output, depth);
		output.append('}');

		return false;
	}

	private void writeMethodInvoke(Appendable output, CodeMethodInvokeExpression expression)
		throws IOException {

		String methodName = expression.getMethodName();
		this.writeExpression(output, expression.getTarget());
		output.append('.').append(methodName).append('(');
		boolean needsDelim = false;
		List<CodeExpression> args = expression.getArguments();
		boolean singleArg = (args.size() == 1);
		for (CodeExpression arg : args) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			this.writeExpression(output, arg, singleArg ? ParensSetting.SUPPRESS : ParensSetting.AUTO);
		}
		output.append(')');
	}

	private void writeCast(Appendable output, CodeCastExpression expression)
		throws IOException {

		output.append('(');
		this.writeTypeName(output, expression.getResultType());
		output.append(')');
		this.writeExpression(output, expression.getExpression(), ParensSetting.FORCE);
	}

	private void writeTypeName(Appendable output, Class<?> type)
		throws IOException {

		String typeName;
		if (type == Void.class) {
			typeName = "void";
		} else {
			Package pkg = type.getPackage();
			String pkgName = (pkg == null) ? null : pkg.getName();
			if (pkgName == null || "java.lang".equals(pkgName) || "java.io".equals(pkgName) || "java.util".equals(pkgName) || DUEL_PACKAGE.equals(pkgName)) {
				typeName = type.getSimpleName();
			} else {
				typeName = type.getName();
			}
		}
		output.append(typeName);
	}

	private void writeAccessModifier(Appendable output, AccessModifierType access)
		throws IOException {
		
		switch (access) {
			case PRIVATE:
				output.append("private ");
				break;
			case PROTECTED:
				output.append("protected ");
				break;
			case PUBLIC:
				output.append("public ");
				break;
		}
	}

	private void writeArrayCreate(Appendable output, CodeArrayCreateExpression expression)
		throws IOException {

		List<CodeExpression> args = expression.getInitializers();
		if (args.size() < 1) {
			output.append("new ArrayList<");
			this.writeTypeName(output, expression.getType());
			output.append(">(");
			if (expression.getSize() > 0) {
				output.append(Integer.toString(expression.getSize()));
			}
			output.append(")");
			return;
		}

		output.append("Arrays.asList(");
		boolean needsDelim = false;
		boolean singleArg = (args.size() == 1);
		for (CodeExpression arg : args) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			this.writeExpression(output, arg, singleArg ? ParensSetting.SUPPRESS : ParensSetting.AUTO);
		}
		output.append(")");
	}

	private void writeObjectCreate(Appendable output, CodeObjectCreateExpression expression)
		throws IOException {

		output.append("new ").append(expression.getTypeName()).append('(');
		boolean needsDelim = false;
		List<CodeExpression> args = expression.getArguments();
		boolean singleArg = (args.size() == 1);
		for (CodeExpression arg : args) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			this.writeExpression(output, arg, singleArg ? ParensSetting.SUPPRESS : ParensSetting.AUTO);
		}
		output.append(')');
	}

	private void writePackage(Appendable output, String ns)
		throws IOException {

		if (ns != null && ns.length() > 0) {
			output.append("package ").append(ns).append(";");
			this.writeln(output, 0, 2);
		}

		output.append("import java.io.*;");
		this.writeln(output, 0);
		output.append("import java.util.*;");
		this.writeln(output, 0);
		output.append("import java.util.Map.Entry;");
		this.writeln(output, 0);
		output.append("import ").append(DUEL_PACKAGE).append(".*;");
		this.writeln(output, 0, 2);
	}

	private void writePrimitive(Appendable output, Object value)
		throws IOException {

		if (value == null) {
			output.append("null");
			return;
		}

		Class<?> type = value.getClass();
		if (String.class.equals(type)) {
			this.writeString(output, (String)value);

		} else if (DuelData.isNumber(type)) {
			double number = ((Number)value).doubleValue();
			if (Double.isNaN(number)) {
				output.append("Double.NaN");
			} else if (Double.isInfinite(number)) {
				output.append("Double.POSITIVE_INFINITY");
			} else {
				output.append(String.valueOf(value));
			}

		} else if (Character.class.equals(type)) {
			this.writeCharacter(output, (Character)value);

		} else {
			output.append(String.valueOf(value));
		}
	}

	private void writeCharacter(Appendable output, char value)
		throws IOException {

		switch (value) {
			case '\'':
				output.append("'\\''");
				break;
			case '\\':
				output.append("'\\\\'");
				break;
			case '\t':
				String indent = this.settings.getIndent();
				if (this.settings.getConvertLineEndings() && !"\t".equals(indent)) {
					this.writeString(output, indent);
				} else {
					output.append("'\\t'");
				}
				break;
			case '\n':
				String newline = this.settings.getNewline();
				if (this.settings.getConvertLineEndings() && !"\n".equals(newline)) {
					this.writeString(output, newline);
				} else {
					output.append("'\\n'");
				}
				break;
			case '\r':
				// if the source came via the DuelLexer then CRLF have been
				// compressed to single LF and these will not be present
				output.append("'\\r'");
				break;
			case '\f':
				output.append("'\\f'");
				break;
			case '\b':
				output.append("'\\b'");
				break;
			default:
				// no need to escape ASCII chars
				if (value >= ' ' && value < '\u007F') {
					output.append('\'').append(value).append('\'');
				} else {
					output.append(String.format("'\\u%04X'", (""+value).codePointAt(0)));
				}
				break;
		}
	}

	private void writeString(Appendable output, String value)
		throws IOException {

		if (value == null) {
			output.append("null");
			return;
		}

		if (this.settings.getConvertLineEndings()) {
			// not very efficient but allows simple normalization
			if (!"\t".equals(this.settings.getIndent())) {
				value = value.replace("\t", this.settings.getIndent());
			}
			// if the source came via the DuelLexer then CRLF have been
			// compressed to single LF and these will not be present
			value = value.replace("\r\n", "\n").replace("\r", "\n");
			if (!"\n".equals(this.settings.getNewline())) {
				value = value.replace("\n", this.settings.getNewline());
			}
		}

		int start = 0,
			length = value.length();

		output.append('"');

		for (int i=start; i<length; i++) {
			String escape;

			char ch = value.charAt(i);
			switch (ch) {
				case '"':
					escape = "\\\"";
					break;
				case '\\':
					escape = "\\\\";
					break;
				case '\t':
					escape = "\\t";
					break;
				case '\n':
					escape = "\\n";
					break;
				case '\r':
					escape = "\\r";
					break;
				case '\f':
					escape = "\\f";
					break;
				case '\b':
					escape = "\\b";
					break;
				default:
					if (ch >= ' ' && ch < '\u007F') {
						// no need to escape ASCII chars
						continue;
					}

					escape = String.format("\\u%04X", value.codePointAt(i));
					break;
			}

			if (i > start) {
				output.append(value, start, i);
			}
			start = i+1;

			output.append(escape);
		}

		if (length > start) {
			output.append(value, start, length);
		}

		output.append('"');
	}

	private void writeln(Appendable output, int depth)
		throws IOException {

		this.writeln(output, depth, 1);
	}

	private void writeln(Appendable output, int depth, int newlines)
		throws IOException {

		String newline = this.settings.getNewline();
		for (int i=newlines; i>0; i--) {
			output.append(newline);
		}

		String indent = this.settings.getIndent();
		for (int i=depth; i>0; i--) {
			output.append(indent);
		}
	}
}