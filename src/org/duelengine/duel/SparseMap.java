package org.duelengine.duel;

import java.util.*;

/**
 * Represents sparsely populated graph of data to be
 * serialized over the top of existing JavaScript Objects
 */
@SuppressWarnings("serial")
public class SparseMap extends LinkedHashMap<String, Object> {

	public SparseMap() {
	}

	public SparseMap(int initialCapacity) {
		super(initialCapacity);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object putSparse(String ident, Object value) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}
		if (!JSUtility.isValidIdentifier(ident, true)) {
			return this.put(ident, value);
		}

		Map parent = this;
		int prevDot = -1;
		int nextDot = ident.indexOf('.');
		while (nextDot > prevDot) {
			String childKey = ident.substring(prevDot+1, nextDot);
			Map child;
			if (parent.containsKey(childKey)) {
				Object obj = parent.get(childKey);
				if (obj instanceof Map) {
					child = (Map)obj;
				} else {
					throw new IllegalArgumentException("Object cannot have properties: "+ident);
				}
			} else {
				// build out with SparseMap since these will be
				// encoded slightly differently to allow overlap
				child = new SparseMap();
				parent.put(childKey, child);
			}

			parent = child;
			prevDot = nextDot;
			nextDot = ident.indexOf('.', nextDot+1);
		}

		return parent.put(ident.substring(prevDot+1), DuelData.asProxy(value, false));
	}

	/**
	 * Builds a mutable Map from an interlaced sequence of key-value pairs
	 * @param pairs
	 * @return
	 */
	public static SparseMap asSparseMap(Object... pairs) {
		if (pairs == null || pairs.length < 1) {
			return new SparseMap(0);
		}

		int length = pairs.length/2;
		SparseMap map = new SparseMap(length+2);
		for (int i=0; i<length; i++) {
			String key = DuelData.coerceString(pairs[2*i]);
			Object value = pairs[2*i+1];
			map.putSparse(key, value);
		}
		return map;
	}
}
