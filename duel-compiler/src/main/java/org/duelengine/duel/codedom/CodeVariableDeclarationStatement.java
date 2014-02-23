package org.duelengine.duel.codedom;

/**
 * Represents a variable declaration
 */
public class CodeVariableDeclarationStatement extends CodeStatement {

	private Class<?> type = Object.class;
	private String name;
	private CodeExpression initExpression;

	public CodeVariableDeclarationStatement() {
	}

	public CodeVariableDeclarationStatement(Class<?> type, String name, CodeExpression initExpression) {
		if (type != null) {
			this.type = type;
		}
		this.name = name;
		this.initExpression = initExpression;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> value) {
		type = (value == null) ? Object.class : value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public CodeExpression getInitExpression() {
		return initExpression;
	}

	public void setInitExpression(CodeExpression value) {
		initExpression = value;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			if (initExpression != null) {
				initExpression.visit(visitor);
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeVariableDeclarationStatement)) {
			// includes null
			return false;
		}

		CodeVariableDeclarationStatement that = (CodeVariableDeclarationStatement)arg;

		if (this.name == null ? that.name != null : !this.name.equals(that.name)) {
			return false;
		}

		if (this.initExpression == null ? that.initExpression != null : !this.initExpression.equals(that.initExpression)) {
			return false;
		}

		return this.type.equals(that.type);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = type.hashCode();
		if (name != null) {
			hash = hash * HASH_PRIME + name.hashCode();
		}
		if (initExpression != null) {
			hash = hash * HASH_PRIME + initExpression.hashCode();
		}
		return hash;
	}
}
