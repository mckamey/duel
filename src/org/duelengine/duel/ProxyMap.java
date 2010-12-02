package org.duelengine.duel;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

class ProxyMap extends AbstractMap<String, Object> {

	private final boolean readonly;
	private final Object value;
	private BeanInfo beanInfo;
	private final Map<String, Method> getters = new HashMap<String, Method>();
	private final Map<String, Method> setters = new HashMap<String, Method>();

	public ProxyMap(Object value) {
		this(value, true);
	}

	public ProxyMap(Object value, boolean readonly) {
		if (value == null) {
			throw new NullPointerException("value");
		}

		this.value = value;
		this.readonly = readonly;

		try {
			this.beanInfo = Introspector.getBeanInfo(value.getClass());

		} catch (IntrospectionException ex) {
			this.beanInfo = null;
			ex.printStackTrace();
		}

		// TODO: perform this lazily
		this.ensureProperties();
	}

	/**
	 * Initializes all property getters/setters
	 */
	private void ensureProperties() {
		if (this.beanInfo == null) {
			return;
		}

		PropertyDescriptor[] properties = this.beanInfo.getPropertyDescriptors();
        if (properties != null) {
            for (PropertyDescriptor property : properties) {
                if (property == null) {
                	continue;
                }

                String name = property.getName();
                Method readMethod = property.getReadMethod();
                if (readMethod != null) {
                	this.getters.put(name, readMethod);
                }
                if (!this.readonly) {
	                Method writeMethod = property.getWriteMethod();
	                if (writeMethod != null) {
	                    this.setters.put(name, writeMethod);
	                }
                }
            }
        }

        this.beanInfo = null;
	}
	
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return new BeanEntrySet(this.value, this.getters);
	}

	@Override
	public Object get(Object key) {
		Method method = this.getters.get(key);
		try {
			return method.invoke(this.value);
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	class BeanEntrySet implements Set<Map.Entry<String, Object>> {

		private final Object value;
		private final Map<String, Method> getters;

		public BeanEntrySet(Object value, Map<String, Method> getters) {
			this.value = value;
			this.getters = getters;
		}

		@Override
		public boolean add(Map.Entry<String, Object> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends Map.Entry<String, Object>> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public Iterator<Map.Entry<String, Object>> iterator() {
			return new BeanIterator(this.value, this.getters);
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return this.getters.size();
		}

		@Override
		public Object[] toArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			throw new UnsupportedOperationException();
		}
	}

	class BeanIterator implements Iterator<Map.Entry<String, Object>> {

		private final Object value;
		private final Map<String, Method> getters;
		private final Iterator<String> iter; 

		public BeanIterator(Object value, Map<String, Method> getters) {
			this.value = value;
			this.getters = getters;
			this.iter = getters.keySet().iterator(); 
		}

		@Override
		public boolean hasNext() {
			return this.iter.hasNext();
		}

		@Override
		public Map.Entry<String, Object> next() {
			String key = this.iter.next();
			Method method = this.getters.get(key);
			Object val = null;
			try {
				val = method.invoke(this.value);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}
			return new AbstractMap.SimpleImmutableEntry<String, Object>(key, val);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
