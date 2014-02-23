package org.duelengine.duel;

public class IncClientIDStrategy implements ClientIDStrategy {

	private final String prefix;
	private int counter;

	public IncClientIDStrategy() {
		this("_");
	}

	public IncClientIDStrategy(String value) {
		prefix = value;
	}

	public void resetID() {
		counter = 0;
	}

	@Override
	public String nextID() {
		return prefix + (counter++);
	}
}
