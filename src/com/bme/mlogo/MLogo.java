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

		for(int z = args.size() - 1; z >= 0; z--) {
			if ("-h".equals(args.get(z))) { printHelp   = true; args.remove(z); }
			if ("-i".equals(args.get(z))) { interactive = true; args.remove(z); }
		}

		if (printHelp) {
			System.out.println(version);
			System.out.println("usage: MLogo [-hi] file ...");
			System.out.println();
			System.out.println(" h : print this help message");
			System.out.println(" i : provide an interactive REPL session");
			System.out.println();
		}

		Environment e = kernel();
		primitiveIO(e);
		
		for(String fileName : args) { runFile(e, fileName); }
		
		if (interactive) {
			System.out.println(version);
			System.out.println("type 'exit' to quit.");
			System.out.println();
			Scanner in = new Scanner(System.in);
			TurtleGraphics t = new TurtleGraphics(e);
			while(true) {
				System.out.print(">");
				String line = in.nextLine();
				if ("exit".equals(line)) { break; }
				while(!Parser.complete(line)) {
					System.out.print(">>");
					line += "\n" + in.nextLine();
				}
				runString(e, line, t);
			}
			System.exit(0);
		}
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
		catch(SyntaxError e) {
			System.out.format("syntax error: %s%n", e.getMessage());
			System.out.format("\t%s%n\t", e.line);
			for(int z = 0; z < e.lineIndex; z++) {
				System.out.print(e.line.charAt(z) == '\t' ? '\t' : ' ');
			}
			System.out.println("^");
			env.reset();
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

	private static void runFile(Environment env, String filename) {
		try {
			LList code = Parser.parse(loadFile(filename));
			Interpreter.run(code, env);
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

	private static void primitiveIO(Environment e) {
		final LWord a = new LWord(LWord.Type.Name, "argument1");
		final Scanner in = new Scanner(System.in);

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