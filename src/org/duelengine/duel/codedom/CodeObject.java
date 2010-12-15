package org.duelengine.duel.codedom;

import java.util.*;
import org.duelengine.duel.DuelData;

public abstract class CodeObject {

	private Map<String, Object> userData;

	public Object getUserData(String key) {
		if (this.userData == null || !this.userData.containsKey(key)) {
			return null;
		}

		return this.userData.get(key);
	}

	public Object putUserData(String key, Object value) {
		if (this.userData == null) {
			this.userData = new HashMap<String, Object>();
		}

		return this.userData.put(key, value);
	}

	public CodeObject withUserData(Object... pairs) {
		if (pairs == null || pairs.length < 1) {
			return this;
		}

		if (this.userData == null) {
			this.userData = new HashMap<String, Object>();
		}

		int length = pairs.length/2;
		for (int i=0; i<length; i++) {
			String key = DuelData.coerceString(pairs[2*i]);
			Object value = pairs[2*i+1];
			this.userData.put(key, value);
		}
		return this;
	}

	public void visit(CodeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeObject)) {
			// includes null
			return false;
		}

		CodeObject that = (CodeObject)arg;
		if (this.userData == null) {
			return (that.userData == null);
		}

		for (String name : this.userData.keySet()) {
			if (!that.userData.containsKey(name)) {
				return false;
			}

			Object thisValue = this.userData.get(name);
			Object thatValue = that.userData.get(name);

			if (thisValue == null ? thatValue != null : !thisValue.equals(thatValue)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		return (this.userData != null) ? this.userData.hashCode() : super.hashCode();
	}

	@Override
	public String toString() {
		try {
			StringBuilder buffer = new StringBuilder();
			new org.duelengine.duel.codegen.JavaCodeGen().writeCode(buffer, this);
			return buffer.toString();
		} catch (Exception ex) {
			return super.toString()+'\n'+ex.getMessage();
		}
	}
}
