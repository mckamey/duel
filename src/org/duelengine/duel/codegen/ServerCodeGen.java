package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;
import org.duelengine.duel.codedom.*;

public class ServerCodeGen implements CodeGenerator {

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

	public void write(Appendable output, CodeTypeDeclaration view)
		throws IOException {

		this.depth = 0;
		this.writeIntro(output, view.getNamespace(), view.getAccess(), view.getTypeName(), view.getBaseType());
		for (CodeMember member : view.getMembers()) {
			if (member instanceof CodeMethod) {
				this.writeMethod(output, (CodeMethod)member);
			} else if (member != null) {
				throw new UnsupportedOperationException("Not implemented: "+member.getClass());
			}
		}
		this.writeOutro(output);
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
		this.writeln(output);
	}

	private void writeStatement(Appendable output, CodeStatement statement)
		throws IOException {

		if (statement == null) {
			return;
		}

		this.writeln(output);

		if (statement instanceof CodeExpressionStatement) {
			this.writeExpression(output, ((CodeExpressionStatement)statement).getExpression());
			output.append(';');

		} else {
			throw new UnsupportedOperationException("Statement not yet supported: "+statement.getClass());
		}
	}

	private void writeExpression(Appendable output, CodeExpression expression)
		throws IOException {

		if (expression == null) {
			return;
		}

		boolean addParens = expression.getHasParens();
		if (addParens) {
			output.append('(');
		}

		try {
			if (expression instanceof CodePrimitiveExpression) {
				this.writeLiteral(output, ((CodePrimitiveExpression)expression).getValue());

			} else if (expression instanceof CodeVariableReferenceExpression) {
				output.append(((CodeVariableReferenceExpression)expression).getIdent());

			} else if (expression instanceof CodeMethodInvokeExpression) {
				this.writeMethodInvokeExpression(output, (CodeMethodInvokeExpression)expression);

			} else {
				throw new UnsupportedOperationException("Expression not yet supported: "+expression.getClass());
			}
		} finally {
			if (addParens) {
				output.append('(');
			}
		}
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
			if (pkg == null || pkg.getName().equals("java.lang") || pkg.getName().equals("java.io") || pkg.getName().equals("java.util")) {
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
		this.writeln(output, 2);
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
