package com.bme.mlogo;

import com.bme.logo.*;
import java.io.*;
import java.util.*;
import static com.bme.logo.Primitives.*;

public class MLogo {
	static final String version = "MLogo 0.1";

	public static void main(String[] a) {
		List<String> args = new ArrayList<String>(Arrays.asList(a));

		boolean printHelp   = args.size() == 0;
		boolean interactive = false;
		boolean turtles     = false;
		boolean trace       = false;

		for(int z = args.size() - 1; z >= 0; z--) {
			if ("-h".equals(args.get(z))) { printHelp   = true; args.remove(z--); continue; }
			if ("-i".equals(args.get(z))) { interactive = true; args.remove(z--); continue; }
			if ("-t".equals(args.get(z))) { turtles     = true; args.remove(z--); continue; }
			if ("-T".equals(args.get(z))) { trace       = true; args.remove(z--); continue; }
		}

		if (printHelp) {
			System.out.println(version);
			System.out.println("usage: MLogo [-hit] file ...");
			System.out.println();
			System.out.println(" h : print this help message");
			System.out.println(" i : provide an interactive REPL session");
			System.out.println(" t : enable turtle graphics during batch mode");
			System.out.println(" T : enable execution trace");
			System.out.println();
		}

		Environment e = kernel();
		primitiveIO(e, trace);

		// the repl always loads turtle graphics primitives,
		// but they're strictly opt-in for batch mode.
		if (turtles) {
			TurtleGraphics t = new TurtleGraphics(e);
			for(String fileName : args) { runFile(e, fileName, t); }
			if (interactive) { repl(e, t); }
			else { System.exit(0); }
		}
		else {
			for(String fileName : args) { runFile(e, fileName, null); }
			if (interactive) {
				TurtleGraphics t = new TurtleGraphics(e);
				repl(e, t);
			}
		}
	}

	private static void repl(Environment env, TurtleGraphics t) {
		System.out.println(version);
		System.out.println("type 'exit' to quit.");
		System.out.println();
		Scanner in = new Scanner(System.in);

		while(true) {
			System.out.print(">");
			try {
				String line = in.nextLine();
				if ("exit".equals(line)) { break; }
				while(Parser.complete(line).size() > 0) {
					System.out.print(">>");
					line += "\n" + in.nextLine();
				}
				runString(env, line, t);
			}
			catch(SyntaxError e) {
				System.out.format("syntax error: %s%n", e.getMessage());
				System.out.format("\t%s%n\t", e.line);
				for(int z = 0; z < e.lineIndex; z++) {
					System.out.print(e.line.charAt(z) == '\t' ? '\t' : ' ');
				}
				System.out.println("^");
				env.reset();
			}
		}
		System.exit(0);
	}

	private static void runString(Environment env, String sourceText, TurtleGraphics t) {
		try {
			LList code = Parser.parse(sourceText);
			Interpreter.init(code, env);
			while(true) {
				// execute until the interpreter is paused
				if (!Interpreter.runUntil(env)) { return; }
				
				// update the display until animation is complete
				while(!t.update()) {
					try { Thread.sleep(1000 / 30); }
					catch(InterruptedException e) {}
				}
			}
		}
		catch(RuntimeError e) {
			System.out.format("runtime error: %s%n", e.getMessage());
			//e.printStackTrace();
			for(LAtom atom : e.trace) {
				System.out.format("\tin %s%n", atom);
			}
			env.reset();
		}
	}

	private static void runFile(Environment env, String filename, TurtleGraphics t) {
		try {
			LList code = Parser.parse(loadFile(filename));
			if (t == null) {
				Interpreter.run(code, env);
				return;
			}
			Interpreter.init(code, env);
			while(true) {
				// execute until the interpreter is paused
				if (!Interpreter.runUntil(env)) { return; }
				
				// update the display until animation is complete
				while(!t.update()) {
					try { Thread.sleep(1000 / 30); }
					catch(InterruptedException e) {}
				}
			}
		}
		catch(SyntaxError e) {
			System.out.format("%d: syntax error: %s%n", e.lineNumber, e.getMessage());
			System.out.format("\t%s%n\t", e.line);
			for(int z = 0; z < e.lineIndex; z++) {
				System.out.print(e.line.charAt(z) == '\t' ? '\t' : ' ');
			}
			System.out.println("^");
			System.exit(1);
		}
		catch(RuntimeError e) {
			System.out.format("runtime error: %s%n", e.getMessage());
			for(LAtom atom : e.trace) {
				System.out.format("\tin %s%n", atom);
			}
			System.exit(1);
		}
	}

	private static String loadFile(String filename) {
		try {
			Scanner in = new Scanner(new File(filename));
			StringBuilder ret = new StringBuilder();
			while(in.hasNextLine()) {
				// this will conveniently convert platform-specific
				// newlines into an internal unix-style convention:
				ret.append(in.nextLine()+"\n");
			}
			// shave off the trailing newline we just inserted:
			ret.deleteCharAt(ret.length()-1);
			return ret.toString();
		}
		catch(IOException e) {
			System.err.format("Unable to load file '%s'.%n", filename);
			System.exit(1);
			return null;
		}
	}

	private static void primitiveIO(Environment e, boolean trace) {
		final LWord a = new LWord(LWord.Type.Name, "argument1");
		final Scanner in = new Scanner(System.in);

		if (trace) {
			e.addTracer(new Tracer() {
				public void begin()  { System.out.println("tracer: begin."); }
				public void end()    { System.out.println("tracer: end.");   }
				//public void tick() { System.out.println("tracer: tick.");  }

				public void callPrimitive(String name, Map<LAtom, LAtom> args) {
					System.out.format("trace: PRIM %s%s%n",
						name,
						args.size() > 0 ? " " + args : ""
					);
				}
				public void call(String name, Map<LAtom, LAtom> args, boolean tail) {
					System.out.format("trace: CALL %s%s%s%n",
						name,
						args.size() > 0 ? " " + args : "",
						tail ? " (tail)" : ""
					);
				}
				public void output(String name, LAtom val, boolean implicit) {
					System.out.format("trace: RETURN %s- %s%s%n", name, val, implicit ? " (implicit)" : "");
				}
				public void stop(String name, boolean implicit) {
					System.out.format("trace: STOP %s%s%n", name, implicit ? " (implicit)" : "");
				}
				public void define(String name) {
					System.out.format("trace: DEFINE %s%n", name);
				}
			});
		}

		e.bind(new LWord(LWord.Type.Prim, "version") {
			public void eval(Environment e) {
				System.out.println(MLogo.version);
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "words") {
			public void eval(Environment e) {
				List<LWord> words = new ArrayList<LWord>(e.words());
				Collections.sort(words);
				for(LWord word : words) { System.out.print(word + " "); }
				System.out.println();
				System.out.println();
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "erase") {
			public void eval(Environment e) {
				LWord key = word(e, a);
				// dereference the name to ensure that
				// it originally had a binding.
				// we don't care what it was.
				e.thing(key);
				e.erase(key);
			}
		}, a);

		e.bind(new LWord(LWord.Type.Prim, "trace") {
			public void eval(Environment e) {
				System.out.println("trace: ");
				for(LAtom s : e.trace()) {
					System.out.println("\t" + s);
				}
				System.out.println();
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "print") {
			public void eval(Environment e) {
				System.out.println(e.thing(a));
			}
		}, a);

		e.bind(new LWord(LWord.Type.Prim, "println") {
			public void eval(Environment e) {
				System.out.println();
			}
		});

		e.bind(new LWord(LWord.Type.Prim, "readlist") {
			public void eval(Environment e) {
				e.output(Parser.parse(in.nextLine()));
			}
		});
	}
}