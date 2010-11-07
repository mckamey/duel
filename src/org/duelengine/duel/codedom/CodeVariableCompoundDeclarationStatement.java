package org.duelengine.duel.codedom;

import java.util.*;

/**
 * Represents a compound variable declaration statement
 * Note: the first var determines the type 
 */
public class CodeVariableCompoundDeclarationStatement extends CodeStatement {

	private final List<CodeVariableDeclarationStatement> vars = new ArrayList<CodeVariableDeclarationStatement>();

	public CodeVariableCompoundDeclarationStatement() {
	}

	public CodeVariableCompoundDeclarationStatement(CodeVariableDeclarationStatement[] vars) {
		if (vars != null) {
			for (CodeVariableDeclarationStatement var : vars) {
				this.addVar(var);
			}
		}
	}

	public void addVar(CodeVariableDeclarationStatement value) {
		if (value == null) {
			return;
		}

		this.vars.add(value);
	}

	public CodeVariableDeclarationStatement getVar(int index) {
		return this.vars.get(index);
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeVariableCompoundDeclarationStatement)) {
			// includes null
			return false;
		}

		CodeVariableCompoundDeclarationStatement that = (CodeVariableCompoundDeclarationStatement)arg;

		int length = this.vars.size();
		if (length != that.vars.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeVariableDeclarationStatement thisVar = this.vars.get(i);
			CodeVariableDeclarationStatement thatVar = this.vars.get(i);

			if (thisVar == null ? thatVar != null : !thisVar.equals(thatVar)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return this.vars.hashCode();
	}
}
