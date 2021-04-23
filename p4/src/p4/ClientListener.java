package p4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//This class listens to the packets from the receiver and respond to them
public class ClientListener implements Runnable {
	private static final double SEC2NANOSEC = Math.pow(10, 9);
	private SharedData sharedData;
	private int lastACK;
	private int repTime;
	private int nextExpected;
	private Map<Integer, Integer> byteReceived;

	public ClientListener(SharedData sharedData) {
		this.sharedData = sharedData;
		this.lastACK = -1;
		this.repTime = 0;
		this.nextExpected = 0;
		this.byteReceived = new HashMap<>();
	}

	public void run() {
		DatagramSocket socket = sharedData.socket;
		byte[] packetData = new byte[sharedData.mtu];
		DatagramPacket packet = new DatagramPacket(packetData, sharedData.mtu);
		while (true) {
			try {
				socket.receive(packet);
				sharedData.packetReceived++;
				TCPacket recPacket = new TCPacket();
				recPacket.deserialize(packet.getData(), 0, packet.getLength());
				short oldCheckSum = recPacket.getChecksum();
				recPacket.resetChecksum();
				recPacket.serialize();
				short newCheckSum = recPacket.getChecksum();
				if (oldCheckSum == newCheckSum) {
					sharedData.printInfo(sharedData.RECEIVED, recPacket, System.nanoTime());
					synchronized (sharedData) {
						if (recPacket.ACK) { // it's an ACK
							// calculate new timeOut
							long curTime = System.nanoTime();
							if (recPacket.getSequence() == 0) {
								sharedData.ERTT = (curTime - recPacket.getTimeStamp()) / SEC2NANOSEC;
								sharedData.timeOut = sharedData.ERTT * 2;
							} else {
								sharedData.ERTT = 0.875 * sharedData.ERTT
										+ 0.125 * ((curTime - recPacket.getTimeStamp()) / SEC2NANOSEC);
								sharedData.EDEV = 0.75 * sharedData.EDEV
										+ 0.25 * Math.abs((curTime - recPacket.getTimeStamp()) / SEC2NANOSEC - sharedData.ERTT);
								sharedData.timeOut = sharedData.ERTT + 4 * sharedData.EDEV;
							}
							//System.out.println("New timeout:"+Double.toString(sharedData.timeOut));
							// check for dupACK
							if (recPacket.getAcknowledge() == lastACK) {
								repTime++;
								sharedData.dupACK++;
								if (repTime == 3) {
									for (BufferItem bufferItem : sharedData.sw) {
										TCPacket updated = bufferItem.getPacket();
										updated.setChecksum((short) 0);
										updated.setTimeStamp(System.nanoTime());
										sharedData.retransmission++;
										sharedData.sendPacket(updated);
										sharedData.printInfo(sharedData.SENT, updated, updated.getTimeStamp());
										bufferItem.setPacket(updated);
										bufferItem.incRetransmitTime();
										break;
									}
								}
							} else {
								// new ACK, properly clean buffer
								lastACK = recPacket.getAcknowledge();
								repTime = 0;
								while (sharedData.sw.peek() != null && sharedData.sw.peek().getPacket().getSequence()
										+ sharedData.sw.peek().getPacket().getLength() <= recPacket.getAcknowledge()) {
									BufferItem item = sharedData.sw.poll();
									TCPacket removedPacket = item.getPacket();
									//System.out.println("This packet is removed out of buffer:");
									//sharedData.printInfo(sharedData.SENT, removedPacket, curTime);
									if (!removedPacket.SYN && !removedPacket.FIN) {
										sharedData.sws++;
									}
								}
								sharedData.notify();
							}
						} else if (recPacket.SYN || recPacket.FIN) {
							// if it receives a SYN or a FIN, reply an ACK
							this.byteReceived.put(recPacket.getSequence(), recPacket.getLength());
							while(this.byteReceived.get(this.nextExpected) != null) {
								this.nextExpected += this.byteReceived.get(this.nextExpected);
							}
							TCPacket ACK = new TCPacket(sharedData.seq, this.nextExpected,
									recPacket.getTimeStamp(), 0, false, false, true, (short) 0, null, 0);
							sharedData.sendPacket(ACK);
							sharedData.printInfo(sharedData.SENT, ACK, System.nanoTime());
							if (recPacket.SYN) {
								// activate sender
								sharedData.notify();
							} else {
								Timer timer = new Timer();
								timer.schedule(new TimerTask() {
									public void run() {
										sharedData.printStat();
										System.exit(1);
									}
								}, Math.max((long)(sharedData.timeOut * 2000), 2000));
							}

						} else {
							// if it receives a data packet, which is abnormal
							System.out.println("Error: Sender receiving data packet");
							System.exit(1);
						}
					}
				} else {
					System.out.println("Received packet with incorrect checksum");
					sharedData.incorrectChecksum++;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
