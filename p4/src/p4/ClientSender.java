package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

//This class establish SYN, FIN and manage file transmission
public class ClientSender implements Runnable{
	private SharedData sharedData;
	private ArrayList<byte[]> dataSlices;
	
	public ClientSender(SharedData sharedData, ArrayList<byte[]> dataSlices) {
		this.sharedData = sharedData;
		this.dataSlices = dataSlices;
	}
	
	public void run() {
		synchronized(sharedData) {
			//send a SYN packet
			TCPacket SYN = new TCPacket(sharedData.seq, 0, System.nanoTime(), TCPSender.DUMMY.length, true, false, false, (short)0, TCPSender.DUMMY, 0);
			byte[] SYNData = SYN.serialize();
			DatagramPacket SYNPacket = new DatagramPacket(SYNData, SYNData.length, sharedData.address);
			try {
				sharedData.socket.send(SYNPacket);
			} catch (IOException e) {
				System.out.println("Failed to send packet using socket");
				e.printStackTrace();
			}
			BufferItem SYNBuffer = new BufferItem(SYN);
			sharedData.sw.offer(SYNBuffer);
			sharedData.seq += SYN.getLength();
			try {
				//wait after sending a SYN until it's activated by listener after receiving a SYN from receiver
				sharedData.wait();
			} catch (InterruptedException e) {
				System.out.println("Synchronization error");
				e.printStackTrace();
			}
			//start sending file data, properly block itself after the sliding window is full
			for(byte[] data: dataSlices) {
				if(sharedData.sws > 0) {
					TCPacket dataPacket = new TCPacket(sharedData.seq, 0, System.nanoTime(), data.length, false, false, false, (short)0, data, 0);
					byte[] byteData = dataPacket.serialize();
					DatagramPacket datagramPacket = new DatagramPacket(byteData, byteData.length, sharedData.address);
					try {
						sharedData.socket.send(datagramPacket);
					} catch (IOException e) {
						System.out.println("Failed to send packet using socket");
						e.printStackTrace();
					}
					BufferItem dataBuffer = new BufferItem(dataPacket);
					sharedData.sw.offer(dataBuffer);
					sharedData.seq += dataPacket.getLength();
					sharedData.sws--;
				}else {
					try {
						sharedData.wait();
					} catch (InterruptedException e) {
						System.out.println("Synchronization error");
						e.printStackTrace();
					}
				}
			}
			//send a FIN and end itself no matter what
			TCPacket FIN = new TCPacket(sharedData.seq, 0, System.nanoTime(), TCPSender.DUMMY.length, false, true, false, (short)0, TCPSender.DUMMY, 0);
			byte[] FINData = FIN.serialize();
			DatagramPacket FINPacket = new DatagramPacket(FINData, FINData.length, sharedData.address);
			try {
				sharedData.socket.send(FINPacket);
			} catch (IOException e) {
				System.out.println("Failed to send packet using socket");
				e.printStackTrace();
			}
			BufferItem FINBuffer = new BufferItem(FIN);
			sharedData.sw.offer(FINBuffer);
			sharedData.seq += FIN.getLength();
		}
		
	}
}
