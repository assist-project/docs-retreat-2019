package learner;

import de.ls5.jlearn.abstractclasses.LearningException;
import de.ls5.jlearn.interfaces.Oracle;
import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.interfaces.Word;
import de.ls5.jlearn.shared.WordImpl;

/**
 * Class used to run membership queries on the system. 
 */
public class MembershipOracle implements Oracle {
	private static final long serialVersionUID = -1374892499287788040L;
	private SutWrapper sutWrapper;
	private int numMembQueries = 0;

	public MembershipOracle() {
		sutWrapper = new SutWrapper();
	}

	//@Override
	public Word processQuery(Word query) throws LearningException {
		Word result = new WordImpl();
		System.out.println("LearnLib Query: " + query);

		sutWrapper.sendReset();
		
		numMembQueries++;
		System.out.println("Member query number: " + numMembQueries);

		for (Symbol input : query.getSymbolList()) {
			System.out.println("Sending: " + input);

			Symbol output = sutWrapper.sendInput(input);
			System.out.println("Received: " + output.toString());

			result.addSymbol(output);
		}

		System.out.println("Returning to LearnLib: " + result);

		return result;
	}

	public int getNumMembQueries() {
		int result = numMembQueries;
		numMembQueries = 0;
		return result;
	}
}
