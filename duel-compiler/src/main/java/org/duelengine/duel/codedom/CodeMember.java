package org.duelengine.duel.codedom;

/**
 * Represents a type member
 */
public abstract class CodeMember extends CodeObject {

	private AccessModifierType access;
	private String name;

	public CodeMember() {
		access = AccessModifierType.DEFAULT;
	}

	public CodeMember(AccessModifierType access, String name) {
		this.access = (access != null) ? access : AccessModifierType.DEFAULT;
		this.name = name;
	}

	public AccessModifierType getAccess() {
		return access;
	}

	public void setAccess(AccessModifierType value) {
		access = (value != null) ? value : AccessModifierType.DEFAULT;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
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

		if (name == null ? that.name != null : !name.equals(that.name)) {
			return false;
		}

		return access == that.access;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (access == null) ? 0 :access.hashCode();
		if (name != null) {
			hash = hash * HASH_PRIME + name.hashCode();
		}

		return hash;
	}
}
