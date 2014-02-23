package org.duelengine.duel;

import java.util.UUID;

public class UniqueClientIDStrategy implements ClientIDStrategy {

	private final String prefix;

	public UniqueClientIDStrategy() {
		this("_");
	}

	public UniqueClientIDStrategy(String value) {
		prefix = value;
	}

	@Override
	public String nextID() {
		return prefix + UUID.randomUUID().toString();
	}
}
