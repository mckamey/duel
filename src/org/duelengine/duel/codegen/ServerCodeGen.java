package org.duelengine.duel.codegen;

import java.io.*;
import java.util.*;
import org.duelengine.duel.ast.*;

public class ServerCodeGen implements CodeGenerator {

	private final CodeGenSettings settings;

	public ServerCodeGen() {
		this(null);
	}

	public ServerCodeGen(CodeGenSettings settings) {
		this.settings = (settings != null) ? settings : new CodeGenSettings();
	}

	@Override
	public CodeGenSettings getSettings() {
		return this.settings;
	}

	@Override
	public String getFileExtension() {
		return ".java";
	}

	@Override
	public void write(Writer writer, ViewRootNode view) {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(Writer writer, ViewRootNode[] views) {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(Writer writer, Iterable<ViewRootNode> views) {
		// TODO Auto-generated method stub
	}
}
