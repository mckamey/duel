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

	/**
	 * Generates server-side code for the given view
	 * @param writer
	 * @param view
	 * @throws Exception
	 */
	@Override
	public void write(Writer writer, ViewRootNode view) {
		if (view == null) {
			throw new NullPointerException("view");
		}

		List<ViewRootNode> views = new ArrayList<ViewRootNode>();
		views.add(view);

		this.write(writer, views);
	}

	/**
	 * Generates server-side code for the given views
	 * @param writer
	 * @param views
	 * @throws Exception
	 */
	@Override
	public void write(Writer writer, ViewRootNode[] views) {
		this.write(writer, views != null ? Arrays.asList(views) : null);
	}

	/**
	 * Generates server-side code for the given views
	 * @param writer
	 * @param views
	 * @throws Exception
	 */
	@Override
	public void write(Writer writer, Iterable<ViewRootNode> views) {
		if (writer == null) {
			throw new NullPointerException("writer");
		}
		if (views == null) {
			throw new NullPointerException("views");
		}

		PrintWriter pw = (writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer);
		for (ViewRootNode view : views) {
			if (view == null) {
				continue;
			}

			this.writeView(pw, view);
		}
	}

	private void writeView(PrintWriter writer, ViewRootNode view) {
		
	}
}
