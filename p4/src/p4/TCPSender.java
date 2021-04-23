package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.Arrays;

public class TCPSender {
	public static final byte[] DUMMY = { 0x1 };
	int currentPort;
	int remotePort;
	String remoteIP;
	byte[] data;
	int mtu;
	int sws;
	ArrayList<byte[]> dataSlices;
	int seq = 0;
	double timeOut = 5.0;
	Queue<BufferItem> sw;
	DatagramSocket socket;
	String fileName;

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

	public TCPSender(int currentPort, int remotePort, String remoteIP, byte[] data, int mtu, int sws, String fileName) {
		this.currentPort = currentPort;
		this.remotePort = remotePort;
		this.remoteIP = remoteIP;
		this.data = data;
		this.mtu = mtu;
		this.sws = sws;
		this.fileName = fileName;
		dataSlices = new ArrayList<byte[]>();
		sw = new LinkedList<BufferItem>();
		try {
			this.socket = new DatagramSocket(currentPort);
		} catch (SocketException e) {
			System.out.println("Failed to create socket");
		}

		// slice the data and put it in dataSlices
		// header has 24bytes ==> one TCPacket should have at most (mtu-24)byte in DATA
		if (data != null) {
			if (data.length % (mtu - 24) == 0) // can be sliced perfectly
			{
				int pieces = data.length / (mtu - 24);
				int jump = mtu - 24;
				for (int i = 0; i < pieces; i += jump) {
					byte[] slice = Arrays.copyOfRange(data, i, i + jump);
					dataSlices.add(slice);
				}

			} else {// cannot slice perfectly
				int pieces = data.length / (mtu - 24);
				int jump = mtu - 24;
				for (int i = 0; i < pieces; i += jump) {
					byte[] slice = Arrays.copyOfRange(data, i, i + jump);
					dataSlices.add(slice);
				}
				int last = pieces * jump;
				byte[] lastslice = Arrays.copyOfRange(data, last, data.length); // not sure if it is length of length +1
				dataSlices.add(lastslice);
			}
			System.out.println(data.length);
			for(byte[] slice:dataSlices) {
				System.out.println(slice);
				System.out.println(slice.length);
			}
		}
		if (data != null) {
			System.out.println("Client booted");
			InetSocketAddress address = new InetSocketAddress(remoteIP, remotePort);
			SharedData sharedData = new SharedData(socket, sw, address, timeOut, seq, sws, mtu);
			ClientSender sender = new ClientSender(sharedData, dataSlices);
			ClientListener listener = new ClientListener(sharedData);
			ClientTimer timer = new ClientTimer(sharedData);
			new Thread(sender).start();
			new Thread(listener).start();
			new Thread(timer).start();
		} else {
			System.out.println("Server booted");
			SharedData sharedData = new SharedData(socket, sw, null, timeOut, seq, sws, mtu);
			ServerListener listener = new ServerListener(sharedData, fileName);
			ClientTimer timer = new ClientTimer(sharedData);
			new Thread(listener).start();
			new Thread(timer).start();
		}
	}
	

// not needed any more

//	public void sent(int seq, int ack, boolean SYN, boolean ACK, boolean FIN, byte[] data) throws IOException {
//		// slice the file to byte[] and then make a TCPacket --> new UDP instance
//		// invoke UDP.sent() to send the data
//
//		// example send, not implements TCP algorithm yet
//		// a SYN flag packet
//		long timestamp = System.nanoTime();
//		short checksum = 0;
//		TCPacket aPacket = new TCPacket(seq, ack, timestamp, 0, SYN, ACK, FIN, checksum, data, 0);
//		byte[] UDPacket = aPacket.serialize();
//		TCPacket dePacket = aPacket.deserialize(UDPacket, 0, Math.min(UDPacket.length, mtu));// why min? why dePacket
//		UDP udp = new UDP(UDPacket, currentPort, dePacket.getChecksum(), dePacket.getLength(), remotePort, remoteIP);
//		udp.sent();
//
//		// example output: snd 34.335 S - - - 0 0 0
//		// String s = dePacket.isSYN()? "S" : "-";
//		// String a = dePacket.isACK()? "A" : "-";
//		// String f = dePacket.isFIN()? "F" : "-";
//		// String d = dePacket.getData().length > 1? "D" : "-";
//		// System.out.println("snd " + timestamp + " " + s + a + f + d +
//		// dePacket.getSequence() + dePacket.getLength() + dePacket.getAcknowledge());
//
//	}

}
