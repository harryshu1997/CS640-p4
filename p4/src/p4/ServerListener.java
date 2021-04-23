package p4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class ServerListener implements Runnable {
	private static final double SEC2NANOSEC = Math.pow(10, 9);
	private SharedData sharedData;
	private int lastACK;
	private int repTime;
	private Map<Integer, byte[]> filePieces;
	private int fileSize;
	private int FINReceived;
	private boolean FINSent;
	private int nextExpected;
	private Map<Integer, Integer> byteReceived;
	private String fileName;

	public ServerListener(SharedData sharedData, String fileName) {
		this.sharedData = sharedData;
		this.lastACK = -1;
		this.repTime = 0;
		this.filePieces = new HashMap<>();
		this.fileSize = 0;
		this.FINReceived = -1;
		this.FINSent = false;
		this.nextExpected = 0;
		this.byteReceived = new HashMap<>();
		this.fileName = fileName;
	}

	public void run() {
		DatagramSocket socket = sharedData.socket;
		byte[] packetData = new byte[sharedData.mtu];
		DatagramPacket packet = new DatagramPacket(packetData, sharedData.mtu);
		while (true) {
			try {
				socket.receive(packet);
				sharedData.packetReceived++;
				if (sharedData.address == null) {
					sharedData.address = (InetSocketAddress) packet.getSocketAddress();
				}
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
							//System.out.println("Old timeout:"+Double.toString(sharedData.timeOut));
							long curTime = System.nanoTime();
							if (recPacket.getSequence() == 0) {
								sharedData.ERTT = (curTime - recPacket.getTimeStamp()) / SEC2NANOSEC;
								sharedData.timeOut = sharedData.ERTT * 2;
							} else {
								sharedData.ERTT = 0.875 * sharedData.ERTT
										+ 0.125 * (curTime - recPacket.getTimeStamp()) / SEC2NANOSEC;
								sharedData.EDEV = 0.75 * sharedData.EDEV
										+ 0.25 * Math.abs((curTime - recPacket.getTimeStamp()) / SEC2NANOSEC - sharedData.ERTT);
								sharedData.timeOut = sharedData.ERTT + 4 * sharedData.EDEV;
							}
							//System.out.println(curTime - recPacket.getTimeStamp());
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
								if(this.FINSent && lastACK == this.sharedData.seq) {
									writeFile();
									//print info
									System.out.println("File Transmission complete");
									sharedData.printStat();
									System.exit(1);
								}
								//sharedData.notify();
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
								//send a SYN packet
								TCPacket SYN = new TCPacket(sharedData.seq, 0, System.nanoTime(), TCPSender.DUMMY.length, true, false, false, (short)0, TCPSender.DUMMY, 0);
								sharedData.sendPacket(SYN);
								sharedData.printInfo(sharedData.SENT, SYN, SYN.getTimeStamp());
								BufferItem SYNBuffer = new BufferItem(SYN);
								sharedData.sw.offer(SYNBuffer);
								sharedData.seq += SYN.getLength();
							} else {
								this.FINReceived = recPacket.getSequence() + recPacket.getLength();
								if(this.FINReceived == this.nextExpected) {
									//send a FIN packet
									TCPacket FIN = new TCPacket(sharedData.seq, 0, System.nanoTime(), TCPSender.DUMMY.length, false, true, false, (short)0, TCPSender.DUMMY, 0);
									sharedData.sendPacket(FIN);
									sharedData.printInfo(sharedData.SENT, FIN, FIN.getTimeStamp());
									BufferItem FINBuffer = new BufferItem(FIN);
									sharedData.sw.offer(FINBuffer);
									sharedData.seq += FIN.getLength();
									this.FINSent = true;
								}
							}

						} else {
							// if it receives a data packet
							if(this.byteReceived.get(recPacket.getSequence()) == null) {
								this.byteReceived.put(recPacket.getSequence(), recPacket.getLength());
								this.filePieces.put(recPacket.getSequence(), recPacket.getData());
								this.fileSize += recPacket.getLength();
								sharedData.dataReceived += recPacket.getLength();
							}else {
								sharedData.packetDiscarded++;
							}
							while(this.byteReceived.get(this.nextExpected) != null) {
								this.nextExpected += this.byteReceived.get(this.nextExpected);
							}
							TCPacket ACK = new TCPacket(sharedData.seq, this.nextExpected,
									recPacket.getTimeStamp(), 0, false, false, true, (short) 0, null, 0);
							sharedData.sendPacket(ACK);
							sharedData.printInfo(sharedData.SENT, ACK, System.nanoTime());
							if(this.FINReceived == this.nextExpected) {
								//send a FIN packet
								TCPacket FIN = new TCPacket(sharedData.seq, 0, System.nanoTime(), TCPSender.DUMMY.length, false, true, false, (short)0, TCPSender.DUMMY, 0);
								sharedData.sendPacket(FIN);
								sharedData.printInfo(sharedData.SENT, FIN, FIN.getTimeStamp());
								BufferItem FINBuffer = new BufferItem(FIN);
								sharedData.sw.offer(FINBuffer);
								sharedData.seq += FIN.getLength();
								this.FINSent = true;
							}
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
	
	public void writeFile() {
		ByteBuffer buffer = ByteBuffer.allocate(this.fileSize);
		Object[] keyArray = (this.filePieces.keySet().toArray());
		Arrays.sort(keyArray);
		for(Object key: keyArray) {
			buffer.put(this.filePieces.get(key));
		}
		buffer.flip();
		byte[] fileData = new byte[buffer.remaining()];
		buffer.get(fileData,0, buffer.remaining());
		try {
			File file = new File(fileName);
			OutputStream os = new FileOutputStream(file);
			os.write(fileData);
			os.close();
		} catch(Exception e) {
			System.out.println("Error writing data");
		}
	}
}
