package com.bme.logo;

import java.util.*;

/**
* <p>The Parser can convert raw Strings into LList objects recursively
* composed of LLists, LWords and LNumbers. In addition to basic translation,
* this parser desugars parenthesis-delimited infix expressions comprising
* basic arithmetic operators <, >, =, +, -, *, / and % into prefix forms and
* desugars the 'to ... end' form into an explicit argument binding
* and local variable assignment.</p>
*
* <p>For example, the following expression:</p>
*
* <pre>print (3 + 2 * 5)</pre>
*
* <p>will parse as:</p>
*
* <pre>print sum 3 product 2 5</pre>
*
* <p>And the following procedure definition:</p>
*
*<pre>
*to any :list
*   output item random size :list :list
*end
*</pre>
*
* <p>will parse as:</p>
*
* <pre>local 'any bind ['list][output item random size :list :list]</pre>
*
* @author John Earnest
**/

public class Parser {

	private Parser() {}

	static final String token = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMOPQRSTUVWXYZ.,!?";
	static final String digit = "0123456789";

	private static final LWord local      = new LWord(LWord.Type.Call, "local");
	private static final LWord bind       = new LWord(LWord.Type.Call, "bind");
	private static final LWord negate     = new LWord(LWord.Type.Call, "negate");
	private static final LWord product    = new LWord(LWord.Type.Call, "product");
	private static final LWord quotient   = new LWord(LWord.Type.Call, "quotient");
	private static final LWord remainder  = new LWord(LWord.Type.Call, "remainder");
	private static final LWord sum        = new LWord(LWord.Type.Call, "sum");
	private static final LWord difference = new LWord(LWord.Type.Call, "difference");
	private static final LWord greater    = new LWord(LWord.Type.Call, "greater?");
	private static final LWord less       = new LWord(LWord.Type.Call, "less?");
	private static final LWord equal      = new LWord(LWord.Type.Call, "equal?");

	private static LAtom parseToken(Cursor c) {
		if (c.match("["))   { return parseList(c); }
		if (c.starts('\'')) { c.skip(); return new LWord(LWord.Type.Name,  c.token()); }
		if (c.starts(':' )) { c.skip(); return new LWord(LWord.Type.Value, c.token()); }
		if (c.tokenChar())  {           return new LWord(LWord.Type.Call,  c.token()); }
		if (c.signed())     { return new LNumber(c.number()); }
		throw new SyntaxError(c, "invalid character '%c'!", c.curr());
	}

	private static LList infixUnary(Cursor c, LList r) {
		if (c.signed())   { return r.lput(parseToken(c)); }
		if (c.match("-")) { return infixUnary(c, r.lput(negate)); }
		if (c.match("(")) {
			while(!c.match(")")) {
				if (c.eof()) { throw new SyntaxError(c, "missing ')' ?"); }
				r = infix(c, r);
			}
			return r;
		}
		if (c.match("to ")) {
			int baseindex = c.index;
			int endindex = -1;
			String basetext = c.text;
			StringBuilder wordname = new StringBuilder();
			if (!c.tokenChar()) { throw new SyntaxError(c, "word name expected!"); }
			while(c.tokenChar()) { wordname.append(c.curr()); c.skip(); }
			LWord word = new LWord(LWord.Type.Name, wordname.toString());

			LList args = new LList();
			while(true) {
				if (c.eof()) { throw new SyntaxError(c, "incomplete 'to' block!"); }
				if (c.curr() == '\n') { break; }
				while(!c.eof() && c.curr() != '\n' && c.white()) { c.skip(); }
				if (c.curr() == '\n') { break; }
				if (c.curr() != ':') {
					throw new SyntaxError(c, "'to' arguments must begin with ':'!");
				}
				c.skip();
				StringBuilder name = new StringBuilder();
				while(c.tokenChar()) {
					name.append(c.curr());
					c.skip();
				}
				args = args.lput(new LWord(LWord.Type.Name, name.toString()));
			}
			c.trim();

			LList body = new LList();
			while(true) {
				endindex = c.index;
				if (c.match("end ")) { break; }
				if (c.eof()) { throw new SyntaxError(c, "'to' without 'end'!"); }
				body = infixUnary(c, body);
			}

			body.sourceText = "to " + basetext.substring(0, endindex - baseindex) + "end";
			return r.lput(local).lput(word).lput(bind).lput(args).lput(body);
		}
		return r.lput(parseToken(c));
	}

	private static LList infixMul(Cursor c, LList r) {
		LList b = infixUnary(c, new LList());
		if (c.match("*")) { return infixMul(c, r.join(b.fput(product  ))); }
		if (c.match("/")) { return infixMul(c, r.join(b.fput(quotient ))); }
		if (c.match("%")) { return infixMul(c, r.join(b.fput(remainder))); }
		return r.join(b);
	}

	private static LList infixAdd(Cursor c, LList r) {
		LList b = infixMul(c, new LList());
		if (c.match("+")) { return infixAdd(c, r.join(b.fput(sum       ))); }
		if (c.match("-")) { return infixAdd(c, r.join(b.fput(difference))); }
		return r.join(b);
	}

	private static LList infix(Cursor c, LList r) {
		LList b = infixAdd(c, new LList());
		if (c.match(">")) { return infix(c, r.join(b.fput(greater))); }
		if (c.match("<")) { return infix(c, r.join(b.fput(less   ))); }
		if (c.match("=")) { return infix(c, r.join(b.fput(equal  ))); }
		return r.join(b);
	}

	private static LList parseList(Cursor c) {
		LList r = new LList();
		while(!c.match("]")) {
			if (c.eof()) { throw new SyntaxError(c, "missing ']' ?"); }
			r = infixUnary(c, r);
		}
		return r;
	}

	/**
	* Convert a String into an LList object recursively
	* composed of LLists, LWords and LNumbers.
	*
	* @param s the String to parse.
	**/
	public static LList parse(String s) {
		Cursor c = new Cursor(s);
		LList r = new LList();
		while(!c.eof()) {
			r = infixUnary(c, r);
		}
		return r;
	}

	/**
	* Determine whether a String contains a complete,
	* parseable expression with closing brackets,
	* parentheses and to...end, respecting comments, nesting
	* and token delimiters. If the String is not complete,
	* return a Stack of tokens necessary for the expression
	* to be well-formed, in the order they should appear.
	* Useful for implementing multiline REPLs.
	*
	* @param s the String to consider.
	* @return an empty Stack if the expression is complete or a list of closing tokens.
	**/
	public static Stack<String> complete(String s) {
		Cursor c = new Cursor(s);
		Stack<String> r = new Stack<String>();
		
		while(true) {
			c.trim();
			if (c.eof()) { break; }
			if (c.curr() == '#') {
				while(!c.eof() && c.curr() != '\n') { c.skip(); }
				continue;
			}
			if (c.curr() == ')') {
				if (!r.peek().equals(")")) { throw new SyntaxError(c, "missing '%s' ?", r.peek()); }
				r.pop(); c.skip(); continue;
			}
			if (c.curr() == ']') {
				if (!r.peek().equals("]")) { throw new SyntaxError(c, "missing '%s' ?", r.peek()); }
				r.pop(); c.skip(); continue;
			}
			if (c.match("end ")) {
				if (!r.peek().equals("end")) { throw new SyntaxError(c, "missing '%s' ?", r.peek()); }
				r.pop(); continue;
			}
			if (c.curr() == '(') { r.push(")"); c.skip(); continue; }
			if (c.curr() == '[') { r.push("]"); c.skip(); continue; }
			if (c.match("to "))  { r.push("end");         continue; }
			if (c.tokenChar())   { c.token();             continue; }
			if (c.signed())      { c.number();            continue; }

			if ("+-*/%><=:'".indexOf(c.curr()) >= 0) { c.skip(); continue; }
			throw new SyntaxError(c, "invalid character '%c'!", c.curr());
		}
		return r;
	}
}

class Cursor {

	int index = 0;
	String originalText;
	String text;

	Cursor(String s) {
		this.originalText = s;
		this.text = s;
		trim();
	}

	void skip()            { text = text.substring(1); index++; }
	char curr()            { return text.charAt(0); }
	boolean eof()          { return text.length() < 1; }
	boolean white()        { return !eof() && Character.isWhitespace(curr()); }
	boolean tokenChar()    { return !eof() && Parser.token.indexOf(curr()) >= 0; }
	boolean numeral()      { return !eof() && Parser.digit.indexOf(curr()) >= 0; }
	boolean starts(char c) { return !eof() && curr() == c; }

	boolean signed() {
		return numeral() ||
		(text.length() > 1 && text.startsWith("-") && Parser.digit.indexOf(text.charAt(1)) >= 0);
	}

	void trim() {
		while(white()) { skip(); }
		while (!eof() && curr() == '#') {
			while(!eof() && curr() != '\n') { skip(); }
			while(white()) { skip(); }
		}
	}

	boolean match(String s) {
		// expect whitespace after token?
		boolean alone = s.endsWith(" ");
		s = s.trim();
		if (!text.startsWith(s)) { return false; }
		if (alone && text.length() > s.length()) {
			char n = text.charAt(s.length());
			if (n != '#' && !Character.isWhitespace(n)) { return false; }
		}
		for(char c : s.toCharArray()) { skip(); }
		trim();
		return true;
	}

	String token() {
		StringBuilder r = new StringBuilder();
		while(tokenChar()) {
			r.append(curr());
			skip();
		}
		trim();
		return r.toString();
	}

	int number() {
		int r = 0;
		boolean negative = text.startsWith("-");
		if (negative) { skip(); }
		while(numeral()) {
			r *= 10;
			r += Character.getNumericValue(curr());
			skip();
		}
		trim();
		return negative ? -r : r;
	}
}