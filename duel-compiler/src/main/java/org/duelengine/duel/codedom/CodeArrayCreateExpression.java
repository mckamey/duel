package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeArrayCreateExpression extends CodeExpression {

	private Class<?> type;
	private int size;
	private final List<CodeExpression> initializers = new ArrayList<CodeExpression>();

	public CodeArrayCreateExpression() {
		setType(null);
	}

	public CodeArrayCreateExpression(Class<?> type, int size) {
		setType(type);
		this.size = size;
	}

	public CodeArrayCreateExpression(Class<?> type, CodeExpression... initializerExpressions) {
		setType(type);
		if (initializerExpressions != null) {
			initializers.addAll(Arrays.asList(initializerExpressions));
		}
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> value) {
		type = (value != null) ? value : Object.class;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int value) {
		size = value;
	}

	public List<CodeExpression> getInitializers() {
		return initializers;
	}

	@Override
	public Class<?> getResultType() {
		return List.class;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeExpression expression : initializers) {
				if (expression != null) {
					expression.visit(visitor);
				}
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeArrayCreateExpression)) {
			// includes null
			return false;
		}

		CodeArrayCreateExpression that = (CodeArrayCreateExpression)arg;

		if (size != that.size) {
			return false;
		}
		
		if (type == null ? that.type != null : !type.equals(that.type)) {
			return false;
		}

		int length = initializers.size();
		if (length != that.initializers.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeExpression thisArg = initializers.get(i);
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

		int hash = super.hashCode() * HASH_PRIME + initializers.hashCode();
		hash = hash * HASH_PRIME + size;
		if (type != null) {
			hash = hash * HASH_PRIME + type.hashCode();
		}
		return hash;
	}
}
