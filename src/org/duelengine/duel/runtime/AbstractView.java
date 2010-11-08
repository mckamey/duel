package org.duelengine.duel.runtime;

import java.io.*;
import java.util.*;

import org.duelengine.duel.codegen.HTMLFormatter;

/**
 * The skeletal implementation of DUEL view runtime
 */
public abstract class AbstractView {
	
	private final ClientIDStrategy clientID;
	private HTMLFormatter formatter;

	protected AbstractView() {
		this(new IncClientIDStrategy());
	}

	protected AbstractView(ClientIDStrategy clientID) {
		if (clientID == null) {
			throw new NullPointerException("clientID");
		}

		this.clientID = clientID;

		this.init();
	}

	protected AbstractView(AbstractView view) {
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

	public void render(Appendable output, Object model) {
		try {
			// TODO.
		} finally {
			this.formatter = null;
			this.clientID.resetID();
		}
	}

	/**
	 * Adapts any Object to Iterable with size()
	 * @param model
	 * @return
	 */
	protected Collection<?> asItems(Object model) {

		if (model instanceof Collection<?>) {
			return (Collection<?>)model;
		}

		if (model instanceof Object[]) {
			return new ArrayIterable((Object[])model);
		}

		if (model instanceof Iterable<?>) {
			// unfortunate but we need the size
			List<Object> list = new LinkedList<Object>();
			for (Object item : (Iterable<?>)model) {
				list.add(item);
			}
			return list;
		}

		// null is allowed
		return new SingleIterable(model);
	}

	/**
	 * Adapts any Object to Iterable<Map.Entry> with size()
	 * @param model
	 * @return
	 */
	protected Collection<Map.Entry<Object,Object>> asEntries(Object model) {

		// TODO: determine if Map.Entry<?,?> is flexible enough or if should just use a thin adapter

		if (model instanceof Map<?,?>) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> map = (Map<Object,Object>)model;
			return map.entrySet();
		}

		// TODO: convert arbitrary object to list of properties
		throw new IllegalArgumentException("TODO: inject JSON convertor");
	}

	/**
	 * Ensures the literal text is properly encoded as HTML text
	 * @param output
	 * @param literal
	 * @throws IOException
	 */
	protected void htmlEncode(Appendable output, Object literal) throws IOException {
		if (literal == null) {
			return;
		}

		if (literal instanceof Boolean || literal instanceof Number) {
			// no need to encode non-text primitives
			output.append(String.valueOf(literal));

		} else {
			if (this.formatter == null) {
				this.formatter = new HTMLFormatter(output);
			}

			this.formatter.writeLiteral(String.valueOf(literal));
		}
	}
}
