package org.duelengine.duel;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

class ProxyMap extends AbstractMap<String, Object> {

	private final Object value;
	private final Map<String, Method> getters;
	private final Map<String, Method> setters;
	private boolean inited;

	public ProxyMap(Object value) {
		this(value, true);
	}

	public ProxyMap(Object value, boolean readonly) {
		if (value == null) {
			throw new NullPointerException("value");
		}

		this.value = value;
		this.getters = new HashMap<String, Method>();
		this.setters = readonly ? null : new HashMap<String, Method>();
	}

	/**
	 * Initializes all property getters/setters
	 */
	private void ensureProperties() {
		if (this.inited) {
			return;
		}

		try {
			BeanInfo info = Introspector.getBeanInfo(this.value.getClass());

			PropertyDescriptor[] properties = info.getPropertyDescriptors();
			if (properties != null) {

				boolean processSetters = !this.isReadonly();
				for (PropertyDescriptor property : properties) {
	                if (property == null) {
	                	continue;
	                }

	                String name = property.getName();
	                Method readMethod = property.getReadMethod();
	                if (readMethod != null) {
	                	this.getters.put(name, readMethod);
	                }

	                if (processSetters) {
		                Method writeMethod = property.getWriteMethod();
		                if (writeMethod != null) {
		                    this.setters.put(name, writeMethod);
		                }
	                }
	            }
	        }

		} catch (IntrospectionException ex) {
			ex.printStackTrace();

		} finally {
			this.inited = true;
		}
	}

	public boolean isReadonly() {
		return (this.setters == null);
	}

	@Override
	public boolean isEmpty() {
		this.ensureProperties();

		return this.getters.isEmpty();
	}

	@Override
	public Object get(Object key) {
		this.ensureProperties();

		Method method = this.getters.get(key);
		if (method == null) {
			return null;
		}

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

	@Override
	public Object put(String key, Object value) {
		if (this.isReadonly()) {
			throw new IllegalStateException("ProxyMap is readonly");
		}

		this.ensureProperties();

		Method method = this.getters.get(key);
		if (method == null) {
			return null;
		}

		try {
			return method.invoke(this.value, value);

		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		this.ensureProperties();

		return new ProxyEntrySet(this.value, this.getters);
	}

	class ProxyEntrySet extends AbstractSet<Map.Entry<String, Object>> {

		private final Object value;
		private final Map<String, Method> getters;

		public ProxyEntrySet(Object value, Map<String, Method> getters) {
			this.value = value;
			this.getters = getters;
		}

		@Override
		public boolean isEmpty() {
			return this.getters.isEmpty();
		}

		@Override
		public Iterator<Map.Entry<String, Object>> iterator() {
			return new ProxyIterator(this.value, this.getters);
		}

		@Override
		public int size() {
			return this.getters.size();
		}
	}

	class ProxyIterator implements Iterator<Map.Entry<String, Object>> {

		private final Object value;
		private final Map<String, Method> getters;
		private final Iterator<String> iterator; 

		public ProxyIterator(Object value, Map<String, Method> getters) {
			this.value = value;
			this.getters = getters;
			this.iterator = getters.keySet().iterator(); 
		}

		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}

		@Override
		public Map.Entry<String, Object> next() {
			String key = this.iterator.next();
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
