package learner;

import java.util.List;
import java.util.Random;

import de.ls5.jlearn.abstractclasses.LearningException;
import de.ls5.jlearn.equivalenceoracles.EquivalenceOracleOutputImpl;
import de.ls5.jlearn.interfaces.Automaton;
import de.ls5.jlearn.interfaces.EquivalenceOracle;
import de.ls5.jlearn.interfaces.EquivalenceOracleOutput;
import de.ls5.jlearn.interfaces.Oracle;
import de.ls5.jlearn.interfaces.State;
import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.interfaces.Word;
import de.ls5.jlearn.shared.WordImpl;

/**
 * A basic equivalence oracle implementation. 
 */
// This is the class you should adapt in order to learn VendingMachine3.0.
public class BasicEquivalenceOracle implements EquivalenceOracle {

	/**
	 * The oracle by which the EquivalenceOracle interracts with the systems.
	 */
	private Oracle oracle;

	/**
	 * Used random symbol generation.
	 */
	private Random rand = new Random(0);
	
	

	// You will definitely want to play around with the fields bellow
	/**
	 * Probability of reseting the system
	 */
	private double probReset = 0.1;

	/**
	 * The maximum length of a trace in terms of symbols
	 */
	private int maxTraceLength = 10;

	/**
	 * The maximum number of queries to run.
	 */
	private int maxQueries = 100;

	public BasicEquivalenceOracle() {
	}

	/**
	 * Searches for a counterexample to the hypothesis hyp. Returns the
	 * counterexample if it finds it, otherwise returns null.
	 */
	// You should leave this method unchanged
	@Override
	public EquivalenceOracleOutput findCounterExample(Automaton hyp) {
		EquivalenceOracleOutputImpl equivOracleOutput = null;
		for (int i = 0; i < maxQueries; i++) {
			Word trace = generateTrace(hyp);
			Word hypOutput = hyp.getTraceOutput(trace);
			Word sutOutput;
			try {
				sutOutput = oracle.processQuery(trace);
				if (!hypOutput.equals(sutOutput)) {
					System.err.println("Found counterexample \n" +
							"for trace: " + trace + "\n" +
							"expected: " + sutOutput + "\n" +
							"received: " + hypOutput);
					equivOracleOutput = new EquivalenceOracleOutputImpl();
					equivOracleOutput.setCounterExample(trace);
					equivOracleOutput.setOracleOutput(sutOutput);
					break;
				}
			} catch (LearningException e) {
				e.printStackTrace();
				System.err.println("Error executing the test query: " + trace);
				System.exit(0);
			}
		}
		System.err.println("Could not find any counterexamples");
		return equivOracleOutput;
	}

	/*
	 * Generates a trace of inputs given a hypothesis.
	 * 
	 * THIS IS THE METHOD YOU SHOULD EDIT !!!
	 * 
	 */
	private Word generateTrace(Automaton hyp) {
		List<Symbol> listOfSymbols = hyp.getAlphabet().getSymbolList();
		Word trace = new WordImpl();
		for (int i = 0; i < maxTraceLength; i++) {
			int selectedSymbolIndex = rand.nextInt(listOfSymbols.size());
			Symbol selectedSymbol = listOfSymbols.get(selectedSymbolIndex);
			trace.addSymbol(selectedSymbol);

			// generate a (pseudo-)random value between 0.0 and 1.0
			// if it is less than probReset, break
			if (rand.nextDouble() < probReset) {
				break;
			}
		}
		
		return trace;
	}
	
	/*
	 * Returns a modifiable access sequence to a randomly selected state in the hypothesis.
	 * HINT: this should be useful in improving the testing algorithm.
	 */
	private Word getModifiableAccessSequence(Automaton hyp) {
		int stateIndex = rand.nextInt(hyp.getAllStates().size());
		State state = hyp.getAllStates().get(stateIndex);
		Word accseq = hyp.getTraceToState(state);
		Word trace = new WordImpl();
		for (Symbol sym : accseq.getSymbolList()) {
	    	trace.addSymbol(sym);
	    }
		
		return trace;
	} 

	/**
	 * Sets the oracle used to execute queries/run tests on the system.
	 */
	@Override
	public void setOracle(Oracle oracle) {
		this.oracle = oracle;

	}

}
