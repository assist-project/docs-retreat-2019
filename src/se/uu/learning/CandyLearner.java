package se.uu.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.cache.Caches;
import de.learnlib.eqtests.basic.CompleteExplorationEQOracle;
import de.learnlib.eqtests.basic.RandomWordsEQOracle;
import de.learnlib.eqtests.basic.WMethodEQOracle.MealyWMethodEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticSUL;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

/**
 * Project built on an assignment given at Radboud University, as part of the Testing Techniques
 * course. 
 */

public class CandyLearner {

	public static int  sutInterface_portNumber=7892;
	
	public static String OUTPUT_DIR = "output";
	
	public static String [] inputAlphabet = {
		 "IREFUND",
//		 "IBUTTONMARS",
		 "IBUTTONSNICKERS",
//		 "IBUTTONBOUNTY",
		 "ICOIN10",
		 "ICOIN5"
	};
	

	    public static void main(String[] args) throws NoSuchMethodException, IOException {

	    	// create alphabet
	        Alphabet<String> inputs = new SimpleAlphabet<>();
	        for (String input : inputAlphabet) {
	        	inputs.add(input);
	        }
	        
	        // Instantiate the sul
	        SUL<String,String> sul = new SulAdapter(sutInterface_portNumber);
	        
	        // oracle for counting queries wraps sul
	        StatisticSUL<String, String> statisticSul = new ResetCounterSUL<>("membership queries", sul);
	        
	        SUL<String,String> effectiveSul = statisticSul;
	        // use caching in order to avoid duplicate queries
	        effectiveSul = Caches.createSULCache(inputs, effectiveSul);
	        
	        SULOracle<String, String> mqOracle = new SULOracle<>(effectiveSul);

	        // create initial set of suffixes
	        List<Word<String>> suffixes = new ArrayList<>();
	        for (String input : inputAlphabet) {
	        	suffixes.add(Word.fromSymbols(input));
	        }
	       
	        // construct L* instance (almost classic Mealy version)
	        // almost: we use words (Word<String>) in cells of the table 
	        // instead of single outputs.
	        MealyLearner<String,String> lstar =
	                new ExtensibleLStarMealy<>(
	                inputs, // input alphabet
	                mqOracle, // mq oracle
	                suffixes, // initial suffixes
	                ObservationTableCEXHandlers.FIND_LINEAR_ALLSUFFIXES, // handling of counterexamples
	                ClosingStrategies.CLOSE_SHORTEST // choose row for closing the table
	                );

	        // create random words equivalence test
	       RandomWordsEQOracle<String, Word<String>, MealyMachine<?,String,?,String>> randomWords =
	        		new RandomWordsEQOracle<String, Word<String>, MealyMachine<?,String,?,String>>(
	        				mqOracle, 
	        				3, // int minLength
	        				8,  // int maxLength
	        				1000,  // int maxTests
	        				new Random(46346293) // make results reproducible
	        				);
	        
	        // create complete exploration equivalence test
	        CompleteExplorationEQOracle<String, Word<String>> completeOracle =
	        		new CompleteExplorationEQOracle<>(
	        				mqOracle, // a membership oracle
	        				5, // int minDepth
	        				7  // int maxDepth
	        				); 
	        
	     // create equivalence oracle based on the W method
	        MealyWMethodEQOracle<String, String> wOracle=
	        		new MealyWMethodEQOracle<>(
	        				5, //int maxDepth
	        				mqOracle  // a membership oracle
	        				); 
	        			

	        // construct a learning experiment from
	        // the learning algorithm and one of the equivalence oracles.
	        // The experiment will execute the main loop of
	        // active learning
	        MealyExperiment<String,String> experiment =
	                new MealyExperiment<>(
	                		lstar, 
	                		randomWords, // equivalence oracle, choose among [randomWords | completeOracle | wOracle] **remember to change their settings**
	                		inputs // input alphabet
	                		);

	        // turn off time profiling
	        experiment.setProfile(true);

	        // enable logging of models
	        experiment.setLogModels(true);

	        // run experiment
	        experiment.run();

	        // get learned model
	        MealyMachine<?, String, ?, String> result = experiment.getFinalHypothesis();

	        // report results
	        System.out.println("-------------------------------------------------------");

	        // profiling
	        System.out.println(SimpleProfiler.getResults());

	        // learning statistics
	        System.out.println(experiment.getRounds().getSummary());
	        System.out.println(statisticSul.getStatisticalData().getSummary());

	        // model statistics
	        System.out.println("States: " + result.size());
	        System.out.println("Sigma: " + inputs.size());

	        // show model
	        System.out.println();
	        System.out.println("Model: ");
	        
	        GraphDOT.write(result, inputs, System.out); // may throw IOException!
//	        Writer w = DOT.createDotWriter(true);
//	        GraphDOT.write(result, inputs, w);
//	        w.close();
	        
	        // create output directory
	        File outputDir = new File(OUTPUT_DIR);
	        if (!outputDir.exists()) {
	        	outputDir.mkdirs();
	        }
	        
	        String learnedModel = OUTPUT_DIR + File.separator + "CandyMachine.dot";
	        PrintStream writer = new PrintStream(
	       	     new FileOutputStream(learnedModel)); 
	        GraphDOT.write(result, inputs, writer); // may throw IOException!
	        
	        System.out.println(executeCommand("dot -Tpdf " + learnedModel + " -o " + OUTPUT_DIR + File.separator  +"CandyMachine.pdf"));


	        System.out.println("-------------------------------------------------------");

	    }
    
	    // execute command, for translation from dot to pdf 
    public static String executeCommand(String command) {
    	 
		StringBuffer output = new StringBuffer();
 
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		return output.toString();
 
	}
}
