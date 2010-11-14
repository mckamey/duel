package org.duelengine.duel;

public class IncClientIDStrategy implements ClientIDStrategy {

	private final String prefix;
	private int counter;

	public IncClientIDStrategy() {
		this("_");
	}

	public IncClientIDStrategy(String prefix) {
		this.prefix = prefix;
	}

	public void resetID() {
		this.counter = 0;
	}

	@Override
	public String nextID() {
		return this.prefix + (this.counter++);
	}
}
