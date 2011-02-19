package org.duelengine.duel;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Adapts Array to partially implemented List without performing a copy.
 * Implements only iterator(), size() and get()
 */
class ArrayIterable extends AbstractList<Object> {

	private class ArrayIterator implements Iterator<Object> {

		private final Object array;
		private final int last;
		private int index = -1;

		public ArrayIterator(Object array, int length) {
			this.array = array;
			this.last = length-1;
		}

		@Override
		public boolean hasNext() {
			return (this.index < this.last);
		}

		@Override
		public Object next() {
			if (this.index > this.last) {
				// JavaScript style out of bounds
				return null;
			}

			return Array.get(this.array, ++this.index);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final Object array;
	private final int length;
	
	public ArrayIterable(Object array) {
		this.array = array;
		this.length = Array.getLength(this.array);
	}

	@Override
	public Iterator<Object> iterator() {
		if (array == null) {
			throw new NullPointerException("array");
		}

		return new ArrayIterator(this.array, this.length);
	}

	@Override
	public int size() {
		return this.length;
	}

	@Override
	public boolean isEmpty() {
		return this.length > 0;
	}

	@Override
	public Object[] toArray() {
		// this will only work for Object arrays
		return (Object[])this.array;
	}

	@Override
	public Object get(int index) {
		if (index < 0 || index >= this.length) {
			// JavaScript style out of bounds
			return null;
		}

		return Array.get(this.array, index);
	}
}
