package org.duelengine.duel;

import java.util.*;

/**
 * Adapts a single Object partially implemented List without allocating a List.
 * Adapts Array to partially implemented List without performing a copy.
 * Implements only iterator(), size() and get()
 */
class SingleIterable implements List<Object> {

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
		return new Object[] { this.value };
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends Object> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(int index) {
		if (index != 0) {
			throw new ArrayIndexOutOfBoundsException();
		}

		return this.value;
	}

	@Override
	public int indexOf(Object val) {
		return (val == this.value) ? 0 : -1;
	}

	@Override
	public int lastIndexOf(Object val) {
		return this.indexOf(val);
	}

	@Override
	public ListIterator<Object> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<Object> listIterator(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object set(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Object> subList(int arg0, int arg1) {
		throw new UnsupportedOperationException();
	}
}
