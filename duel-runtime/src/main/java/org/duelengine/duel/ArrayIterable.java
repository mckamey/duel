package org.duelengine.duel;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Iterator;

/**
 * Adapts Array to partially implemented List without performing a copy.
 * Implements only iterator(), size() and get()
 */
class ArrayIterable extends AbstractList<Object> {

	private class ArrayIterator implements Iterator<Object> {

		private final Object array;
		private final int last;
		private int index = -1;

		public ArrayIterator(Object value, int length) {
			array = value;
			last = length-1;
		}

		@Override
		public boolean hasNext() {
			return (index < last);
		}

		@Override
		public Object next() {
			if (index > last) {
				// JavaScript style out of bounds
				return null;
			}

			return Array.get(array, ++index);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final Object array;
	private final int length;
	
	public ArrayIterable(Object value) {
		array = value;
		length = Array.getLength(array);
	}

	@Override
	public Iterator<Object> iterator() {
		if (array == null) {
			throw new NullPointerException("array");
		}

		return new ArrayIterator(array, length);
	}

	@Override
	public int size() {
		return length;
	}

	@Override
	public boolean isEmpty() {
		return length > 0;
	}

	@Override
	public Object[] toArray() {
		// this will only work for Object arrays
		return (Object[])array;
	}

	@Override
	public Object get(int index) {
		if (index < 0 || index >= length) {
			// JavaScript style out of bounds
			return null;
		}

		return Array.get(array, index);
	}
}
