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
		init(code, e);
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
		if (e.tracer != null) { e.tracer.begin(); }
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

	/**
	* Execute a running program until it completes,
	* it is paused by a primitive procedure or
	* it reaches a specified tick timeout.
	* Used in conjunction with {@link #init}.
	*
	* @param e an environment within which to execute the program.
	* @param maxTicks the maximum number of execution ticks to allow before returning.
	* @return true if the program has been paused or timed out, false if it has completed.
	**/
	public static boolean runUntil(Environment e, int maxTicks) {
		for(int z = 0; z < maxTicks && !e.paused; z++) {
			if (!tick(e)) { return false; }
		}
		return true;
	}

	private static boolean tick(Environment e) {
		if (e.tracer != null) { e.tracer.tick(); }

		Scope s = e.scopes.peek();

		//System.err.format("index %d code %s%n", s.index, s.code);

		// check for gravid procedure invocations
		if (s.trace.size() > 0) {
			Func f = s.trace.peek();
			if (f.args.size() == f.vals.size()) {
				s.trace.pop();
				boolean tailCalled = newScope(e, f.code);
				for(int z = 0; z < f.args.size(); z++) {
					e.scopes.peek().bindings.put(Primitives.word(e, f.args.item(z)), f.vals.get(z));
				}

				if (e.tracer != null) {
					String name = e.getName(f.code).toString();
					if (name.startsWith("'")) { name = name.substring(1); }
					Map<LAtom, LAtom> args = new HashMap<LAtom, LAtom>();
					for(int z = 0; z < f.args.size(); z++) { args.put(f.args.item(z), f.vals.get(z)); }
					if (Primitives.prim(f.code)) { e.tracer.callPrimitive(name, args); }
					else { e.tracer.call(name, args, tailCalled); }
				}
				return true;
			}
		}

		// check for an exhausted list
		if (s.index >= s.code.size()) {
			if (s.trace.size() > 0) {
				throw new RuntimeError(e, RuntimeError.Type.NotEnoughArguments,
					e.getName(s.trace.peek().code)
				);
			}
			if (e.scopes.size() <= 1) {
				if (e.tracer != null) { e.tracer.end(); }
				return false;
			}

			if (e.tracer != null && !Primitives.prim(e.scopes.peek().code)) {
				// implied 'stop' or 'output':
				String name = e.getName(e.scopes.peek().code).toString();
				if (name.startsWith("'")) { name = name.substring(1); }
				Stack<Func> f = e.scopes.get(e.scopes.size()-2).trace;
				if (f.size() > 0 && f.peek().vals.size() > 0) {
					LAtom last = f.peek().vals.get(f.peek().vals.size()-1);
					e.tracer.output(name, last, true);
				}
				else {
					e.tracer.stop(name, true);
				}
			}

			e.scopes.pop();
			return true;
		}

		s.code.item(s.index).eval(e);
		s.index++;
		return true;
	}

	private static boolean newScope(Environment e, LList code) {
		Scope outer = null;
		for(int z = e.scopes.size()-1; z >= 1; z--) {
			if (e.scopes.get(z).procedure && !(Primitives.prim(e.scopes.get(z).code))) {
				outer = e.scopes.get(z); break;
			}
		}
		if (canTail(outer, e, code)) {
			// smash the call stack down to the tail procedure
			while(e.scopes.peek() != outer) { e.scopes.pop(); }
			outer.index = 0;
			outer.trace.clear();
			outer.bindings.clear();
			return true;
		}
		else {
			// apply the collected arguments in a new scope
			e.push(code, true);
			return false;
		}
	}

	private static boolean canTail(Scope s, Environment e, LList target) {
		if (s == null)               { return false; } // we must be in a procedure.
		if (!s.code.equals(target))  { return false; } // our procedure must match our target.

		// if the procedure has not been fully evaluated,
		// the next operation must be a call to 'stop':
		if (s.index < s.code.size()) {
			LAtom next = e.scopes.peek().code.item(e.scopes.peek().index);
			if (!(next instanceof LWord)) { return false; }
			LWord nword = (LWord)next;
			if (nword.type != LWord.Type.Call) { return false; }
			if (!"stop".equals(nword.value)) { return false; }
		}

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