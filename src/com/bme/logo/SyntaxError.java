package com.bme.logo;

/**
* An Exception representing a problem in the syntax of
* a Logo program or expression.
*
* @author John Earnest
**/

public class SyntaxError extends LogoError {
	
	static final long serialVersionUID = 1;

	/** The class of syntax error which produced this exception. **/
	public final Type type;

	/** The argument related to this syntax error type or null if none. **/
	public final String arg;

	/** The complete string which was originally being parsed. **/
	public final String sourceText;

	/** The index into the string at which a problem was encountered. **/
	public final int index;

	/** the line number on which the problem was encountered. **/
	public final int lineNumber;

	/** the position within the error line where the problem was encountered. **/
	public final int lineIndex;

	/** the line within which the problem was encountered. **/
	public final String line;

	/**
	* Construct a new SyntaxError with a formatted message.
	*
	* @param c         a Cursor indicating the parser's position.
	* @param errorType the class of error which produced this exception.
	* @param args      the argument for this error class or null if none.
	**/
	SyntaxError(Cursor c, Type errorType, String arg) {
		super(String.format(errorType.format, arg));
		this.type       = errorType;
		this.arg        = arg;
		this.sourceText = c.originalText;
		this.index      = c.index;

		int lines = 1;
		int chars = 0;
		StringBuilder line = new StringBuilder();
		for(int z = 0; z < index; z++) {
			if (sourceText.charAt(z) == '\n') {
				lines++;
				chars = 0;
				line = new StringBuilder();
			}
			else {
				chars++;
				line.append(sourceText.charAt(z));
			}
		}
		for(int z = index; z < sourceText.length(); z++) {
			if (sourceText.charAt(z) == '\n') { break; }
			line.append(sourceText.charAt(z));
		}

		this.lineNumber = lines;
		this.lineIndex  = chars;
		this.line       = line.toString();
	}

	/**
	* Construct a new SyntaxError with a formatted message.
	*
	* @param c         a Cursor indicating the parser's position.
	* @param errorType the class of error which produced this exception.
	**/
	SyntaxError(Cursor c, Type errorType) {
		this(c, errorType, null);
	}

	public static enum Type {
		MissingToken    ("missing '%s'?"),
		MissingName     ("word name expected!"),
		InvalidCharacter("invalid character '%s'!"),
		ArgumentNoColon ("'to' arguments must begin with ':'!"),
		ToWithoutEnd    ("'to' without 'end'!");

		public final String format;
		private Type(String format) { this.format = format; }
	}
}