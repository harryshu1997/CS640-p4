package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class TCPReceiver {

	int port;
	int mtu;
	int sws;
	String filename;
	
	ArrayList<TCPacket> buffer;
	
	public TCPReceiver(int port, int mtu, int sws, String filename) {
		this.filename = filename;
		this.port = port;
		this.mtu = mtu;
		this.sws = sws;
		buffer = new ArrayList<TCPacket>();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getMtu() {
		return mtu;
	}

	public void setMtu(int mtu) {
		this.mtu = mtu;
	}

	public int getSws() {
		return sws;
	}

	public void setSws(int sws) {
		this.sws = sws;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public void receive() throws IOException {
		// Step 1 : Create a socket to listen at port 1234
        DatagramSocket ds = new DatagramSocket(this.port);
        byte[] receive = new byte[this.mtu]; //use mtu as one packet size
        DatagramPacket DpReceive = null;
        while (true)
        {
  
            // Step 2 : create a DatgramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);
  
            // Step 3 : revieve the data in byte buffer.
            ds.receive(DpReceive);
            TCPacket packet = new TCPacket(); //empty packet for receiving
            packet.deserialize(receive, 0, receive.length); 
            buffer.add(packet);
            
            
            //example output: rcv 34.8   S A - - 0 0 1
            String s = packet.isSYN()? "S" : "-";
            String a = packet.isACK()? "A" : "-";
            String f = packet.isFIN()? "F" : "-";
            String d = packet.getData() == null? "D" : "-";
            
            
            System.out.println("rcv " + packet.getTimeStamp() + s + a + f + d + packet.getSequence() + " " + packet.getLength() + " " + packet.getAcknowledge());
            
            
            //break the receiving mode if FIN get
            if(packet.isFIN()) {
            	System.out.println("get FIN need to shut down");
            	break;
            }
  
            // Clear the buffer after every message.
            receive = new byte[this.mtu];
        }
	}
	
	
	
	// A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

	
	
	
	
	
	
}
