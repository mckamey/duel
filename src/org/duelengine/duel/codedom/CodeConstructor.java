package org.duelengine.duel.codedom;

import java.util.*;

public class CodeConstructor extends CodeMethod {

	private final List<CodeExpression> baseCtorArgs = new ArrayList<CodeExpression>();
	private final List<CodeExpression> chainedCtorArgs = new ArrayList<CodeExpression>();

	public CodeConstructor() {
		this.setName(".ctor");
	}

	public CodeConstructor(AccessModifierType access, CodeParameterDeclarationExpression[] parameters, CodeExpression[] baseCtorArgs, CodeExpression[] chainedCtorArgs, CodeStatement[] statements) {
		super(access, Void.class, ".ctor", parameters, statements);

		if (baseCtorArgs != null) {
			for (CodeExpression arg : baseCtorArgs) {
				this.baseCtorArgs.add(arg);
			}
		}

		if (chainedCtorArgs != null) {
			for (CodeExpression arg : chainedCtorArgs) {
				this.chainedCtorArgs.add(arg);
			}
		}
	}

	public List<CodeExpression> getBaseCtorArgs() {
		return this.baseCtorArgs;
	}

	public List<CodeExpression> getChainedCtorArgs() {
		return this.chainedCtorArgs;
	}
}
