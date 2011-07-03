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
	private URLInterceptor interceptor;
	private DataEncoder encoder;
	private FormatPrefs format;

	private Object data;
	private ExtraState extraState = ExtraState.NONE;
	private SparseMap extras;
	private SparseMap dirty;

	public Appendable getOutput() {
		if (this.output == null) {
			this.output = new StringBuilder();
		}
		return this.output;
	}

	public DuelContext setOutput(Appendable output) {
		this.output = output;

		return this;
	}

	public DuelContext setURLInterceptor(URLInterceptor value) {
		this.interceptor = value;

		return this;
	}

	public DuelContext setClientID(ClientIDStrategy value) {
		this.clientID = value;

		return this;
	}

	public FormatPrefs getFormat() {
		if (this.format == null) {
			this.format = new FormatPrefs();
		}
		return this.format;
	}

	public DuelContext setFormat(FormatPrefs value) {
		this.format = value;
		this.encoder = null;

		return this;
	}

	public Object getData() {
		return this.data;
	}

	public DuelContext setData(Object data) {
		this.data = data;

		return this;
	}

	public DuelContext clearExtras() {
		this.extraState = ExtraState.NONE;
		this.extras = null;
		this.dirty = null;

		return this;
	}

	public DuelContext putExtras(Map<String, ?> values) {
		if (values == null) {
			throw new NullPointerException("values");
		}
		for (String ident : values.keySet()) {
			this.putExtra(ident, values.get(ident));
		}

		return this;
	}

	public DuelContext putExtra(String ident, Object value) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.extras == null) {
			this.extras = new SparseMap();
		}
		this.extras.putSparse(ident, value);

		switch (this.extraState) {
			case NONE:
			case PENDING:
				this.extraState = ExtraState.PENDING;
				break;
			case EMITTED:
			case DIRTY:
				this.extraState = ExtraState.DIRTY;
				if (this.dirty == null) {
					this.dirty = new SparseMap();
				}
				// also add to dirty set
				this.dirty.putSparse(ident, value);
				break;
		}

		return this;
	}

	boolean hasExtras(String... idents) {
		if (this.extras == null) {
			return false;
		}

		if (idents != null) {
			for (String ident : idents) {
				if (!this.extras.containsKey(ident)) {
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

		if (this.extras == null || !this.extras.containsKey(ident)) {
			return null;
		}

		return this.extras.get(ident);
	}

	SparseMap getPendingExtras() {

		SparseMap sparseMap = (this.extraState == ExtraState.DIRTY) ? this.dirty : this.extras;
		this.extraState = ExtraState.EMITTED;
		this.dirty = null;
		return sparseMap;
	}

	boolean hasExtrasPending() {
		switch (this.extraState) {
			case PENDING:
			case DIRTY:
				return true;
			default:
				return false;
		}
	}

	DataEncoder getEncoder() {
		if (this.encoder == null) {
			this.encoder = new DataEncoder(this.getFormat().getNewline(), this.getFormat().getIndent());
		}

		return this.encoder;
	}

	String nextID() {
		if (this.clientID == null) {
			this.clientID = new IncClientIDStrategy();
		}

		return this.clientID.nextID();
	}

	String transformURL(String url) {
		if (this.interceptor == null) {
			return url;
		}

		return this.interceptor.transformURL(url);
	}
}
