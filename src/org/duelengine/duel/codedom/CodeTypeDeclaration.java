package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a class definition
 */
public class CodeTypeDeclaration extends CodeMember implements IdentifierScope {

	private Map<String, String> identMap;
	private int nextID;
	private AccessModifierType access;
	private Class<?> baseType;
	private String typeName;
	private String typeNS;
	private final List<CodeMember> members = new ArrayList<CodeMember>();

	public CodeTypeDeclaration() {
		this.baseType = Object.class;
		this.access = AccessModifierType.DEFAULT;
	}

	public CodeTypeDeclaration(AccessModifierType access, String typeNS, String typeName, Class<?> baseType, CodeMember... members) {
		this.baseType = (baseType != null) ? baseType : Object.class;
		this.access = (access != null) ? access : AccessModifierType.DEFAULT;
		this.typeNS = typeNS;
		this.typeName = typeName;

		if (members != null) {
			for (CodeMember member : members) {
				this.add(member);
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
		this.baseType = (value != null) ? value : Object.class;
	}

	public void setTypeName(String value) {
		this.typeName = value;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setNamespace(String value) {
		this.typeNS = value;
	}

	public String getNamespace() {
		return this.typeNS;
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
	public boolean isLocalIdent(String ident) {
		return (this.identMap != null) && this.identMap.containsKey(ident);
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
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeMember member : this.members) {
				if (member != null) {
					member.visit(visitor);
				}
			}
		}
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

		if (this.typeNS == null ? that.typeNS != null : !this.typeNS.equals(that.typeNS)) {
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
		if (this.typeNS != null) {
			hash = hash * HASH_PRIME + this.typeNS.hashCode();
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
