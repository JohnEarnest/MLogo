package com.bme.logo;

/**
* LWords are a wrapper value type representing Logo words.
* Words can generally be thought of as symbol-like atoms, but
* in some contexts are treated more like strings.
* All primitive operators extend this class.
*
* @author John Earnest
**/

public class LWord implements LAtom, Comparable<LWord> {
	static final LWord trueSymbol  = new LWord(Type.Name, "true");
	static final LWord falseSymbol = new LWord(Type.Name, "false");

	/**
	* The context in which an LWord should be interpreted.
	**/
	public enum Type {
		/** The name of a procedure to call.    **/ Call,
		/** The name of a value to dereference. **/ Value,
		/** A name, as a symbol.                **/ Name,
		/** A primitive operator.               **/ Prim
	}

	/** The Type of this word. **/
	public final Type   type ;
	/** The String value of the name of this word. **/
	public final String value;

	/**
	* Construct a new LWord with a given type and value.
	*
	* @param type the Type of the new LWord.
	* @param value the String name of the new LWord.
	**/
	public LWord(Type type, String value) {
		this.type  = type;
		this.value = value;
	}

	public void eval(Environment e) {
		switch(type) {
			case Value: e.value(e.thing(this)); break;
			case Name : e.value(this);          break;
			case Call : e.call(this);           break;
			default   : throw new Error(String.format("Primitive '%s' not implemented!", value));
		}
	}

	public int hashCode() {
		return value.hashCode();
	}

	/**
	* Two words are considered equal if they have the same {@link LWord#value}.
	* This comparison does not consider {@link LWord#type}, unless one of the
	* words is primitive in which case reference equality is used.
	* (primitives are magical singleton thingies)
	**/
	public boolean equals(Object o) {
		if (!(o instanceof LWord)) { return false; }
		LWord other = (LWord)o;
		if (type == Type.Prim || other.type == Type.Prim) {
			return this == other;
		}
		return value.equals(other.value);
	}

	public String toString() {
		switch(type) {
			case Value: return ":"+value;
			case Name : return "'"+value;
			case Prim : return "@"+value;
			default   : return     value;
		}
	}

	public int compareTo(LWord other) {
		return value.compareTo(other.value);
	}

	public int load() { return 1; }
}