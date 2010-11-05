package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a method call
 */
public class CodeMethodInvokeExpression extends CodeExpression {

	private CodeExpression target;
	private String methodName;
	private List<CodeExpression> arguments = new ArrayList<CodeExpression>();

	public CodeMethodInvokeExpression() {
	}

	public CodeMethodInvokeExpression(CodeExpression target, String methodName, CodeExpression[] args) {
		this.target = target;
		this.methodName = methodName;
		if (args != null) {
			this.arguments.addAll(Arrays.asList(args));
		}
	}

	public CodeExpression getTarget() {
		return this.target;
	}

	public void setTarget(CodeExpression value) {
		this.target = value;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String value) {
		this.methodName = value;
	}

	public List<CodeExpression> getArguments() {
		return this.arguments;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeMethodInvokeExpression)) {
			// includes null
			return false;
		}

		CodeMethodInvokeExpression that = (CodeMethodInvokeExpression)arg;
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

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = this.arguments.hashCode();
		if (this.target != null) {
			hash = hash * HASH_PRIME + this.target.hashCode();
		}
		if (this.methodName != null) {
			hash = hash * HASH_PRIME + this.methodName.hashCode();
		}
		return hash;
	}
}
