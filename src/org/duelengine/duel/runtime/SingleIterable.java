package org.duelengine.duel.runtime;

import java.util.*;

/**
 * Adapts a single Object to Iterable interface without allocating a List.
 */
class SingleIterable implements Iterable<Object> {

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
				throw new NoSuchElementException("No more elements.");
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
}
