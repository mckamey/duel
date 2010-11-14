package org.duelengine.duel;

import java.io.*;

public class DuelContext implements Appendable, ClientIDStrategy {

	private final Appendable output;
	private final ClientIDStrategy clientID;

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
