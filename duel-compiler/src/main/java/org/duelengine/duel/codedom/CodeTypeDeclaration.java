package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		baseType = Object.class;
		access = AccessModifierType.DEFAULT;
	}

	public CodeTypeDeclaration(AccessModifierType access, String typeNS, String typeName, Class<?> baseType, CodeMember... members) {
		this.baseType = (baseType != null) ? baseType : Object.class;
		this.access = (access != null) ? access : AccessModifierType.DEFAULT;
		this.typeNS = typeNS;
		this.typeName = typeName;

		if (members != null) {
			for (CodeMember member : members) {
				add(member);
			}
		}
	}

	public AccessModifierType getAccess() {
		return access;
	}

	public void setAccess(AccessModifierType value) {
		access = (value != null) ? value : AccessModifierType.DEFAULT;
	}

	public Class<?> getBaseType() {
		return baseType;
	}

	public void setBaseType(Class<?> value) {
		baseType = (value != null) ? value : Object.class;
	}

	public void setTypeName(String value) {
		typeName = value;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setNamespace(String value) {
		typeNS = value;
	}

	public String getNamespace() {
		return typeNS;
	}

	/**
	 * Contains the output methods. The first one is the entry point.
	 * @return
	 */
	public List<CodeMember> getMembers() {
		return members;
	}

	public void add(CodeMember member) {
		members.add(member);
	}

	public void addAll(Collection<? extends CodeMember> value) {
		members.addAll(value);
	}

	@Override
	public boolean isLocalIdent(String ident) {
		return (identMap != null) && identMap.containsKey(ident);
	}

	@Override
	public String uniqueIdent(String ident) {
		if (identMap == null) {
			identMap = new HashMap<String, String>();
		}
		else if (identMap.containsKey(ident)) {
			return identMap.get(ident);
		}

		String unique = nextIdent(ident);
		identMap.put(ident, unique);
		return unique;
	}

	@Override
	public String nextIdent(String prefix) {
		// generate a unique var name
		return prefix+(++nextID);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeMember member : members) {
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

		int hash = (access == null) ? 0 :access.hashCode();
		if (baseType != null) {
			hash = hash * HASH_PRIME + baseType.hashCode();
		}
		if (typeNS != null) {
			hash = hash * HASH_PRIME + typeNS.hashCode();
		}
		if (typeName != null) {
			hash = hash * HASH_PRIME + typeName.hashCode();
		}
		if (members != null) {
			hash = hash * HASH_PRIME + members.hashCode();
		}
		return hash;
	}
}
