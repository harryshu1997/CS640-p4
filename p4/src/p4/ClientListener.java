package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

//This class listens to the packets from the receiver and respond to them
public class ClientListener implements Runnable {
	private static final double SEC2NANOSEC = Math.pow(10, 9);
	private SharedData sharedData;
	private int lastACK;
	private int repTime;

	public ClientListener(SharedData sharedData) {
		this.sharedData = sharedData;
		this.lastACK = -1;
		this.repTime = 0;
	}

	public void run() {
		DatagramSocket socket = sharedData.socket;
		byte[] packetData = new byte[sharedData.mtu];
		DatagramPacket packet = new DatagramPacket(packetData, sharedData.mtu);
		while (true) {
			try {
				socket.receive(packet);
				TCPacket recPacket = new TCPacket();
				recPacket.deserialize(packetData, 0, packetData.length);
				
				//add checksum here
				synchronized (sharedData) {
					if (recPacket.ACK) { // it's an ACK
						// calculate new timeOut
						long curTime = System.nanoTime();
						if (recPacket.getSequence() == 0) {
							sharedData.ERTT = (curTime - recPacket.getTimeStamp()) / SEC2NANOSEC;
							sharedData.timeOut = sharedData.ERTT * 2;
						} else {
							sharedData.ERTT = 0.875 * sharedData.ERTT + 0.125 * (curTime - recPacket.getTimeStamp());
							sharedData.EDEV = 0.75 * sharedData.EDEV
									+ 0.25 * Math.abs(curTime - recPacket.getTimeStamp() - sharedData.ERTT);
							sharedData.timeOut = sharedData.ERTT + 4 * sharedData.EDEV;
						}
						// check for dupACK
						if (recPacket.getAcknowledge() == lastACK) {
							if (repTime < 2) {
								repTime++;
							} else {
								for (BufferItem bufferItem : sharedData.sw) {
									TCPacket updated = bufferItem.getPacket();
									updated.setChecksum((short) 0);
									updated.setTimeStamp(System.nanoTime());
									byte[] updatedData = updated.serialize();
									DatagramPacket updatedPacket = new DatagramPacket(updatedData, updatedData.length,
											sharedData.address);
									try {
										sharedData.socket.send(updatedPacket);
									} catch (IOException e) {
										e.printStackTrace();
									}
									bufferItem.setPacket(updated);
									bufferItem.incRetransmitTime();
									break;
								}
							}
						} else {
							//new ACK, properly clean buffer
							lastACK = recPacket.getAcknowledge();
							repTime = 0;
							while (sharedData.sw.peek() != null && sharedData.sw.peek().getPacket().getSequence()
									+ sharedData.sw.peek().getPacket().getLength() <= recPacket.getAcknowledge()) {
								BufferItem item = sharedData.sw.poll();
								TCPacket removedPacket = item.getPacket();
								if(!removedPacket.SYN && !removedPacket.FIN) {
									sharedData.sws++;
								}
							}
							sharedData.notify();
						}
					} else if (recPacket.SYN || recPacket.FIN) {
						//if it receives a SYN or a FIN, reply an ACK
						TCPacket ACK = new TCPacket(sharedData.seq, recPacket.getSequence()+1, recPacket.getTimeStamp(), 0, false, false, true, (short)0, null, 0);
						byte[] ACKData = ACK.serialize();
						DatagramPacket ACKPacket = new DatagramPacket(ACKData, ACKData.length, sharedData.address);
						sharedData.socket.send(ACKPacket);
						if(recPacket.SYN) {
							//activate sender
							sharedData.notify();
						}else {
							System.out.println("Transmission complete");
							//print stat here
							System.exit(1);
						}
						
					} else {
						//if it receives a data packet, which is abnormal
						System.out.println("Error: Sender receiving data packet");
						System.exit(1);
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
