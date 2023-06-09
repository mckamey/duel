package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a constructor call
 */
public class CodeObjectCreateExpression extends CodeExpression {

	private String typeName;
	private final List<CodeExpression> arguments = new ArrayList<CodeExpression>();

	public CodeObjectCreateExpression() {
	}

	public CodeObjectCreateExpression(String typeName, CodeExpression... args) {
		this.typeName = typeName;
		if (args != null) {
			this.arguments.addAll(Arrays.asList(args));
		}
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String value) {
		typeName = value;
	}

	public List<CodeExpression> getArguments() {
		return arguments;
	}

	@Override
	public Class<?> getResultType() {
		return Object.class;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeExpression expression : arguments) {
				if (expression != null) {
					expression.visit(visitor);
				}
			}
		}
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

		int hash = super.hashCode() * HASH_PRIME + arguments.hashCode();
		if (typeName != null) {
			hash = hash * HASH_PRIME + typeName.hashCode();
		}
		return hash;
	}
}
