package learner;

import de.ls5.jlearn.abstractclasses.LearningException;
import de.ls5.jlearn.interfaces.Oracle;
import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.interfaces.Word;
import de.ls5.jlearn.shared.WordImpl;

/**
 * A membership oracle used during equivalence checking (checking that hypothesis
 * behaves the same as the SUT).
 */
public class EquivalenceMembershipOracle implements Oracle {
	private static final long serialVersionUID = -5409624854115451929L;
	private SutInterface sut;
	private int numEquivMemQueries = 0;

	public EquivalenceMembershipOracle(SutInterface sut) {
		this.sut = sut;
	}

	public Word processQuery(Word query) throws LearningException {
		Word result = new WordImpl();
		System.out.println("LearnLib Query: " + query);

		sut.sendReset();

		numEquivMemQueries++;
		System.out.println("Equivalence query number: " + numEquivMemQueries);

		for (Symbol input : query.getSymbolList()) {

			Symbol output = sut.sendInput(input);

			result.addSymbol(output);
		}

		System.out.println("Returning to LearnLib: " + result);
		return result;
	}
	
	public int getNumEquivMemQueries() {
		return numEquivMemQueries;
	}
}
