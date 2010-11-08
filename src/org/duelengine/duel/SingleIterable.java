package org.duelengine.duel;

import java.util.*;

/**
 * Adapts a single Object to Iterable interface without allocating a List.
 * Implements Collection only to provide size()
 */
class SingleIterable implements Collection<Object> {

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

	@Override
	public int size() {
		return 1;
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
