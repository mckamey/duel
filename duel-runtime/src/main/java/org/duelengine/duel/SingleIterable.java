package org.duelengine.duel;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * Adapts a single Object partially implemented List without allocating a List.
 * Adapts Array to partially implemented List without performing a copy.
 * Implements only iterator(), size() and get()
 */
class SingleIterable extends AbstractList<Object> {

	private class SingleIterator implements Iterator<Object> {

		private final Object value;
		private boolean consumed;

		public SingleIterator(Object proxyValue) {
			value = proxyValue;
		}

		@Override
		public boolean hasNext() {
			return !consumed;
		}

		@Override
		public Object next() {
			if (consumed) {
				// JavaScript style out of bounds
				return null;
			}

			consumed = true;
			return value;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final Object value;

	public SingleIterable(Object proxyValue) {
		value = proxyValue;
	}

	@Override
	public Iterator<Object> iterator() {
		return new SingleIterator(value);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Object[] toArray() {
		return new Object[] { value };
	}

	@Override
	public Object get(int index) {
		if (index != 0) {
			// JavaScript style out of bounds
			return null;
		}

		return value;
	}

	@Override
	public int indexOf(Object val) {
		return (value == val) ? 0 : -1;
	}

	@Override
	public int lastIndexOf(Object val) {
		return indexOf(val);
	}
}
