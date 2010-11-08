package org.duelengine.duel.codedom;

import java.util.*;

/**
 * A simplified class definition which assumes defining a View
 */
public class CodeTypeDeclaration extends CodeObject implements IdentifierScope {

	private Map<String, String> identMap;
	private int nextID;
	private AccessModifierType access;
	private Class<?> baseType = org.duelengine.duel.runtime.AbstractView.class;
	private String typeName;
	private String namespace;
	private final List<CodeMember> members = new ArrayList<CodeMember>();

	public CodeTypeDeclaration() {
		this.access = AccessModifierType.DEFAULT;
	}

	public CodeTypeDeclaration(AccessModifierType access, String namespace, String typeName, CodeMethod[] methods) {
		this.access = (access != null) ? access : AccessModifierType.DEFAULT;
		this.namespace = namespace;
		this.typeName = typeName;

		if (methods != null) {
			for (CodeMethod method : methods) {
				this.add(method);
			}
		}
	}

	public AccessModifierType getAccess() {
		return this.access;
	}

	public void setAccess(AccessModifierType value) {
		this.access = (value != null) ? value : AccessModifierType.DEFAULT;
	}

	public Class<?> getBaseType() {
		return this.baseType;
	}

	public void setBaseType(Class<?> value) {
		this.baseType = value;
	}

	public void setTypeName(String value) {
		this.typeName = value;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setNamespace(String value) {
		this.namespace = value;
	}

	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * Contains the output methods. The first one is the entry point.
	 * @return
	 */
	public List<CodeMember> getMembers() {
		return this.members;
	}

	public void add(CodeMember member) {
		this.members.add(member);
	}

	public void addAll(Collection<? extends CodeMember> members) {
		this.members.addAll(members);
	}

	@Override
	public String uniqueIdent(String ident) {
		if (this.identMap == null) {
			this.identMap = new HashMap<String, String>();
		}
		else if (this.identMap.containsKey(ident)) {
			return this.identMap.get(ident);
		}

		String unique = this.nextIdent(ident);
		this.identMap.put(ident, unique);
		return unique;
	}

	@Override
	public String nextIdent(String prefix) {
		// generate a unique var name
		return prefix+(++this.nextID);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeTypeDeclaration)) {
			// includes null
			return false;
		}

		CodeTypeDeclaration that = (CodeTypeDeclaration)arg;
		if (this.baseType == null ? that.baseType != null : !this.baseType.equals(that.baseType)) {
			return false;
		}

		if (this.namespace == null ? that.namespace != null : !this.namespace.equals(that.namespace)) {
			return false;
		}

		if (this.typeName == null ? that.typeName != null : !this.typeName.equals(that.typeName)) {
			return false;
		}

		int length = this.members.size();
		if (length != that.members.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeMember thisMethod = this.members.get(i);
			CodeMember thatMethod = that.members.get(i);
			if (thisMethod == null ? thatMethod != null : !thisMethod.equals(thatMethod)) {
				return false;
			}
		}

		return this.access == that.access;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (this.access == null) ? 0 :this.access.hashCode();
		if (this.baseType != null) {
			hash = hash * HASH_PRIME + this.baseType.hashCode();
		}
		if (this.namespace != null) {
			hash = hash * HASH_PRIME + this.namespace.hashCode();
		}
		if (this.typeName != null) {
			hash = hash * HASH_PRIME + this.typeName.hashCode();
		}
		if (this.members != null) {
			hash = hash * HASH_PRIME + this.members.hashCode();
		}
		return hash;
	}
}
