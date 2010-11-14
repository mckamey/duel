package org.duelengine.duel;

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
	public String nextID() {
		return this.prefix + new Date().getTime();
	}
}
