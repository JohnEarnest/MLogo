package com.bme.logo;

import java.util.*;

/**
* The Environment data structure captures and stores
* the complete state of the Logo interpreter.
* Atoms are always evaluated with respect to an Environment.
*
* @author John Earnest
**/

public class Environment {

	boolean paused = false;
	Stack<Scope> scopes = new Stack<Scope>();
	{ scopes.push(new Scope(null, false)); }

	/**
	* Construct a new, empty Environment structure
	* with no primitive definitions loaded.
	* {@link com.bme.logo.Primitives#kernel} will initialize
	* an environment with primitives loaded, and is generally
	* how user code should obtain one.
	**/
	public Environment() {}

	void push(LList code, boolean procedure) {
		if (scopes.size() > Interpreter.RECURSION_LIMIT && Interpreter.RECURSION_LIMIT != 0) {
			throw new RuntimeError(this, "Stack overflow!");
		}
		scopes.push(new Scope(code, procedure));
	}

	private boolean implicitOutput(LAtom a) {
		// we must be at the end of the current code list:
		if (scopes.peek().index < scopes.peek().code.size()-1) { return false; }
		// there must be some expression in a lower scope which wants a result:
		if (scopes.size() < 2) { return false; }
		if (scopes.get(scopes.size()-2).trace.size() < 1) { return false; }
		return true;
	}

	void value(LAtom a) {
		if (scopes.peek().trace.size() < 1) {
			if (!implicitOutput(a)) {
				throw new RuntimeError(this, "I don't know what to do with '%s'!", a);
			}
			else {
				scopes.get(scopes.size() - 2).trace.peek().vals.add(a);
				return;
			}
		}
		scopes.peek().trace.peek().vals.add(a);
	}

	/**
	* Halt the current Logo procedure, returning a specified value.
	* This method should be called exactly once
	* by any primitive procedure which produces a result.
	*
	* @param a an LAtom to return.
	**/
	public void output(LAtom a) {
		scopes.pop();
		value(a);
	}

	private void set(Map<LWord, LAtom> bindings, LWord name, LAtom value) {
		if (bindings.containsKey(name) && bindings.get(name) instanceof LList) {
			if (Primitives.prim((LList)bindings.get(name))) {
				throw new RuntimeError(this,
					"The word '%s' is primitive and cannot be reassigned.",
					name.value
				);
			}
		}
		bindings.put(name, value);
	}

	void local(LWord name, LAtom value) {
		set(scopes.get(scopes.size()-2).bindings, name, value);
	}

	void make(LWord name, LAtom value) {
		for(int z = scopes.size()-2; z >= 1; z--) {
			if (scopes.get(z).bindings.containsKey(name)) {
				set(scopes.get(z).bindings, name, value);
				return;
			}
		}
		set(scopes.get(0).bindings, name, value);
	}

	/**
	* Dereference a name with respect to the current
	* dynamic scope, just like the Logo primitive 'thing'.
	*
	* @param name an LWord representing the name of an object to look up.
	**/
	public LAtom thing(LWord name) {
		for(int z = scopes.size()-1; z >= 0; z--) {
			if (scopes.get(z).bindings.containsKey(name)) {
				return scopes.get(z).bindings.get(name);
			}
		}
		throw new RuntimeError(this, "'%s' has no value!", name.value);
	}

	LAtom getName(LAtom value) {
		for(int z = scopes.size()-1; z >= 0; z--) {
			for(Map.Entry<LWord, LAtom> e : scopes.get(z).bindings.entrySet()) {
				if (e.getValue().equals(value)) { return e.getKey(); }
			}
		}
		return value;
	}

	void call(LWord name) {
		for(int z = scopes.size()-1; z >= 0; z--) {
			if (scopes.get(z).bindings.containsKey(name)) {
				LList code = Primitives.list(this, scopes.get(z).bindings.get(name));
				scopes.peek().trace.push(new Func(this, code));
				return;
			}
		}
		throw new RuntimeError(this, "I don't know how to '%s'!", name.value);
	}

	/**
	* Install a new primitive procedure with a specified
	* argument list. The primitive's value will be used
	* as its name, and any code for the primitive should
	* override the {@link com.bme.logo.LAtom#eval} method.
	*
	* @param prim the name and body of the new primitive.
	* @param args a series of names the primitive will take as arguments.
	**/
	public void bind(LWord prim, LWord... args) {
		LList code = new LList(prim, new LList(args));
		make(new LWord(LWord.Type.Call, prim.value), code);
	}

	/**
	* Remove all bindings for a given name.
	*
	* @param name the name of the word to erase.
	**/
	public void erase(LWord name) {
		for(Scope s : scopes) {
			s.bindings.remove(name);
		}
	}

	/**
	* Collect a Set of every name that is bound
	* to a value in the current scope.
	**/
	public Set<LWord> words() {
		Set<LWord> ret = new HashSet<LWord>();
		for(Scope s : scopes) {
			ret.addAll(s.bindings.keySet());
		}
		return ret;
	}

	/**
	* Collect a callstack trace.
	**/
	public List<LAtom> trace() {
		List<LAtom> ret = new ArrayList<LAtom>();
		for(int z = scopes.size()-1; z >= 0; z--) {
			if (scopes.get(z).procedure && !Primitives.prim(scopes.get(z).code)) {
				ret.add(getName(scopes.get(z).code));
			}
		}
		return ret;
	}

	/**
	* Peel off all scopes and activation records
	* down to globals. Call this method to restore
	* an Environment to a usable state after encountering
	* a RuntimeError.
	**/
	public void reset() {
		while(scopes.size() > 1) { scopes.pop(); }
		scopes.peek().trace.clear();
		scopes.peek().code = null;
		scopes.peek().index = 0;
		resume();
	}

	/**
	* Pause execution of this program.
	**/
	public void pause() { paused = true; }

	/**
	* Resume execution of this program.
	**/
	public void resume() { paused = false; }
}

class Scope {
	// dynamic value bindings
	final Map<LWord, LAtom> bindings = new HashMap<LWord, LAtom>();	

	// execution context
	final Stack<Func> trace = new Stack<Func>();
	final boolean procedure;
	LList code;
	int index = 0;

	// note: code will be null in the global environment scope.
	Scope(LList code, boolean procedure) {
		this.code      = code;
		this.procedure = procedure;
	}
}

class Func {
	LList code;
	LList args;
	List<LAtom> vals = new ArrayList<LAtom>();

	Func(Environment e, LList code) {
		this.code = code;
		this.args = code.arguments == null ? new LList() : code.arguments;
	}
}