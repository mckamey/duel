package org.duelengine.duel.codedom;

import java.util.*;

public class CodeType extends CodeObject {

	private int nextID;
	private String typeName;
	private String namespace;
	private final List<CodeMethod> methods = new ArrayList<CodeMethod>();

	public CodeType() {
	}

	public CodeType(String namespace, String typeName, CodeMethod[] methods) {
		this(namespace, typeName, Arrays.asList(methods));
	}

	public CodeType(String namespace, String typeName, Iterable<CodeMethod> methods) {
		this.namespace = namespace;
		this.typeName = typeName;

		if (methods != null) {
			for (CodeMethod method : methods) {
				this.addMethod(method);
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
	 * The output methods. The first one is the entry point.
	 * @return
	 */
	public Iterable<CodeMethod> getMethods() {
		return this.methods;
	}

	public void addMethod(CodeMethod method) {
		// assign a unique method name
		method.setMethodName("t_"+this.nextID++);
		this.methods.add(method);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeType)) {
			// includes null
			return false;
		}

		CodeType that = (CodeType)arg;
		if (this.namespace == null ? that.namespace != null : !this.namespace.equals(that.namespace)) {
			return false;
		}

		if (this.typeName == null ? that.typeName != null : !this.typeName.equals(that.typeName)) {
			return false;
		}

		if ((this.methods == null || that.methods == null) && (this.methods != that.methods)) {
			return false;
		}

		int length = this.methods.size();
		if (length != that.methods.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeMethod thisMethod = this.methods.get(i);
			CodeMethod thatMethod = that.methods.get(i);
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
		if (this.methods != null) {
			hash = hash * HASH_PRIME + this.methods.hashCode();
		}
		return hash;
	}
}
