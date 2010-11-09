package org.duelengine.duel;

import java.io.*;
import java.util.*;

import org.duelengine.duel.codegen.HTMLFormatter;

/**
 * The skeletal implementation of DUEL view runtime
 */
public abstract class View {
	
	private final ClientIDStrategy clientID;
	private HTMLFormatter formatter;

	protected View() {
		this(new IncClientIDStrategy());
	}

	protected View(ClientIDStrategy clientID) {
		if (clientID == null) {
			throw new NullPointerException("clientID");
		}

		this.clientID = clientID;

		this.init();
	}

	protected View(View view) {
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

	public void render(Appendable output){
		this.render(output, Collections.emptyMap());
	}

	public void render(Appendable output, Object data) {
		if (output == null) {
			throw new NullPointerException("output");
		}

		try {
			// TODO.
		} finally {
			this.formatter = null;
			this.clientID.resetID();
		}
	}

	/**
	 * Adapts any Object to Iterable with size()
	 * @param data
	 * @return
	 */
	protected Collection<?> asItems(Object data) {

		if (data instanceof Collection<?>) {
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

		// null is allowed
		return new SingleIterable(data);
	}

	/**
	 * Adapts any Object to Iterable<Map.Entry> with size()
	 * @param data
	 * @return
	 */
	protected Collection<Map.Entry<Object,Object>> asEntries(Object data) {

		if (data instanceof Map<?,?>) {
			@SuppressWarnings("unchecked")
			Map<Object,Object> map = (Map<Object,Object>)data;
			return map.entrySet();
		}

		// TODO: convert arbitrary object to list of Map.Entry
		throw new IllegalArgumentException("TODO: convert object to map");
	}

	/**
	 * Writes the value to the output
	 * @param output
	 * @param value
	 * @throws IOException
	 */
	protected void write(Appendable output, Object value) throws IOException {
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
	protected void htmlEncode(Appendable output, Object value) throws IOException {
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
