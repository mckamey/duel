package org.duelengine.duel;

import java.io.IOException;

/**
 * Maintains context state for a single request/response cycle.
 * DuelContext is not thread-safe and not intended to be reusable.
 */
public class DuelContext implements Appendable, ClientIDStrategy {

	private final Appendable output;
	private final ClientIDStrategy clientID;
	private boolean encodeNonASCII = true;
	private SparseMap globalData;

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

	Object getGlobals() {
		return this.globalData;
	}

	public Object getGlobalData(String ident) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.globalData == null || !this.globalData.containsKey(ident)) {
			return null;
		}

		return this.globalData.get(ident);
	}

	public void putGlobalData(String ident, Object value) {
		if (ident == null) {
			throw new NullPointerException("ident");
		}

		if (this.globalData == null) {
			this.globalData = new SparseMap();
		}

		this.globalData.putSparse(ident, value);
	}

	public boolean hasGlobalData(String... idents) {
		if (this.globalData == null) {
			return false;
		}

		if (idents != null) {
			for (String ident : idents) {
				if (!this.globalData.containsKey(ident)) {
					return false;
				}
			}
		}

		return true;
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

	@Override
	public String nextID() {
		return this.clientID.nextID();
	}
}
