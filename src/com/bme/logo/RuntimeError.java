package com.bme.logo;

import java.util.*;

/**
* An Exception representing a run-time error in a Logo program.
*
* @author John Earnest
**/

public class RuntimeError extends LogoError {
	
	static final long serialVersionUID = 1;

	public final List<LAtom> trace;

	/**
	* Construct a new RuntimeError with a formatted message.
	*
	* @param e       the environment of the current program.
	* @param message the format string describing the error.
	* @param args    the arguments for the format string.
	**/
	public RuntimeError(Environment e, String message, Object... args) {
		super(String.format(message, args));
		this.trace = e.trace();
	}
}