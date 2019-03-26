package se.uu.learning;

import de.learnlib.api.SUL;

/**
 * The Adapter: needed in order to let LearnLib and the sut to communicate 
 * 
 */
public class SulAdapter implements SUL<String, String> {

    // system under learning
    private SutSocketWrapper sul;
    
    public SulAdapter(Integer sutInterface_portNumber) {
    	sul = new SutSocketWrapper(sutInterface_portNumber);
    }

    // reset the SUL
    @Override
    public void reset() {
        sul.sendReset();
    }

    // execute one input on the SUL
    @Override
    public String step(String in) {
    	String output = sul.sendInput(in);
    	//System.out.println(in + ":" + output);
        return output;
    }
}