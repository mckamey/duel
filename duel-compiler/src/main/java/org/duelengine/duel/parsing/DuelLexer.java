package org.duelengine.duel.parsing;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Processes source text into a token sequence
 */
public class DuelLexer implements Iterator<DuelToken> {

	private static final int EOF = -1;
	private static final String CONFIG_RESOURCE = "org.duelengine.duel.parsing.HTMLCharRefs";
	private static ResourceBundle htmlConfig;

	private final LineNumberReader reader;
	private final StringBuilder buffer = new StringBuilder(512);
	private DuelToken token = DuelToken.start;
	private boolean hasToken;
	private String lastTag;
	private boolean suspendMode;
	private int ch;
	private int index = -1;
	private int column = -1;
	private int line = -1;
	private int token_index = -1;
	private int token_column = -1;
	private int token_line = -1;
	private int mark_ch;
	private int mark_index = -1;
	private int mark_column = -1;
	private int mark_line = -1;
	private Throwable lastError;

	/**
	 * Ctor
	 * @param text
	 */
	public DuelLexer(String text) {
		this(new StringReader(text));
	}

	/**
	 * Ctor
	 * @param input
	 */
	public DuelLexer(Reader input) {
		reader = new LineNumberReader(input);

		try {
			// prime the sequence
			nextChar();

		} catch (IOException ex) {
			lastError = ex;
			token = DuelToken.error(ex.getMessage(), token_index, token_line, token_column);
		}
	}

	/**
	 * Gets the current line within the input
	 * @return
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Gets the current column within the input
	 * @return
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Gets the current index within the input 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the last exception encountered
	 */
	public Throwable getLastError() {
		return lastError;
	}

	/**
	 * Clears the last error
	 */
	public void clearLastError() {
		lastError = null;

		if (ensureToken().getToken().equals(DuelTokenType.ERROR)) {
			token = DuelToken.start;
		}
	}

	/**
	 * Determines if any more tokens are available
	 */
	public boolean hasNext() {

		switch (ensureToken().getToken()) {
			case END:
			case ERROR:
				// NOTE: cannot pass error until cleared
				return false;
			default:
				return true;
		}
	}

	/**
	 * Returns the next token in the input
	 */
	public DuelToken next() {
		try {
			return ensureToken();
		} finally {
			hasToken = false;
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
		if (hasToken) {
			return token;
		}

		token_index = index;
		token_line = line;
		token_column = column;
		
		try {
			while (true) {
				switch (token.getToken()) {
					case LITERAL:
					case BLOCK:
						switch (ch) {
							case DuelGrammar.OP_ELEM_BEGIN:
								if (tryScanBlock(false) || tryScanTag()) {
									return token;
								}
								break;
							case EOF:
								return (token = DuelToken.end);
						}

						return scanLiteral();

					case ELEM_BEGIN:
					case ATTR_VALUE:
						// skip whitespace
						while (CharUtility.isWhiteSpace(ch)) {
							nextChar();
						}

						switch (ch) {
							case DuelGrammar.OP_ELEM_CLOSE:
								if (nextChar() == DuelGrammar.OP_ELEM_END) {
									// need to end suspendMode for void tags
									suspendMode = false;
									// immediately close the last tag
									return (token = DuelToken.elemEnd(lastTag, token_index, token_line, token_column));
								}
								break;

							case DuelGrammar.OP_ELEM_END:
								// reset to start state
								nextChar();
								token = DuelToken.start;
								continue;

							case EOF:
								return (token = DuelToken.end);
						}

						if (tryScanAttrName()) {
							return token;
						}

						boolean skip = true;
						while (skip) {
							switch (ch) {
								case DuelGrammar.OP_ELEM_CLOSE:
								case DuelGrammar.OP_ELEM_END:
								case EOF:
									skip = false;
									break;
								default:
									nextChar();
									break;
							}
						}
						continue;

					case ATTR_NAME:
						if (tryScanAttrValue()) {
							return token;
						}

						// no value, reset to elem state
						token = DuelToken.elemBegin(lastTag, token_index, token_line, token_column);
						continue;

					case ELEM_END:
						// ignore all content until end of close tag
						while (ch != DuelGrammar.OP_ELEM_END && ch != EOF) {
							nextChar();
						}

						if (ch != EOF) {
							// consume end of tag
							nextChar();
						}

						// reset to start state
						token = DuelToken.start;
						continue;

					case END:
					case ERROR:
						// remain in these states
						return token;
				}
			}

		} catch (IOException ex) {
			lastError = ex;
			return (token = DuelToken.error(ex.getMessage(), token_index, token_line, token_column));

		} finally {
			hasToken = true;
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
		buffer.setLength(0);

		while (true) {
			switch (ch) {
				case DuelGrammar.OP_ELEM_BEGIN:
					if (buffer.length() != 0) {
						// flush the buffer
						return (token = DuelToken.literal(buffer.toString(), token_index, token_line, token_column));
					}

					buffer.append((char)ch);
					nextChar();
					continue;

				case DuelGrammar.OP_ENTITY_BEGIN:
					// attempt to decode
					decodeEntity();
					continue;

				case EOF:
					// flush the buffer
					return (token = DuelToken.literal(buffer.toString(), token_index, token_line, token_column));

				default:
					// consume until reach a special char
					buffer.append((char)ch);
					nextChar();
					continue;
			}
		}
	}

	/**
	 * Decodes HTML/XML/SGML character references (entities)
	 * @throws IOException
	 */
	private void decodeEntity()
		throws IOException {

		final int CAPACITY = 32;
		setMark(CAPACITY+2);

		// main buffer if already in use
		StringBuilder entity = new StringBuilder(CAPACITY);
		boolean isValid = false;

		if (nextChar() == DuelGrammar.OP_ENTITY_NUM) {
			// entity is Unicode Code Point
			nextChar();

			boolean isHex =
				(ch == DuelGrammar.OP_ENTITY_HEX) ||
				(ch == DuelGrammar.OP_ENTITY_HEX_ALT);

			if (isHex) {
				// skip over the 'x'
				// consume hex digits
				while (CharUtility.isHexDigit(nextChar()) && entity.length() < 8) {
					entity.append((char)ch);
				}
			} else {
				// consume digits
				while (CharUtility.isDigit(ch) && entity.length() < 10) {
					entity.append((char)ch);
					nextChar();
				}
			}

			int codePoint;
			try {
				codePoint = Integer.parseInt(entity.toString(), isHex ? 0x10 : 10);
			} catch (NumberFormatException ex) {
				codePoint = -1;
			}

			if (codePoint > 0) {
				buffer.append(Character.toChars(codePoint));
				isValid = true;
			}

		} else {

			// consume letters
			while (CharUtility.isLetter(ch) && entity.length() < CAPACITY) {
				entity.append((char)ch);
				nextChar();
			}

			String value = DuelLexer.decodeEntityName(entity.toString());
			if (value != null) {
				buffer.append(value);
				isValid = true;
			}
		}

		if (!isValid) {
			// not an entity, emit as simple ampersand
			resetMark();
			buffer.append((char)ch);
			nextChar();

		} else if (ch == DuelGrammar.OP_ENTITY_END) {
			nextChar();
		}
	}

	/**
	 * Decodes HTML5 character reference names
	 * @param name
	 * @return
	 */
	private static String decodeEntityName(String name) {

		if (name == null || name.isEmpty()) {
			return null;
		}

		if (htmlConfig == null) {
			// definitions maintained in HTMLCharRefs.properties
			htmlConfig = ResourceBundle.getBundle(CONFIG_RESOURCE, Locale.ROOT);
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
		setMark(CAPACITY+1);
		buffer.setLength(0);

		boolean isEndTag = (nextChar() == DuelGrammar.OP_ELEM_CLOSE);
		if (isEndTag) {
			// consume end char
			nextChar();
		}

		if (CharUtility.isNameStartChar(ch)) {
			buffer.append((char)ch);

			// consume tag name
			while (CharUtility.isNameChar(nextChar()) && buffer.length() < CAPACITY) {
				buffer.append((char)ch);
			}
		}
		
		if (buffer.length() == 0) {
			// not a valid tag name
			resetMark();
			return false;
		}

		String tagName = buffer.toString();

		// check if should exit suspendMode
		if (suspendMode) {
			if (isEndTag && lastTag.equals(tagName)) {
				suspendMode = false;
			} else {
				// treat as literal text
				resetMark();
				return false;
			}
		}

		lastTag = tagName;
		token = isEndTag ?
			DuelToken.elemEnd(lastTag, token_index, token_line, token_column) :
			DuelToken.elemBegin(lastTag, token_index, token_line, token_column);

		// tags with unparsed content enter suspendMode
		suspendMode = !isEndTag && (("script".equals(lastTag)) || ("style".equals(lastTag)));
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
		setMark(CAPACITY+1);
		buffer.setLength(0);

		// consume attribute name
		if (CharUtility.isAttrNameChar(ch)) {
			buffer.append((char)ch);

			while (CharUtility.isAttrNameChar(nextChar()) && buffer.length() < CAPACITY) {
				buffer.append((char)ch);
			}
		}

		if (buffer.length() == 0) {
			// not a valid attribute name
			resetMark();
			return false;
		}

		token = DuelToken.attrName(buffer.toString(), token_index, token_line, token_column);
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
		while (CharUtility.isWhiteSpace(ch)) {
			nextChar();
		}

		if (ch != DuelGrammar.OP_PAIR_DELIM) {
			return false;
		}

		// skip whitespace
		while (CharUtility.isWhiteSpace(nextChar()));

		int delim, altDelim;
		switch (ch) {
			case DuelGrammar.OP_STRING_DELIM:
			case DuelGrammar.OP_STRING_DELIM_ALT:
				delim = ch;
				altDelim = EOF;
				nextChar();
				break;
			default:
				delim = DuelGrammar.OP_ATTR_DELIM;
				altDelim = DuelGrammar.OP_ELEM_END;
				break;
		}

		if (ch != DuelGrammar.OP_ELEM_BEGIN || !tryScanBlock(true)) {
			scanAttrLiteral(delim, altDelim);
		}

		if (ch == delim) {
			nextChar();
		}

		return true;
	}

	/**
	 * Scans the next token as a literal attribute value
	 * @return
	 * @throws IOException
	 */
	private DuelToken scanAttrLiteral(int delim, int altDelim)
		throws IOException {

		// reset the buffer
		buffer.setLength(0);

		while (true) {
			switch (ch) {
				case EOF:
					// flush the buffer
					return (token = DuelToken.attrValue(buffer.toString(), token_index, token_line, token_column));

				case DuelGrammar.OP_ENTITY_BEGIN:
					// attempt to decode entities
					decodeEntity();
					continue;

				default:
					if (ch == delim || ch == altDelim) {
						// flush the buffer
						return (token = DuelToken.attrValue(buffer.toString(), token_index, token_line, token_column));
					}

					// consume until reach a special char
					buffer.append((char)ch);
					nextChar();
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
		setMark(CAPACITY);

		String value = null;
		String begin = null;
		String end = null;

		// '<'
		switch (nextChar()) {

			case '%':
				switch (nextChar()) {
					case '-':	// "<%--", "--%>"		ASP/PSP/JSP code comment
						begin = "<%--";
						end = "--%>";
						value = tryScanBlockValue("--", end);
						break;

					case '@':	// "<%@",  "%>"			ASP/JSP/PSP directive
					case '=':	// "<%=",  "%>"			ASP/JSP/ERB/PSP expression
					case '!':	// "<%!",  "%>"			JSP declaration
					case '#':	// "<%#",  "%>"			ASP.NET data-bind expression/ERB comment
					case '$':	// "<%$",  "%>"			ASP.NET extension
					case ':':	// "<%:",  "%>"			ASP.NET HTML-encoded expression
						begin = "<%"+(char)ch;
						end = "%>";
						value = tryScanBlockValue(""+(char)ch, end);
						break;

					default:	// "<%",  "%>"			ASP/JSP/ERB/PSP code block
						begin = "<%";
						end = "%>";
						value = tryScanBlockValue("", end);
						break;
				}
				break;

			case '!':
				// TODO: ideally scan these as single tokens <!--[if EXPR]>, <![endif]-->, <![if EXPR]>, <![endif]>
				switch (nextChar()) {
					case '-':	// "<!--", "-->"		XML/HTML/SGML comment, IE conditional comment, or server-side include

						// IE conditional comment (downlevel hidden)
						// http://msdn.microsoft.com/en-us/library/ms537512.aspx#syntax
						begin = "<!--[";
						end = "]>";
						value = tryScanBlockValue("--[", end);
						if (value == null) {
							resetMark();

							// standard XML/HTML/SGML comment
							begin = "<!--";
							end = "-->";
							value = tryScanBlockValue(begin, end);
						}
						break;

					case '[':	// "<![CDATA[", "]]>"	CDATA section, or IE conditional comment
						value = tryScanBlockValue("[CDATA[", "]]>");
						if (value != null) {
							if (ch == DuelGrammar.OP_ELEM_END) {
								nextChar();
							}

							// always unwrap CDATA as plain literal text
							token = asAttr ?
								DuelToken.attrValue(value, token_index, token_line, token_column) :
								DuelToken.literal(value, token_index, token_line, token_column);
							return true;
						}
						resetMark();

						// IE conditional comment (downlevel revealed)
						// http://msdn.microsoft.com/en-us/library/ms537512.aspx#syntax
						begin = "<![";
						end = ">";
						value = tryScanBlockValue(begin, end);
						break;

					default:	// "<!", ">"			SGML declaration (e.g. DocType)
						begin = "<!";
						end = ">";
						value = tryScanBlockValue("", end);
						if ((value != null) && (value.length() >= 7) &&
							value.substring(0, 7).equalsIgnoreCase("doctype")) {
							value = value.substring(7).trim();
							begin = "<!DOCTYPE";
						}
						break;
				}
				break;

			case '?':
				switch (nextChar()) {
					case '=':	// "<?=", "?>"			PHP-style expression
						begin = "<?=";
						end = "?>";
						value = tryScanBlockValue("--", end);
						break;

					default:	// "<?", "?>"			PHP code block / XML processing instruction (e.g. XML declaration)
						begin = "<?";
						end = "?>";
						value = tryScanBlockValue("", end);
						break;
				}
				break;

			case '#':
				switch (nextChar()) {
					case '-':	// "<#--", "--#>"		T4-style code comment
						begin = "<#--";
						end = "--#>";
						value = tryScanBlockValue("--", end);
						break;

					case '@':	// "<#@",  "#>"			T4 directive
					case '=':	// "<#=",  "#>"			T4 expression
					case '+':	// "<#+",  "#>"			T4 ClassFeature blocks

						begin = "<#"+(char)ch;
						end = "#>";
						value = tryScanBlockValue(""+(char)ch, end);
						break;

					default:
						begin = "<#";
						end = "#>";
						value = tryScanBlockValue("", end);
						break;
				}
				break;
		}

		if (value == null) {
			resetMark();
			return false;
		}

		if (suspendMode && !asAttr && (begin.equals(DuelGrammar.OP_COMMENT))) {

			// always unwrap commented content of suspend-mode elements
			token = DuelToken.literal(value, token_index, token_line, token_column);
			return true;
		}

		BlockValue block = new BlockValue(begin, end, value);
		token = asAttr ?
			DuelToken.attrValue(block, token_index, token_line, token_column) :
			DuelToken.block(block, token_index, token_line, token_column);
		return true;
	}

	/**
	 * Tries to scan the next token as an unparsed block value
	 * @param begin
	 * @param end
	 * @return
	 * @throws IOException
	 */
	private String tryScanBlockValue(String begin, String end)
		throws IOException {

		for (int i=0, length=begin.length(); i<length; i++) {
			if (ch != begin.charAt(i)) {
				// didn't match begin delim
				return null;
			}

			nextChar();
		}

		// reset the buffer
		buffer.setLength(0);

		for (int i=0, length=end.length(); ch != EOF; ) {
			// check each char
			if (ch == end.charAt(i)) {
				// move to next char
				i++;
				if (i >= length) {
					length--;

					// consume final char
					nextChar();

					// trim ending delim from buffer
					buffer.setLength(buffer.length() - length);
					return buffer.toString();
				}
			} else {
				// reset to start of delim
				i = 0;
			}

			buffer.append((char)ch);
			nextChar();
		}

		throw new SyntaxException("Unterminated block", token_index, token_line, token_column);
	}

	/**
	 * Gets the next character in the input and updates statistics
	 * @return
	 * @throws IOException
	 */
	private int nextChar() throws IOException {
		int prevLine = line;

		ch = reader.read();
		line = reader.getLineNumber();

		// update statistics
		if (prevLine != line) {
			column = 0;
		} else {
			column++;
		}
		index++;

		return ch;
	}

	/**
	 * Marks the input location to enable resetting
	 * @param bufferSize
	 * @throws IOException
	 */
	private void setMark(int bufferSize) throws IOException {
		// store current statistics
		mark_line = line;
		mark_column = column;
		mark_index = index;
		mark_ch = ch;

		reader.mark(bufferSize);
	}

	/**
	 * Resets the input location to the marked location
	 * @throws IOException
	 */
	private void resetMark() throws IOException {
		// restore current statistics
		line = mark_line;
		column = mark_column;
		index = mark_index;
		ch = mark_ch;

		reader.reset();
	}

	/**
	 * Produces a list of the remaining tokens
	 * @return
	 */
	public ArrayList<DuelToken> toList() {

		ArrayList<DuelToken> list = new ArrayList<DuelToken>();
		while (hasNext()) {
			list.add(next());
		}
		return list;
	}
}
