package org.duelengine.duel;

import java.io.*;
import java.util.*;
import org.duelengine.duel.codegen.HTMLFormatter;

/**
 * The skeletal implementation of DUEL view runtime
 */
public abstract class DuelView {
	
	private final ClientIDStrategy clientID;
	private HTMLFormatter formatter;
	private static final Double ZERO = Double.valueOf(0);
	private static final Double NaN = Double.valueOf(Double.NaN);

	protected DuelView() {
		this(new IncClientIDStrategy());
	}

	protected DuelView(ClientIDStrategy clientID) {
		if (clientID == null) {
			throw new NullPointerException("clientID");
		}

		this.clientID = clientID;

		this.init();
	}

	protected DuelView(DuelView view) {
		if (view == null) {
			throw new NullPointerException("view");
		}

		// share the naming context
		this.clientID = view.clientID;

		this.init();
	}

	/**
	 * Allows more complex initialization of internal objects
	 */
	protected void init() {}

	/**
	 * Renders the view to the output
	 * @param output
	 */
	public void render(Appendable output){
		this.render(output, Collections.emptyMap());
	}

	/**
	 * Binds the view to the data and renders the view to the output
	 * @param output
	 * @param data
	 */
	public void render(Appendable output, Object data) {
		if (output == null) {
			throw new NullPointerException("output");
		}

		try {
			this.render(output, data, 0, 1, null);
		} finally {
			this.formatter = null;
			this.clientID.resetID();
		}
	}

	/**
	 * The entry point into the view tree
	 * @param output
	 * @param data
	 * @param index
	 * @param count
	 * @param key
	 */
	protected abstract void render(Appendable output, Object data, int index, int count, String key);

	/**
	 * Retrieves the property from the data object
	 * @param data
	 * @return
	 */
	protected Object getProperty(Object data, Object property) {
		if (data == null || property == null) {
			return null;
		}

		String propertyName = property.toString();

		if (data instanceof Map<?,?>) {
			@SuppressWarnings("unchecked")
			Map<Object,Object> map = (Map<Object,Object>)data;
			return map.get(propertyName);
		}

		// TODO: convert arbitrary object to Map
		throw new IllegalArgumentException("TODO: convert object to map");
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
		return (data == null) ? "" : data.toString();
	}

	/**
	 * Coerces any Object to a JS Array
	 * @param data
	 * @return
	 */
	protected Collection<?> asArray(Object data) {
		if (data == null) {
			return Collections.EMPTY_LIST;
		}

		if (data instanceof Collection<?>) {
			// already is
			return (Collection<?>)data;
		}

		if (data instanceof Object[]) {
			return new ArrayIterable((Object[])data);
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
	protected Collection<Map.Entry<Object,Object>> asObject(Object data) {

		if (data instanceof Map<?,?>) {
			@SuppressWarnings("unchecked")
			Map<Object,Object> map = (Map<Object,Object>)data;
			return map.entrySet();
		}

		// TODO: convert arbitrary object to Iterable<Map.Entry> with size()
		throw new IllegalArgumentException("TODO: convert object to map");
	}

	/**
	 * Writes the value to the output
	 * @param output
	 * @param value
	 * @throws IOException
	 */
	protected void write(Appendable output, Object value)
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
	protected void htmlEncode(Appendable output, Object value)
		throws IOException {

		if (value == null) {
			return;
		}

		if (value instanceof Boolean || value instanceof Number) {
			// no need to encode non-text primitives
			output.append(value.toString());

		} else {
			if (this.formatter == null) {
				this.formatter = new HTMLFormatter(output);
			}

			this.formatter.writeLiteral(String.valueOf(value));
		}
	}
}
