package com.bme.logo;

import java.util.*;

/**
* The Interpreter provides the means of executing an Environment.
*
* @author John Earnest
**/

public class Interpreter {

	/**
	* The number of activation frames which the interpreter
	* is allowed to create before signaling a stack overflow.
	* Note that the global scope and invocation of primitives
	* contributes to this count, so it is not strictly
	* a measurement of how many procedure calls are in flight.
	* If this is set to 0, no limit will be enforced.
	**/
	public static int RECURSION_LIMIT = 1000;

	private Interpreter() {}

	/**
	* Execute a program using the specified Environment
	* as a starting point. The supplied environment should
	* be initialized with any primitives necessary to execute
	* the supplied program. See {@link com.bme.logo.Primitives#kernel}.
	*
	* @param code a List of words to execute.
	* @param e an environment within which to execute the program.
	**/
	public static void run(LList code, Environment e) {
		e.scopes.peek().code = code;
		e.scopes.peek().index = 0;
		e.scopes.peek().trace.clear();
		while(tick(e)) {}
	}

	/**
	* Prime an environment to run a specified chunk of code.
	* See {@link #runUntil}.
	*
	* @param code a List of words to execute.
	* @param e an environment within which to execute the program.
	**/
	public static void init(LList code, Environment e) {
		e.scopes.peek().code = code;
		e.scopes.peek().index = 0;
		e.scopes.peek().trace.clear();
	}

	/**
	* Execute a running program until it completes
	* or it is paused by a primitive procedure.
	* Used in conjunction with {@link #init}.
	*
	* @param e an environment within which to execute the program.
	* @return true if the program has been paused, false if it has completed.
	**/
	public static boolean runUntil(Environment e) {
		while(tick(e)) {
			if (e.paused) { return true; }
		}
		return false;
	}

	private static boolean tick(Environment e) {
		Scope s = e.scopes.peek();

		//System.err.format("index %d code %s%n", s.index, s.code);

		// check for gravid procedure invocations
		if (s.trace.size() > 0) {
			Func f = s.trace.peek();
			if (f.args.size() == f.vals.size()) {
				s.trace.pop();
				newScope(e, f.code);
				for(int z = 0; z < f.args.size(); z++) {
					e.scopes.peek().bindings.put(Primitives.word(e, f.args.item(z)), f.vals.get(z));
				}
				return true;
			}
		}

		// check for an exhausted list
		if (s.index >= s.code.size()) {
			if (s.trace.size() > 0) {
				throw new RuntimeError(e, "Not enough arguments for '%s'!",
					e.getName(s.trace.peek().code)
				);
			}
			if (e.scopes.size() <= 1) {
				return false;
			}
			e.scopes.pop();
			return true;
		}

		s.code.item(s.index).eval(e);
		s.index++;
		return true;
	}

	private static void newScope(Environment e, LList code) {
		Scope outer = null;
		for(int z = e.scopes.size()-1; z >= 1; z--) {
			if (e.scopes.get(z).procedure) { outer = e.scopes.get(z); break; }
		}
		if (canTail(outer, e, code)) {
			// smash the call stack down to the tail procedure
			while(e.scopes.peek() != outer) { e.scopes.pop(); }
			outer.index = 0;
			outer.trace.clear();
			outer.bindings.clear();
		}
		else {
			// apply the collected arguments in a new scope
			e.push(code, true);
		}
	}

	private static boolean canTail(Scope s, Environment e, LList target) {
		if (s == null)               { return false; } // we must be in a procedure.
		if (!s.code.equals(target))  { return false; } // our procedure must match our target.
		if (s.index < s.code.size()) { return false; } // our procedure must be fully evaluated.
		
		// we can tail-call if we're a base statement in our procedure:
		if (s.trace.size() == 0) { return true; }

		// we can tail-call if our result flows to an output:
		if (s.trace.size() == 1) {
			LAtom caller = s.trace.peek().code.first();
			if (caller instanceof LWord) {
				LWord word = (LWord)caller;
				if (word.type == LWord.Type.Prim && word.value.equals("output")) { return true; }
			}
		}
		return false;
	}
}