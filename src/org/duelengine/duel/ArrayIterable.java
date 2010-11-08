package org.duelengine.duel;

import java.util.*;

/**
 * Adapts Array to Iterable interface without performing a copy.
 * Implements Collection only to provide size()
 */
class ArrayIterable implements Collection<Object> {

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

	@Override
	public int size() {
		return this.array.length;
	}

	@Override
	public boolean add(Object e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}
}
