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
	private String lastTag;
	private boolean suspendMode;
	private boolean hasToken;
	private final StringBuilder buffer = new StringBuilder(1024);
	private DuelToken token = DuelToken.None;
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

	/**
	 * Gets the current line within the input
	 * @return
	 */
	public int getLine() {
		return this.line;
	}

	/**
	 * Gets the current column within the input
	 * @return
	 */
	public int getColumn() {
		return this.column;
	}

	/**
	 * Gets the current index within the input 
	 * @return
	 */
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

		if (this.token.getToken().equals(DuelTokenType.ERROR)) {
			this.token = DuelToken.None;
		}
	}

	/**
	 * Determines if any more tokens exist
	 */
	public boolean hasNext() {
		return !this.ensureToken().equals(DuelToken.End);
	}

	/**
	 * Returns the next token in the input
	 */
	public DuelToken next() {
		try {
			return this.ensureToken();
		} finally {
			this.hasToken = false;
		}
	}

	/**
	 * Altering the input is not supported
	 */
	public void remove()
		throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Not supported");
	}

	/**
	 * Processes the next token in the input
	 */
	private DuelToken ensureToken() {
		if (this.hasToken) {
			return this.token;
		}

		try {
			while (true) {
				switch (this.token.getToken()) {
					case NONE:
					case LITERAL:
					case UNPARSED:
						switch (this.ch) {
							case DuelGrammar.OP_ELEM_BEGIN:
								if (this.tryScanBlock(false) || this.tryScanTag()) {
									return this.token;
								}
								break;
							case DuelGrammar.EOF:
								return (this.token = DuelToken.End);
						}

						return this.scanLiteral();

					case ELEM_BEGIN:
					case ATTR_LITERAL:
					case ATTR_UNPARSED:
						// skip whitespace
						while (CharUtility.isWhiteSpace(this.ch)) {
							this.nextChar();
						}

						switch (this.ch) {
							case DuelGrammar.OP_ELEM_CLOSE:
								if (this.nextChar() == DuelGrammar.OP_ELEM_END) {
									// immediately close the last tag
									return (this.token = DuelToken.ElemEnd(this.lastTag));
								}
								break;

							case DuelGrammar.OP_ELEM_END:
								// reset to start state
								this.nextChar();
								this.token = DuelToken.None;
								continue;

							case DuelGrammar.EOF:
								return (this.token = DuelToken.End);
						}

						if (this.tryScanAttrName()) {
							return this.token;
						}

						boolean skip = true;
						while (skip) {
							switch (this.ch) {
								case DuelGrammar.OP_ELEM_CLOSE:
								case DuelGrammar.OP_ELEM_END:
								case DuelGrammar.EOF:
									skip = false;
									break;
								default:
									this.nextChar();
									break;
							}
						}
						continue;

					case ATTR_NAME:
						if (this.tryScanAttrValue()) {
							return this.token;
						}

						// no value, reset to elem state
						this.token = DuelToken.ElemBegin(this.lastTag);
						continue;

					case ELEM_END:
						// skip until end of close tag
						while (this.ch != DuelGrammar.OP_ELEM_END && this.ch != DuelGrammar.EOF) {
							this.nextChar();
						}

						if (this.ch != DuelGrammar.EOF) {
							this.nextChar();
						}

						// reset to start state
						this.token = DuelToken.None;
						continue;

					case END:
					case ERROR:
						// remain in these states
						return this.token;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();

			this.lastError = ex;
			return (this.token = DuelToken.Error(ex.getMessage()));

		} finally {
			this.hasToken = true;
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
					return (this.token = DuelToken.Literal(this.buffer.toString()));

				case DuelGrammar.OP_ENTITY_BEGIN:
					// attempt to decode
					this.decodeEntity();
					continue;

				case DuelGrammar.OP_ELEM_BEGIN:
					if (this.buffer.length() != 0) {
						// flush the buffer
						return (this.token = DuelToken.Literal(this.buffer.toString()));
					}

					this.buffer.append((char)this.ch);
					this.nextChar();
					continue;

				default:
					// consume until reach a special char
					this.buffer.append((char)this.ch);
					this.nextChar();
					continue;
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
			// not an entity, emit as simple ampersand
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
	 * Tries to scan the next token as a tag
	 * @return true if tag token was found
	 * @throws IOException
	 */
	private boolean tryScanTag()
		throws IOException {

		final int CAPACITY = 64;
		this.setMark(CAPACITY+1);
		this.buffer.setLength(0);

		boolean isEndTag = (this.nextChar() == DuelGrammar.OP_ELEM_CLOSE);
		if (isEndTag) {
			// consume end char
			this.nextChar();
		}

		if (CharUtility.isNameStartChar(this.ch)) {
			this.buffer.append((char)this.ch);

			// consume tag name
			while (CharUtility.isNameChar(this.nextChar()) && this.buffer.length() < CAPACITY) {
				this.buffer.append((char)this.ch);
			}
		}
		
		if (this.buffer.length() == 0) {
			// not a valid tag name
			this.resetMark();
			return false;
		}

		// always generates lowercase tags
		String tagName = this.buffer.toString().toLowerCase();

		// check if should unsuspend lexer
		if (this.suspendMode) {
			if (isEndTag && this.lastTag.equals(tagName)) {
				this.suspendMode = false;
			} else {
				// treat as literal text
				this.resetMark();
				return false;
			}
		}

		this.lastTag = tagName;
		this.token = isEndTag ? DuelToken.ElemEnd(this.lastTag) : DuelToken.ElemBegin(this.lastTag);

		// tags with unparsed content put lexer into suspended mode
		this.suspendMode = (lastTag.equals("script")) || (lastTag.equals("style"));
		return true;
	}

	/**
	 * Tries to scan the next token as an attribute name
	 * @return true if attribute name was found
	 * @throws IOException
	 */
	private boolean tryScanAttrName()
		throws IOException {

		final int CAPACITY = 64;
		this.setMark(CAPACITY+1);
		this.buffer.setLength(0);

		if (CharUtility.isNameStartChar(this.ch)) {
			this.buffer.append((char)this.ch);

			// consume tag name
			while (CharUtility.isNameChar(this.nextChar()) && this.buffer.length() < CAPACITY) {
				this.buffer.append((char)this.ch);
			}
		}

		if (this.buffer.length() == 0) {
			// not a valid tag name
			this.resetMark();
			return false;
		}

		this.token = DuelToken.AttrName(this.buffer.toString());
		return true;
	}

	/**
	 * Tries to scan the next token as an attribute value
	 * @return true if attribute value was found
	 * @throws IOException
	 */
	private boolean tryScanAttrValue()
		throws IOException {

		// skip whitespace
		while (CharUtility.isWhiteSpace(this.ch)) {
			this.nextChar();
		}

		if (this.ch != DuelGrammar.OP_PAIR_DELIM) {
			return false;
		}

		// skip whitespace
		while (CharUtility.isWhiteSpace(this.nextChar()));

		int delim;
		switch (this.ch) {
			case DuelGrammar.OP_STRING_DELIM:
			case DuelGrammar.OP_STRING_DELIM_ALT:
				delim = this.ch;
				this.nextChar();
				break;
			default:
				delim = DuelGrammar.OP_ATTR_DELIM;
		}

		if (this.ch != DuelGrammar.OP_ELEM_BEGIN || !this.tryScanBlock(true)) {
			this.scanAttrLiteral(delim);
		}

		if (this.ch == delim) {
			this.nextChar();
		}

		return true;
	}

	/**
	 * Scans the next token as a literal attribute value
	 * @return
	 * @throws IOException
	 */
	private DuelToken scanAttrLiteral(int delim)
		throws IOException {

		// reset the buffer
		this.buffer.setLength(0);

		while (true) {
			switch (this.ch) {
				case DuelGrammar.EOF:
				case DuelGrammar.OP_ELEM_END:
					// flush the buffer
					return (this.token = DuelToken.AttrValue(this.buffer.toString()));

				case DuelGrammar.OP_ENTITY_BEGIN:
					// attempt to decode entities
					this.decodeEntity();
					continue;

				default:
					if (this.ch == delim) {
						// flush the buffer
						return (this.token = DuelToken.AttrValue(this.buffer.toString()));
					}

					// consume until reach a special char
					this.buffer.append((char)this.ch);
					this.nextChar();
					continue;
			}
		}
	}

	/**
	 * Tries to scan the next token as an unparsed block
	 * @return true if block token was found
	 * @throws IOException
	 */
	private boolean tryScanBlock(boolean asAttr)
		throws IOException {
		
		// mark current position with capacity to check start delims
		final int CAPACITY = 16;
		this.setMark(CAPACITY);

		String value = null;
		String begin = null;
		String end = null;

		// '<'
		switch (this.nextChar()) {

			case '%':
				switch (this.nextChar()) {
					case '-':	// "<%--", "--%>"		ASP/PSP/JSP-style code comment
						begin = "<%--";
						end = "--%>";
						value = this.scanBlockValue("--", end);
						break;

					case '@':	// "<%@",  "%>"			ASP/PSP/JSP directive
					case '=':	// "<%=",  "%>"			ASP/PSP/JSP expression
					case '!':	// "<%!",  "%>"			JSP/JBST declaration
					case '#':	// "<%#",  "%>"			ASP.NET databind expression
					case '$':	// "<%$",  "%>"			ASP.NET extension
					case ':':	// "<%:",  "%>"			ASP.NET 4 HTML-encoded expression
						begin = "<%"+(char)this.ch;
						end = "%>";
						value = this.scanBlockValue(""+(char)this.ch, end);
						break;

					default:
						begin = "<%";
						end = "%>";
						value = this.scanBlockValue("", end);
						break;
				}
				break;

			case '!':
				switch (this.nextChar()) {
					case '-':	// "<!--", "-->"		XML/HTML/SGML comment
						begin = "<!--";
						end = "-->";
						value = this.scanBlockValue("--", end);
						break;

					case '[':	// "<![CDATA[", "]]>"	CDATA section
						value = this.scanBlockValue("[CDATA[", "]]>");
						if (value != null) {
							if (this.ch == DuelGrammar.OP_ELEM_END) {
								this.nextChar();
							}

							// always unwrap CDATA as plain literal text
							this.token = asAttr ? DuelToken.AttrValue(value) : DuelToken.Literal(value);
							return true;
						}
						break;

					default:	// "<!", ">"			SGML declaration (e.g. DOCTYPE or server-side include)
						begin = "<!";
						end = ">";
						value = this.scanBlockValue("", ">");
						break;
				}
				break;

			case '?':
				switch (this.nextChar()) {
					case '=':	// "<?=", "?>"			PHP-style expression
						begin = "<?=";
						end = "?>";
						value = this.scanBlockValue("--", end);
						break;

					default:	// "<?", "?>"			PHP code block / XML processing instruction (e.g. XML declaration)
						begin = "<?";
						end = "?>";
						value = this.scanBlockValue("", end);
						break;
				}
				break;

			case '#':
				switch (this.nextChar()) {
					case '-':	// "<#--", "--#>"		T4-style code comment
						begin = "<#--";
						end = "--#>";
						value = this.scanBlockValue("--", end);
						break;

					case '@':	// "<#@",  "#>"			T4 directive
					case '=':	// "<#=",  "#>"			T4 expression
					case '+':	// "<#+",  "#>"			T4 ClassFeature blocks

						begin = "<#"+(char)this.ch;
						end = "#>";
						value = this.scanBlockValue(""+(char)this.ch, end);
						break;

					default:
						begin = "<#";
						end = "#>";
						value = this.scanBlockValue("", end);
						break;
				}
				break;
		}

		if (value == null) {
			// NOTE: this may throw an exception if block was unterminated
			this.resetMark();
			return false;
		}

		if (this.ch == DuelGrammar.OP_ELEM_END) {
			this.nextChar();
		}

		if (this.suspendMode && !asAttr && (begin.equals(DuelGrammar.OP_COMMENT))) {

			// always unwrap commented content of suspend-mode elements
			this.token = DuelToken.Literal(value);
			return true;
		}

		UnparsedBlock block = new UnparsedBlock(begin, end, value);
		this.token = asAttr ? DuelToken.AttrValue(block) : DuelToken.Unparsed(block);
		return true;
	}

	private String scanBlockValue(String begin, String end)
		throws IOException {

		for (int i=0, length=begin.length(); i<length; i++) {
			if (this.ch != begin.charAt(i)) {
				// didn't match begin delim
				return null;
			}

			this.nextChar();
		}

		// reset the buffer, mark start
		this.buffer.setLength(0);

		for (int i=0, length=end.length(); this.ch != DuelGrammar.EOF; ) {
			// check each char
			if (this.ch == end.charAt(i)) {
				// move to next char
				i++;
				if (i >= length) {
					length--;

					// trim ending delim from buffer
					this.buffer.setLength(this.buffer.length() - length);
					return this.buffer.toString();
				}
			} else {
				// reset to start of delim
				i = 0;
			}

			this.buffer.append((char)this.ch);
			this.nextChar();
		}

		// TODO: determine better exception type
		throw new IOException("Unterminated block");
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

	/**
	 * Marks the input location to enable resetting
	 * @param bufferSize
	 * @throws IOException
	 */
	private void setMark(int bufferSize) throws IOException {
		this.reader.mark(bufferSize);

		// store current statistics
		this.mark_line = this.line;
		this.mark_column = this.column;
		this.mark_index = this.index;
		this.mark_ch = this.ch;
	}

	/**
	 * Resets the input location to the marked location
	 * @throws IOException
	 */
	private void resetMark() throws IOException {
		this.reader.reset();

		// restore current statistics
		this.line = this.mark_line;
		this.column = this.mark_column;
		this.index = this.mark_index;
		this.ch = this.mark_ch;
	}

	/**
	 * Produces a list of the remaining tokens
	 * @return
	 */
	public ArrayList<DuelToken> toList() {

		ArrayList<DuelToken> list = new ArrayList<DuelToken>();
		while (this.hasNext()) {
			DuelToken token = this.next();
			list.add(token);

			if (token.getToken().equals(DuelTokenType.ERROR)) {
				break;
			}
		}
		return list;
	}
}
