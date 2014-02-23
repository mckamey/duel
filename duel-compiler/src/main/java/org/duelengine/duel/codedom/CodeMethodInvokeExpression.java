package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a method call
 */
public class CodeMethodInvokeExpression extends CodeExpression {

	private Class<?> resultType;
	private CodeExpression target;
	private String methodName;
	private final List<CodeExpression> arguments = new ArrayList<CodeExpression>();

	public CodeMethodInvokeExpression() {
		setResultType(null);
	}

	public CodeMethodInvokeExpression(Class<?> returnType, CodeExpression target, String methodName, CodeExpression... args) {
		setResultType(returnType);
		this.target = target;
		this.methodName = methodName;
		if (args != null) {
			arguments.addAll(Arrays.asList(args));
		}
	}

	public CodeExpression getTarget() {
		return target;
	}

	public void setTarget(CodeExpression value) {
		target = value;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String value) {
		methodName = value;
	}

	public List<CodeExpression> getArguments() {
		return arguments;
	}

	@Override
	public Class<?> getResultType() {
		return resultType;
	}

	public void setResultType(Class<?> value) {
		resultType = (value != null) ? value : Object.class;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (target != null) {
				target.visit(visitor);
			}
			for (CodeExpression expression : arguments) {
				if (expression != null) {
					expression.visit(visitor);
				}
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeMethodInvokeExpression)) {
			// includes null
			return false;
		}

		CodeMethodInvokeExpression that = (CodeMethodInvokeExpression)arg;
		if (this.resultType == null ? that.resultType != null : !this.resultType.equals(that.resultType)) {
			return false;
		}

		if (this.target == null ? that.target != null : !this.target.equals(that.target)) {
			return false;
		}

		if (this.methodName == null ? that.methodName != null : !this.methodName.equals(that.methodName)) {
			return false;
		}

		int length = this.arguments.size();
		if (length != that.arguments.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeExpression thisArg = this.arguments.get(i);
			CodeExpression thatArg = that.arguments.get(i);
			if (thisArg == null ? thatArg != null : !thisArg.equals(thatArg)) {
				return false;
			}
		}

		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode() * HASH_PRIME + arguments.hashCode();
		if (resultType != null) {
			hash = hash * HASH_PRIME + resultType.hashCode();
		}
		if (target != null) {
			hash = hash * HASH_PRIME + target.hashCode();
		}
		if (methodName != null) {
			hash = hash * HASH_PRIME + methodName.hashCode();
		}
		return hash;
	}
}
