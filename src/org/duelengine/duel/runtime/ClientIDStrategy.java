package org.duelengine.duel.runtime;

public interface ClientIDStrategy {

	public void resetID();

	public String nextID();
}
