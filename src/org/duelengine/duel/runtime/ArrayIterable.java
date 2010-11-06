package org.duelengine.duel.runtime;

import java.util.*;

/**
 * Adapts Array to Iterable interface without performing a copy.
 */
class ArrayIterable implements Iterable<Object> {

	private class ArrayIterator implements Iterator<Object> {

		private final Object[] array;
		private int index = -1;
		private final int last;

		public ArrayIterator(Object[] array) {
			this.array = array;
			this.last = array.length-1;
		}

		@Override
		public boolean hasNext() {
			return (this.index < this.last);
		}

		@Override
		public Object next() {
			if (this.index >= this.last) {
				throw new NoSuchElementException("Passed end of array.");
			}

			return this.array[++this.index];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final Object[] array;
	
	public ArrayIterable(Object[] array) {
		this.array = array;
	}

	@Override
	public Iterator<Object> iterator() {
		if (array == null) {
			throw new NullPointerException("array");
		}

		return new ArrayIterator(this.array);
	}
}
