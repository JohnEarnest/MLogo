package com.bme.logo;

/**
* An Exception representing a problem in the syntax of
* a Logo program or expression.
*
* @author John Earnest
**/

public class SyntaxError extends LogoError {
	
	static final long serialVersionUID = 1;

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
	* @param c       a Cursor indicating the parser's position.
	* @param message the format string describing the error.
	* @param args    the arguments for the format string.
	**/
	SyntaxError(Cursor c, String message, Object... args) {
		super(String.format(message, args));
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
}