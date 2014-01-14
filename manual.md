MLogo Programmer's Manual
=========================

MLogo is a dialect of the Logo programming language. It contains a very small kernel of primitive procedures and simple, uniform semantics. Logo supports imperative and functional styles of programming, provides higher-order procedures and although the kernel vocabulary does not explicitly contain routines to aid it, metaprogramming and dynamic code generation is quite possible. Logo closely resembles the Lisp programming language except it uses something closer to M-Expression syntax and is dynamically scoped, rather than lexically scoped.

Datatypes
---------

All data in Logo is composed of Atoms. An Atom can be a Number, List or Word. Words themselves can exist in three syntactic forms which each have a different semantic effect.

Numbers are the simplest of the Logo datatypes. They are represented as 32-bit signed integers.

Lists are recursively composed of any (potentially mixed) Atomic types. A List literal is encosed within square brackets. (`[]`) Lists represent both code and data, and the logo program itself is an (implied) List of Words and other literals. As described below, a List which represents code may have an argument list associated with them- the `bind` primitive is used for attaching an argument list to an ordinary List, and the `args` primitive is used for retrieving the argument list, if any, from such a list. Here are a few examples of lists:

	[]
	[1 2 3]
	[A [B C][D [E]] F]

Words come in three forms. If a Word is prefixed with a single quote (`'`), it is a Name and is simply an item which can be passed as an argument to procedures. If a Word is prefixed with a colon (`:`), it is a Value and encountering it in the program will look up the value associated with the word. If a word has no prefix, encountering it in the program will invoke a procedure of that name and consume arguments as necessary. Apart from prefixes and these three forms, Words can contain any upper or lower case alphabetic characters or any of the symbols `.,!?`. Here are a few examples of Words:

	foobar
	MASHED.TATERS
	'mimsy
	:x

(Notes: Many other Logo implementations use a `"` prefix instead of a `'` for name literals. The single quote is used here both to be more consistent with the syntax of Lisp and Forth with respect to "quoting" a name, and also to free the double quote character for use as a string delimiter in the future.)

Control Flow
------------

Logo follows a simple, uniform evaluation rule. A program is a List of Calls and other literal values. The Logo interpreter steps through elements of these lists one at a time, evaluating Calls. When a Call invokes a procedure which consumes one or more arguments, they are taken from the elements to the right of said Call, which may in turn result in evaluating further Calls. This has the effect of behaving like prefix-notation (as in Lisp) and many expressions read similarly to how they would in English. Here is an example of an expression:

	print sum product 2 9 difference 2 1

Parentheses can be used (optionally) to add clarity to expressions. The following is equivalent to the previous example:

	print (sum (product 2 9) (difference 2 1))

A few primitives modify the operation of the interpreter as described above. The primitive `run` takes as its argunment a List, which it will evaluate. The primitives `if` and `unless` each consume a boolean value followed by a List, and evaluate their list if said boolean is true or false, respectively. For the purposes of these words, the empty List `[]`, the Number zero or the Word `'false` are considered false and any other value is considered true. The primitives `stop` and `output` halt the surrounding procedure and, in the case of `output`, return a result. The `repeat` primitive consumes a Number followed by a List and evaluates the List that many times.

When an indeterminate or infinite number of iterations are desired for a loop, a procedure may invoke itself recursively. Logo will perform tail-call optimization (TCO) on direct tail-recursive calls which return a result or are the final statement in a procedure. Consider two ways of writing a simple counting loop:

	to one :x
		print x
		if equal? :x 1 [stop]
		one difference :x 1
	end

	to two :x
		repeat :x [
			print :x
			make 'x difference :x 1
		]
	end

Scoping
-------

Every time the Logo interpreter begins executing a List, as in evaluating a Call or the primitives `run`, `if`, `unless`, or `repeat`, it creates a new scope linked to any prior ones. When a List has been exhausted or the interpreter encounters the primitives `stop` or `output` it discards this new scope, returning to a previous one.

Within a scope, the primitive `local` consumes a Name followed by any expression and creates a new association for this name. If a Value is then later encountered it will evaluate the Atom associated with the Name- if no binding exists in the current scope, progressively deeper scopes will be examined and the first to contain a binding for the Name will be used. To perform multiple indirection the primitive `thing` can take any name and dereference it to get the Atom associated with it. Finally, `make` works the same as `local`, except instead of always creating a new binding it will modify the first binding it finds, creating a new binding only if it searches to the outermost scope without finding one.

For Your Convenience
--------------------

Procedures are defined by using `to` syntax. The keyword `to` is followed by a procedure name, any arguments (each with a colon prefix, as if they were Values) and a newline. The body of the procedure extends until an `end` is encountered. Definitions may be nested.

To-blocks are syntactic sugar for a combination of `local` and `bind`. The following definitions are semantically identical:

	to any :list
		output item random size :list :list
	end

	local 'any bind ['list][output item random size :list :list]

The `to` form is simply syntactic sugar which makes programs slightly more convenient and readable.

Logo allows a small set of arithmetic operators (`+-*/%<>=`) to be used in infix expressions. These are parsed according to normal arithmetic precedence rules. Infix expressions should always be separated from prefix expressions with parentheses to avoid ambiguity. The following two expressions are equivalent:

	print sum 1 product 3 5
	print (1 + 3 * 5)

Finally, the hash character `#` may be used anywhere as a line-comment.

Numeric Primitives
------------------

- `sum (number, number -> number)`: adds together two operands.
- `difference (number, number -> number)`: subtracts the second operand from the first.
- `product (number, number -> number)`: multiplies together two operands.
- `quotient (number, number -> number)`: calculates the whole part of dividing operands.
- `remainder (number, number -> number)`: calculates the leftover part of dividing operands.
- `negate (number -> number)`: multiplies a number by -1.
- `random (number -> number)`: returns a random number between 0 and the argument, exclusive.
- `less? (number, number -> word)`: returns `'true` if the first operand is less than the second.
- `greater? (number, number -> word)`: returns `'true` if the first operand is greater than the second.
- `equal? (atom, atom -> word)`: returns `'true` if both operands are equal. Performs a recursive comparison of Lists and considers any of the three forms of Word equivalent if they have the same textual value.

List Manipulation Primitives
----------------------------

Logo also has a number of primitives for manipulating Lists. All operations return new Lists and none alter the source list in any way.

- `size (list -> number)`: returns the number of elements in a given List.
- `item (number, list -> atom)`: returns a 0-indexed element of a List or `[]` if the index is out of bounds.
- `first (list -> atom)`: returns the first element of a List or `[]` if the List is empty.
- `last (list -> atom)`: returns the last element of a List or `[]` if the List is empty.
- `butfirst (list -> list)`: returns everything except the first element of a List.
- `butlast (list -> list)`: returns everything except the first element of a List.
- `fput (atom, list -> list)`: return the list with an atom added to the front.
- `lput (atom, list -> list)`: return the list with an atom added to the end.
- `join (list, list -> list)`: concatenate together two lists in sequence.
- `flatten (list -> list)`: recursively combine the elmements of all List inside a List into a single List. For example, flattening the list `[1 2[[3]4][[]5]]` would produce `[1 2 3 4 5]`.
- `member (atom, list -> list)`: if this list contains a given element, return the sublist starting at that element and containing every following item. Otherwise return the empty list. For example, checking for `'food` in `[dog food in cans]` would return `[food in cans]` while checking for `'pork` in `[acceptable kosher foods]` would return `[]`.

Type Primitives
---------------

Words may be converted into one of the other forms (as in metaprogramming) by using the words `asvalue`, `asname` or `ascall`.

The type of an Atom may be checked by using the primitives `word?`, `list?` or `num?`, which return `'true` if the supplied Atom is a Word, List or Number, respectively.

IO Primitives
-------------

The MLogo shell add some basic console IO routines to the kernel:

- `print (atom)`: print the value of an expression to the terminal.
- `println`: print a newline to the terminal.
- `readlist ( -> list)`: read a line from the terminal and parse it into a List.

The MLogo shell additionally provides primitives for manipulating a Turtle, a cursor which can move around a display and draw lines behind it. Use `showturtle` to make this display visible, or start drawing with any of the below primitives, and `hideturtle` to dismiss the display.

- `forward (number)`: move the turtle forward by some number of pixels.
- `back (number)`: move the turtle backward by some number of pixels.
- `right (number)`: turn the turtle right some number of degrees.
- `left (number)`: turn the turtle left some number of degrees.
- `clear`: erase the turtle display.
- `home`: move the turtle to its starting position and angle.
- `penup`: lift the turtle's pen so that it no longer draws.
- `pendown`: resume drawing with the turtle's pen.
- `setcolor (number, number, number)`: set the pen's color in red, green and blue values ranging 0-255.