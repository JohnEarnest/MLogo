package com.bme.logo;

import java.util.*;

/**
* A Tracer can be registered with an Environment
* and will be fed events as programs are executed,
* allowing for the creation of debuggers, logs
* or visualizations of execution.
*
* @author John Earnest
**/

public abstract class Tracer {

	/**
	* Called when a program is initialized through
	* {@link com.bme.logo.Interpreter#run} or {@link com.bme.logo.Interpreter#init}.
	**/
	public void begin() {}

	/**
	* Called immediately before every execution tick of the program.
	**/
	public void tick() {}

	/**
	* Called when the program completes successfully.
	* This method will not be fired if the program halts due to an exception.
	**/
	public void end() {}

	/**
	* Called immediately before a primitive procedure is executed.
	*
	* @param name the name of the primitive procedure.
	* @param args the arguments (if any) supplied to the primitive.
	**/
	public void callPrimitive(String name, Map<LAtom, LAtom> args) {}

	/**
	* Called immediately before a non-primitive procedure is executed.
	*
	* @param name the name of the procedure (or null if none).
	* @param args the arguments (if any) supplied to the procedure.
	* @param tail true if this procedure call is recognized as tail-recursive.
	**/
	public void call(String name, Map<LAtom, LAtom> args, boolean tail) {}

	/**
	* Called immediately before returning a value from a synthetic procedure.
	*
	* @param name the name of the procedure.
	* @param val the return value of the procedure.
	* @param implicit true if this procedure is implicitly returning a value, rather than an explicit 'output'.
	**/
	public void output(String name, LAtom val, boolean implicit) {}

	/**
	* Called immediately before returning from a synthetic void procedure.
	*
	* @param name the name of the procedure.
	* @param implicit true if this procedure is implicitly returning, rather than an explicit 'stop'.
	**/
	public void stop(String name, boolean implicit) {}
}
