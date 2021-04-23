package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Queue;

public class SharedData {
	public static final byte SENT = 0x1;
	public static final byte RECEIVED = 0x2;
	private static final double SEC2NANOSEC = Math.pow(10, 9);
	public DatagramSocket socket;
	public Queue<BufferItem> sw;
	public InetSocketAddress address;
	public double timeOut;
	public double ERTT;
	public double EDEV;
	public int seq;
	public int sws;
	public int mtu;
	public long startTime;
	public int dataSent = 0;
	public int dataReceived = 0;
	public int packetSent = 0;
	public int packetReceived = 0;
	public int packetDiscarded = 0;
	public int incorrectChecksum = 0;
	public int retransmission = 0;
	public int dupACK = 0;
	
	public SharedData(DatagramSocket socket, Queue<BufferItem> sw, InetSocketAddress address, double timeOut, int seq, int sws, int mtu) {
		this.socket = socket;
		this.sw = sw;
		this.address = address;
		this.timeOut = timeOut;
		this.seq = seq;
		this.sws = sws;
		this.mtu = mtu;
		this.ERTT = 0.0;
		this.EDEV = 0.0;
		this.startTime = System.nanoTime();
	}
	
	public void printInfo(byte type, TCPacket packet, long time) {
		StringBuffer buffer = new StringBuffer();
		buffer.append((type == SENT) ? "snd " : "rcv ");
		buffer.append(String.format("%.2f", (time - this.startTime) / SEC2NANOSEC));
		buffer.append(packet.SYN ? " S " : " - ");
		buffer.append(packet.ACK ? "A " : "- ");
		buffer.append(packet.FIN ? "F " : "- ");
		if(!packet.SYN && !packet.ACK && !packet.FIN ) {
			buffer.append("D "+Integer.toString(packet.getSequence())+" "+Integer.toString(packet.getLength())+" -");
		}else if (packet.ACK) {
			buffer.append("- "+Integer.toString(packet.getSequence())+" 0 "+Integer.toString(packet.getAcknowledge()));
		}else {
			buffer.append("- "+Integer.toString(packet.getSequence())+" 0 "+"-");
		}
		System.out.println(buffer);
	}
	
	public void sendPacket(TCPacket packet) {
		byte[] data = packet.serialize();
		DatagramPacket dataPacket = new DatagramPacket(data, data.length, address);
		try {
			socket.send(dataPacket);
		} catch (IOException e) {
			System.out.println("Failed to send packet using socket");
			e.printStackTrace();
		}
		this.packetSent++;
	}
	
	public void printStat() {
		System.out.println("Data transffered (in byte): "+Integer.toString(this.dataSent));
		System.out.println("Data received (in byte): "+Integer.toString(this.dataReceived));
		System.out.println("Packet sent: "+Integer.toString(this.packetSent));
		System.out.println("Packet received: "+Integer.toString(this.packetReceived));
		System.out.println("Out-of-sequence packet discarded: "+Integer.toString(this.packetDiscarded));
		System.out.println("Incorrect checksum packet discarded: "+Integer.toString(this.incorrectChecksum));
		System.out.println("Retransmissions: "+Integer.toString(this.retransmission));
		System.out.println("Duplicate acknowledgements: "+Integer.toString(this.dupACK));
	}
}
