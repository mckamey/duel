package org.duelengine.duel;

/**
 * Maintains context state for a single request/response cycle.
 * DuelContext is not thread-safe and not intended to be reusable.
 */
public class DuelContext {

	private final Appendable output;
	private ClientIDStrategy clientID;
	private DataEncoder encoder;
	private String newline;
	private String indent;
	private boolean encodeNonASCII = true;

	private boolean globalsPending;
	private SparseMap globals;

	public DuelContext(Appendable output) {
		if (output == null) {
			throw new NullPointerException("output");
		}

		this.output = output;
	}

	String getNewline() {
		return this.newline;
	}

	public void setNewline(String value) {
		this.newline = value;
	}

	String getIndent() {
		return this.indent;
	}

	public void setIndent(String value) {
		this.indent = value;
	}

	boolean getEncodeNonASCII() {
		return this.encodeNonASCII;
	}

	public void setEncodeNonASCII(boolean value) {
		this.encodeNonASCII = value;
	}

	public void putGlobal(String ident, Object value) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.globals == null) {
			this.globals = new SparseMap();
		}

		this.globalsPending = true;
		this.globals.putSparse(ident, value);
	}

	boolean hasGlobals(String... idents) {
		if (this.globals == null) {
			return false;
		}

		if (idents != null) {
			for (String ident : idents) {
				if (!this.globals.containsKey(ident)) {
					return false;
				}
			}
		}

		return true;
	}

	Object getGlobal(String ident) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.globals == null || !this.globals.containsKey(ident)) {
			return null;
		}

		return this.globals.get(ident);
	}

	SparseMap getGlobals() {
		return this.globals;
	}

	boolean isGlobalsPending() {
		return this.globalsPending;
	}

	void setGlobalsPending(boolean value) {
		this.globalsPending = value;
	}

	Appendable getOutput() {
		return this.output;
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
