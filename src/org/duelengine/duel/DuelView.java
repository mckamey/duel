package org.duelengine.duel;

import java.io.*;
import java.util.*;

/**
 * The skeletal implementation of DUEL view runtime
 */
public abstract class DuelView {

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

		this.render(new DuelContext(output), null, 0, 1, null);
	}

	/**
	 * Binds the view to the data and renders the view to the output
	 * @param output
	 * @param data
	 */
	public void render(Appendable output, Object data)
		throws IOException {

		this.render(new DuelContext(output), DuelData.asProxy(data), 0, 1, null);
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

		this.render(output, Collections.EMPTY_MAP, 0, 1, null);
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

		this.render(new DuelContext(output), DuelData.asProxy(data), 0, 1, null);
	}

	/**
	 * The entry point into the view tree
	 * @param output
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected abstract void render(DuelContext output, Object data, int index, int count, String key)
		throws IOException;

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
		String key = DuelData.coerceString(property);

		if (DuelData.isString(dataType)) {
			String str = DuelData.coerceString(data);

			if ("length".equals(key)) {
				return str.length();
			}

			if (DuelData.isNumber(property.getClass())) {
				int index = ((Number)DuelData.coerceNumber(property)).intValue();
				if ((index < 0) || (index >= str.length())) {
					// technically "undefined"
					return null;
				}
				return str.charAt(index);
			}

			// technically "undefined"
			return null;
		}

		if (DuelData.isArray(dataType)) {
			Collection<?> array = DuelData.coerceJSArray(data);

			if ("length".equals(key)) {
				return array.size();
			}

			if (DuelData.isNumber(property.getClass()) &&
				array instanceof List<?>) {
				int index = ((Number)DuelData.coerceNumber(property)).intValue();
				if ((index < 0) || (index >= array.size())) {
					// technically "undefined"
					return null;
				}
				return DuelData.asProxy(((List<?>)array).get(index));
			}

			// technically "undefined"
			return null;
		}

		Map<?,?> map = DuelData.coerceJSObject(data);
		if (map == null || !map.containsKey(key)) {
			// technically "undefined"
			return null;
		}

		return DuelData.asProxy(map.get(key));
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

		if (DuelData.isNumber(aType)) {
			b = DuelData.coerceNumber(b);

		} else if (DuelData.isString(aType)) {
			b = DuelData.coerceString(b);

		} else if (DuelData.isBoolean(aType)) {
			b = DuelData.coerceBoolean(b);
		}

		return a.equals(b);
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

		output.append(DuelData.coerceString(value));
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
			output.append(DuelData.coerceString(value));

		} else {
			formatter.writeLiteral(output, DuelData.coerceString(value), output.getEncodeNonASCII());
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
