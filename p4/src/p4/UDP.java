package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class UDP {

	/*
	 * 	
	 * 
	 * 	databuf : byte array
		offset : offset into the array
		length : length of message to deliver
		address : address of destination
		port : port number of destination
		
		UDP datagram format:
		
		----sourcePort-----
		----destPort----
		----length----
		----zeros+checksum----
		----Data----
		
		
		
		
	 */
	byte[] databuf;
	int length;
	int destPort;
	String destAddress;
	short checksum;
	int sourcePort;
	
	
	public int getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public short getChecksum() {
		return checksum;
	}
	public void setChecksum(short checksum) {
		this.checksum = checksum;
	}
	public byte[] getDatabuf() {
		return databuf;
	}
	public void setDatabuf(byte[] databuf) {
		this.databuf = databuf;
	}
	
	public int getDestPort() {
		return destPort;
	}
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	
	public String getDestAddress() {
		return destAddress;
	}
	public void setDestAddress(String destAddress) {
		this.destAddress = destAddress;
	}
	public void resetChecksum() {
		this.checksum = 0;
	}
	
	/**
	 * 
	 * @param data serialized data from TCPacket
	 * @param sourcePort current port number
	 * @param checksum TCPacket checksum
	 * @param length TCPacket data length
	 * @param destport destination port 
	 * @param destAddress destination address
	 */
	
	public UDP(byte[] data, int sourcePort, short checksum, int length, int destport, String destAddress) {
		this.databuf = data;
		this.destPort = destport;
		this.destAddress = destAddress;
		this.length = length;
		this.checksum = checksum;
		this.sourcePort = sourcePort;
	}
	
	
	public void sent() throws IOException {
		// Step 1:Create the socket object for
        // carrying the data.
        DatagramSocket ds = new DatagramSocket();
  
        InetAddress ip = InetAddress.getByName(this.destAddress);
     
        // Step 2 : Create the datagramPacket for sending
        // the data.
        DatagramPacket DpSend = new DatagramPacket(this.databuf, this.databuf.length, ip, this.destPort);
     
        // Step 3 : invoke the send call to actually send
        // the data.
        ds.send(DpSend);
	}
	
	
	
	
	/*
	public byte[] serialize() {
		this.length = 16 + ((this.databuf == null)? 0: this.databuf.length);
		byte[] data = new byte[this.length];
		ByteBuffer bb = ByteBuffer.wrap(data);
		
		bb.putInt(this.sourcePort);
		bb.putInt(this.destPort);
		bb.putInt(this.length);
		short zeros = 0;
		bb.putShort(zeros);
		bb.putShort(this.checksum);
		if(this.databuf != null) {
			bb.put(databuf);
		}
		
		//compute checksum  very confused!
		if(this.checksum == 0) {
			bb.rewind();
			int accumulation = 0;
			accumulation += ((this.sourcePort >> 16) & 0xffff) + (this.sourcePort & 0xffff);
			accumulation += ((this.destPort >> 16) & 0xffff) + (this.destPort & 0xffff);
			accumulation += ((this.length >> 16) & 0xffff) + (this.length & 0xffff);
			
			 for (int i = 0; i < this.length / 2; ++i) {
	                accumulation += 0xffff & bb.getShort();
	            }
	            // pad to an even number of shorts
	            if (this.length % 2 > 0) {
	                accumulation += (bb.get() & 0xff) << 8;
	            }

	            accumulation = ((accumulation >> 16) & 0xffff)
	                    + (accumulation & 0xffff);
	            this.checksum = (short) (~accumulation & 0xffff);
	            bb.putShort(14, this.checksum);
			
		}
		return data;
		
	}
	
	public UDP deserialized(byte[] data, int offset, int length) {
		ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
		this.sourcePort = bb.getInt();
		this.destPort = bb.getInt();
		this.length = bb.getInt();
		bb.getShort(); //all zeros
		this.checksum = bb.getShort();
		byte[] b = new byte[bb.remaining()];
		this.databuf = b;
		return this;
	}
	
	*/
	
	
}




















