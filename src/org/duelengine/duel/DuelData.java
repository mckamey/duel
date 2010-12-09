package org.duelengine.duel;

import java.lang.reflect.Array;
import java.math.*;
import java.util.*;

public final class DuelData {
	
	private static final Double ZERO = Double.valueOf(0.0);
	private static final Double NaN = Double.valueOf(Double.NaN);

	// static class
	private DuelData() {}

	/**
	 * Builds a mutable Map from an interlaced sequence of key-value pairs
	 * @param pairs
	 * @return
	 */
	public static Map<String, Object> asMap(Object... pairs) {
		if (pairs == null || pairs.length < 1) {
			return new LinkedHashMap<String, Object>(0);
		}

		int length = pairs.length/2;
		Map<String, Object> map = new LinkedHashMap<String, Object>(length+2);
		for (int i=0; i<length; i++) {
			String key = coerceString(pairs[2*i]);
			Object value = pairs[2*i+1];
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Builds a mutable List from a sequence of items
	 * @param items
	 * @return
	 */
	public static <T> List<T> asList(T... items) {
		if (items == null || items.length < 1) {
			return new ArrayList<T>(0);
		}

		return Arrays.asList(items);
	}

	public static boolean isPrimitive(Class<?> dataType) {
		return (dataType.isPrimitive() ||
				String.class.equals(dataType) ||
				Number.class.isAssignableFrom(dataType) ||
				Date.class.equals(dataType) ||
				Boolean.class.equals(dataType) ||
				Character.class.isAssignableFrom(dataType));
	}

	public static boolean isBoolean(Class<?> exprType) {
		return (Boolean.class.equals(exprType) ||
				boolean.class.equals(exprType));
	}

	public static boolean isNumber(Class<?> exprType) {
		return (Number.class.isAssignableFrom(exprType) ||
				int.class.isAssignableFrom(exprType) ||
				double.class.isAssignableFrom(exprType) ||
				float.class.isAssignableFrom(exprType) ||
				long.class.isAssignableFrom(exprType) ||
				short.class.isAssignableFrom(exprType) ||
				byte.class.isAssignableFrom(exprType) ||
				BigInteger.class.isAssignableFrom(exprType) ||
				BigDecimal.class.isAssignableFrom(exprType));
	}

	public static boolean isString(Class<?> exprType) {
		return (String.class.equals(exprType));
	}

	public static boolean isArray(Class<?> exprType) {
		return (exprType.isArray() ||
				Iterable.class.isAssignableFrom(exprType));
	}

	/**
	 * Coerces any Object to a JS-style Boolean
	 * @param data
	 * @return
	 */
	public static boolean coerceBoolean(Object data) {
		return
			!(data == null ||
			Boolean.FALSE.equals(data) ||
			"".equals(data) ||
			ZERO.equals(data) ||
			NaN.equals(data));
	}

	/**
	 * Coerces any Object to a JS-style Number (double)
	 * @param data
	 * @return
	 */
	public static double coerceNumber(Object data) {
		if (data instanceof Number) {
			return ((Number)data).doubleValue();
		}

		if (data instanceof Boolean) {
			return ((Boolean)data).booleanValue() ? 1.0 : 0.0;
		}

		return coerceBoolean(data) ? Double.NaN : 0.0;
	}

	/**
	 * Coerces any Object to a JS-style String 
	 * @param data
	 * @return
	 */
	public static String coerceString(Object data) {
		if (data == null) {
			return "";
		}

		Class<?> dataType = data.getClass();

		if (String.class.equals(dataType)) {
			return (String)data;
		}

		if (Date.class.equals(dataType)) {
			// YYYY-MM-DD HH:mm:ss Z
			//return String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS.%1$tLZ", data);
			return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS Z", data);
		}

		if (isNumber(dataType)) {
			// format like JavaScript
			double number = ((Number)data).doubleValue();

			// integers formatted without trailing decimals
			if (number == (double)((long)number)) {
				return Long.toString((long)number);
			}

			// correctly prints NaN, Infinity, -Infinity
			return Double.toString(number);
		}

		if (Iterable.class.isAssignableFrom(dataType)) {
			// flatten into simple list
			StringBuilder buffer = new StringBuilder();
			boolean needsDelim = false;
			for (Object item : (Iterable<?>)data) {
				if (needsDelim) {
					buffer.append(", ");
				} else {
					needsDelim = true;
				}
				buffer.append(coerceString(item));
			}
			return buffer.toString();
		}

		if (dataType.isArray()) {
			// flatten into simple list
			StringBuilder buffer = new StringBuilder();
			boolean needsDelim = false;
			for (int i=0, length=Array.getLength(data); i<length; i++) {
				if (needsDelim) {
					buffer.append(", ");
				} else {
					needsDelim = true;
				}
				buffer.append(coerceString(Array.get(data, i)));
			}
			return buffer.toString();
		}

		if (Map.class.isAssignableFrom(dataType)) {
			// format JSON-like
			Map<?,?> map = (Map<?,?>)data;
			Iterator<?> iterator = map.entrySet().iterator();
			StringBuilder buffer = new StringBuilder().append('{');

			boolean needsDelim = false;
			while (iterator.hasNext()) {
				if (needsDelim) {
					buffer.append(", ");
				} else {
					needsDelim = true;
				}

				Map.Entry<?,?> entry = (Map.Entry<?,?>)iterator.next();
				buffer
					.append(coerceString(entry.getKey()))
					.append('=')
					.append(coerceString(entry.getValue()));
			}

			return buffer.append('}').toString();
		}

		return data.toString();
	}

	/**
	 * Coerces any Object to a JS-style Array (List)
	 * @param data
	 * @return
	 */
	public static Collection<?> coerceCollection(Object data) {
		if (data == null) {
			return Collections.EMPTY_LIST;
		}

		Class<?> dataType = data.getClass();

		if (Collection.class.isAssignableFrom(dataType)) {
			// already correct type
			return (Collection<?>)data;
		}

		if (dataType.isArray()) {
			return new ArrayIterable(data);
		}

		if (Iterable.class.isAssignableFrom(dataType)) {
			// unfortunate but we need the size
			List<Object> list = new LinkedList<Object>();
			for (Object item : (Iterable<?>)data) {
				list.add(item);
			}
			return list;
		}

		return new SingleIterable(data);
	}

	/**
	 * Coerces any Object to a JS-style Object (Map)
	 * @param data
	 * @return
	 */
	public static Map<?,?> coerceMap(Object data) {
		if (data == null) {
			return Collections.EMPTY_MAP;
		}

		Class<?> dataType = data.getClass();

		if (Map.class.isAssignableFrom(dataType)) {
			return (Map<?,?>)data;
		}

		if (isPrimitive(dataType)) {
			// doesn't make sense to coerce to Map
			// this will give results similar to client-side
			return asMap("", data);
		}

		if (isArray(dataType)) {
			int i = 0;
			Collection<?> array = coerceCollection(data);
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(array.size());
			for (Object item : array) {
				map.put(Integer.toString(i++), item);
			}
			return map;
		}

		return new ProxyMap(data, true);
	}

	/**
	 * Ensures a data object is easily walked
	 * @param data
	 * @return
	 */
	static Object asProxy(Object data, boolean readonly) {
		if (data == null) {
			return null;
		}

		Class<?> dataType = data.getClass();
		if (isPrimitive(dataType) ||
			isArray(dataType) ||
			Map.class.isAssignableFrom(dataType)) {

			return data;
		}

		// wrap for easy access
		return new ProxyMap(data, readonly);
	}
}
