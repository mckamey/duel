package org.duelengine.duel;

import java.util.*;

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
	private boolean encodeNonASCII;

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

	private ExternalsState externalsState = ExternalsState.NONE;
	private SparseMap externals;
	private SparseMap dirty;

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

	public void putExternal(String ident, Object value) {
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
