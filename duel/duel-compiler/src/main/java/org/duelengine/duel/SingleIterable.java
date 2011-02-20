package org.duelengine.duel;

import java.util.*;

/**
 * Adapts a single Object partially implemented List without allocating a List.
 * Adapts Array to partially implemented List without performing a copy.
 * Implements only iterator(), size() and get()
 */
class SingleIterable extends AbstractList<Object> {

	private class SingleIterator implements Iterator<Object> {

		private final Object value;
		private boolean consumed;

		public SingleIterator(Object value) {
			this.value = value;
		}

		@Override
		public boolean hasNext() {
			return !this.consumed;
		}

		@Override
		public Object next() {
			if (this.consumed) {
				// JavaScript style out of bounds
				return null;
			}

			this.consumed = true;
			return this.value;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final Object value;

	public SingleIterable(Object value) {
		this.value = value;
	}

	@Override
	public Iterator<Object> iterator() {
		return new SingleIterator(this.value);
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
		return new Object[] { this.value };
	}

	@Override
	public Object get(int index) {
		if (index != 0) {
			// JavaScript style out of bounds
			return null;
		}

		return this.value;
	}

	@Override
	public int indexOf(Object val) {
		return (this.value == val) ? 0 : -1;
	}

	@Override
	public int lastIndexOf(Object val) {
		return this.indexOf(val);
	}
}
