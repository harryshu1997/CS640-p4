package p4;

import java.net.DatagramPacket;

public class BufferItem {
	private TCPacket packet;
	//private double time;
	private int retransmitTime;
	
	public BufferItem(TCPacket packet) {
		this.packet = packet;
		//this.time = 0.0;
		this.retransmitTime = 0;
	}
	
	public TCPacket getPacket() {
		return this.packet;
	}
	
	public void setPacket(TCPacket packet) {
		this.packet = packet;
	}
	
	public int getRetransmitTime() {
		return this.retransmitTime;
	}
	
	public void incRetransmitTime() {
		this.retransmitTime++;
	}
	
//	public double getTime() {
//		return this.time;
//	}
//	
//	public void incTime() {
//		this.time += 0.1;
//	}
}
