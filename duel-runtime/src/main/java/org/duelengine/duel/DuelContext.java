package org.duelengine.duel;

import java.util.Map;

/**
 * Maintains context state for a single binding/render cycle (usually a request).
 * DuelContext is NOT thread-safe and not intended to be reusable.
 */
public class DuelContext {

	private enum ExtraState {

		/**
		 * No external data
		 */
		NONE,

		/**
		 * Never emitted
		 */
		PENDING,

		/**
		 * Emitted
		 */
		EMITTED,

		/**
		 * Changed after emitted 
		 */
		DIRTY
	}

	private Appendable output;
	private ClientIDStrategy clientID;
	private LinkInterceptor interceptor;
	private DataEncoder encoder;
	private FormatPrefs format;

	private Object data;
	private ExtraState extraState = ExtraState.NONE;
	private SparseMap extras;
	private SparseMap dirty;

	public Appendable getOutput() {
		if (output == null) {
			output = new StringBuilder();
		}
		return output;
	}

	public DuelContext setOutput(Appendable value) {
		output = value;

		return this;
	}

	public DuelContext setLinkInterceptor(LinkInterceptor value) {
		interceptor = value;

		return this;
	}

	public DuelContext setClientID(ClientIDStrategy value) {
		clientID = value;

		return this;
	}

	public FormatPrefs getFormat() {
		if (format == null) {
			format = new FormatPrefs();
		}
		return format;
	}

	public DuelContext setFormat(FormatPrefs value) {
		format = value;
		encoder = null;

		return this;
	}

	public Object getData() {
		return data;
	}

	public DuelContext setData(Object value) {
		data = value;

		return this;
	}

	public DuelContext clearExtras() {
		extraState = ExtraState.NONE;
		extras = null;
		dirty = null;

		return this;
	}

	public DuelContext putExtras(Map<String, ?> values) {
		if (values == null) {
			throw new NullPointerException("values");
		}
		for (String ident : values.keySet()) {
			putExtra(ident, values.get(ident));
		}

		return this;
	}

	public DuelContext putExtra(String ident, Object value) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (extras == null) {
			extras = new SparseMap();
		}
		extras.putSparse(ident, value);

		switch (extraState) {
			case NONE:
			case PENDING:
				extraState = ExtraState.PENDING;
				break;
			case EMITTED:
			case DIRTY:
				extraState = ExtraState.DIRTY;
				if (dirty == null) {
					dirty = new SparseMap();
				}
				// also add to dirty set
				dirty.putSparse(ident, value);
				break;
		}

		return this;
	}

	boolean hasExtras(String... idents) {
		if (extras == null) {
			return false;
		}

		if (idents != null) {
			for (String ident : idents) {
				if (!extras.containsKey(ident)) {
					return false;
				}
			}
		}

		return true;
	}

	Object getExtra(String ident) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (extras == null || !extras.containsKey(ident)) {
			return null;
		}

		return extras.get(ident);
	}

	SparseMap getPendingExtras() {

		SparseMap sparseMap = (extraState == ExtraState.DIRTY) ? dirty : extras;
		extraState = ExtraState.EMITTED;
		dirty = null;
		return sparseMap;
	}

	boolean hasExtrasPending() {
		switch (extraState) {
			case PENDING:
			case DIRTY:
				return true;
			default:
				return false;
		}
	}

	DataEncoder getEncoder() {
		if (encoder == null) {
			encoder = new DataEncoder(getFormat().getNewline(), getFormat().getIndent());
		}

		return encoder;
	}

	String nextID() {
		if (clientID == null) {
			clientID = new IncClientIDStrategy();
		}

		return clientID.nextID();
	}

	String transformURL(String url) {
		if (interceptor == null) {
			return url;
		}

		return interceptor.transformURL(url);
	}
}
