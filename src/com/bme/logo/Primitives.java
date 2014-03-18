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

	private static final LWord A         = new LWord(LWord.Type.Name, "a");
	private static final LWord B         = new LWord(LWord.Type.Name, "b");
	private static final LWord RANGE     = new LWord(LWord.Type.Name, "range");
	private static final LWord ATOM      = new LWord(LWord.Type.Name, "atom");
	private static final LWord WORD      = new LWord(LWord.Type.Name, "word");
	private static final LWord LIST      = new LWord(LWord.Type.Name, "list");
	private static final LWord INDEX     = new LWord(LWord.Type.Name, "index");
	private static final LWord ARGUMENTS = new LWord(LWord.Type.Name, "arguments");
	private static final LWord BODY      = new LWord(LWord.Type.Name, "bodyList");
	private static final LWord CONDITION = new LWord(LWord.Type.Name, "condition");
	private static final LWord COUNT     = new LWord(LWord.Type.Name, "count");
	private static final LWord NAME      = new LWord(LWord.Type.Name, "name");
	private static final LWord VALUE     = new LWord(LWord.Type.Name, "value");


	public static Environment kernel() {
		Environment e = new Environment();

		// numeric primitives:

		e.bind(new LWord(LWord.Type.Prim, "sum") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, A) + num(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "difference") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, A) - num(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "product") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, A) * num(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "quotient") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, A) / nonzero(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "remainder") {
			public void eval(Environment e) {
				int x = num(e, A);
				int y = nonzero(e, B);
				x %= y;
				e.output(new LNumber(x < 0 ? x+y : x));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "negate") {
			public void eval(Environment e) {
				e.output(new LNumber(num(e, A) * -1));
			}
		}, A);
		e.bind(new LWord(LWord.Type.Prim, "less?") {
			public void eval(Environment e) {
				e.output(toBool(num(e, A) < num(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "greater?") {
			public void eval(Environment e) {
				e.output(toBool(num(e, A) > num(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "equal?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(A).equals(e.thing(B))));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "random") {
			public void eval(Environment e) {
				e.output(new LNumber((int)(Math.random() * num(e, RANGE))));
			}
		}, RANGE);

		// type conversions and predicates:
		
		e.bind(new LWord(LWord.Type.Prim, "word?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(ATOM) instanceof LWord));
			}
		}, ATOM);
		e.bind(new LWord(LWord.Type.Prim, "list?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(ATOM) instanceof LList));
			}
		}, ATOM);
		e.bind(new LWord(LWord.Type.Prim, "num?") {
			public void eval(Environment e) {
				e.output(toBool(e.thing(ATOM) instanceof LNumber));
			}
		}, ATOM);
		e.bind(new LWord(LWord.Type.Prim, "asname") {
			public void eval(Environment e) {
				e.output(new LWord(LWord.Type.Name, word(e, WORD).value));
			}
		}, WORD);
		e.bind(new LWord(LWord.Type.Prim, "asvalue") {
			public void eval(Environment e) {
				e.output(new LWord(LWord.Type.Value, word(e, WORD).value));
			}
		}, WORD);
		e.bind(new LWord(LWord.Type.Prim, "ascall") {
			public void eval(Environment e) {
				e.output(new LWord(LWord.Type.Call, word(e, WORD).value));
			}
		}, WORD);


		// list manipulation:

		e.bind(new LWord(LWord.Type.Prim, "size") {
			public void eval(Environment e) {
				e.output(new LNumber(list(e, LIST).size()));
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "first") {
			public void eval(Environment e) {
				e.output(list(e, LIST).first());
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "last") {
			public void eval(Environment e) {
				e.output(list(e, LIST).last());
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "butfirst") {
			public void eval(Environment e) {
				e.output(list(e, LIST).butFirst());
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "butlast") {
			public void eval(Environment e) {
				e.output(list(e, LIST).butLast());
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "flatten") {
			public void eval(Environment e) {
				e.output(list(e, LIST).flatten());
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "item") {
			public void eval(Environment e) {
				e.output(list(e, LIST).item(num(e, INDEX)));
			}
		}, INDEX, LIST);
		e.bind(new LWord(LWord.Type.Prim, "fput") {
			public void eval(Environment e) {
				e.output(list(e, LIST).fput(e.thing(ATOM)));
			}
		}, ATOM, LIST);
		e.bind(new LWord(LWord.Type.Prim, "lput") {
			public void eval(Environment e) {
				e.output(list(e, LIST).lput(e.thing(ATOM)));
			}
		}, ATOM, LIST);
		e.bind(new LWord(LWord.Type.Prim, "join") {
			public void eval(Environment e) {
				e.output(list(e, A).join(list(e, B)));
			}
		}, A, B);
		e.bind(new LWord(LWord.Type.Prim, "member") {
			public void eval(Environment e) {
				e.output(list(e, LIST).member(e.thing(ATOM)));
			}
		}, ATOM, LIST);


		// values and scopes:

		e.bind(new LWord(LWord.Type.Prim, "local") {
			public void eval(Environment e) {
				e.local(word(e, NAME), e.thing(VALUE));
			}
		}, NAME, VALUE);
		e.bind(new LWord(LWord.Type.Prim, "make") {
			public void eval(Environment e) {
				e.make(word(e, NAME), e.thing(VALUE));
			}
		}, NAME, VALUE);
		e.bind(new LWord(LWord.Type.Prim, "thing") {
			public void eval(Environment e) {
				e.output(e.thing(word(e, WORD)));
			}
		}, WORD);
		e.bind(new LWord(LWord.Type.Prim, "bind") {
			public void eval(Environment e) {
				LList body = list(e, BODY);
				LList ret = new LList(body, list(e, ARGUMENTS));
				ret.sourceText = body.sourceText;
				e.output(ret);
			}
		}, ARGUMENTS, BODY);
		e.bind(new LWord(LWord.Type.Prim, "args") {
			public void eval(Environment e) {
				LList code = list(e, LIST);
				e.output(code.arguments != null ? code.arguments : new LList());
			}
		}, LIST);


		// control:

		e.bind(new LWord(LWord.Type.Prim, "stop") {
			public void eval(Environment e) {
				e.scopes.pop();
				while(!e.scopes.peek().procedure || prim(e.scopes.peek().code)) {
					if (e.scopes.size() <= 1) {
						throw new RuntimeError(e, RuntimeError.Type.OutsideProcedure, "stop");
					}
					e.scopes.pop();
				}
				e.scopes.pop();
			}
		});
		e.bind(new LWord(LWord.Type.Prim, "output") {
			public void eval(Environment e) {
				LAtom r = e.thing(VALUE);
				e.scopes.pop();
				while(!e.scopes.peek().procedure || prim(e.scopes.peek().code)) {
					if (e.scopes.size() <= 1) {
						throw new RuntimeError(e, RuntimeError.Type.OutsideProcedure, "output");
					}
					e.scopes.pop();
				}
				e.scopes.pop();
				e.value(r);
			}
		}, VALUE);
		e.bind(new LWord(LWord.Type.Prim, "run") {
			public void eval(Environment e) {
				e.push(list(e, LIST), false);
			}
		}, LIST);
		e.bind(new LWord(LWord.Type.Prim, "if") {
			public void eval(Environment e) {
				if (bool(e.thing(CONDITION))) { e.push(list(e, BODY), false); }
			}
		}, CONDITION, BODY);
		e.bind(new LWord(LWord.Type.Prim, "unless") {
			public void eval(Environment e) {
				if (!bool(e.thing(CONDITION))) { e.push(list(e, BODY), false); }
			}
		}, CONDITION, BODY);
		e.bind(new LWord(LWord.Type.Prim, "repeat") {
			public void eval(Environment e) {
				int index = num(e, COUNT);
				if (index == 0) {
					e.scopes.pop();
					return;
				}
				e.scopes.peek().bindings.put(COUNT, new LNumber(index - 1));
				e.scopes.peek().index--;
				e.push(list(e, BODY), false);
			}
		}, COUNT, BODY);

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
		throw new RuntimeError(e, RuntimeError.Type.TypeMismatch, o, "word");
	}

	static LList list(Environment e, LAtom o) {
		if (o instanceof LList) { return (LList)o; }
		throw new RuntimeError(e, RuntimeError.Type.TypeMismatch, o, "list");
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
		throw new RuntimeError(e, RuntimeError.Type.TypeMismatch, o, "number");
	}

	private static int nonzero(Environment e, LWord key) {
		int ret = num(e, key);
		if (ret == 0) { throw new RuntimeError(e, RuntimeError.Type.DivideByZero); }
		return ret;
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

	/**
	* Identify LLists representing primitive procedures.
	*
	* @param code an LList to check.
	* @return true if the provided LList is a primitive procedure.
	**/
	public static boolean prim(LList code) {
		if (code == null)   { return false; }
		if (code.size() != 1) { return false; }
		LAtom a = code.first();
		if (!(a instanceof LWord)) { return false; }
		return ((LWord)a).type == LWord.Type.Prim;
	}
}