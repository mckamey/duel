package org.duelengine.duel.codedom;

import java.util.ArrayList;
import java.util.List;

public class CodeConstructor extends CodeMethod {

	private final List<CodeExpression> baseCtorArgs = new ArrayList<CodeExpression>();
	private final List<CodeExpression> chainedCtorArgs = new ArrayList<CodeExpression>();

	public CodeConstructor() {
		setName(".ctor");
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
		return baseCtorArgs;
	}

	public List<CodeExpression> getChainedCtorArgs() {
		return chainedCtorArgs;
	}

	@Override
	public void visit(CodeVisitor visitor) {
		if (visitor.visit(this)) {
			for (CodeParameterDeclarationExpression parameter : getParameters()) {
				if (parameter != null) {
					parameter.visit(visitor);
				}
			}
			for (CodeExpression arg : baseCtorArgs) {
				if (arg != null) {
					arg.visit(visitor);
				}
			}
			for (CodeExpression arg : chainedCtorArgs) {
				if (arg != null) {
					arg.visit(visitor);
				}
			}
			for (CodeStatement statement : getStatements()) {
				if (statement != null) {
					statement.visit(visitor);
				}
			}
		}
	}

	@Override
	public boolean equals(Object arg) {
		if (!(arg instanceof CodeConstructor)) {
			// includes null
			return false;
		}

		CodeConstructor that = (CodeConstructor)arg;

		int length = this.baseCtorArgs.size();
		if (length != that.baseCtorArgs.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeExpression thisArg = this.baseCtorArgs.get(i);
			CodeExpression thatArg = that.baseCtorArgs.get(i);
			if (thisArg == null ? thatArg != null : !thisArg.equals(thatArg)) {
				return false;
			}
		}

		length = this.chainedCtorArgs.size();
		if (length != that.chainedCtorArgs.size()) {
			return false;
		}

		for (int i=0; i<length; i++) {
			CodeExpression thisArg = this.chainedCtorArgs.get(i);
			CodeExpression thatArg = that.chainedCtorArgs.get(i);
			if (thisArg == null ? thatArg != null : !thisArg.equals(thatArg)) {
				return false;
			}
		}

		return super.equals(that);
	}

	@Override
	public int hashCode() {
		final int HASH_PRIME = 1000003;

		return ((super.hashCode() * HASH_PRIME) + baseCtorArgs.hashCode()) * HASH_PRIME + chainedCtorArgs.hashCode();
	}
}
