package org.duelengine.duel.codedom;

import org.duelengine.duel.codegen.ServerCodeGen;

public abstract class CodeObject {

	@Override
	public String toString() {
		try {
			StringBuilder buffer = new StringBuilder();
			new ServerCodeGen().writeCode(buffer, this);
			return buffer.toString();
		} catch (Exception ex) {
			return super.toString();
		}
	}
}
