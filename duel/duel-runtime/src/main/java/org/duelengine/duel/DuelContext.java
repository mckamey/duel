package org.duelengine.duel;

/**
 * Maintains context state for a single binding/render cycle (usually a request).
 * DuelContext is NOT thread-safe and not intended to be reusable.
 */
public class DuelContext {

	private enum ExternalsState {

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
	private DataEncoder encoder;
	private String newline;
	private String indent;
	private boolean encodeNonASCII;

	private ExternalsState externalsState = ExternalsState.NONE;
	private SparseMap externals;
	private SparseMap dirty;

	public DuelContext() {
	}

	public DuelContext(Appendable output) {
		if (output == null) {
			throw new NullPointerException("output");
		}

		this.output = output;
	}

	public Appendable getOutput() {
		if (this.output == null) {
			this.output = new StringBuilder();
		}

		return this.output;
	}

	public DuelContext setOutput(Appendable output) {
		if (output == null) {
			throw new NullPointerException("output");
		}

		this.output = output;
		return this;
	}

	ClientIDStrategy getClientID() {
		return this.clientID;
	}

	public DuelContext setClientID(ClientIDStrategy value) {
		this.clientID = value;
		return this;
	}

	String getNewline() {
		return this.newline;
	}

	public DuelContext setNewline(String value) {
		this.newline = value;
		return this;
	}

	String getIndent() {
		return this.indent;
	}

	public DuelContext setIndent(String value) {
		this.indent = value;
		return this;
	}

	boolean getEncodeNonASCII() {
		return this.encodeNonASCII;
	}

	public DuelContext setEncodeNonASCII(boolean value) {
		this.encodeNonASCII = value;
		return this;
	}

	public DuelContext putExternal(String ident, Object value) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.externals == null) {
			this.externals = new SparseMap();
		}
		this.externals.putSparse(ident, value);

		switch (this.externalsState) {
			case NONE:
			case PENDING:
				this.externalsState = ExternalsState.PENDING;
				break;
			case EMITTED:
			case DIRTY:
				this.externalsState = ExternalsState.DIRTY;
				if (this.dirty == null) {
					this.dirty = new SparseMap();
				}
				// also add to dirty set
				this.dirty.putSparse(ident, value);
				break;
		}
		return this;
	}

	boolean hasExternals(String... idents) {
		if (this.externals == null) {
			return false;
		}

		if (idents != null) {
			for (String ident : idents) {
				if (!this.externals.containsKey(ident)) {
					return false;
				}
			}
		}

		return true;
	}

	Object getExternal(String ident) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.externals == null || !this.externals.containsKey(ident)) {
			return null;
		}

		return this.externals.get(ident);
	}

	SparseMap getPendingExternals() {

		SparseMap sparseMap = (this.externalsState == ExternalsState.DIRTY) ? this.dirty : this.externals;
		this.externalsState = ExternalsState.EMITTED;
		this.dirty = null;
		return sparseMap;
	}

	boolean hasExternalsPending() {
		switch (this.externalsState) {
			case PENDING:
			case DIRTY:
				return true;
			default:
				return false;
		}
	}

	DataEncoder getEncoder() {
		if (this.encoder == null) {
			this.encoder = new DataEncoder(this.newline, this.indent);
		}

		return this.encoder;
	}

	String nextID() {
		if (this.clientID == null) {
			this.clientID = new IncClientIDStrategy();
		}

		return this.clientID.nextID();
	}
}
