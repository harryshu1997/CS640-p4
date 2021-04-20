package p4;

import java.io.*;
import java.net.*;
import java.util.Collections;

public class Server {
	public int[] connect(int portNumber) throws IOException {
		long startTime = 0;
		int size = 0;
		int duration = 0;
        try (
        	ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();     
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);                   
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
        	String inputLine;
            while ((inputLine = in.readLine()) != null) {
            	if(size == 0)
            		startTime = System.currentTimeMillis();
                size++;
            }
            duration = (int)(System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.exit(1);
        }
        return new int[]{size, duration};
    }
}
