package com.bme.logo;

/**
* A superclass for all Logo exceptions.
*
* @author John Earnest
**/

public class LogoError extends RuntimeException {
	
	static final long serialVersionUID = 1;

	/**
	* Construct a new LogoError with a formatted message.
	*
	* @param message the format string describing the error.
	* @param args    the arguments for the format string.
	**/
	public LogoError(String message, Object... args) {
		super(String.format(message, args));
	}
}