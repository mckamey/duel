package org.duelengine.duel;

public interface ClientIDStrategy {

	public void resetID();

	public String nextID();
}
