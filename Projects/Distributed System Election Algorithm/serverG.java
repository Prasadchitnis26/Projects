import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class serverG implements Runnable {
	int id;
	static int count = 3;
	static DatagramSocket serverSocket;
	static String myAddr = "glados.cs.rit.edu";
	static boolean receivedReply = false, startReceiving = false, leaderElected = false, listenAsLeader = false,
			leaderAlive = false,isLeaderSelf=false;
	static String leader;
	static int leader2;
	static byte[] recieve;
	Thread personal;
	static InetAddress[] list;
	static int[] port = { 35000, 35000, 35000, 35000 };

	public serverG(int i) {
		this.id = i;
	}

	public static void main(String args[]) throws IOException {
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		list=new InetAddress[4];
		 list[0] = InetAddress.getByName("kansas.cs.rit.edu");
		 list[1] = InetAddress.getByName("iowa.cs.rit.edu");
		 list[2] = InetAddress.getByName("idaho.cs.rit.edu");
		 list[3] = InetAddress.getByName("newyork.cs.rit.edu");
		serverSocket = new DatagramSocket(35000);
		new Thread(new serverG(0)).start();
		new Thread(new serverG(1)).start();
		new Thread(new serverG(2)).start();
		new Thread(new serverG(3)).start();
	}

	@Override
	public void run() {
		if (this.id == 0) {
			System.out.println(this.id + " entered");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.println("Enter 1 to start election");
				try {
					int n = Integer.parseInt(br.readLine());
					if (n == 1) {
						receivedReply=false;
						String msg = "election," + myAddr + "," + "35000," + count + ",";
						byte[] send = msg.getBytes();
						for (int i = 0; i < 4; i++) {
							 DatagramPacket sendPacket = new
							 DatagramPacket(send, send.length, list[i],
							 port[i]);
//							DatagramPacket sendPacket = new DatagramPacket(send, send.length,
//									InetAddress.getByName("localhost"), port[i]);

							serverSocket.send(sendPacket);

						}
						startReceiving = true;
						long start = System.currentTimeMillis();
						long end = System.currentTimeMillis();
						while ((end - start) < 5000) {
							end = System.currentTimeMillis();
						}
						startReceiving = false;
						if (!receivedReply) {
							leaderElected=true; 
							isLeaderSelf=true;
							System.out.println("no replies recieved");
							String msg2 = "leader," + myAddr + "," + "35000," + count + ",";
							byte[] send2 = msg2.getBytes();
							for (int i = 0; i < 4; i++) {
								 DatagramPacket sendPacket = new
								 DatagramPacket(send2, send2.length, list[i],
								 port[i]);
//								DatagramPacket sendPacket = new DatagramPacket(send2, send2.length,
//										InetAddress.getByName("localhost"), port[i]);
								serverSocket.send(sendPacket);

							}
						}

					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else if (this.id == 1) {
			System.out.println(this.id + " entered");
			while (true) {
				try {
					recieve = new byte[1024];
					DatagramPacket pac = new DatagramPacket(recieve, recieve.length);
					serverSocket.receive(pac);
					String msg = new String(pac.getData());
					msg = msg.trim();
					System.out.println("RECEIVED: " + msg);
					String arr[] = msg.split(",");
					if (arr[0].equals("election")) {
						if (Integer.parseInt(arr[3]) < count) {
							String msg2 = "haddd";
							byte[] send2 = msg2.getBytes();
//							DatagramPacket sendPacket = new DatagramPacket(send2, send2.length,
//									InetAddress.getByName("localhost"), Integer.parseInt(arr[2]));

							 DatagramPacket sendPacket = new
							 DatagramPacket(send2, send2.length,
							 InetAddress.getByName(arr[1]),
							 Integer.parseInt(arr[2]));
							serverSocket.send(sendPacket);
						}
						else{
							System.out.println("wont reply to "+arr[2]);
						}
					} else if (arr[0].equals("leader")) {
						leader = arr[1];
						leader2 = Integer.parseInt(arr[2]);
						System.out.println("leader elected");
						leaderElected = true;
						isLeaderSelf=false;
						
					} else if (msg.equals("haddd")) {
						if (startReceiving) {
							receivedReply = true;
							System.out.println("recieved a reply");						}
					}
					else if (arr[0].equals("alive")) {
						String msg3 = "yes";
						byte[] send3 = msg3.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(send3, send3.length,
								InetAddress.getByName(arr[1]), Integer.parseInt(arr[2]));
						serverSocket.send(sendPacket);
					
				}
					else if (msg.equals("yes")) {
						leaderAlive=true;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} else if (this.id == 2) {
			while(true){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (leaderElected && !isLeaderSelf) {
				System.out.println("leader is finally elected");
				Random r = new Random();
				int n = r.nextInt(20000);
				long start = System.currentTimeMillis();
				long end = System.currentTimeMillis();
				while ((end - start) < n) {
					end = System.currentTimeMillis();
				}
				System.out.println("checking if eader is alive");
				String msg2 = "alive," + myAddr + "," + "35000," + count + ",";
				byte[] send2 = msg2.getBytes();
				DatagramPacket sendPacket;
				try {
					sendPacket = new DatagramPacket(send2, send2.length, InetAddress.getByName(leader), leader2);
					serverSocket.send(sendPacket);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				start = System.currentTimeMillis();
				end = System.currentTimeMillis();
				listenAsLeader = true;
				while ((end - start) < 5000) {
					end = System.currentTimeMillis();
				}
				listenAsLeader = false;
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (!leaderAlive) {
					System.out.println("leader is down");
					leaderElected=false;
					receivedReply=false;
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String msg = "election," + myAddr + "," + "35000," + count + ",";
					byte[] send = msg.getBytes();
					for (int i = 0; i < 4; i++) {
						// DatagramPacket sendPacket = new DatagramPacket(send,
						// send.length, list[i], port[i]);
						DatagramPacket sendPacket2;
						try {
							sendPacket2 = new DatagramPacket(send, send.length, list[i],
									port[i]);
							serverSocket.send(sendPacket2);
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					startReceiving = true;
					long start3 = System.currentTimeMillis();
					long end3 = System.currentTimeMillis();
					while ((end3 - start3) < 5000) {
						end3 = System.currentTimeMillis();
					}
					startReceiving = false;
					if (!receivedReply) {
						leaderElected=true; 
						isLeaderSelf=true;
						System.out.println("no replies recieved");
						System.out.println();
						System.out.println(" I AM THE LEADER!!!");
						System.out.println();
						String msg3 = "leader," + myAddr + "," + "35000," + count + ",";
						byte[] send3 = msg3.getBytes();
						for (int i = 0; i < 4; i++) {
							// DatagramPacket sendPacket = new
							// DatagramPacket(send, send.length, list[i],
							// port[i]);
							
							try {
								DatagramPacket sendPacket3 = new DatagramPacket(send3, send3.length,
										list[i], port[i]);
								serverSocket.send(sendPacket3);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

				
				}
				else{
					System.out.println("leader is alive");
					leaderAlive=false;
				}
				}
			}
		}
			else{
				try {
					Thread.sleep(50000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String msg = "election," + myAddr + "," + "35000," + count + ",";
				byte[] send = msg.getBytes();
				for (int i = 0; i < 4; i++) {
					// DatagramPacket sendPacket = new DatagramPacket(send,
					// send.length, list[i], port[i]);
					DatagramPacket sendPacket2;
					try {
						sendPacket2 = new DatagramPacket(send, send.length, list[i],
								port[i]);
						serverSocket.send(sendPacket2);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				startReceiving = true;
				long start3 = System.currentTimeMillis();
				long end3 = System.currentTimeMillis();
				while ((end3 - start3) < 5000) {
					end3 = System.currentTimeMillis();
				}
				startReceiving = false;
				if (!receivedReply) {
					leaderElected=true; 
					isLeaderSelf=true;
					System.out.println("no replies recieved");
					System.out.println();
					System.out.println(" I AM THE LEADER!!!");
					System.out.println();
					String msg3 = "leader," + myAddr + "," + "35000," + count + ",";
					byte[] send3 = msg3.getBytes();
					for (int i = 0; i < 4; i++) {
						// DatagramPacket sendPacket = new
						// DatagramPacket(send, send.length, list[i],
						// port[i]);
						
						try {
							DatagramPacket sendPacket3 = new DatagramPacket(send3, send3.length,
									list[i], port[i]);
							serverSocket.send(sendPacket3);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}

			
			}
	}
	}