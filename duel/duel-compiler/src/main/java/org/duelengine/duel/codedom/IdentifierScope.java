package org.duelengine.duel.codedom;

public interface IdentifierScope {

	/**
	 * Determines if the client ident is declared in local scope
	 * @param ident
	 * @return
	 */
	public boolean isLocalIdent(String ident);

	/**
	 * Gets the corresponding unique server ident for the client ident
	 * @param ident
	 * @return
	 */
	public String uniqueIdent(String ident);

	/**
	 * Gets a new unique ident with the given prefix
	 * @param prefix
	 * @return
	 */
	public String nextIdent(String prefix);
}
