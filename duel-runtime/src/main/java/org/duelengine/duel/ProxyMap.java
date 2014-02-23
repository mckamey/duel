package org.duelengine.duel;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

	public ProxyMap(Object proxyValue, boolean readonly) {
		if (proxyValue == null) {
			throw new NullPointerException("proxyValue");
		}

		value = proxyValue;
		getters = new HashMap<String, Method>();
		setters = readonly ? null : new HashMap<String, Method>();
	}

	/**
	 * Initializes all property getters/setters
	 */
	private void ensureProperties() {
		if (inited) {
			return;
		}

		try {
			BeanInfo info = Introspector.getBeanInfo(value.getClass());

			PropertyDescriptor[] properties = info.getPropertyDescriptors();
			if (properties != null) {

				boolean processSetters = !isReadonly();
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
	                	getters.put(name, readMethod);
	                }

	                if (processSetters) {
		                Method writeMethod = property.getWriteMethod();
		                if (writeMethod != null) {
		                    setters.put(name, writeMethod);
		                }
	                }
	            }
	        }

		} catch (IntrospectionException ex) {
			ex.printStackTrace();

		} finally {
			inited = true;
		}
	}

	public boolean isReadonly() {
		return (setters == null);
	}

	@Override
	public boolean isEmpty() {
		ensureProperties();

		return getters.isEmpty() && (expando == null || expando.isEmpty());
	}

	@Override
	public Object get(Object key) {
		ensureProperties();

		Method method = getters.get(key);
		if (method == null) {
			if (expando == null) {
				return null;
			}
			return expando.get(key); 
		}

		try {
			return method.invoke(value);

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
		if (isReadonly()) {
			throw new IllegalStateException("The ProxyMap is readonly");
		}

		ensureProperties();

		Method method = setters.get(key);
		if (method == null) {
			if (expando == null) {
				expando = new LinkedHashMap<String, Object>();
			}
			return expando.put(key, value);
		}

		try {
			Object old = get(key);
			method.invoke(value, value);
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
		ensureProperties();

		return new ProxyEntrySet(value, getters, expando);
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
			return getters.isEmpty() && (expando == null || expando.isEmpty());
		}

		@Override
		public Iterator<Map.Entry<String, Object>> iterator() {
			return new ProxyIterator(value, getters, expando);
		}

		@Override
		public int size() {
			return getters.size() + (expando == null ? 0 : expando.size());
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
			getterIterator = getters.keySet().iterator(); 
			expandoIterator = (expando != null) ? expando.keySet().iterator() : null; 
		}

		@Override
		public boolean hasNext() {
			return getterIterator.hasNext() || (expando != null && expandoIterator.hasNext());
		}

		@Override
		public Map.Entry<String, Object> next() {
			String key = null;
			Object val = null;
			if (getterIterator.hasNext()) {
				key = getterIterator.next();
				Method method = getters.get(key);
	
				try {
					val = method.invoke(value);
	
				} catch (InvocationTargetException ex) {
					ex.printStackTrace();
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				}
			} else if (expando != null){
				key = expandoIterator.next();
				val = expando.get(key);
			}

			return new AbstractMap.SimpleImmutableEntry<String, Object>(key, val);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
