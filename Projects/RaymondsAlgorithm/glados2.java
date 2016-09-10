/* 
 * glados2.java 
 * 
 * Version: 
 *     1.0 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
/**
 * This class is used to represent one of the servers
 * in the Raymond's algorithm
 * 
 * @author Prasad Chitnis
 **/
public class glados2 implements Runnable {
	int id;
	static String state = "RELEASED";
	static LinkedList<String> list = new LinkedList<String>();
	static int forward = 0;
	static String myAddr = "glados.cs.rit.edu", holder = "iowa.cs.rit.edu";
	static boolean transmitted = false, hasToken = false, addedToList = false, reqSent = false;
	static ServerSocket s;

	public glados2(int i) {
		this.id = i;
	}

	public static void main(String args[]) throws IOException {
		s = new ServerSocket(35500);
		new Thread(new glados2(0)).start();
		new Thread(new glados2(1)).start();
		new Thread(new glados2(2)).start();
		new Thread(new glados2(3)).start();
	}

	public void run() {
		while (true) {
			if (this.id == 0) {
				System.out.println("Thread has entered 0 " + this.id);
				try {
					Thread.sleep(11000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (true) {
					Socket client1;
					try {
						client1 = s.accept();
						InputStream i1 = client1.getInputStream();
						DataInputStream in1 = new DataInputStream(i1);
						OutputStream o1 = client1.getOutputStream();
						byte b[] = new byte[64000];
						i1.read(b);
						client1.close();
						i1.close();
						o1.close();
						in1.close();
						String s = new String(b);
						s = s.trim();
						if (s.length() > 5) {
							String arr[] = s.split(",");
							list.add(arr[1]);
							System.out.println("received request!! from" + arr[1] + "  list " + list);
							if (!hasToken) {
								forward++;
								System.out.println((forward > 0 && !reqSent && !myAddr.equals(holder)) + " bracket");
							}
						} else if (s.equals("token")) {
							hasToken = true;
							holder = myAddr;
							reqSent = false; // new change
							System.out.println(
									"received token" + (!list.isEmpty()) + " hast" + hasToken + "  state " + state);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			} else if (this.id == 1) {
				System.out.println("Thread has entered 1 " + this.id);
				System.out.println(forward + " " + reqSent);
				try {
					Thread.sleep(11000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if (forward > 0 && !reqSent && !myAddr.equals(holder)) {
						String msg = "request," + myAddr + ",k";
						byte[] buffer0 = new byte[64000];
						try {
							System.out.println("sending request");
							System.out.println("sender:" + myAddr + "   Holder:" + holder);
							Socket client0 = new Socket(holder, 35500);
							InputStream i0 = client0.getInputStream();
							OutputStream o0 = client0.getOutputStream();
							DataInputStream in0 = new DataInputStream(i0);
							o0.write(msg.getBytes());
							client0.close();
							i0.close();
							o0.close();
							in0.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						forward = 0;
						reqSent = true;
					}
				}
			} else if (this.id == 2) {
				System.out.println("Thread has entered 2 " + this.id);
				System.out.println(state + " " + addedToList);
				try {
					Thread.sleep(11000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if (hasToken) {
						if (state.equals("WANT")) {
							if (list.peek().equals(myAddr)) {
								System.out.println(list);
								list.remove();
								System.out.println(list);
								state = "HELD";
								System.out.println("state is held");
								// wait for 5
								// leave
								state = "RELEASED";
								System.out.println("state is released");

							}
						} else {
							if (!list.isEmpty()) {
								System.out.println("list is not empty and state is released");
								String adr = list.remove();
								Socket client2;
								try {
									client2 = new Socket(adr, 35500);
									InputStream i0 = client2.getInputStream();
									OutputStream o0 = client2.getOutputStream();
									DataInputStream in0 = new DataInputStream(i0);
									String msg = "token";
									System.out.println("send token to " + adr);
									hasToken = false;
									holder = adr;
									o0.write(msg.getBytes());
									i0.close();
									o0.close();
									in0.close();
									client2.close();
									System.out.println(list + " if any more remaining   " + !list.isEmpty());
									while (!list.isEmpty()) {
										String adr1 = list.remove();
										try {
											Socket client = new Socket(adr1, 35500);
											InputStream i = client.getInputStream();
											OutputStream o = client.getOutputStream();
											DataInputStream in = new DataInputStream(i);
											String msg1 = "request," + myAddr + ",k";
											o0.write(msg1.getBytes());
											i.close();
											o.close();
											in.close();
											client.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
								}

							}
						}
					}
				}
			} else {
				System.out.println("Thread has entered 3 " + this.id);
				while (true) {
					try {
						System.out.println("GTS");
						Thread.sleep(10000);
						System.out.println("woke up");
						while (true) {
							if ((state.equals("RELEASED") || !state.equals("HELD")) && !state.equals("WANT")) {
								state = "WANT";
								list.add(myAddr);
								System.out.println(list);
								forward++;
								System.out.println("state is want");
								Thread.sleep(10000);

							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

}
