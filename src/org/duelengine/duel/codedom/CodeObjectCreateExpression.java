package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a constructor call
 */
public class CodeObjectCreateExpression extends CodeExpression {

	private String typeName;
	private List<CodeExpression> arguments = new ArrayList<CodeExpression>();

	public CodeObjectCreateExpression() {
	}

	public CodeObjectCreateExpression(String typeName, CodeExpression... args) {
		this.typeName = typeName;
		if (args != null) {
			this.arguments.addAll(Arrays.asList(args));
		}
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String value) {
		this.typeName = value;
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
		if (!(arg instanceof CodeObjectCreateExpression)) {
			// includes null
			return false;
		}

		CodeObjectCreateExpression that = (CodeObjectCreateExpression)arg;

		if (this.typeName == null ? that.typeName != null : !this.typeName.equals(that.typeName)) {
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

		int hash = super.hashCode() * HASH_PRIME + this.arguments.hashCode();
		if (this.typeName != null) {
			hash = hash * HASH_PRIME + this.typeName.hashCode();
		}
		return hash;
	}
}
