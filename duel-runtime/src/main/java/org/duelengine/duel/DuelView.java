package org.duelengine.duel;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The skeletal implementation of DUEL view runtime.
 * Inherently thread-safe as contains no mutable instance data.
 */
public abstract class DuelView {

	private static final int DEFAULT_INDEX = 0;
	private static final int DEFAULT_COUNT = 1;
	private static final String DEFAULT_KEY = null;

	private static final HTMLFormatter formatter = new HTMLFormatter();
	private Map<String, DuelPart> parts = null;

	protected DuelView() {
		// allow view to define child views
		init();
	}

	protected DuelView(DuelPart... viewParts) {

		// first allow view to define child views and default parts
		init();

		// then allow caller to replace any parts by name
		if (viewParts != null && viewParts.length > 0) {
			if (parts == null) {
				parts = new HashMap<String, DuelPart>(viewParts.length);
			}

			for (DuelPart part : viewParts) {
				if (part == null || part.getPartName() == null) {
					continue;
				}

				parts.put(part.getPartName(), part);
			}
		}
	}

	/**
	 * Initialization of parts and child-views
	 */
	protected void init() {}

	/**
	 * Renders the view to the output
	 * @param output
	 */
	public void render(Appendable output)
		throws IOException {

		if (output == null) {
			throw new NullPointerException("output");
		}

		// build a context
		render(new DuelContext().setOutput(output));
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

		// build a context
		render(new DuelContext().setOutput(output).setData(data));
	}

	/**
	 * Binds the view to any data and renders the view to the output
	 * @param context
	 */
	public void render(DuelContext context)
		throws IOException {

		if (context == null) {
			throw new NullPointerException("context");
		}

		render(context, DuelData.asProxy(context.getData(), true), DEFAULT_INDEX, DEFAULT_COUNT, DEFAULT_KEY);
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
	 * Sets the partial view for a named area
	 * @param part
	 */
	protected void addPart(DuelPart part) {
		if (part == null || part.getPartName() == null) {
			return;
		}

		if (parts == null) {
			parts = new HashMap<String, DuelPart>(4);
		}

		parts.put(part.getPartName(), part);
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

		if (parts == null || !parts.containsKey(partName)) {
			return;
		}

		DuelPart part = parts.get(partName);
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
	protected void write(DuelContext context, char value)
		throws IOException {

		context.getOutput().append(value);
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
			context.getOutput().append(DuelData.coerceString(value));

		} else {
			formatter.writeLiteral(context.getOutput(), DuelData.coerceString(value), context.getFormat().getEncodeNonASCII());
		}
	}

	/**
	 * Emits data object as a graph of JavaScript literals
	 * @param context
	 * @param data
	 * @param depth
	 * @throws IOException
	 */
	protected void dataEncode(DuelContext context, Object data, int depth)
		throws IOException {

		context.getEncoder().write(context.getOutput(), data, depth);
	}

	protected Object getExtra(DuelContext context, String ident) {
		return context.getExtra(ident);
	}

	protected void putExtra(DuelContext context, String ident, Object value) {
		context.putExtra(ident, value);
	}

	protected boolean hasExtras(DuelContext context, String... idents) {
		return context.hasExtras(idents);
	}

	protected void writeExtras(DuelContext context, boolean needsTags)
		throws IOException {

		if (!context.hasExtrasPending()) {
			return;
		}

		DataEncoder encoder = context.getEncoder();
		Appendable output = context.getOutput();
		if (needsTags) {
			formatter.writeOpenElementBeginTag(output, "script");
			if (context.getFormat().getScriptTypeAttr()) {
				formatter.writeAttribute(output, "type", "text/javascript");
			}
			formatter.writeCloseElementBeginTag(output);
			encoder.writeln(output, 0);
		}
		encoder.writeVars(output, context.getPendingExtras());
		if (needsTags) {
			// arbitrarily 1, ideally this would be the current depth
			encoder.indent(output, 1);
			formatter.writeElementEndTag(output, "script");
			// arbitrarily 1, ideally this would be emitted as a literal in the AST
			// this is emitted even if script block is compacted
			output.append("\n\t");
		}
	}
	
	protected String nextID(DuelContext context) {
		return context.nextID();
	}

	/**
	 * Allows view to intercept and transform URLs within element attributes
	 * @param url URL
	 * @return transformed URL
	 */
	protected String transformURL(DuelContext context, String url) {
		return context.transformURL(url);
	}

	/**
	 * Retrieves the value of a property from the data object
	 * @param data
	 * @return
	 */
	protected Object getProperty(Object data, Object property) {
		if (data == null || property == null) {
			// technically NPE if data was null but we
			// allow here since we chain property accesses
			// to test if extras are available server-side
			return JSUtility.UNDEFINED;
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
					return JSUtility.UNDEFINED;
				}
				return str.charAt(index);
			}

			return JSUtility.UNDEFINED;
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
					return JSUtility.UNDEFINED;
				}
				return DuelData.asProxy(((List<?>)array).get(index), true);
			}

			return JSUtility.UNDEFINED;
		}

		Map<?,?> map = DuelData.coerceMap(data);
		if (map == null || !map.containsKey(key)) {
			// technically NPE if map was null but we
			// allow here since we chain property accesses
			// to test if extras are available server-side
			return JSUtility.UNDEFINED;
		}

		return DuelData.asProxy(map.get(key), true);
	}

	/**
	 * Stores the value for a property of the data object
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setProperty(Object data, Object property, Object value) {
		if (data == null || property == null) {
			// technically error if data is null
			return;
		}

		Class<?> dataType = data.getClass(); 

		if (Map.class.isAssignableFrom(dataType)) {
			Map map = (Map)data;
			map.put(DuelData.coerceString(property), value);
			return;
		}

		if (dataType.isArray()) {
			if (DuelData.isNumber(property.getClass())) {
				int index = ((Number)DuelData.coerceNumber(property)).intValue();
				if ((index < 0) || (index >= Array.getLength(data))) {
					return;
				}
				Array.set(data, index, value);
				return;
			}
			return;
		}

		if (List.class.isAssignableFrom(dataType)) {
			List array = (List)data;
			if (DuelData.isNumber(property.getClass())) {
				int index = ((Number)DuelData.coerceNumber(property)).intValue();
				if ((index < 0) || (index >= array.size())) {
					return;
				}
				array.set(index, data);
				return;
			}
			return;
		}
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
			// ensure both are Double
			a = DuelData.coerceNumber(a);
			b = DuelData.coerceNumber(b);

		} else if (DuelData.isString(aType)) {
			b = DuelData.coerceString(b);

		} else if (DuelData.isBoolean(aType)) {
			b = DuelData.coerceBoolean(b);
		}

		return a.equals(b);
	}

	protected Object LogicalOR(Object left, Object right) {
		return DuelData.coerceBoolean(left) ? left : right;
	}

	protected Object LogicalAND(Object left, Object right) {
		return DuelData.coerceBoolean(left) ? right : left;
	}

	/**
	 * A work-around for dynamic post-inc/dec operator semantics
	 * @param value
	 * @param ignore
	 * @return
	 */
	protected double echo(double value, double ignore) {
		return value;
	}
}
