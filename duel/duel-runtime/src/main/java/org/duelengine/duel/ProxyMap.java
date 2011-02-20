package org.duelengine.duel;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A proxy for arbitrary classes.
 * Allows bean-like property access via a Map interface,
 * and allows expando properties to be dynamically added
 */
class ProxyMap extends AbstractMap<String, Object> {

	private final Object value;
	private final Map<String, Method> getters;
	private final Map<String, Method> setters;
	private Map<String, Object> expando;
	private boolean inited;

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
	                if ("class".equals(name)) {
	                	continue;
	                }

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

		return this.getters.isEmpty() && (this.expando == null || this.expando.isEmpty());
	}

	@Override
	public Object get(Object key) {
		this.ensureProperties();

		Method method = this.getters.get(key);
		if (method == null) {
			if (this.expando == null) {
				return null;
			}
			return this.expando.get(key); 
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
			throw new IllegalStateException("The ProxyMap is readonly");
		}

		this.ensureProperties();

		Method method = this.setters.get(key);
		if (method == null) {
			if (this.expando == null) {
				this.expando = new LinkedHashMap<String, Object>();
			}
			return this.expando.put(key, value);
		}

		try {
			Object old = this.get(key);
			method.invoke(this.value, value);
			return old;

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

		return new ProxyEntrySet(this.value, this.getters, this.expando);
	}

	static class ProxyEntrySet extends AbstractSet<Map.Entry<String, Object>> {

		private final Object value;
		private final Map<String, Method> getters;
		private final Map<String, Object> expando;

		public ProxyEntrySet(Object value, Map<String, Method> getters, Map<String, Object> expando) {
			this.value = value;
			this.getters = getters;
			this.expando = expando;
		}

		@Override
		public boolean isEmpty() {
			return this.getters.isEmpty() && (this.expando == null || this.expando.isEmpty());
		}

		@Override
		public Iterator<Map.Entry<String, Object>> iterator() {
			return new ProxyIterator(this.value, this.getters, this.expando);
		}

		@Override
		public int size() {
			return this.getters.size() + (this.expando == null ? 0 : this.expando.size());
		}
	}

	static class ProxyIterator implements Iterator<Map.Entry<String, Object>> {

		private final Object value;
		private final Map<String, Method> getters;
		private final Map<String, Object> expando;
		private final Iterator<String> getterIterator;
		private final Iterator<String> expandoIterator;

		public ProxyIterator(Object value, Map<String, Method> getters, Map<String, Object> expando) {
			this.value = value;
			this.getters = getters;
			this.expando = expando;
			this.getterIterator = getters.keySet().iterator(); 
			this.expandoIterator = (expando != null) ? expando.keySet().iterator() : null; 
		}

		@Override
		public boolean hasNext() {
			return this.getterIterator.hasNext() || (this.expando != null && this.expandoIterator.hasNext());
		}

		@Override
		public Map.Entry<String, Object> next() {
			String key = null;
			Object val = null;
			if (this.getterIterator.hasNext()) {
				key = this.getterIterator.next();
				Method method = this.getters.get(key);
	
				try {
					val = method.invoke(this.value);
	
				} catch (InvocationTargetException ex) {
					ex.printStackTrace();
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				}
			} else if (this.expando != null){
				key = this.expandoIterator.next();
				val = this.expando.get(key);
			}

			return new AbstractMap.SimpleImmutableEntry<String, Object>(key, val);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
