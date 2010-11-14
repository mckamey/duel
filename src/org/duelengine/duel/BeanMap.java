package org.duelengine.duel;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

class BeanMap extends AbstractMap<String, Object> {

	private final Object bean;
	private final Map<String, Method> getters = new HashMap<String, Method>();
	
	public BeanMap(Object bean) {
		if (bean == null) {
			throw new NullPointerException("value");
		}

		this.bean = bean;

		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(bean.getClass());
		} catch (IntrospectionException ex) {
			ex.printStackTrace();
		}

		PropertyDescriptor[] properties = (beanInfo == null) ? null : beanInfo.getPropertyDescriptors();
        if (properties != null) {
            for (PropertyDescriptor property : properties) {
                if (property == null) {
                	continue;
                }
                String name = property.getName();
                Method readMethod = property.getReadMethod();
                if (readMethod != null) {
                    getters.put(name, readMethod);
                }
            }
        }
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return new BeanEntrySet(this.bean, this.getters);
	}

	@Override
	public Object get(Object key) {
		Method method = getters.get(key);
		try {
			return method.invoke(this.bean);
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

		private final Object bean;
		private final Map<String, Method> getters;

		public BeanEntrySet(Object bean, Map<String, Method> getters) {
			this.bean = bean;
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
			return new BeanIterator(this.bean, this.getters);
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

		private final Object bean;
		private final Map<String, Method> getters;
		private final Iterator<String> iter; 

		public BeanIterator(Object bean, Map<String, Method> getters) {
			this.bean = bean;
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
			Method method = getters.get(key);
			Object val = null;
			try {
				val = method.invoke(this.bean);
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
