MLogo
=====

MLogo is an implementation of the Logo programming language which can be used as a scripting language within Java programs. Its primary design goals are to be small, easily understood and robust. Since Logo has no official standard, the language presented here is closely modeled after Apple 2 Logo.

![Turtle graphics demonstration](http://i.imgur.com/VyqKHEX.gif)

This repository includes both the Logo engine and an example application using it which provides an interactive Logo shell which adds console IO and Turtle Graphics. A suite of test programs which run against this shell program is also provided. The public interfaces of the Logo have JavaDoc documentation. A manual for the language itself can be found [Here](https://github.com/JohnEarnest/MLogo/blob/master/manual.md).

Compiling and Running
=====================

Building MLogo requires the [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Apache Ant](http://ant.apache.org/manual/install.html). The automated test script for the interpreter is written in bash.

Compiling using the included Ant script is as simple as moving to the project directory and running:

		ant

You can additionally build JavaDoc documentation by running the task:

		ant doc

Running the automated test script (will also compile MLogo):

		./test.sh

Running an interactive session with the Logo shell:

		java -jar dist/MLogo.jar -i

Try loading the shell and entering a simple program:

		to any :list
			output item random size :list :list
		end
		repeat 10 [print any [A B C]]

The command `exit` will leave the shell and `words` will list all current word definitions.

Using MLogo to Script Your Applications
=======================================

Using the Logo engine in your own applications will require you to import the `com.bme.logo` package and ensure that `Logo.jar` is within your classpath.

All the state of the MLogo interpreter is captured in instances of a class called `Environment`. The `Primitives.kernel()` method will produce one with all the Logo primitives pre-loaded- without these primitives your programs won't be able to accomplish much. You'll also need a program to execute- the `Parser.parse()` method can convert a string of source code into an executable Logo data structure. Finally, the `Interpreter` class exposes methods for running a program with respect to a given `Environment`:

		Environment env = Primitives.kernel();
		LList program = Parser.parse("repeat 4 [forward 100 right 90]");
		Interpreter.run(LList code, Environment e);

If your program is malformed, `Parser` may throw a `SyntaxError`. If your program encounters an error at runtime, such as a mismatch in expected argument types, it may throw a `RuntimeError`. Each exception type exposes useful methods for pinpointing the source of the problem.

To allow your Logo code to influence your Java application, you will want to install new primitive procedures in your environment. The `Environment.bind()` method allows you to do so with a subclass of `LWord` and one or more argument names as inputs. Within the `eval()` method of your primitive class you may use various methods within `Environment` to look up argument values and `Environment.output()` to return a result if desired. `Primitives` also contains some useful helper methods.

		Environment env = ...
		final LWord arg = new LWord(LWord.Type.Name, "argument1");

		env.bind(new LWord(LWord.Type.Prim, "print") {
			public void eval(Environment e) {
				System.out.println(e.thing(arg));
			}
		}, arg);
		env.bind(new LWord(LWord.Type.Prim, "readlist") {
			public void eval(Environment e) {
				e.output(Parser.parse(in.nextLine()));
			}
		});

Please refer to the JavaDoc documentation for more details.