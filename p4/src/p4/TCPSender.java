package p4;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;




public class TCPSender {
		int currentPort;
		int remotePort;
		String remoteIP;
		byte[] data;
		int mtu;
		int sws;
		HashMap<Integer,byte[]> dataSlices; 
		
		
		
		public int getCurrentPort() {
			return currentPort;
		}
		public void setCurrentPort(int currentPort) {
			this.currentPort = currentPort;
		}
		public int getRemotePort() {
			return remotePort;
		}
		public void setRemotePort(int remotePort) {
			this.remotePort = remotePort;
		}
		public String getRemoteIP() {
			return remoteIP;
		}
		public void setRemoteIP(String remoteIP) {
			this.remoteIP = remoteIP;
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
		public TCPSender(int currentPort, int remotePort, String remoteIP, byte[] data, int mtu, int sws) {
			this.currentPort = currentPort;
			this.remotePort = remotePort;
			this.remoteIP = remoteIP;
			this.data = data;
			this.mtu = mtu;
			this.sws = sws;
			dataSlices = new HashMap<Integer,byte[]>();
			
			//slice the data and put it in dataSlices
			//header has 24bytes ==> one TCPacket should have at most (mtu-24)byte in DATA
			if(data != null) {
				if(data.length % (mtu-24) == 0) //can be sliced perfectly
				{
					int pieces = data.length / (mtu-24);
					int jump = mtu-24;
					for(int i=0; i<pieces; i+=jump) {
						byte[] slice = Arrays.copyOfRange(data,i,i+jump);
						dataSlices.put(i, slice);
					}
					
					
				}else {//cannot slice perfectly
					int pieces = data.length / (mtu-24);
					int jump = mtu-24;
					for(int i=0; i<pieces; i+=jump) {
						byte[] slice = Arrays.copyOfRange(data,i,i+jump);
						dataSlices.put(i, slice);
					}
					int last = pieces * jump;
					byte[] lastslice = Arrays.copyOfRange(data,last,data.length); //not sure if it is length of length +1
					dataSlices.put(last,lastslice);
				}
			}
		}
		
		
		
		
		public void sent() throws IOException {
			//slice the file to byte[] and then make a TCPacket --> new UDP instance
			// invoke UDP.sent() to send the data
			
			//example send, not implements TCP algorithm yet
			// a SYN flag packet
			byte[] synData = new byte[1];
			long timestamp = System.nanoTime();
			short checksum = 0;
			TCPacket aPacket = new TCPacket(0,0,timestamp,0,true,false,false,checksum,synData,0);
			byte[] UDPacket = aPacket.serialize();
			TCPacket dePacket = aPacket.deserialize(UDPacket, 0, Math.min(UDPacket.length, mtu));
			UDP udp = new UDP(UDPacket,currentPort,dePacket.getChecksum(),dePacket.getLength(),remotePort, remoteIP);
			udp.sent();
			
			
			//example output: snd 34.335 S - - - 0 0 0
			String s = dePacket.isSYN()? "S" : "-";
            String a = dePacket.isACK()? "A" : "-";
            String f = dePacket.isFIN()? "F" : "-";
            String d = dePacket.getData().length > 1? "D" : "-";
			System.out.println("snd " + timestamp + " " + s + a + f + d + dePacket.getSequence() + dePacket.getLength() + dePacket.getAcknowledge());
			
			
		}
		
}













