package p4;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class TCPacket {
	
	int sourceport;
	int destport;
	int sequence; 
	int acknowledge;
	int length;
	long timeStamp;
	boolean SYN;
	boolean FIN;
	boolean ACK;
	short checksum;
	UDP udp;
	byte[] data;
	int offset;
	
	public TCPacket(int sequence, int acknowledge, long timeStamp, int length, boolean sYN, boolean fIN, boolean aCK,
			short checksum, byte[] data, int offset) {
		super();
		this.sequence = sequence;
		this.acknowledge = acknowledge;
		this.timeStamp = timeStamp;
		this.length = length;
		SYN = sYN;
		FIN = fIN;
		ACK = aCK;
		this.checksum = checksum;
		this.data = data;
		this.offset = offset;
	}

	

	public UDP getUdp() {
		return udp;
	}
	public void setUdp(UDP udp) {
		this.udp = udp;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getAcknowledge() {
		return acknowledge;
	}
	public void setAcknowledge(int acknowledge) {
		this.acknowledge = acknowledge;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public boolean isSYN() {
		return SYN;
	}
	public void setSYN(boolean sYN) {
		SYN = sYN;
	}
	public boolean isFIN() {
		return FIN;
	}
	public void setFIN(boolean fIN) {
		FIN = fIN;
	}
	public boolean isACK() {
		return ACK;
	}
	public void setACK(boolean aCK) {
		ACK = aCK;
	}
	public short getChecksum() {
		return checksum;
	}
	public void setChecksum(short checksum) {
		this.checksum = checksum;
	}
	
	
	public byte[] serialize() {
		int pktlength;
		int length = 0;
		if(offset == 0) {
			offset = 6; //default header offset 
		}
		pktlength = offset << 2;
		if(data != null){
			length = data.length << 3; //for the length filed, indicate the length of data below
			pktlength += data.length;
		}
		byte[] bytes = new byte[pktlength];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.putInt(sequence);
		bb.putInt(acknowledge);
		int[] times = long2doubleInt(timeStamp);
		bb.putInt(times[0]);
		bb.putInt(times[1]);
		if(SYN) {
			modifyBit(length, 2, 1);
		}else {
			modifyBit(length, 2, 0);
		}
		if(FIN) {
			modifyBit(length, 1, 1);
		}else {
			modifyBit(length, 1, 0);
		}
		if(ACK) {
			modifyBit(length, 0, 1);
		}else {
			modifyBit(length, 0, 0);
		}
		bb.putInt(length);
		int comChecksum = (int) (checksum & 0x0000ffff);
		bb.putInt(comChecksum);
		if(data != null) {
			bb.put(data);
		}
		//compute checksum
		if(this.checksum == 0) {
			bb.rewind();
			int accumulation = 0;
			accumulation += ((sequence >> 16) & 0xffff) + (sequence & 0xffff);
			accumulation += ((acknowledge >> 16) & 0xffff) + (acknowledge & 0xffff);
			accumulation += ((times[0] >> 16) & 0xffff) + (times[0] & 0xffff);
			accumulation += ((times[1] >> 16) & 0xffff) + (times[1] & 0xffff);
			accumulation += ((length >> 16) & 0xffff) + (length & 0xffff);
			for (int i = 0; i < length / 2; ++i) {
                accumulation += 0xffff & bb.getShort();
            }
            // pad to an even number of shorts
            if (length % 2 > 0) {
                accumulation += (bb.get() & 0xff) << 8;
            }

            accumulation = ((accumulation >> 16) & 0xffff)
                    + (accumulation & 0xffff);
            this.checksum = (short) (~accumulation & 0xffff);
			bb.putShort(22,this.checksum);
			
		}
		return bytes;
	}
	
	public TCPacket deserialize(byte[] bytes, int offset, int pktlength) {
		ByteBuffer bb = ByteBuffer.wrap(data, offset, pktlength);
		this.sequence = bb.getInt();
		this.acknowledge = bb.getInt();
		this.timeStamp = bb.getLong();
		int length = bb.getInt();
		if(((length >>> 2) & 1) != 0) {
			this.SYN = true;
		}else {
			this.SYN = false;
		}
		if(((length >>> 1) & 1) != 0) {
			this.FIN = true;
		}else {
			this.FIN = false;
		}
		if(((length >>> 0) & 1) != 0) {
			this.ACK = true;
		}else {
			this.ACK = false;
		}
		bb.getShort(); // all zeros before checksum
		this.checksum = bb.getShort();
		byte[] b = new byte[bb.remaining()];
		this.data = b;
		return this;
	}
	
	
	
	
	
	
	public void resetChecksum() {
		this.checksum = 0;
	}
	
	 public static int[] long2doubleInt(long a) {
		 	int a1 = (int) (a & 0x0000ffff); //低32位
			int a2 = (int) (a >> 32); //高32位
	        return new int[] { a1, a2 };
	    }
	 
	// Returns modified n.
	 public static int modifyBit(int n, int p,int b)
	 {
	     int mask = 1 << p;
	     return (n & ~mask) |
	            ((b << p) & mask);
	 }
	
}


















