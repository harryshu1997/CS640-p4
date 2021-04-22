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
	UDP udp; //not used for now
	byte[] data;
	int offset;
	
	public int getSourceport() {
		return sourceport;
	}

	public void setSourceport(int sourceport) {
		this.sourceport = sourceport;
	}

	public int getDestport() {
		return destport;
	}

	public void setDestport(int destport) {
		this.destport = destport;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
//	public UDP getUdp() {
//		return udp;
//	}
//	public void setUdp(UDP udp) {
//		this.udp = udp;
//	}
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
	
	public TCPacket() {
		
	}
	
	
	
	public TCPacket(int sequence, int acknowledge, long timeStamp, int length, boolean sYN, boolean fIN, boolean aCK,
			short checksum, byte[] data, int offset) {
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
			length = modifyBit(length, 2, 1);
		}else {
			length = modifyBit(length, 2, 0);
		}
		if(FIN) {
			length = modifyBit(length, 1, 1);
		}else {
			length = modifyBit(length, 1, 0);
		}
		if(ACK) {
			length = modifyBit(length, 0, 1);
		}else {
			length = modifyBit(length, 0, 0);
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
			for (int i = 0; i < pktlength / 2; ++i) {
                accumulation += 0xffff & bb.getShort();
            }
            // pad to an even number of shorts
            if (pktlength % 2 > 0) {
                accumulation += (bb.get() & 0xff) << 8;
            }

            accumulation = ((accumulation >> 16) & 0xffff)
                    + (accumulation & 0xffff);
            this.checksum = (short) (~accumulation & 0xffff);
			bb.putShort(22,this.checksum);
			
		}
		return bytes;
	}
	
	
	/**
	 * 
	 * @param bytes TCPacket -->byte[]
	 * @param offset position to start read 
	 * @param pktlength length to read, no large than byte[].length-offset
	 * @return
	 */
	public TCPacket deserialize(byte[] bytes, int offset, int pktlength) {
		//System.out.println("data length: " + bytes.length + " pktlength: " + pktlength + " offset: " + offset);
		ByteBuffer bb = ByteBuffer.wrap(bytes, offset, pktlength);

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
		this.length = length >>> 3;
		bb.getShort(); // all zeros before checksum
		this.checksum = bb.getShort();
		byte[] b = new byte[bb.remaining()];
		bb.get(b);
		this.data = b;
		return this;
	}
	
	@Override
	public boolean equals(Object obj) { //not working don't know why 
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof TCPacket)) {
			return false;
		}
		TCPacket other = (TCPacket)obj;
		//System.out.println(length + " || " + other.length);
		return (sequence == other.sequence) &&
				(acknowledge == other.acknowledge) &&
				(timeStamp == other.timeStamp) &&
				(length == other.length) &&
				(SYN == other.SYN) &&
				(FIN == other.FIN) &&
				(ACK == other.ACK) &&
				(checksum == other.checksum); //do not check carried data yet 
				
	}
	
	@Override
	public String toString() {
		String res = "";
		res = "\nThe following is the packet info: \n" + "\tsequence: " + this.sequence + " acknowledge: " + this.acknowledge +
				" timeStamp: " + this.timeStamp + " length: " + this.length + " SYN " + this.SYN + " FIN " + this.FIN +
				" ACK " + this.ACK + " checksum: " + this.checksum + " \n\tdata: " + new String(this.data);
		return res;
	}
	
	
	
	
	public void resetChecksum() {
		this.checksum = 0;
	}
	
	 public static int[] long2doubleInt(long a) {
		 	int a1 = (int)(a>>32); //high32
			int a2 = (int) a; //low32
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


















