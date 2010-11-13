package org.duelengine.duel.codegen;

import java.io.IOException;

import org.duelengine.duel.ast.*;

public interface CodeGenerator {

	public String getFileExtension();

	public void write(Appendable output, ViewRootNode... views) throws IOException;

	public void write(Appendable output, Iterable<ViewRootNode> views) throws IOException;
}
