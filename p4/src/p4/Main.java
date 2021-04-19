package p4;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length == 0){
			System.err.println("Error: missing or additional arguments");
		    System.exit(1);
		}
		
		//-p options for specifying port number
		if(!args[0].equals("TCPend")) {
			System.err.println("Error: name should be TCPend");
			System.exit(1);
		}
		
		//-p options for specifying port number
		if(!args[1].equals("-p")) {
			System.err.println("Error: first argument should be -p");
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
		if(indicator.equals("-s")) { //if it is sender
			
			int remoteIP = 0;
			try {
				remoteIP = Integer.parseInt(args[4]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[5].equals("-a")) {
				System.err.println("Error: argument should be -a");
				System.exit(1);
			}
			
			int remotePort = 0;
			try {
				remotePort = Integer.parseInt(args[6]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[7].equals("-f")) {
				System.err.println("Error: argument should be -f to specify a filename");
				System.exit(1);
			}
			
			String filename = args[8];
			
			if(!args[9].equals("-m")) {
				System.err.println("Error: argument should be -m to specify a MTU");
				System.exit(1);
			}
			
			int mtu = 0;
			try {
				mtu = Integer.parseInt(args[10]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[11].equals("-c")) {
				System.err.println("Error: argument should be -c to specify a sliding window size");
				System.exit(1);
			}
			
			int sws = 0;
			try {
				sws = Integer.parseInt(args[12]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			//do the TCP transporting (sender side)
			
			
		}else if(indicator.equals("-m")) { // if it is receiver
			int mtu = 0;
			try {
				mtu = Integer.parseInt(args[4]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[5].equals("-c")) {
				System.err.println("Error: argument should be -c to specify a sliding window size");
				System.exit(1);
			}
			
			int sws = 0;
			try {
				sws = Integer.parseInt(args[6]);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!args[7].equals("-f")) {
				System.err.println("Error: argument should be -f to specify a filename");
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
