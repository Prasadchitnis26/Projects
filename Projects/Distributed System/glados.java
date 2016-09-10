import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


public class glados implements Runnable {
	static ServerSocket s[]=new ServerSocket[3];
	static String state = "RELEASED";
	static String[] array={"kansas.cs.rit.edu","iowa.cs.rit.edu","newyork.cs.rit.edu"};
	int port;
	int tid, id;
	static int ltime = 0, bal = 1000, rcd = 0;
	static LinkedList<Integer> data = new LinkedList<Integer>();
	static boolean[] fTransfer = new boolean[3];
	static boolean[] fRecieve = new boolean[3];
	static boolean[] fSend = new boolean[3];
	static boolean[] flag = new boolean[3];
	static boolean transmitted = false;
	static File file;
	static FileWriter fw;
	static BufferedWriter bw;
	java.util.Date timeStmp= new java.util.Date();
	public glados(int num, int id, int p) {
		this.id = num;
		this.tid = id;
		this.port = p;
	}
	/*
	 * This function is used to set flags
	 */
		public static void setFlags() {
			fTransfer[0] = false;
			fTransfer[1] = false;
			fTransfer[2] = false;
			fSend[0] = false;
			fSend[1] = false;
			fSend[2] = false;
			fRecieve[0] = false;
			fRecieve[1] = false;
			fRecieve[2] = false;
			flag[0] = false;
			flag[1] = false;
			flag[2] = false;

		}
	/*
	 * Main method
	 */
	public static void main(String args[]) throws IOException {
//		file = new File("/Users/pgc5277/Desktop/namelist.txt");
//		fw = new FileWriter(file);
//		bw = new BufferedWriter(fw);
		setFlags();
		s[0] = new ServerSocket(34000);
		s[1] = new ServerSocket(35000);
		s[2] = new ServerSocket(36000);
		new Thread(new glados(0,1, 31000)).start();
		new Thread(new glados(1,1, 38000)).start();
		new Thread(new glados(2,1, 41000)).start();
		new Thread(new glados(0,2, 31000)).start();
		new Thread(new glados(1,2, 38000)).start();
		new Thread(new glados(2,2, 41000)).start();
		new Thread(new glados(0,3, 31000)).start();
		new Thread(new glados(1,4, 38000)).start();
	}
	@Override
	/*
	 * run()
	 */
	public void run() {
		while (true) {
			if (this.tid == 1) {//listening thread
				//will check for incoming messages
				while (true) {
					Socket client1 = new Socket();
					try {
						client1 = s[this.id].accept();
						InputStream i1 = client1.getInputStream();
						DataInputStream in1 = new DataInputStream(i1);
						OutputStream o1 = client1.getOutputStream();
						byte b[] = new byte[64000];
						i1.read(b);
						String s = new String(b);
						s = s.trim();
						System.out.println(s + " is recieved " + this.id);
						String[] sp = s.split(",");
						if(sp[0].equals("send")){
							bal=bal+100;
							if(ltime<Integer.parseInt(sp[2])){
								ltime=Integer.parseInt(sp[2]);
							}
						}
						if (s.equals("allowed")) {
							System.out.println("permisiion granted by "
						+this.id);
							flag[this.id] = true;
							System.out.println(flag[0] + " : " + flag[1] + " : " + flag[2] + " ");
						} else if (sp[0].equals("grantreqpls")) {
							if(flag[this.id]){
								data.add(this.id);
							}
							else{
							int temp = Integer.parseInt(sp[1]);
							int t = Integer.parseInt(sp[2]);
							if (state.equals("WANT")) {
								if (temp < rcd) {
									System.out.println("lower ltime permission granted");
									fRecieve[this.id] = true;
								} else if (temp == rcd) {
									if (1 < t) {
										data.add(this.id);
									} else {
										System.out.println("id funa permission granted");
										fRecieve[this.id] = true;
									}
								} else {
									data.add(this.id);
								}
							} else if(state.equals("HELD")){
								data.add(this.id);
							}
							else{
								fRecieve[this.id] = true;
							}
						}
					}
					}catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (this.tid == 2) {//sender thread
				while (true) {

					if (fSend[this.id]) {
						byte[] buffer0 = new byte[64000];
						System.out.println("sending request now!! from" + this.id + " to " + this.port);
						try {
							Socket client0 = new Socket(array[this.id], this.port);
							InputStream i0 = client0.getInputStream();
							OutputStream o0 = client0.getOutputStream();
							DataInputStream in0 = new DataInputStream(i0);
							o0.write(("grantreqpls," + rcd + ",1" + ",k").getBytes());
							i0.close();
							o0.close();
							in0.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.out.println("waiting for response from" + this.id + " to " + this.port);

						System.out.println("recieved response");
						fSend[this.id] = false;
					}
					if(fTransfer[this.id]){
						byte[] buffer0 = new byte[64000];
						System.out.println("sending request now!! from" + this.id + " to " + this.port);
						try {
							Socket client0 = new Socket(array[this.id], this.port);
							InputStream i0 = client0.getInputStream();
							OutputStream o0 = client0.getOutputStream();
							DataInputStream in0 = new DataInputStream(i0);
							o0.write(("send," + 100 + "," + ltime+ ",k").getBytes());
							i0.close();
							o0.close();
							in0.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						fTransfer[this.id] = false;
					}
					if (fRecieve[this.id]) {
						try {
							Socket client0 = new Socket(array[this.id], this.port);
							InputStream i0 = client0.getInputStream();
							OutputStream o0 = client0.getOutputStream();
							DataInputStream in0 = new DataInputStream(i0);
							o0.write("allowed".getBytes());
							i0.close();
							o0.close();
							in0.close();
							fRecieve[this.id] = false;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else if (this.tid == 3) {
				while (true) {
					if ((state.equals("WANT")) && !transmitted) {
						fSend[0] = true;
						fSend[1] = true;
						fSend[2] = true;
						transmitted = true;
					}
					if (flag[0] && flag[1] && flag[2]) {
						state = "HELD";
						System.out.println();
						System.out.println("Entering critical section"+System.currentTimeMillis());
						
						transmitted = false;
						// do stuff
						
						System.out.println("Leaving critical section ");
						System.out.println();
						flag[0] = false;
						flag[1] = false;
						flag[2] = false;

						state = "RELEASED";
						if (!data.isEmpty()) {
							System.out.println("Emptying the Queue after exiting critical section ");
							while (!data.isEmpty()) {
								fRecieve[data.pop()] = true;
								;
							}
						}
//						try {
//							bw.write("number1");
//
//							bw.flush();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} else {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while (true) {
					Random r = new Random();
					int s = r.nextInt(4);
					if (s == 0) {
						int amount = (1 + r.nextInt(19)) * 5;
						System.out.println("Amount deposited is " + amount);
						ltime += 1;
						bal += amount;
					} else if (s == 1) {
						int amount = (1 + r.nextInt(19)) * 5;
						if (bal < amount) {
							System.out.println("Insufficient Funds");
						} else {
							System.out.println("Amount withdrawn = " + amount);
							bal -= amount;
							ltime += 1;
						}
					} else if (s == 2) {
						if (bal < 100) {
							System.out.println("Insufficient Funds for money transfer");
						} else {
							int s2 = r.nextInt(3);
							fTransfer[s2]=true;
							System.out.println("Amount transferred is 100 to"+s2);
							bal -= 100;
							ltime += 1;
						}
					} else {
						if (state.equals("RELEASED")) {
							state = "WANT";
							fSend[0] = true;
							fSend[1] = true;
							fSend[2] = true;
							rcd = ltime;
							System.out.println("Wants to enter CS" + ((state.equals("WANT")) && !transmitted));
						}

					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}