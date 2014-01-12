package com.bme.logo;

/**
* LNumbers are a wrapper value type representing integers.
*
* @author John Earnest
**/

public class LNumber implements LAtom {

	/**
	* The integer value of this LNumber.
	**/
	public final int value;

	/**
	* Construct a new LNumber with a given value.
	*
	* @param n the value of the new LNumber.
	**/
	public LNumber(int n) {
		this.value = n;
	}

	public void eval(Environment e) {
		e.value(this);
	}

	public int hashCode() {
		return value;
	}

	public boolean equals(Object o) {
		if (!(o instanceof LNumber)) { return false; }
		return value == ((LNumber)o).value;
	}

	public String toString() {
		return ""+value;
	}
}