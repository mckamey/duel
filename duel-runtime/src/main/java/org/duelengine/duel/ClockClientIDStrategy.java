package org.duelengine.duel;

public class ClockClientIDStrategy implements ClientIDStrategy {

	private final String prefix;

	public ClockClientIDStrategy() {
		this("_");
	}

	public ClockClientIDStrategy(String value) {
		prefix = value;
	}

	@Override
	public String nextID() {
		return prefix + System.currentTimeMillis();
	}
}
