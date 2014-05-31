package com.bme.logo;

import java.util.*;

/**
* LList is an list implementation which exposes
* methods equivalent to Logo primitive list operations.
*
* @author John Earnest
**/

public class LList implements LAtom {
	/** if non-null, the argument list of this LList when treated as a procedure. **/
	public final LList arguments;

	/** if a to...end block, a cache of the original source code generating this body. **/
	public String sourceText = "";

	private final List<LAtom> values = new ArrayList<LAtom>();

	/**
	* Construct a new, empty LList.
	**/
	public LList() {
		this.arguments = null;
	}

	LList(LList base, LList arguments) {
		this.values.addAll(base.values);
		this.arguments = arguments;
	}

	LList(LAtom single, LList arguments) {
		this.values.add(single);
		this.arguments = arguments;
	}

	LList(LWord[] names) {
		this();
		for(LWord w : names) {
			this.values.add(w);
		}
	}

	public void eval(Environment e) {
		e.value(this);
	}

	public int hashCode() {
		return values.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof LList)) { return false; }
		return values.equals(((LList)o).values);
	}

	public String toString() {
		StringBuilder ret = new StringBuilder("[");
		for(int z = 0; z < values.size(); z++) {
			if (z > 0) { ret.append(' '); }
			ret.append(values.get(z).toString());
		}
		ret.append("]");
		return ret.toString();
	}

	/**
	* Return the number of items in this list.
	**/
	public int size() {
		return values.size();
	}

	/**
	* Return a given item from this list.
	* Requesting an item outside the list bounds will return an empty list.
	*
	* @param index the zero-indexed position of the object to extract.
	**/
	public LAtom item(int index) {
		if (index < 0 || index >= values.size()) { return new LList(); }
		return values.get(index);
	}
	
	/**
	* Return the first item in this list or an empty list.
	**/
	public LAtom first() {
		if (values.size() < 1) { return new LList(); }
		return values.get(0);
	}

	/**
	* Return the last item in this list or an empty list.
	**/
	public LAtom last() {
		if (values.size() < 1) { return new LList(); }
		return values.get(values.size()-1);
	}

	/**
	* Return a list containing everything in this list except the first item.
	**/
	public LList butFirst() {
		LList ret = new LList();
		for(int z = 1; z < values.size(); z++) {
			ret.values.add(values.get(z));
		}
		return ret;
	}

	/**
	* Return a list containing everything in this list except the last item.
	**/
	public LList butLast() {
		LList ret = new LList();
		for(int z = 0; z < values.size()-1; z++) {
			ret.values.add(values.get(z));
		}
		return ret;		
	}

	/**
	* Return a list composed of a given item followed by the items in this list.
	*
	* @param o the new list's first item.
	**/
	public LList fput(LAtom o) {
		LList ret = new LList();
		ret.values.addAll(values);
		ret.values.add(0, o);
		return ret;
	}

	/**
	* Return a list composed of the items in this list followed by a given item.
	*
	* @param o the new list's last item.
	**/
	public LList lput(LAtom o) {
		LList ret = new LList();
		ret.values.addAll(values);
		ret.values.add(o);
		return ret;
	}

	/**
	* Return a list formed by recursively flattening out any lists contained in this list.
	* For example, flattening the list [1 2[[3]4][[]5]] would produce [1 2 3 4 5].
	**/
	public LList flatten() {
		LList ret = new LList();
		for(LAtom a : values) {
			if (a instanceof LList) {
				ret.values.addAll(((LList)a).flatten().values);
			}
			else {
				ret.values.add(a);
			}
		}
		return ret;
	}

	/**
	* If this list contains a given element, return the sublist starting at that element
	* and containing every following item. Otherwise return the empty list.
	* For example, checking for 'food in [dog food in cans] would return [food in cans]
	* while checking for 'pork in [acceptable kosher foods] would return [].
	*
	* @param o the item to search for.
	**/
	public LList member(LAtom o) {
		for(int z = 0; z < values.size(); z++) {
			if (o.equals(values.get(z))) {
				LList ret = new LList();
				for(; z < values.size(); z++) {
					ret.values.add(values.get(z));
				}
				return ret;
			}
		}
		return new LList();
	}

	/**
	* Return a list containing all the elements of this list followed by
	* all the elements of a given list.
	*
	* @param after the list of elements to concatenate with this list.
	**/
	public LList join(LList after) {
		LList ret = new LList();
		ret.values.addAll(values);
		ret.values.addAll(after.values);
		return ret;
	}

	private int loadFactor = -1;
	public int load() {
		if (loadFactor < 0) {
			loadFactor = 1;
			for(LAtom a : values) {
				loadFactor += a.load();
			}
		}
		return loadFactor;
	}
}