package org.duelengine.duel.codedom;

public interface IdentifierScope {

	public String uniqueIdent(String ident);
	
	public String nextIdent(String prefix);
}
