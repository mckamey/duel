package org.duelengine.duel.codedom;

/**
 * Represents a type member
 */
public abstract class CodeMember extends CodeObject {

	private AccessModifierType access;
	private String name;

	public CodeMember() {
		this.access = AccessModifierType.DEFAULT;
	}

	public CodeMember(AccessModifierType access, String name) {
		this.name = name;
		this.access = (access != null) ? access : AccessModifierType.DEFAULT;
	}

	public AccessModifierType getAccess() {
		return this.access;
	}

	public void setAccess(AccessModifierType value) {
		this.access = (value != null) ? value : AccessModifierType.DEFAULT;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	@Override
	public CodeMember withUserData(Object... pairs) {
		return (CodeMember)super.withUserData(pairs);
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

		return this.access == that.access;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (this.access == null) ? 0 :this.access.hashCode();
		if (this.name != null) {
			hash = hash * HASH_PRIME + this.name.hashCode();
		}

		return hash;
	}
}
