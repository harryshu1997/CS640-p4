package p4;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Queue;

public class SharedData {
	public DatagramSocket socket;
	public Queue<BufferItem> sw;
	public InetSocketAddress address;
	public double timeOut;
	public double ERTT;
	public double EDEV;
	public int seq;
	public int sws;
	public int mtu;
	
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
	}
}
