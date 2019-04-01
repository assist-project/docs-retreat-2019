package learner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.ls5.jlearn.interfaces.Symbol;
import de.ls5.jlearn.shared.SymbolImpl;

public class SutWrapper {
	private Socket sock;
	private PrintWriter sockout;
	private BufferedReader sockin;
	
	public SutWrapper() {
		try {
			sock = new Socket("localhost", 7892);
			sockout = new PrintWriter(sock.getOutputStream(), true);
			sockin = new BufferedReader(new InputStreamReader(sock.getInputStream()));			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Symbol sendInput(Symbol input) {
		try {	
			// Send input to SUT
			sockout.println(input.toString());
			sockout.flush();
			
			// Receive output from SUT
			Symbol concreteOutput = new SymbolImpl(sockin.readLine());
			
			return concreteOutput;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void sendReset()  {
		sockout.println("reset");
		sockout.flush();
	}
	
	public void close() {
		try {
			sockin.close();
			sockout.close();
			sock.close();
		} catch (IOException ex) {
			
		}
	}
	
}