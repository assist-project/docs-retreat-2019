package learner;

import de.ls5.jlearn.abstractclasses.LearningException;
import de.ls5.jlearn.interfaces.Oracle;
import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.interfaces.Word;
import de.ls5.jlearn.shared.WordImpl;

/**
 * An oracle used during learning.
 * It executes queries (input words/sequences) on the SUT and returns
 * the resulting outputs.
 */
public class MembershipOracle implements Oracle {
	private static final long serialVersionUID = -1374892499287788040L;
	private SutInterface sut;
	private int numMembQueries = 0;

	public MembershipOracle(SutInterface sut) {
		this.sut = sut;
	}

	//@Override
	public Word processQuery(Word query) throws LearningException {
		Word result = new WordImpl();
		System.out.println("LearnLib Query: " + query);

		sut.sendReset();
		
		numMembQueries++;
		System.out.println("Member query number: " + numMembQueries);

		for (Symbol input : query.getSymbolList()) {

			Symbol output = sut.sendInput(input);

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
