package org.duelengine.duel;

import java.io.IOException;

/**
 * Maintains context state for a single request/response cycle.
 * DuelContext is not thread-safe and not intended to be reusable.
 */
public class DuelContext implements Appendable {

	private final Appendable output;
	private final ClientIDStrategy clientID;
	private boolean encodeNonASCII = true;
	private boolean globalsPending;
	private SparseMap globals;

	public DuelContext(Appendable output) {
		this(output, new IncClientIDStrategy());
	}

	public DuelContext(Appendable output, ClientIDStrategy clientID) {
		if (output == null) {
			throw new NullPointerException("output");
		}
		if (clientID == null) {
			throw new NullPointerException("clientID");
		}

		this.output = output;
		this.clientID = clientID;
	}

	public boolean getEncodeNonASCII() {
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
	
	String nextID() {
		return this.clientID.nextID();
	}
	
	@Override
	public Appendable append(CharSequence csq)
		throws IOException {

		return this.output.append(csq);
	}

	@Override
	public Appendable append(char c)
		throws IOException {

		return this.output.append(c);
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {

		return this.output.append(csq, start, end);
	}
}
