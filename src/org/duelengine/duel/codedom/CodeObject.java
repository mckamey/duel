package org.duelengine.duel.codedom;

import java.util.*;
import org.duelengine.duel.DuelData;
import org.duelengine.duel.codegen.ServerCodeGen;

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
	
	@Override
	public String toString() {
		try {
			StringBuilder buffer = new StringBuilder();
			new ServerCodeGen().writeCode(buffer, this);
			return buffer.toString();
		} catch (Exception ex) {
			return super.toString()+'\n'+ex.getMessage();
		}
	}
}
