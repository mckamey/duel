package org.duelengine.duel.codedom;

/**
 * Represents a type member
 */
public abstract class CodeMember extends CodeObject {

	private String name;

	public CodeMember() {
	}

	public CodeMember(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeMember)) {
			// includes null
			return false;
		}

		CodeMember that = (CodeMember)arg;

		if (this.name == null ? that.name != null : !this.name.equals(that.name)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return (this.name == null) ? 0 : this.name.hashCode();
	}
}
