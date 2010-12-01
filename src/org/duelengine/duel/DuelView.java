package org.duelengine.duel;

import java.io.*;
import java.util.*;

/**
 * The skeletal implementation of DUEL view runtime
 */
public abstract class DuelView {

	private static final Double ZERO = Double.valueOf(0.0);
	private static final Double NaN = Double.valueOf(Double.NaN);
	private static final HTMLFormatter formatter = new HTMLFormatter();
	private Map<String, DuelPart> parts = null;

	protected DuelView() {
		// allow view to define child views
		this.init();
	}

	protected DuelView(DuelPart... parts) {

		// first allow view to define child views and default parts
		this.init();

		// then allow caller to replace any parts by name
		if (parts != null && parts.length > 0) {
			if (this.parts == null) {
				this.parts = new HashMap<String, DuelPart>(parts.length);
			}

			for (DuelPart part : parts) {
				if (part == null || part.getPartName() == null) {
					continue;
				}

				this.parts.put(part.getPartName(), part);
			}
		}
	}

	/**
	 * Initialization of views and parts
	 */
	protected void init() {}

	/**
	 * Sets the partial view for a named area
	 * @param part
	 */
	protected void addPart(DuelPart part) {
		if (part == null || part.getPartName() == null) {
			return;
		}

		if (this.parts == null) {
			this.parts = new HashMap<String, DuelPart>(4);
		}

		this.parts.put(part.getPartName(), part);
	}

	/**
	 * Renders a named partial view
	 * @param partName
	 * @param output
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected void renderPart(String partName, DuelContext output, Object data, int index, int count, String key)
		throws IOException {

		if (this.parts == null || !this.parts.containsKey(partName)) {
			return;
		}

		DuelPart part = this.parts.get(partName);
		if (part == null) {
			return;
		}

		part.render(output, data, index, count, key);
	}

	/**
	 * Allows one view to render another
	 * @param view
	 * @param output
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected void renderView(DuelView view, DuelContext output, Object data, int index, int count, String key)
		throws IOException {

		view.render(output, data, index, count, key);
	}

	/**
	 * Renders the view to the output
	 * @param output
	 */
	public void render(Appendable output)
		throws IOException {

		this.render(new DuelContext(output), Collections.emptyMap(), 0, 1, null);
	}

	/**
	 * Binds the view to the data and renders the view to the output
	 * @param output
	 * @param data
	 */
	public void render(Appendable output, Object data)
		throws IOException {

		this.render(new DuelContext(output), data, 0, 1, null);
	}

	/**
	 * Renders the view to the output
	 * @param output
	 */
	public void render(DuelContext output)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}

		this.render(output, Collections.emptyMap(), 0, 1, null);
	}

	/**
	 * Binds the view to the data and renders the view to the output
	 * @param output
	 * @param data
	 */
	public void render(DuelContext output, Object data)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}

		this.render(new DuelContext(output), data, 0, 1, null);
	}

	/**
	 * The entry point into the view tree
	 * @param output
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected abstract void render(DuelContext output, Object data, int index, int count, String key) throws IOException;

	/**
	 * Retrieves the property from the data object
	 * @param data
	 * @return
	 */
	protected Object getProperty(Object data, Object property) {
		if (data == null || property == null) {
			// technically "undefined" or error
			return null;
		}

		Class<?> dataType = data.getClass(); 
		String key = this.asString(property);

		if (isArray(dataType)) {
			List<?> list = this.asArray(data);

			if ("length".equals(key)) {
				return list.size();
			}

			if (isNumber(property.getClass())) {
				int index = ((Number)this.asNumber(property)).intValue();
				if ((index < 0) || (index >= list.size())) {
					// technically "undefined"
					return null;
				}
				return list.get(index);
			}

			// technically "undefined" or error
			return null;
		}

		if (isString(dataType)) {
			String str = this.asString(data);

			if ("length".equals(key)) {
				return str.length();
			}

			if (isNumber(property.getClass())) {
				int index = ((Number)this.asNumber(property)).intValue();
				if ((index < 0) || (index >= str.length())) {
					// technically "undefined"
					return null;
				}
				return str.charAt(index);
			}

			// technically "undefined" or error
			return null;
		}

		Map<?,?> map = this.asObject(data);
		if (map == null || !map.containsKey(key)) {
			// technically "undefined"
			return null;
		}
		return map.get(key);
	}

	/**
	 * Performs equality test
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean equal(Object a, Object b) {
		return (a == null) ? (b == null) : a.equals(b);
	}

	/**
	 * Coerces objects before performing equality test
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean coerceEqual(Object a, Object b) {
		if (a == null) {
			return (b == null);
		}

		// attempt to coerce b to the type of a
		Class<?> aType = a.getClass();
		if (isNumber(aType)) {
			b = this.asNumber(b);
		} else if (isString(aType)) {
			b = this.asString(b);
		} else if (isBoolean(aType)) {
			b = this.asBoolean(b);
		}

		return a.equals(b);
	}

	private static boolean isBoolean(Class<?> exprType) {
		return (boolean.class.equals(exprType) ||
			Boolean.class.equals(exprType));
	}

	private static boolean isNumber(Class<?> exprType) {
		return (Number.class.isAssignableFrom(exprType) ||
			int.class.isAssignableFrom(exprType) ||
			double.class.isAssignableFrom(exprType) ||
			float.class.isAssignableFrom(exprType) ||
			long.class.isAssignableFrom(exprType) ||
			short.class.isAssignableFrom(exprType) ||
			byte.class.isAssignableFrom(exprType));
	}

	private static boolean isString(Class<?> exprType) {
		return (String.class.equals(exprType));
	}

	private static boolean isArray(Class<?> exprType) {
		return (exprType.isArray() ||
			List.class.isAssignableFrom(exprType));
	}

	/**
	 * Coerces any Object to a boolean
	 * @param data
	 * @return
	 */
	protected boolean asBoolean(Object data) {
		return
			!(data == null ||
			Boolean.FALSE.equals(data) ||
			"".equals(data) ||
			ZERO.equals(data) ||
			NaN.equals(data));
	}

	/**
	 * Coerces any Object to a Number
	 * @param data
	 * @return
	 */
	protected double asNumber(Object data) {
		if (data instanceof Number) {
			return ((Number)data).doubleValue();
		}

		if (data instanceof Boolean) {
			return ((Boolean)data).booleanValue() ? 1.0 : 0.0;
		}

		return this.asBoolean(data) ? Double.NaN : 0.0;
	}

	/**
	 * Coerces any Object to a String 
	 * @param data
	 * @return
	 */
	protected String asString(Object data) {
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

		if (dataType.isArray()) {
			// flatten into simple list
			StringBuilder buffer = new StringBuilder();
			boolean needsDelim = false;
			for (Object item : (Object[])data) {
				if (needsDelim) {
					buffer.append(", ");
				} else {
					needsDelim = true;
				}
				buffer.append(this.asString(item));
			}
			return buffer.toString();
		}

		if (List.class.isAssignableFrom(dataType)) {
			// flatten into simple list
			StringBuilder buffer = new StringBuilder();
			boolean needsDelim = false;
			for (Object item : (Iterable<?>)data) {
				if (needsDelim) {
					buffer.append(", ");
				} else {
					needsDelim = true;
				}
				buffer.append(this.asString(item));
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
					.append(this.asString(entry.getKey()))
					.append('=')
					.append(this.asString(entry.getValue()));
			}

			return buffer.append('}').toString();
		}

		return data.toString();
	}

	/**
	 * Coerces any Object to a List
	 * @param data
	 * @return
	 */
	protected List<?> asArray(Object data) {
		if (data == null) {
			return Collections.EMPTY_LIST;
		}

		if (data instanceof List<?>) {
			// already correct type
			return (List<?>)data;
		}

		if (data instanceof Object[]) {
			return new ArrayIterable((Object[])data);
		}

		if (data instanceof Collection<?>) {
			// unfortunate but we need the size
			return new ArrayList<Object>((Collection<?>)data);
		}

		if (data instanceof Iterable<?>) {
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
	 * Coerces any Object to a Map
	 * @param data
	 * @return
	 */
	protected Map<?,?> asObject(Object data) {
		if (data == null) {
			return null;//Collections.EMPTY_MAP;
		}

		if (data instanceof Map<?,?>) {
			return (Map<?,?>)data;
		}

		return new BeanMap(data);
	}

	/**
	 * Builds a Map from an interlaced sequence of key-value pairs
	 * @param data
	 * @return
	 */
	protected Map<String,Object> asMap(Object... items) {
		if (items == null) {
			return new LinkedHashMap<String, Object>(0);
		}

		int length = items.length/2;
		Map<String, Object> map = new LinkedHashMap<String, Object>(length+2);
		for (int i=0; i<length; i++) {
			String key = this.asString(items[2*i]);
			Object value = items[2*i+1];
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Writes the value to the output
	 * @param output
	 * @param value
	 * @throws IOException
	 */
	protected void write(DuelContext output, Object value)
		throws IOException {

		if (value == null) {
			return;
		}

		output.append(this.asString(value));
	}

	/**
	 * Ensures the value is properly encoded as HTML text
	 * @param output
	 * @param value
	 * @throws IOException
	 */
	protected void htmlEncode(DuelContext output, Object value)
		throws IOException {

		if (value == null) {
			return;
		}

		if (value instanceof Boolean || value instanceof Number) {
			// no need to encode non-text primitives
			output.append(this.asString(value));

		} else {
			formatter.writeLiteral(output, this.asString(value), output.getEncodeNonASCII());
		}
	}

	/**
	 * A work-around for dynamic post-inc/dec operators
	 * @param value
	 * @param ignore
	 * @return
	 */
	protected double echo(double value, double ignore) {
		return value;
	}
}
