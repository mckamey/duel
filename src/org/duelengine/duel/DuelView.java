package org.duelengine.duel;

import java.io.*;
import java.util.*;

/**
 * The skeletal implementation of DUEL view runtime.
 * Inherently thread-safe as contains no instance data.
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
	 * @param context
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected void renderPart(DuelContext context, String partName, Object data, int index, int count, String key)
		throws IOException {

		if (this.parts == null || !this.parts.containsKey(partName)) {
			return;
		}

		DuelPart part = this.parts.get(partName);
		if (part == null) {
			return;
		}

		part.render(context, data, index, count, key);
	}

	/**
	 * Allows one view to render another
	 * @param view
	 * @param context
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected void renderView(DuelContext context, DuelView view, Object data, int index, int count, String key)
		throws IOException {

		view.render(context, data, index, count, key);
	}

	/**
	 * Renders the view to the output
	 * @param output
	 */
	public void render(Appendable output)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}

		DuelContext context = (output instanceof DuelContext) ? (DuelContext)output : new DuelContext(output);
		
		this.render(context, Collections.EMPTY_MAP, 0, 1, null);
	}

	/**
	 * Binds the view to the data and renders the view to the output
	 * @param output
	 * @param data
	 */
	public void render(Appendable output, Object data)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}

		DuelContext context = (output instanceof DuelContext) ? (DuelContext)output : new DuelContext(output);

		this.render(context, DuelData.asProxy(data, true), 0, 1, null);
	}

	/**
	 * The entry point into the view tree
	 * @param context
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected abstract void render(DuelContext context, Object data, int index, int count, String key)
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
			Collection<?> array = DuelData.coerceCollection(data);

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
				return DuelData.asProxy(((List<?>)array).get(index), true);
			}

			// technically "undefined"
			return null;
		}

		Map<?,?> map = DuelData.coerceMap(data);
		if (map == null || !map.containsKey(key)) {
			// technically "undefined"
			return null;
		}

		return DuelData.asProxy(map.get(key), true);
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
	 * @param context
	 * @param value
	 * @throws IOException
	 */
	protected void write(DuelContext context, Object value)
		throws IOException {

		if (value == null) {
			return;
		}

		context.getOutput().append(DuelData.coerceString(value));
	}

	/**
	 * Writes the value to the output
	 * @param context
	 * @param value
	 * @throws IOException
	 */
	protected void write(DuelContext context, String value)
		throws IOException {

		context.getOutput().append(value);
	}

	/**
	 * Writes the value to the output
	 * @param context
	 * @param value
	 * @throws IOException
	 */
	protected void write(DuelContext context, char value)
		throws IOException {

		context.getOutput().append(value);
	}

	/**
	 * Ensures the value is properly encoded as HTML text
	 * @param context
	 * @param value
	 * @throws IOException
	 */
	protected void htmlEncode(DuelContext context, Object value)
		throws IOException {

		if (value == null) {
			return;
		}

		if (value instanceof Boolean || value instanceof Number) {
			// no need to encode non-text primitives
			context.append(DuelData.coerceString(value));

		} else {
			formatter.writeLiteral(context, DuelData.coerceString(value), context.getEncodeNonASCII());
		}
	}

	protected Object getGlobal(DuelContext context, String ident) {
		return context.getGlobal(ident);
	}

	protected Object hasGlobals(DuelContext context, String... idents) {
		return context.hasGlobals(idents);
	}

	protected void writeGlobals(DuelContext context, DataEncoder encoder, boolean needsTags)
		throws IOException {

		if (!context.isGlobalsPending()) {
			return;
		}

		if (needsTags) {
			formatter.writeOpenElementBeginTag(context, "script");
			formatter.writeAttribute(context, "type", "text/javascript");
			formatter.writeCloseElementBeginTag(context);
		}
		encoder.writeVars(context, context.getGlobals());
		if (needsTags) {
			formatter.writeElementEndTag(context, "script");
		}
		context.setGlobalsPending(false);
	}

	protected String nextID(DuelContext context) {
		return context.nextID();
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
