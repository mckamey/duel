package org.duelengine.duel.runtime;

import java.util.*;

public class UniqueClientIDStrategy implements ClientIDStrategy {

	private final String prefix;

	public UniqueClientIDStrategy() {
		this("_");
	}

	public UniqueClientIDStrategy(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void resetID() {
	}

	@Override
	public String nextID() {
		return this.prefix + UUID.randomUUID().toString();
	}
}
