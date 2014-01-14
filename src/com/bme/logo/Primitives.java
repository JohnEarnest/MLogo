package com.bme.logo;

/**
* This class represents a Factory for assembling
* a Logo environment structure loaded with the primitives
* comprising the language kernel.
* Also included are a number of static helper methods
* which are useful in implementing new primitives.
*
* @author John Earnest
**/

public class Primitives {

	private Primitives() {}

	private static LWord a = new LWord(LWord.Type.Name, "argument1");
	private static LWord b = new LWord(LWord.Type.Name, "argument2");

	public static Environment kernel() {
		Environment e = new Environment();

		// numeric primitives:

		e.bind(new LWord(LWord.Type.Prim, "sum") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, a) + num(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "difference") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, a) - num(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "product") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, a) * num(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "quotient") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, a) / num(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "remainder") {
			public void eval(Environment e) {
				int x = num(e, a);
				int y = num(e, b);
				x %= y;
				e.output(new LNumber(x < 0 ? x+y : x));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "negate") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, a) * -1));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "less?") {
			public void eval(Environment e) {
				e.output(toBool(num(e, a) < num(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "greater?") {
			public void eval(Environment e) {
				e.output(toBool(num(e, a) > num(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "equal?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(a).equals(e.thing(b))));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "random") {
			public void eval(Environment e) {
				e.output(new LNumber((int)(Math.random() * num(e, a))));
			}
		}, a);

		// type conversions and predicates:
		
		e.bind(new LWord(LWord.Type.Prim, "word?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(a) instanceof LWord));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "list?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(a) instanceof LList));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "num?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(a) instanceof LNumber));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "asname") {
			public void eval(Environment e) {
				e.output(new LWord(LWord.Type.Name, word(e, a).value));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "asvalue") {
			public void eval(Environment e) {
				e.output(new LWord(LWord.Type.Value, word(e, a).value));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "ascall") {
			public void eval(Environment e) {
				e.output(new LWord(LWord.Type.Call, word(e, a).value));
			}
		}, a);


		// list manipulation:

		e.bind(new LWord(LWord.Type.Prim, "size") {
			public void eval(Environment e) {
				e.output(new LNumber(list(e, a).size()));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "first") {
			public void eval(Environment e) {
				e.output(list(e, a).first());
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "last") {
			public void eval(Environment e) {
				e.output(list(e, a).last());
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "butfirst") {
			public void eval(Environment e) {
				e.output(list(e, a).butFirst());
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "butlast") {
			public void eval(Environment e) {
				e.output(list(e, a).butLast());
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "flatten") {
			public void eval(Environment e) {
				e.output(list(e, a).flatten());
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "item") {
			public void eval(Environment e) {
				e.output(list(e, b).item(num(e, a)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "fput") {
			public void eval(Environment e) {
				e.output(list(e, b).fput(e.thing(a)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "lput") {
			public void eval(Environment e) {
				e.output(list(e, b).lput(e.thing(a)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "join") {
			public void eval(Environment e) {
				e.output(list(e, a).join(list(e, b)));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "member") {
			public void eval(Environment e) {
				e.output(list(e, b).member(e.thing(a)));
			}
		}, a, b);


		// values and scopes:

		e.bind(new LWord(LWord.Type.Prim, "local") {
			public void eval(Environment e) {
				e.local(word(e, a), e.thing(b));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "make") {
			public void eval(Environment e) {
				e.make(word(e, a), e.thing(b));
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "thing") {
			public void eval(Environment e) {
				e.output(e.thing(word(e, a)));
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "bind") {
			public void eval(Environment e) {
				LList body = list(e, b);
				LList ret = new LList(body, list(e, a));
				ret.sourceText = body.sourceText;
				e.output(ret);
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "args") {
			public void eval(Environment e) {
				LList code = list(e, a);
				e.output(code.arguments != null ? code.arguments : new LList());
			}
		}, a);


		// control:

		e.bind(new LWord(LWord.Type.Prim, "stop") {
			public void eval(Environment e) {
				e.scopes.pop();
				while(!e.scopes.peek().procedure || prim(e.scopes.peek().code)) {
					if (e.scopes.size() <= 1) {
						throw new RuntimeError(e, "I can't stop; I'm not running a procedure!");
					}
					e.scopes.pop();
				}
				e.scopes.pop();
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "output") {
			public void eval(Environment e) {
				LAtom r = e.thing(a);
				e.scopes.pop();
				while(!e.scopes.peek().procedure || prim(e.scopes.peek().code)) {
					if (e.scopes.size() <= 1) {
						throw new RuntimeError(e, "I can't output; I'm not running a procedure!");
					}
					e.scopes.pop();
				}
				e.scopes.pop();
				e.value(r);
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "run") {
			public void eval(Environment e) {
				e.push(list(e, a), false);
			}
		}, a);
		e.bind(new LWord(LWord.Type.Prim, "if") {
			public void eval(Environment e) {
				if (bool(e.thing(a))) { e.push(list(e, b), false); }
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "unless") {
			public void eval(Environment e) {
				if (!bool(e.thing(a))) { e.push(list(e, b), false); }
			}
		}, a, b);
		e.bind(new LWord(LWord.Type.Prim, "repeat") {
			public void eval(Environment e) {
				int index = num(e, a);
				if (index == 0) {
					e.scopes.pop();
					return;
				}
				e.scopes.peek().bindings.put(a, new LNumber(index - 1));
				e.scopes.peek().index--;
				e.push(list(e, b), false);
			}
		}, a, b);

		return e;
	}

	/**
	* Convert a Java boolean into an LWord representing
	* a true or false value.
	*
	* @param v the boolean value to consider.
	**/
	static LWord toBool(boolean v) {
		return v ? LWord.trueSymbol : LWord.falseSymbol;
	}

	/**
	* Return whether an LAtom is considered "true" or "false".
	* This Logo implementation considers the empty list,
	* the number zero or the name 'false' to be false, and
	* anything else is considered 'true'.
	*
	* @param a the LAtom to consider.
	**/
	public static boolean bool(LAtom a) {
		// in this interpreter I'm going with the concept of 'falsiness':
		if (a instanceof LWord)   { return !LWord.falseSymbol.equals(a); }
		if (a instanceof LList)   { return ((LList)a).size() > 0;        }
		if (a instanceof LNumber) { return ((LNumber)a).value != 0;      }
		return true;
	}

	static LWord word(Environment e, LAtom o) {
		if (o instanceof LWord) { return (LWord)o; }
		throw new RuntimeError(e, "'%s' is not a word!", o);
	}

	static LList list(Environment e, LAtom o) {
		if (o instanceof LList) { return (LList)o; }
		throw new RuntimeError(e, "'%s' is not a list!", o);
	}

	/**
	* Attempt to dereference a word and cast the result
	* to an LNumber, throwing errors as appropriate.
	*
	* @param e the Environment in which to dereference the key
	* @param key an LWord name to look up.
	**/
	public static int num(Environment e, LWord key) {
		LAtom o = e.thing(key);
		if (o instanceof LNumber) { return ((LNumber)o).value; }
		throw new RuntimeError(e, "'%s' is not a number!", o);
	}

	/**
	* Attempt to dereference a word and cast the result
	* to an LList, throwing errors as appropriate.
	*
	* @param e the Environment in which to dereference the key
	* @param key an LWord name to look up.
	**/
	public static LList list(Environment e, LWord key) {
		LAtom o = e.thing(key);
		return list(e, o);
	}

	/**
	* Attempt to dereference a word and cast the result
	* to an LWord, throwing errors as appropriate.
	*
	* @param e the Environment in which to dereference the key
	* @param key an LWord name to look up.
	**/
	public static LWord word(Environment e, LWord key) {
		LAtom o = e.thing(key);
		return word(e, o);
	}

	static boolean prim(LList code) {
		if (code == null)   { return false; }
		if (code.size() != 1) { return false; }
		LAtom a = code.first();
		if (!(a instanceof LWord)) { return false; }
		return ((LWord)a).type == LWord.Type.Prim;
	}
}