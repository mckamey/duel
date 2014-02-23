package org.duelengine.duel.codedom;

import java.util.HashMap;
import java.util.Map;

import org.duelengine.duel.DuelData;
import org.duelengine.duel.codegen.JavaCodeGen;

public abstract class CodeObject {

	private Map<String, Object> metaData;

	/**
	 * Retrieves a metadata value for the given key
	 * @param key
	 * @return
	 */
	public Object getMetaData(String key) {
		if (metaData == null || !metaData.containsKey(key)) {
			return null;
		}

		return metaData.get(key);
	}

	/**
	 * Inserts a metadata value for the given key
	 * @param key
	 * @param value
	 * @return
	 */
	public Object putMetaData(String key, Object value) {
		if (metaData == null) {
			metaData = new HashMap<String, Object>(4, 1.0f);
		}

		return metaData.put(key, value);
	}

	/**
	 * Inserts multiple metadata key-value pairs
	 * @param pairs alternating key, value...
	 * @return
	 */
	public CodeObject withMetaData(Object... pairs) {
		if (pairs == null || pairs.length < 1) {
			return this;
		}

		final int length = pairs.length/2;
		if (metaData == null) {
			metaData = new HashMap<String, Object>(length, 1.0f);
		}

		for (int i=0; i<length; i++) {
			String key = DuelData.coerceString(pairs[2*i]);
			Object value = pairs[2*i+1];
			metaData.put(key, value);
		}
		return this;
	}

	/**
	 * Walks this code object structure
	 * @param visitor
	 */
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
		if (this.metaData == null) {
			return (that.metaData == null);
		}

		for (String name : this.metaData.keySet()) {
			if (!that.metaData.containsKey(name)) {
				return false;
			}

			Object thisValue = this.metaData.get(name);
			Object thatValue = that.metaData.get(name);

			if (thisValue == null ? thatValue != null : !thisValue.equals(thatValue)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		return (metaData != null) ? metaData.hashCode() : super.hashCode();
	}

	@Override
	public String toString() {
		try {
			// debugging helper
			StringBuilder buffer = new StringBuilder();
			new JavaCodeGen().writeCode(buffer, this);
			return buffer.toString();

		} catch (Exception ex) {
			return super.toString()+'\n'+ex.getMessage();
		}
	}
}
