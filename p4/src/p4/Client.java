package p4;

import java.io.*;
import java.net.*;
import java.util.Collections;
/*
 * not used for now
 */
/*
public class Client {
	private String data;
	
	
	
	
	public int connect(String hostName, int portNumber, int time) throws IOException {
		int size = 0;
        try (
            Socket clientSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
        	long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() < startTime + time * 1000) {
                out.println(data);
                size++;
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
        return size;
    }
}
*/