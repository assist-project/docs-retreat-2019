package learner;

import de.ls5.jlearn.interfaces.Symbol;

/**
 * Interface a SUT should implement.
 */
public interface SutInterface {
	
	public Symbol sendInput(Symbol input);
	
	public void sendReset();
}
