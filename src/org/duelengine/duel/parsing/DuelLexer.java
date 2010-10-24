package org.duelengine.duel.parsing;

import java.io.*;
import java.util.*;

public class DuelLexer implements Iterator<DuelToken> {

	private static final String CONFIG_RESOURCE = "org.duelengine.duel.parsing.HTML5";
	private static ResourceBundle htmlConfig;

	private LineNumberReader reader;
	private int index = -1;
	private int column = -1;
	private int line = -1;
	private int ch;
	private final StringBuilder buffer = new StringBuilder();
	private DuelToken token = DuelToken.Start;
	private Exception lastError;

	public DuelLexer(String text) {
		this(new StringReader(text));
	}

	public DuelLexer(Reader reader) {
		this.reader = new LineNumberReader(reader);

		try {
			// prime the sequence
			this.nextChar();

		} catch (IOException ex) {
			ex.printStackTrace();
			this.lastError = ex;
		}
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}

	public int getIndex() {
		return this.index;
	}

	/**
	 * Returns the last exception encountered
	 */
	public Exception getLastError() {
		return this.lastError;
	}

	/**
	 * Clears the last error
	 */
	public void clearLastError() {
		this.lastError = null;

		if (this.token.equals(DuelToken.Error)) {
			this.token = DuelToken.Start;
		}
	}

	/**
	 * Determines if any more tokens exist
	 */
	public boolean hasNext() {
		return (this.ch != DuelGrammar.EOF) && !this.token.equals(DuelToken.End);
	}

	/**
	 * Not supported.
	 */
	public void remove()
		throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Not supported");
	}

	public DuelToken next()
		throws NoSuchElementException {

		if (this.token.equals(DuelToken.End)) {
			throw new NoSuchElementException("Passed end of file.");
		}

		try {
			switch (this.token.getToken()) {
				case START:
				case LITERAL:
				case UNPARSED:
					switch (this.ch) {
						case DuelGrammar.OP_ELEM_BEGIN:
							if (this.tryScanTag()) {
								return this.token;
							}
							break;
						case DuelGrammar.EOF:
							return (this.token = DuelToken.End);
					}

					return this.scanLiteral();
			}

			return this.token;

		} catch (IOException ex) {
			ex.printStackTrace();

			this.lastError = ex;
			return (this.token = DuelToken.Error);
		}
	}

	private DuelToken scanLiteral()
		throws IOException {

		// reset the buffer
		this.buffer.setLength(0);

		while (true) {
			switch (this.ch) {
				case DuelGrammar.EOF:
					// flush the buffer
					return DuelToken.Literal(this.buffer.toString());

				case DuelGrammar.OP_ENTITY_BEGIN:
					this.decodeEntity();
					break;

				case DuelGrammar.OP_ELEM_BEGIN:
					if (this.buffer.length() != 0) {
						// flush the buffer
						return DuelToken.Literal(this.buffer.toString());
					}

					this.buffer.append((char)this.ch);
					this.nextChar();
					break;

				default:
					// consume until reach a special char
					this.buffer.append((char)this.ch);
					this.nextChar();
					break;
			}

		}
	}

	private void decodeEntity()
		throws IOException {

		this.reader.mark(16);

		// should be short enough that string concat is pretty fast
		StringBuilder entity = new StringBuilder(16);
		boolean isValid = false;

		if (this.nextChar() == DuelGrammar.OP_ENTITY_NUM) {
			// entity is Unicode Code Point
			this.nextChar();

			boolean isHex =
				(this.ch == DuelGrammar.OP_ENTITY_HEX) ||
				(this.ch == DuelGrammar.OP_ENTITY_HEX_ALT);

			if (isHex) {
				// skip over the 'x'
				// consume hex digits
				while (CharUtility.isHexDigit(this.nextChar())) {
					entity.append((char)this.ch);
				}
			} else {
				// consume digits
				while (CharUtility.isDigit(this.ch)) {
					entity.append((char)this.ch);
					this.nextChar();
				}
			}

			int codePoint = Integer.parseInt(entity.toString(), isHex ? 16 : 10);
			if (codePoint > 0) {
				this.buffer.append(Character.toChars(codePoint));
				isValid = true;
			}

		} else {

			// consume letters
			while (CharUtility.isLetter(this.ch)) {
				entity.append((char)this.ch);
				this.nextChar();
			}

			String value = DuelLexer.decodeEntityName(entity.toString());
			if (value != null) {
				this.buffer.append(value);
				isValid = true;
			}
		}

		if (!isValid) {
			// not an entity, add as simple ampersand
			this.buffer.append(DuelGrammar.OP_ENTITY_BEGIN);
			this.reader.reset();
			this.nextChar();

		} else if (this.ch == DuelGrammar.OP_ENTITY_END) {
			this.nextChar();
		}
	}

	/**
	 * Decodes HTML5 character references
	 * @param name
	 * @return
	 */
	private static String decodeEntityName(String name) {

		if (name == null || name.length() == 0) {
			return null;
		}

		if (htmlConfig == null) {
			// definitions maintained in HTML5.properties
			htmlConfig = ResourceBundle.getBundle(CONFIG_RESOURCE);
		}

		if (htmlConfig.containsKey(name)) {
			return htmlConfig.getString(name);
		}

		return null;
	}

	private boolean tryScanTag()
		throws IOException {

		this.reader.mark(16);
		this.buffer.setLength(0);

		return false;
	}

	private int nextChar() throws IOException {
		int prevLine = this.line;

		this.ch = this.reader.read();
		this.line = this.reader.getLineNumber();

		// update statistics
		if (prevLine != this.line) {
			this.column = 0;
		} else {
			this.column++;
		}
		this.index++;

		return this.ch;
	}

	public ArrayList<DuelToken> toList() {

		ArrayList<DuelToken> list = new ArrayList<DuelToken>();
		while (this.hasNext()) {
			DuelToken token = this.next();
			list.add(token);

			if (token.equals(DuelToken.Error)) {
				break;
			}
		}
		return list;
	}
}
