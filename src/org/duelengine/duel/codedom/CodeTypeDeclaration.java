package org.duelengine.duel.codedom;

import java.util.*;

/**
 * A simplified class definition which assumes defining a View
 */
public class CodeTypeDeclaration extends CodeObject implements IdentifierScope {

	private int nextID;
	private String typeName;
	private String namespace;
	private final List<CodeMember> members = new ArrayList<CodeMember>();

	public CodeTypeDeclaration() {
	}

	public CodeTypeDeclaration(String namespace, String typeName, CodeMethod[] methods) {
		this(namespace, typeName, Arrays.asList(methods));
	}

	public CodeTypeDeclaration(String namespace, String typeName, Iterable<CodeMethod> methods) {
		this.namespace = namespace;
		this.typeName = typeName;

		if (methods != null) {
			for (CodeMethod method : methods) {
				this.add(method);
			}
		}
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
	public String nextID() {
		// generate a unique name
		return "t_"+(++this.nextID);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeTypeDeclaration)) {
			// includes null
			return false;
		}

		CodeTypeDeclaration that = (CodeTypeDeclaration)arg;
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

		return true;
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		int hash = (this.namespace == null) ? 0 : this.namespace.hashCode();
		if (this.typeName != null) {
			hash = hash * HASH_PRIME + this.typeName.hashCode();
		}
		if (this.members != null) {
			hash = hash * HASH_PRIME + this.members.hashCode();
		}
		return hash;
	}
}
