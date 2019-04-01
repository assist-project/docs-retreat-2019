package learner;

import de.ls5.jlearn.abstractclasses.LearningException;
import de.ls5.jlearn.interfaces.Oracle;
import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.interfaces.Word;
import de.ls5.jlearn.shared.WordImpl;

public class EquivalenceOracle implements Oracle {
	private static final long serialVersionUID = -5409624854115451929L;
	private SutWrapper sutWrapper;
	private MembershipOracle memberOracle;
	private int numEquivQueries = 0;

	public EquivalenceOracle() {
		sutWrapper = new SutWrapper();
	}

	public Word processQuery(Word query) throws LearningException {
		Word result = new WordImpl();
		System.out.println("LearnLib Query: " + query);

		sutWrapper.sendReset();

		numEquivQueries++;
		System.out.println("Equivalence query number: " + numEquivQueries);

		for (Symbol input : query.getSymbolList()) {
			System.out.println("Sending: " + input);

			Symbol output = sutWrapper.sendInput(input);
			System.out.println("Received: " + output.toString());

			result.addSymbol(output);
		}

		System.out.println("Returning to LearnLib: " + result);
		return result;
	}

	public void setMembershipOracle(MembershipOracle memberOracle) {
		this.memberOracle = memberOracle;
	}
	
	public MembershipOracle getMembershipOracle() {
		return memberOracle;
	}
	
	public int getNumEquivQueries() {
		return numEquivQueries;
	}
}
