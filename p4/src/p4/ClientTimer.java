package p4;

import java.io.IOException;
import java.net.DatagramPacket;

// This class monitors the sender sliding window and properly retransmit those timeouted packets
public class ClientTimer implements Runnable{
	private static final double SEC2NANOSEC = Math.pow(10, 9);
	private SharedData sharedData;
	
	public ClientTimer(SharedData sharedData) {
		this.sharedData = sharedData;
	}
	
	public void run() {
		while(true) {
			try {
				//check every 0.1 s
				Thread.sleep(100);
				long curTime = System.nanoTime();
				synchronized(sharedData) {
					for(BufferItem bufferItem: sharedData.sw) {
						if((curTime-bufferItem.getPacket().getTimeStamp()) / SEC2NANOSEC > sharedData.timeOut) {
							if(bufferItem.getRetransmitTime() >= 16) {
								//maximum number of transmissions reached
								System.out.println("Error: Reaching maximum retransmission time");
								System.exit(1);
							}
							//retransmit
							TCPacket updated = bufferItem.getPacket();
							updated.setChecksum((short)0);
							updated.setTimeStamp(System.nanoTime());
							byte[] updatedData = updated.serialize();
							DatagramPacket updatedPacket = new DatagramPacket(updatedData, updatedData.length, sharedData.address);
							try {
								sharedData.socket.send(updatedPacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
							bufferItem.setPacket(updated);
							bufferItem.incRetransmitTime();
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
