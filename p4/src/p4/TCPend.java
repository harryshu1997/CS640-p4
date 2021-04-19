package p4;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TCPend{

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args.length == 0){
			System.err.println("Error: missing or additional arguments");
		    System.exit(1);
		}
		
		//-p options for specifying port number
		if(!args[0].equals("TCPend")) {
			System.err.println("Correct Format 0: java TCPend -p <port> -s <remote IP> -a <remote port> –f <file name> -m <mtu> -c <sws>");
			System.exit(1);
		}
		
		//-p options for specifying port number
		if(!args[1].equals("-p")) {
			System.err.println("Correct Format 1: java TCPend -p <port> -s <remote IP> -a <remote port> –f <file name> -m <mtu> -c <sws>");
			System.exit(1);
		}
		
		//record port number
		int portNumber = 0;
		try {
			portNumber = Integer.parseInt(args[2]);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//check whether it is sender or receiver
		String indicator = args[3];
		if(indicator.equals("-s")) { //if it is a sender
			
			String remoteIP = args[4];
			
			
			if(!args[5].equals("-a")) {
				System.err.println("Correct Format 5: java TCPend -p <port> -s <remote IP> -a <remote port> –f <file name> -m <mtu> -c <sws>");
				System.exit(1);
			}
			
			int remotePort = 0;
			try {
				remotePort = Integer.parseInt(args[6]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[7].equals("-f")) {
				System.err.println("Correct Format 7: java TCPend -p <port> -s <remote IP> -a <remote port> –f <file name> -m <mtu> -c <sws>");
				System.exit(1);
			}
			
			String filename = args[8];
			
			if(!args[9].equals("-m")) {
				System.err.println("Correct Format 8: java TCPend -p <port> -s <remote IP> -a <remote port> –f <file name> -m <mtu> -c <sws>");
				System.exit(1);
			}
			
			int mtu = 0;
			try {
				mtu = Integer.parseInt(args[10]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[11].equals("-c")) {
				System.err.println("Correct Format 11: java TCPend -p <port> -s <remote IP> -a <remote port> –f <file name> -m <mtu> -c <sws>");
				System.exit(1);
			}
			
			int sws = 0;
			try {
				sws = Integer.parseInt(args[12]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			//do the TCP transporting (sender side)
			//convert file to byte stream
			byte[] bytes = Files.readAllBytes(Paths.get(filename));
			TCPSender sender = new TCPSender(portNumber, remotePort, remoteIP, bytes, mtu, sws);
			
			
			
			
			
			
		}else if(indicator.equals("-m")) { // if it is a receiver
			int mtu = 0;
			try {
				mtu = Integer.parseInt(args[4]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[5].equals("-c")) {
				System.err.println("Correct Format 55: java TCPend -p <port> -m <mtu> -c <sws> -f <file name>");
				System.exit(1);
			}
			
			int sws = 0;
			try {
				sws = Integer.parseInt(args[6]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[7].equals("-f")) {
				System.err.println("Correct Format 77: java TCPend -p <port> -m <mtu> -c <sws> -f <file name>");
				System.exit(1);
			}
			
			String filename = args[8];
			
			//do the TCP transporting (Receiving side)
			
			
		}else {
			System.err.println("Error: argument should be -s or nothing");
			System.exit(1);
		}
		
	}

}
