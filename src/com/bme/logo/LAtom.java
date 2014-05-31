package com.bme.logo;

/**
* Every Logo object is an LAtom.
* This interface provides a common contract
* shared between all datatypes.
* LAtoms should be immutable value types.
*
* @author John Earnest
**/

public interface LAtom {

	/**
	* Evaluate this object with respect
	* to the current environment.
	* If doing so in a primitive procedure would return a result,
	* call the {@link com.bme.logo.Environment#output} method.
	* 
	* @param e the Environment used as a context for this object.
	**/
	public void eval(Environment e);

	/**
	* Determine the loading factor of this LAtom.
	* Used for evaluating memory quotas.
	* Since LAtoms should be immutable data structures
	* the results of this calculation can be cached.
	**/
	public int load();
}