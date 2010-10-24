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
	private int mark_index = -1;
	private int mark_column = -1;
	private int mark_line = -1;
	private int mark_ch;
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
	 * Altering the input is not supported
	 */
	public void remove()
		throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Not supported");
	}

	/**
	 * Finds the next token in the input
	 */
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
							if (this.tryScanUnparsedBlock() || this.tryScanTag()) {
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

	/**
	 * Scans the next token as a literal sequence
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * Decodes HTML/XML/SGML character references
	 * @throws IOException
	 */
	private void decodeEntity()
		throws IOException {

		final int CAPACITY = 32;
		this.setMark(CAPACITY+2);

		// should be short enough that string concat is pretty fast
		StringBuilder entity = new StringBuilder(CAPACITY);
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
				while (CharUtility.isHexDigit(this.nextChar()) && entity.length() < 8) {
					entity.append((char)this.ch);
				}
			} else {
				// consume digits
				while (CharUtility.isDigit(this.ch) && entity.length() < 10) {
					entity.append((char)this.ch);
					this.nextChar();
				}
			}

			int codePoint = Integer.parseInt(entity.toString(), isHex ? 0x10 : 10);
			if (codePoint > 0) {
				this.buffer.append(Character.toChars(codePoint));
				isValid = true;
			}

		} else {

			// consume letters
			while (CharUtility.isLetter(this.ch) && entity.length() < CAPACITY) {
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
			this.resetMark();
			this.buffer.append((char)this.ch);
			this.nextChar();

		} else if (this.ch == DuelGrammar.OP_ENTITY_END) {
			this.nextChar();
		}
	}

	/**
	 * Decodes HTML5 character reference names
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

	/**
	 * Tries to scan the next token as an unparsed block
	 * @return
	 * @throws IOException
	 */
	private boolean tryScanUnparsedBlock()
		throws IOException {

		this.setMark(3);
		this.buffer.setLength(0);

		// TODO
		return false;
	}

	/**
	 * Tries to scan the next token as a tag
	 * @return
	 * @throws IOException
	 */
	private boolean tryScanTag()
		throws IOException {

		final int CAPACITY = 64;
		this.setMark(CAPACITY+1);
		this.buffer.setLength(0);

		// TODO
		return false;
	}

	/**
	 * Gets the next character in the input and updates statistics
	 * @return
	 * @throws IOException
	 */
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

	private void setMark(int bufferSize) throws IOException {
		this.reader.mark(bufferSize);

		// store current statistics
		this.mark_line = this.line;
		this.mark_column = this.column;
		this.mark_index = this.index;
		this.mark_ch = this.ch;
	}

	private void resetMark() throws IOException {
		this.reader.reset();

		// restore current statistics
		this.line = this.mark_line;
		this.column = this.mark_column;
		this.index = this.mark_index;
		this.ch = this.mark_ch;
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
