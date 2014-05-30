package com.bme.logo;

import java.util.*;

/**
* An Exception representing a run-time error in a Logo program.
*
* @author John Earnest
**/

public class RuntimeError extends LogoError {
	
	static final long serialVersionUID = 1;

	/** The class of runtime error which produced this exception. **/
	public final Type type;

	/** The arguments related to this runtime error type. **/
	public final Object[] args;

	/** A list of the wordnames in the stack trace which lead to this exception. **/
	public final List<LAtom> trace;

	/**
	* Construct a new RuntimeError with a formatted message.
	*
	* @param e         the environment of the current program.
	* @param errorType the class of error which produced this exception.
	* @param args      the arguments for this error class.
	**/
	RuntimeError(Environment e, Type errorType, Object... args) {
		super(String.format(errorType.format, args));
		this.type  = errorType;
		this.args  = args;
		this.trace = e.trace();
	}

	public static enum Type {
		StackOverflow     ("Stack overflow!"),
		UnusedValue       ("I don't know what to do with '%s'!"),
		MutatePrimitive   ("The word '%s' is primitive and cannot be reassigned."),
		UndefinedName     ("'%s' has no value!"),
		UndefinedProcedure("I don't know how to '%s'!"),
		NotEnoughArguments("Not enough arguments for '%s'!"),
		OutsideProcedure  ("I can't %s; I'm not running a procedure!"),
		DivideByZero      ("I cannot divide by zero."),
		TypeMismatch      ("'%s' is not a %s!"),
		OutOfMemory       ("Ran out of memory.");

		public final String format;
		private Type(String format) { this.format = format; }
	}
}