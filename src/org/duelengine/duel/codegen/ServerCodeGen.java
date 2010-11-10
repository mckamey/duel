package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;

import org.duelengine.duel.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeGen implements CodeGenerator {

	private enum ParensSetting {
		AUTO,
		FORCE,
		SUPPRESS
	}
	
	private static final String DUEL_PACKAGE = DuelView.class.getPackage().getName();
	private final CodeGenSettings settings;
	private int depth;

	public ServerCodeGen() {
		this(null);
	}

	public ServerCodeGen(CodeGenSettings settings) {
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
	public void write(Appendable output, ViewRootNode[] views)
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
	public void write(Appendable output, Iterable<ViewRootNode> views)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}
		if (views == null) {
			throw new NullPointerException("views");
		}

		for (ViewRootNode view : views) {
			if (view != null) {
				this.write(output, view);
			}
		}
	}

	public void write(Appendable output, ViewRootNode view)
		throws IOException {

		CodeTypeDeclaration viewType = new CodeDOMBuilder(this.settings).build(view);

		this.write(output, viewType);
	}

	public void write(Appendable output, CodeTypeDeclaration type)
		throws IOException {

		this.depth = 0;
		this.writeIntro(output, type.getNamespace(), type.getAccess(), type.getTypeName(), type.getBaseType());
		for (CodeMember member : type.getMembers()) {
			this.writeln(output, 2);
			if (member instanceof CodeConstructor) {
				this.writeConstructor(output, (CodeConstructor)member, type.getTypeName());

			} else if (member instanceof CodeMethod) {
				this.writeMethod(output, (CodeMethod)member);

			} else if (member != null) {
				throw new UnsupportedOperationException("Not implemented: "+member.getClass());
			}
		}
		this.writeOutro(output);
	}

	private void writeConstructor(Appendable output, CodeConstructor ctor, String typeName)
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
			this.writeTypeName(output, param.getResultType());
			output.append(param.getName());
		}
		output.append(") {");
		this.depth++;
		List<CodeExpression> args = ctor.getBaseCtorArgs();
		if (args.size() > 0) {
			this.writeln(output);
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
				this.writeln(output);
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
			this.writeStatement(output, statement);
		}
		this.depth--;
		this.writeln(output);
		output.append("}");
	}

	private void writeMethod(Appendable output, CodeMethod method)
		throws IOException {

		this.writeAccessModifier(output, method.getAccess());
		this.writeTypeName(output, method.getReturnType());
		output.append(method.getName()).append("(");

		boolean needsDelim = false;
		for (CodeParameterDeclarationExpression param : method.getParameters()) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			this.writeTypeName(output, param.getResultType());
			output.append(param.getName());
		}
		output.append(") {");
		this.depth++;
		for (CodeStatement statement : method.getStatements()) {
			this.writeStatement(output, statement);
		}
		this.depth--;
		this.writeln(output);
		output.append("}");
	}

	private void writeStatement(Appendable output, CodeStatement statement)
		throws IOException {

		this.writeStatement(output, statement, false);
	}

	private void writeStatement(Appendable output, CodeStatement statement, boolean inline)
		throws IOException {

		if (statement == null) {
			return;
		}

		if (!inline) {
			this.writeln(output);
		}

		boolean needsSemicolon; 
		if (statement instanceof CodeExpressionStatement) {
			this.writeExpression(output, ((CodeExpressionStatement)statement).getExpression());
			needsSemicolon = true;

		} else if (statement instanceof CodeConditionStatement) {
			needsSemicolon = this.writeConditionStatement(output, (CodeConditionStatement)statement);

		} else if (statement instanceof CodeVariableDeclarationStatement) {
			needsSemicolon = this.writeVariableDeclarationStatement(output, (CodeVariableDeclarationStatement)statement, inline);

		} else if (statement instanceof CodeIterationStatement) {
			needsSemicolon = this.writeIterationStatement(output, (CodeIterationStatement)statement);

		} else if (statement instanceof CodeVariableCompoundDeclarationStatement) {
			needsSemicolon = this.writeVariableCompoundDeclarationStatement(output, (CodeVariableCompoundDeclarationStatement)statement, inline);

		} else {
			throw new UnsupportedOperationException("Statement not yet supported: "+statement.getClass());
		}

		if (needsSemicolon && !inline) {
			output.append(';');
		}
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

		boolean addParens;
		switch (parens) {
			case FORCE:
				addParens = true;
				break;
			case SUPPRESS:
				addParens = false;
				break;
			default:
				addParens = expression.getHasParens();
				break;
		}

		if (addParens) {
			output.append('(');
		}

		try {
			if (expression instanceof CodePrimitiveExpression) {
				this.writeLiteral(output, ((CodePrimitiveExpression)expression).getValue());

			} else if (expression instanceof CodeVariableReferenceExpression) {
				output.append(((CodeVariableReferenceExpression)expression).getIdent());

			} else if (expression instanceof CodeThisReferenceExpression) {
				output.append("this");

			} else if (expression instanceof CodeBinaryOperatorExpression) {
				this.writeBinaryOperatorExpression(output, (CodeBinaryOperatorExpression)expression);

			} else if (expression instanceof CodeUnaryOperatorExpression) {
				this.writeUnaryOperatorExpression(output, (CodeUnaryOperatorExpression)expression);

			} else if (expression instanceof CodePropertyReferenceExpression) {
				this.writePropertyReferenceExpression(output, (CodePropertyReferenceExpression)expression);

			} else if (expression instanceof CodeMethodInvokeExpression) {
				this.writeMethodInvokeExpression(output, (CodeMethodInvokeExpression)expression);

			} else {
				throw new UnsupportedOperationException("Expression not yet supported: "+expression.getClass());
			}
		} finally {
			if (addParens) {
				output.append(')');
			}
		}
	}

	private void writePropertyReferenceExpression(Appendable output, CodePropertyReferenceExpression expression)
		throws IOException {

		output.append("this.getProperty(");
		this.writeExpression(output, expression.getTarget());
		output.append(", ");
		this.writeExpression(output, expression.getPropertyName());
		output.append(')');
	}

	private void writeBinaryOperatorExpression(Appendable output, CodeBinaryOperatorExpression expression)
		throws IOException {

		String operator;
		switch (expression.getOperator()) {
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
			case IDENTITY_EQUALITY:
			case VALUE_EQUALITY:
				// TODO: create semantically correct equality operators
				operator = " == ";
				break;
			case IDENTITY_INEQUALITY:
			case VALUE_INEQUALITY:
				// TODO: create semantically correct inequality operators
				operator = " != ";
				break;
			default:
				throw new UnsupportedOperationException("Binary operator not yet supported: "+expression.getOperator());
		}
		this.writeExpression(output, expression.getLeft());
		output.append(operator);
		this.writeExpression(output, expression.getRight());
	}

	private void writeUnaryOperatorExpression(Appendable output, CodeUnaryOperatorExpression expression)
		throws IOException {

		switch (expression.getOperator()) {
			case PRE_INCREMENT:
				output.append("++");
				this.writeExpression(output, expression.getExpression());
				break;
			case POST_INCREMENT:
				this.writeExpression(output, expression.getExpression());
				output.append("++");
				break;
			default:
				throw new UnsupportedOperationException("Unary operator not yet supported: "+expression.getOperator());
		}
	}

	private boolean writeVariableDeclarationStatement(Appendable output, CodeVariableDeclarationStatement statement, boolean inline)
		throws IOException {

		this.writeTypeName(output, statement.getType());
		output.append(statement.getName());
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

	private boolean writeVariableCompoundDeclarationStatement(Appendable output, CodeVariableCompoundDeclarationStatement statement, boolean inline)
		throws IOException {

		List<CodeVariableDeclarationStatement> vars = statement.getVars();
		if (vars.size() < 1) {
			return false;
		}

		this.writeTypeName(output, vars.get(0).getType());
		this.depth++;
		boolean needsDelim = false;
		for (CodeVariableDeclarationStatement varRef : vars) {
			if (needsDelim) {
				output.append(',');
				if (inline) {
					output.append(' ');
				} else {
					this.writeln(output);
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
		this.depth--;
		return true;
	}

	private boolean writeConditionStatement(Appendable output, CodeConditionStatement statement)
		throws IOException {

		output.append("if (");
		this.writeExpression(output, statement.getCondition(), ParensSetting.SUPPRESS);
		output.append(") {");
		this.depth++;

		for (CodeStatement trueStatement : statement.getTrueStatements()) {
			this.writeStatement(output, trueStatement);
		}

		this.depth--;
		this.writeln(output);
		output.append('}');

		if (statement.getFalseStatements().size() > 0) {
			boolean nestedConditionals =
				(statement.getFalseStatements().size() == 1) &&
				(statement.getFalseStatements().getLastStatement() instanceof CodeConditionStatement);

			output.append(" else ");
			if (!nestedConditionals) {
				output.append('{');
				this.depth++;
				for (CodeStatement falseStatement : statement.getFalseStatements()) {
					this.writeStatement(output, falseStatement);
				}
				this.depth--;
				this.writeln(output);
				output.append('}');
			} else {
				this.writeConditionStatement(output, (CodeConditionStatement)statement.getFalseStatements().getLastStatement());
			}
		}

		return false;
	}

	private boolean writeIterationStatement(Appendable output, CodeIterationStatement statement)
		throws IOException {
		
		output.append("for (");

		this.writeStatement(output, statement.getInitStatement(), true);
		output.append("; ");
		this.writeExpression(output, statement.getTestExpression());
		output.append("; ");
		this.writeStatement(output, statement.getIncrementStatement(), true);

		output.append(") {");
		this.depth++;
		for (CodeStatement childStatement : statement.getStatements()) {
			this.writeStatement(output, childStatement);
		}
		this.depth--;
		this.writeln(output);
		output.append('}');

		return false;
	}

	private void writeMethodInvokeExpression(Appendable output, CodeMethodInvokeExpression expression)
		throws IOException {

		this.writeExpression(output, expression.getTarget());
		String methodName = expression.getMethodName();
		if (methodName != null && methodName.length() > 0) {
			output.append('.').append(methodName);
		}
		output.append('(');
		boolean needsDelim = false;
		for (CodeExpression arg : expression.getArguments()) {
			if (needsDelim) {
				output.append(", ");
			} else {
				needsDelim = true;
			}
			this.writeExpression(output, arg);
		}
		output.append(')');
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
		output.append(typeName).append(' ');
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

	private void writeIntro(Appendable output, String ns, AccessModifierType access, String typeName, Class<?> baseType)
		throws IOException {

		if (ns != null && ns.length() > 0) {
			output.append("package ").append(ns).append(";");
			this.writeln(output, 2);
		}

		output.append("import java.io.*;");
		this.writeln(output);
		output.append("import java.util.*;");
		this.writeln(output);
		output.append("import ").append(DUEL_PACKAGE).append(".*;");
		this.writeln(output, 2);
		this.writeAccessModifier(output, access);
		output.append("class ").append(typeName);
		if (baseType != null) {
			output.append(" extends ");
			this.writeTypeName(output, baseType);
			output.append('{');
		} else {
			output.append(" {");
		}
		this.depth++;
	}

	private void writeOutro(Appendable output) throws IOException {
		this.depth--;
		this.writeln(output);
		output.append('}');
		this.writeln(output);
	}

	private void writeLiteral(Appendable output, Object value)
		throws IOException {

		if (value == null) {
			output.append("null");

		} else if (value instanceof String) {
			this.writeString(output, (String)value);

		} else {
			output.append(String.valueOf(value));
		}
	}

	private void writeString(Appendable output, String value)
		throws IOException {

		if (value == null) {
			output.append("null");
			return;
		}

		int start = 0,
			length = value.length();

		output.append('\"');

		for (int i=start; i<length; i++) {
			String escape;

			char ch = value.charAt(i);
			switch (ch) {
				case '\"':
					escape = "\\\"";
					break;
				case '\\':
					escape = "\\\\";
					break;
				case '\n':
					escape = "\\n";
					break;
				case '\r':
					escape = "\\r";
					break;
				case '\t':
					escape = "\\t";
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

		output.append('\"');
	}

	private void writeln(Appendable output)
		throws IOException {

		this.writeln(output, 1);
	}

	private void writeln(Appendable output, int newlines)
		throws IOException {

		for (int i=newlines; i>0; i--) {
			output.append(this.settings.getNewline());
		}

		for (int i=this.depth; i>0; i--) {
			output.append(this.settings.getIndent());
		}
	}
}
