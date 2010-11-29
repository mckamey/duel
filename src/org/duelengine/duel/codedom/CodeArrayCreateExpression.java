package org.duelengine.duel.codedom;

import java.util.*;

public class CodeArrayCreateExpression extends CodeExpression {

	private Class<?> type;
	private int size;
	private final List<CodeExpression> initializers = new ArrayList<CodeExpression>();

	public CodeArrayCreateExpression() {
		this.setType(null);
	}

	public CodeArrayCreateExpression(Class<?> type, int size) {
		this.setType(type);
		this.size = size;
	}

	public CodeArrayCreateExpression(Class<?> type, CodeExpression... initializers) {
		this.setType(type);
		if (initializers != null) {
			this.initializers.addAll(Arrays.asList(initializers));
		}
	}

	public Class<?> getType() {
		return this.type;
	}

	public void setType(Class<?> value) {
		this.type = (value != null) ? value : Object.class;
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(int value) {
		this.size = value;
	}

	public List<CodeExpression> getInitializers() {
		return this.initializers;
	}

	@Override
	public Class<?> getResultType() {
		return List.class;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeArrayCreateExpression)) {
			// includes null
			return false;
		}

		CodeArrayCreateExpression that = (CodeArrayCreateExpression)arg;

		if (this.size != that.size) {
			return false;
		}
		
		if (this.type == null ? that.type != null : !this.type.equals(that.type)) {
			return false;
		}

		int length = this.initializers.size();
		if (length != that.initializers.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeExpression thisArg = this.initializers.get(i);
			CodeExpression thatArg = that.initializers.get(i);
			if (thisArg == null ? thatArg != null : !thisArg.equals(thatArg)) {
				return false;
			}
		}

		return super.equals(arg);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = super.hashCode() * HASH_PRIME + this.initializers.hashCode();
		hash = hash * HASH_PRIME + this.size;
		if (this.type != null) {
			hash = hash * HASH_PRIME + this.type.hashCode();
		}
		return hash;
	}
}
