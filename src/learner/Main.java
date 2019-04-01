package learner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Random;

import de.ls5.jlearn.abstractclasses.LearningException;
import de.ls5.jlearn.algorithms.packs.ObservationPack;
import de.ls5.jlearn.equivalenceoracles.RandomWalkEquivalenceOracle;
import de.ls5.jlearn.interfaces.Alphabet;
import de.ls5.jlearn.interfaces.Automaton;
import de.ls5.jlearn.interfaces.EquivalenceOracleOutput;
import de.ls5.jlearn.interfaces.Learner;
import de.ls5.jlearn.interfaces.State;
import de.ls5.jlearn.logging.LearnLog;
import de.ls5.jlearn.logging.LogLevel;
import de.ls5.jlearn.logging.PrintStreamLoggingAppender;
import de.ls5.jlearn.shared.AlphabetImpl;
import de.ls5.jlearn.shared.SymbolImpl;
import de.ls5.jlearn.util.DotUtil;

public class Main {
	private static int maxNumTraces;
	private static int minTraceLength;
	private static int maxTraceLength;
	private static long seed = System.currentTimeMillis();
	private static String seedStr = Long.toString(seed);
	public static PrintStream  stdout= System.out;
	
	public static void main(String[] args) throws FileNotFoundException {
	    // output learned state machine as dot and pdf file :
	    File dotfile = new File("learnresult.dot");
	    File pdffile = new File("learnresult.pdf");
	
	    stdout.println("Start Learning");
	    
	    LearnLog.addAppender(new PrintStreamLoggingAppender(LogLevel.INFO, System.out));
		PrintStream fileStream = new PrintStream(new FileOutputStream("out.txt",false));
		System.setOut(fileStream);
		PrintStream statisticsFileStream = new PrintStream(new FileOutputStream("statistics.txt",false));
			
		//seed = 1299777356020l; 		
		seedStr = Long.toString(seed) + " - Set statically";
	
		handleArgs(args);
	
		System.out.println("Maximum number of traces: " + maxNumTraces);
		System.out.println("Minimum length of traces: " + minTraceLength);
		System.out.println("Maximim length of traces: " + maxTraceLength);
		System.out.println("Seed: " + seedStr);
		Random random = new Random(seed);
			
		EquivalenceOracle mapper = new EquivalenceOracle();
		MembershipOracle memberOracle = new MembershipOracle(mapper);
		mapper.setMembershipOracle(memberOracle);
		de.ls5.jlearn.interfaces.EquivalenceOracle eqOracle;
		
		// building the input alphabet and output alphabets
		Alphabet inputAlphabet = new AlphabetImpl();
		inputAlphabet.addSymbol(new SymbolImpl("I5"));
		inputAlphabet.addSymbol(new SymbolImpl("I10"));
		inputAlphabet.addSymbol(new SymbolImpl("IMARS"));
		inputAlphabet.addSymbol(new SymbolImpl("ISNICKERS"));

		Alphabet outputAlphabet = new AlphabetImpl();
		outputAlphabet.addSymbol(new SymbolImpl("I5"));
		outputAlphabet.addSymbol(new SymbolImpl("I10"));
		outputAlphabet.addSymbol(new SymbolImpl("IMARS"));
		outputAlphabet.addSymbol(new SymbolImpl("ISNICKERS"));
		
		Learner learner = null;
		boolean done = false;
		
		// variables for storing learning statistics
		int hypCounter = 0;
		int refinementCounter = 0;
		int memQueries = 0;
		int totalMemQueries = 0;
		int totalEquivQueries = 0;
		long totalTimeMemQueries = 0;
		long totalTimeEquivQueries = 0;
		long start = System.currentTimeMillis();
		long starttmp = System.currentTimeMillis();
		long endtmp;
	
		while (!done) {
			eqOracle = new RandomWalkEquivalenceOracle(20, 100, 100);
			

			eqOracle.setOracle(mapper);
			((RandomWalkEquivalenceOracle)eqOracle).setRandom(random);
			
			learner = new ObservationPack();
			learner.setOracle(memberOracle);
			learner.setAlphabet(inputAlphabet);
	
			try {
				while (!done) {
					System.out.println("starting learning");
					System.out.println("");
					System.out.flush();
					System.err.flush();
					
					//execute membership queries
					learner.learn();
					System.out.flush();
					System.err.flush();
					System.out.println("done learning");
	
					memQueries = memberOracle.getNumMembQueries();
					statisticsFileStream.println("Membership queries: " + memQueries);
					totalMemQueries += memQueries;
					endtmp = System.currentTimeMillis();
					statisticsFileStream.println("Running time of membership queries: " + (endtmp-starttmp) + "ms.");
					totalTimeMemQueries += endtmp-starttmp;
					starttmp = System.currentTimeMillis();
					System.out.flush();
					
					//stable hypothesis after membership queries
					Automaton hyp = learner.getResult();
					DotUtil.writeDot(hyp, new File("tmp-learnresult" + hypCounter++ + ".dot"));
			
					System.out.println("starting equivalence query");
					System.out.flush();
					System.err.flush();
					//search for counterexample
					EquivalenceOracleOutput o = eqOracle.findCounterExample(hyp);
					System.out.flush();
					System.err.flush();
					System.out.println("done equivalence query");
					statisticsFileStream.println("Membership queries in Equivalence query: " + mapper.getNumEquivQueries());
					totalEquivQueries +=  mapper.getNumEquivQueries();
					endtmp = System.currentTimeMillis();
					statisticsFileStream.println("Running time of equivalence query: " + (endtmp-starttmp) + "ms.");
					totalTimeEquivQueries += endtmp-starttmp;
					starttmp = System.currentTimeMillis();
					
					// no counter example -> learning is done
					if (o == null) {
						done = true;
						continue;
					}
					statisticsFileStream.println("Sending CE to LearnLib.");
					System.out.println("Counter Example: " + o.getCounterExample().toString());
					System.out.flush();
					System.err.flush();
					//return counter example to the learner, so that it can use it to generate new membership queries
					learner.addCounterExample(o.getCounterExample(), o.getOracleOutput());
					System.out.flush();
					System.err.flush();
				}
			} catch (LearningException ex) {
				System.out.println("LearningException ex in Main!");
				ex.printStackTrace();
			} catch (Exception ex) {
				statisticsFileStream.println("Exception!");
				System.out.println("Exception!");
				System.out.println("Seed: " + seedStr);
				System.err.println("Seed: " + seedStr);
				ex.printStackTrace();
				System.exit(-1);
			}
		}
		
		long end = System.currentTimeMillis();
		statisticsFileStream.println("");
		statisticsFileStream.println("");
		statisticsFileStream.println("STATISTICS SUMMARY:");
		statisticsFileStream.println("Total running time: " + (end-start) + "ms.");
		statisticsFileStream.println("Total time Membership queries: " + totalTimeMemQueries);
		statisticsFileStream.println("Total time Equivalence queries: " + totalTimeEquivQueries);
		statisticsFileStream.println("Total abstraction refinements: " + refinementCounter);
		statisticsFileStream.println("Total Membership queries: " + totalMemQueries);
		statisticsFileStream.println("Total Membership queries in Equivalence query: " + totalEquivQueries);
	
	    // final output to out.txt
		System.out.println("Seed: " + seedStr);
		System.err.println("Seed: " + seedStr);
		System.out.println("Done.");
		System.err.println("Successful run.");
	
		// output needed for equivalence checking
		//  - learnresult.dot : learned state machine
		Automaton learnedModel = learner.getResult();
		State startState=learnedModel.getStart();
		
		statisticsFileStream.println("Total states in learned abstract Mealy machine: " + learnedModel.getAllStates().size());
		
		// output learned model with the start state highlighted
	
	    LinkedList<State> highlights=new LinkedList<State>();
		highlights.add(startState);
		BufferedWriter out=null;
		try {
			 out= new BufferedWriter(new FileWriter(dotfile));
	
		    DotUtil.writeDot(learnedModel, out, learnedModel.getAlphabet().size(), highlights, "");
		} catch (IOException ex) {
			 //Logger.getLogger(DotUtil.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			statisticsFileStream.close();
			try {
			  out.close();
			} catch (IOException ex) {
			  //Logger.getLogger(DotUtil.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	
		// write pdf
		DotUtil.invokeDot(dotfile, "pdf", pdffile);	
		
		
		System.err.println("Learner Finished!");
	}

	private static void handleArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("--max-traces".equals(args[i])) {
				if (i == args.length - 1) {
					System.err.println("Missing argument for --max-traces.");
					printUsage();
					System.exit(-1);
				}
				try {
					maxNumTraces = new Integer(args[++i]);
				} catch (NumberFormatException ex) {
					System.err.println("Error parsing argument for --max-traces. Must be integer. " + args[i]);
					System.exit(-1);
				}
			}
			if ("--min-trace-length".equals(args[i])) {
				if (i == args.length - 1) {
					System.err.println("Missing argument for --min-trace-length.");
					printUsage();
					System.exit(-1);
				}
				try {
					minTraceLength = new Integer(args[++i]);
				} catch (NumberFormatException ex) {
					System.err.println("Error parsing argument for --min-trace-length. Must be integer. " + args[i]);
					System.exit(-1);
				}
			}
			if ("--max-trace-length".equals(args[i])) {
				if (i == args.length - 1) {
					System.err.println("Missing argument for --max-trace-length.");
					printUsage();
					System.exit(-1);
				}
				try {
					maxTraceLength = new Integer(args[++i]);
				} catch (NumberFormatException ex) {
					System.err.println("Error parsing argument for --max-trace-length. Must be integer. " + args[i]);
					System.exit(-1);
				}
			}
			if ("--seed".equals(args[i])) {
				if (i == args.length - 1) {
					System.err.println("Missing argument for --min-trace-length.");
					printUsage();
					System.exit(-1);
				}
				try {
					seed = new Integer(args[++i]);
					seedStr = Long.toString(seed);
				} catch (NumberFormatException ex) {
					System.err.println("Error parsing argument for --seed. Must be integer. " + args[i]);
					System.exit(-1);
				}
			}
		}
	}

	public static void printUsage() {
		System.out.println(" --max-traces       - Maximum number of traces to run during equivalence testing.");
		System.out.println(" --min-trace-length - Minimum length of traces during equivalence query.");
		System.out.println(" --max-trace-length - Maximum length of traces during equivalence query.");
		System.out.println(" --seed             - Seed to use for random number generator.");
	}
}
