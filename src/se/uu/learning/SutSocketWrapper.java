package se.uu.learning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class SutSocketWrapper  {
	private Socket sock;
	private PrintWriter sockout;
	private BufferedReader sockin;
	private int run;
	
	public SutSocketWrapper(int portNumber) {
		
		try {
			sock = new Socket("localhost", portNumber);
			
			/* Call setTcpNoDelay to improve communication performance : */

			sock.setTcpNoDelay(true);  // remove unnecessary delay in socket communication!
			
			
			// make char writer from byte writer which automatically encodes chars using UTF-8 and 
			// automatically flushes the buffer on each println call.
			sockout = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"),true);
			// make char reader from byte reader which automatically decodes bytes to chars using UTF-8
			sockin = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
			
			
			run=1;			
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("");
			System.err.println("\n\nPROBLEM: problem connecting with SUT:\n\n   " + e.getMessage() +"\n\n");
			System.exit(1);
		}
	}
	
	
	
	public String sendInput(String inputStr) {
		try {	
			
			// Send input to SUT
			sockout.println(inputStr);
			sockout.flush();
			
			// Receive output from SUT
			String outputStr=sockin.readLine();
			if (outputStr==null) {
				System.err.println("");
				System.err.println("\n\nPROBLEM: problem reading output from SUT: SUT closed connection\n\n   " );
				System.exit(1);				
			}
				
			return outputStr;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void sendReset()  {
        
		// send reset to SUT
		sockout.println("reset");
		sockout.flush();

		run=run+1;
	}
	
	

	
	public void close() {
		/*
		try {
			sockin.close();
			sockout.close();
			sock.close();
		} catch (IOException ex) {
			
		}
		*/
	}

	
	
}
