package learner;

import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.shared.SymbolImpl;

public class VendingMachine implements SutInterface{
	public static Integer MAX_VALUE = 200;
	
	public static Integer MARS_COST = 15;
	public static Integer SNICKERS_COST = 20;
	private int currentValue;
	
	public VendingMachine() {
		currentValue = 0;
	}
	
	
	
	@Override
	public Symbol sendInput(Symbol input) {
		Symbol output;
		
		if (input.toString().equals("IMARS") && currentValue > MARS_COST) {
			currentValue -= MARS_COST;
			output = new SymbolImpl("OMARS");
		} else if (input.toString().equals("ISNICKERS") && currentValue > SNICKERS_COST) {
			currentValue -= SNICKERS_COST;
			output = new SymbolImpl("OSNICKERS");
		} else if (input.toString().equals("I10")) {
			if (currentValue + 10 < MAX_VALUE) {
				currentValue += 10;
				output = new SymbolImpl("OACCEPT");
			} else {
				output = new SymbolImpl("OREJECT");
			}
		} else if (input.toString().equals("I5")) {
			if (currentValue + 5 < MAX_VALUE) {
				currentValue += 5;
				output = new SymbolImpl("OACCEPT");
			} else {
				output = new SymbolImpl("OREJECT");
			}
		} else {
			output = new SymbolImpl("Oquiescence");
		}

		return output;
	}

	@Override
	public void sendReset() {
		init();
	}
	
	private void init() {
		currentValue = 0;
	}

}
