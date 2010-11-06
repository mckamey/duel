package org.duelengine.duel.runtime;

import java.util.*;

public class ClockClientIDStrategy implements ClientIDStrategy {

	private final String prefix;

	public ClockClientIDStrategy() {
		this("_");
	}

	public ClockClientIDStrategy(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void resetID() {
	}

	@Override
	public String nextID() {
		return this.prefix + new Date().getTime();
	}
}
