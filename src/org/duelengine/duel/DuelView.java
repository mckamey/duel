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
	protected void renderPart(String partName, DuelContext output, Object data, int index, int count, String key) {
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
	 * Renders the view to the output
	 * @param output
	 */
	public void render(Appendable output){
		this.render(new DuelContext(output), Collections.emptyMap(), 0, 1, null);
	}

	/**
	 * Binds the view to the data and renders the view to the output
	 * @param output
	 * @param data
	 */
	public void render(Appendable output, Object data) {
		this.render(new DuelContext(output), data, 0, 1, null);
	}

	/**
	 * Renders the view to the output
	 * @param output
	 */
	public void render(DuelContext output){
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
	public void render(DuelContext output, Object data) {
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
	protected abstract void render(DuelContext output, Object data, int index, int count, String key);

	/**
	 * Retrieves the property from the data object
	 * @param data
	 * @return
	 */
	protected Object getProperty(Object data, Object property) {
		if (data == null || property == null) {
			return null;
		}

		if (isArray(data.getClass())) {
			List<?> list = this.asArray(data);

			if ("length".equals(this.asString(property))) {
				return list.size();
			}

			if (isNumber(property.getClass())) {
				return list.get((int)this.asNumber(property));
			}

			return null;
		}

		String key = this.asString(property);
		Map<?,?> map = this.asObject(data);
		if (map == null || !map.containsKey(key)) {
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
	 * Coerces any Object to a JS Boolean
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
	 * Coerces any Object to a JS Number
	 * @param data
	 * @return
	 */
	protected double asNumber(Object data) {
		if (data == null) {
			return 0.0;
		}

		if (data instanceof Number) {
			return ((Number)data).doubleValue();
		}

		if (data instanceof Character) {
			return ((Character)data).charValue();
		}

		return this.asBoolean(data) ? NaN : 0.0;
	}

	/**
	 * Coerces any Object to a JS String 
	 * @param data
	 * @return
	 */
	protected String asString(Object data) {
		// TODO: check special values like NaN and +/-Infinity
		return (data == null) ? "" : data.toString();
	}

	/**
	 * Coerces any Object to a JS Array
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
	 * Coerces any Object to a JS Object
	 * @param data
	 * @return
	 */
	protected Map<?,?> asObject(Object data) {
		if (data == null) {
			return Collections.EMPTY_MAP;
		}

		if (data instanceof Map<?,?>) {
			return (Map<?,?>)data;
		}

		// TODO: convert arbitrary object to Map<String, ?>
		throw new IllegalArgumentException("TODO: convert object to map");
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

		output.append(value.toString());
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
			output.append(value.toString());

		} else {
			formatter.writeLiteral(output, String.valueOf(value), output.getEncodeNonASCII());
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
