package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a compound variable declaration statement
 * Note: the first var determines the type 
 */
public class CodeVariableCompoundDeclarationStatement extends CodeStatement {

	private final List<CodeVariableDeclarationStatement> vars = new ArrayList<CodeVariableDeclarationStatement>();

	public CodeVariableCompoundDeclarationStatement() {
	}

	public CodeVariableCompoundDeclarationStatement(CodeVariableDeclarationStatement... vars) {
		if (vars != null) {
			for (CodeVariableDeclarationStatement var : vars) {
				addVar(var);
			}
		}
	}

	public void addVar(CodeVariableDeclarationStatement value) {
		if (value == null) {
			return;
		}

		vars.add(value);
	}

	public List<CodeVariableDeclarationStatement> getVars() {
		return vars;
	}

	public CodeVariableDeclarationStatement getVar(int index) {
		return vars.get(index);
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeVariableDeclarationStatement statement : vars) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
		}
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
		return vars.hashCode();
	}
}
