
/* 
 * Client2.java 
 * 
 * Version: 
 *     1.0 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
/**
 * This class is used to represent client in 
 * Google file system.
 * 
 * @author Prasad Chitnis
 **/
public class Client2 extends UnicastRemoteObject implements ClientInt, Runnable {
	static MasterInt m;
	static Registry r;
	static ServerSocket s;
	int id;
	static boolean send = false;
	static int p = 000, chunkC = 0;
	static Client2 client;
	static boolean masterDown = false;
	static String[][] chunkInfo = new String[100][3];

	protected Client2() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	protected Client2(int x) throws RemoteException {
		super();
		this.id = x;
		// TODO Auto-generated constructor stub
	}

	@Override
	/*
	 * This method is used to get the setup complete for the client master
	 * communication
	 */
	public void setup() throws RemoteException, NotBoundException {
		boolean f = false;
		while (!f) {
			try {
				r = LocateRegistry.getRegistry("localhost", 20000);
				m = (MasterInt) r.lookup("Master");
				f = true;
			} catch (Exception e) {
				try {
					r = LocateRegistry.getRegistry("localhost", 21000);
					m = (MasterInt) r.lookup("Master2");
					f = true;
					masterDown = true;
				} catch (Exception e2) {

				}
			}
		}
		System.out.println("communication established");
	}

	/*
	 * Main method
	 */
	public static void main(String args[]) throws IOException, AlreadyBoundException {
		s = new ServerSocket(9500);
		Registry Naming = LocateRegistry.createRegistry(19000);
		client = new Client2();
		Client2 c = new Client2();
		Client2 c2 = new Client2();
		Naming.bind("Client2", c);
		new Thread(new Client2(0)).start();
		new Thread(new Client2(1)).start();
		try {
			c2.setup();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c2.communicate();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void message(String msg) throws RemoteException {
		System.out.println("message recieved is :" + msg);

	}

	@Override
	/*
	 * This method is used when the main master is down so as to make the backup
	 * the current master
	 */
	public void changeMaster(String name, int p) throws RemoteException {
		System.out.println("Change is in order");
		masterDown = true;
		try {
			r = LocateRegistry.getRegistry("localhost", p);
			m = (MasterInt) r.lookup(name);
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	/*
	 * This method is used when the client wants to append data to an existing
	 * file
	 */
	public void append(String s) throws RemoteException {
		String arr[] = new String[2];
		String k2 = "";
		String tempo = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		if (m.isLockAvailable(s)) {
			m.getLock(s);
			int k = 0;
			try {
				int n = m.nChunk(s);
				System.out.println("enter out of " + n + " which chunk you want to append on");
				k = Integer.parseInt(br.readLine());
				System.out.println("Enter the content");
				k2 = br.readLine();
				int tempor = 0;
				boolean flags = false;
				while (tempor < chunkC) {
					if (chunkInfo[tempor][0].equals(s) && chunkInfo[tempor][1].equals(k + "")) {
						flags = true;
						System.out.println("found in previous");
						tempo = chunkInfo[tempor][2];
					}
					tempor++;
				}
				if (!flags) {
					tempo = m.append(s, k);
					chunkInfo[chunkC][0] = s;
					chunkInfo[chunkC][1] = k + "";
					chunkInfo[chunkC][2] = tempo;
					chunkC++;
				}
				System.out.println(tempo);
				arr = tempo.trim().split(",");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Socket sv1 = new Socket(), sv2 = new Socket();
			InputStream i0;
			byte b[] = new byte[64000];
			try {
				boolean f = true;
				try {
					sv1 = new Socket("localhost", Integer.parseInt(arr[0]));
					sv2 = new Socket("localhost", Integer.parseInt(arr[1]));
				} catch (Exception e) {
					f = false;
				}
				if (f) {
					i0 = sv1.getInputStream();
					OutputStream o0 = sv1.getOutputStream();
					DataInputStream in0 = new DataInputStream(i0);
					String msg = "store," + arr[2] + "," + k2;
					o0.write(msg.getBytes());
					sv1.close();
					i0.close();
					o0.close();
					in0.close();

					i0 = sv2.getInputStream();
					OutputStream o = sv2.getOutputStream();
					DataInputStream in = new DataInputStream(i0);
					String msg2 = "appendp," + arr[2] + "," + k2 + "," + arr[0];
					o.write(msg2.getBytes());
					i0.read(b);
					String temp2 = new String(b);
					System.out.println("reply : " + temp2);
					sv2.close();
					i0.close();
					o.close();
					in.close();
				} else {
					try {
						sv1 = new Socket("localhost", Integer.parseInt(arr[0]));
						i0 = sv1.getInputStream();
						OutputStream o0 = sv1.getOutputStream();
						DataInputStream in0 = new DataInputStream(i0);
						String msg = "append," + arr[2] + "," + k2;
						o0.write(msg.getBytes());
						i0.read(b);
						String temp = new String(b);
						System.out.println("reply : " + temp);
						sv1.close();
						i0.close();
						o0.close();
						in0.close();

					} catch (Exception e) {
						sv2 = new Socket("localhost", Integer.parseInt(arr[1]));
						i0 = sv2.getInputStream();
						OutputStream o = sv2.getOutputStream();
						DataInputStream in = new DataInputStream(i0);
						String msg = "append," + arr[2] + "," + k2;
						o.write(msg.getBytes());
						i0.read(b);
						String temp2 = new String(b);
						System.out.println("i write reply " + temp2);
						sv2.close();
						i0.close();
						o.close();
						in.close();
					}
				}
			} catch (Exception e) {
				System.out.println("both the systems are down!!:(");
			}
			m.releaseLock(s);
		} else {
			System.out.println("lock is unavailable");
		}
	}

	@Override
	/*
	 * This method is used when the client wants to overwrite data to an
	 * existing file
	 */
	public void overWrite(String s) throws RemoteException {
		String arr[] = new String[2];
		String k2 = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		if (m.isLockAvailable(s)) {
			m.getLock(s);
			int k = 0;
			try {
				int n = m.nChunk(s);
				System.out.println("enter out of " + n + " which chunk you want to write on");
				k = Integer.parseInt(br.readLine());
				System.out.println("Enter the content");
				k2 = br.readLine();
				String tempo = "";
				int tempor = 0;
				boolean flags = false;
				while (tempor < chunkC) {
					if (chunkInfo[tempor][0].equals(s) && chunkInfo[tempor][1].equals(k + "")) {
						flags = true;
						System.out.println("found in previous");
						tempo = chunkInfo[tempor][2];
					}
					tempor++;
				}
				if (!flags) {
					tempo = m.append(s, k);
					chunkInfo[chunkC][0] = s;
					chunkInfo[chunkC][1] = k + "";
					chunkInfo[chunkC][2] = tempo;
					chunkC++;
				}
				arr = tempo.trim().split(",");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Socket sv1 = new Socket(), sv2 = new Socket();
			InputStream i0;
			byte b[] = new byte[64000];
			try {
				boolean f = true;
				try {
					sv1 = new Socket("localhost", Integer.parseInt(arr[0]));
					sv2 = new Socket("localhost", Integer.parseInt(arr[1]));
				} catch (Exception e) {
					f = false;
				}
				if (f) {
					i0 = sv1.getInputStream();
					OutputStream o0 = sv1.getOutputStream();
					DataInputStream in0 = new DataInputStream(i0);
					String msg = "store," + arr[2] + "," + k2;
					o0.write(msg.getBytes());
					sv1.close();
					i0.close();
					o0.close();
					in0.close();

					i0 = sv2.getInputStream();
					OutputStream o = sv2.getOutputStream();
					DataInputStream in = new DataInputStream(i0);
					String msg2 = "writep," + arr[2] + "," + k2 + "," + arr[0];
					o.write(msg2.getBytes());
					i0.read(b);
					String temp2 = new String(b);
					System.out.println("reply : " + temp2);
					sv2.close();
					i0.close();
					o.close();
					in.close();
				} else {
					try {
						sv1 = new Socket("localhost", Integer.parseInt(arr[0]));
						i0 = sv1.getInputStream();
						OutputStream o0 = sv1.getOutputStream();
						DataInputStream in0 = new DataInputStream(i0);
						String msg = "write," + arr[2] + "," + k2;
						o0.write(msg.getBytes());
						i0.read(b);
						String temp = new String(b);
						System.out.println("reply : " + temp);
						sv1.close();
						i0.close();
						o0.close();
						in0.close();

					} catch (Exception e) {
						sv2 = new Socket("localhost", Integer.parseInt(arr[1]));
						i0 = sv2.getInputStream();
						OutputStream o = sv2.getOutputStream();
						DataInputStream in = new DataInputStream(i0);
						String msg = "write," + arr[2] + "," + k2;
						o.write(msg.getBytes());
						i0.read(b);
						String temp2 = new String(b);
						System.out.println("reply : " + temp2);
						sv2.close();
						i0.close();
						o.close();
						in.close();
					}
				}
			} catch (Exception e) {
				System.out.println("both the systems are down!!:(");
			}
			m.releaseLock(s);
		} else {
			System.out.println("lock is unavailable");
		}
	}

	@Override
	/*
	 * This method is used when the client wants to create a new file
	 */
	public void create(String s) throws RemoteException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the content");
		try {
			String k = br.readLine();
			m.create(s, k);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	/*
	 * This method is used when the client wants to read data from an existing
	 * file
	 */
	public void read(String s) throws UnknownHostException, RemoteException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int n = m.nChunk(s);
		System.out.println(s);
		System.out.println(
				"enter out of " + n + " which chunk you want to read on and enter -1 to read the complete file");
		int k = 0;
		try {
			k = Integer.parseInt(br.readLine());
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (k == -1) {
			String s2 = m.readF(s, n);
			System.out.println(s2);
		} else {

			String tempo = "";
			int tempor = 0;
			boolean flags = false;
			while (tempor < chunkC) {
				if (chunkInfo[tempor][0].equals(s) && chunkInfo[tempor][1].equals(k + "")) {
					flags = true;
					System.out.println("found in previous");
					tempo = chunkInfo[tempor][2];
				}
				tempor++;
			}
			if (!flags) {
				tempo = m.read(s, k);
				System.out.println("added " + tempo);
				chunkInfo[chunkC][0] = s;
				chunkInfo[chunkC][1] = k + "";
				chunkInfo[chunkC][2] = tempo;
				chunkC++;
			}
			String arr[] = tempo.trim().split(",");
			System.out.println(tempo);
			Socket sv;
			InputStream i0;
			byte b[] = new byte[64000];
			try {
				try {
					sv = new Socket("localhost", Integer.parseInt(arr[0]));
				} catch (Exception e) {
					System.out.println("houston we have a problem");
					sv = new Socket("localhost", Integer.parseInt(arr[1]));
				}

				i0 = sv.getInputStream();
				OutputStream o0 = sv.getOutputStream();
				DataInputStream in0 = new DataInputStream(i0);
				String msg = "read," + arr[2];
				o0.write(msg.getBytes());
				i0.read(b);
				String t = new String(b);
				System.out.println("reply : " + t);
				sv.close();
				i0.close();
				o0.close();
				in0.close();

			} catch (Exception e) {
				System.out.println("both the systems are down!!:(");
			}

		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (this.id == 0) {
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
					System.out.println(" ok " + s);
					s = s.trim();
					// then check whether it was read write or what
					send = true;
					p = 10000;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if (send) {
					System.out.println("in run");
					Socket client0;
					try {
						client0 = new Socket("localhost", p);
						InputStream i0 = client0.getInputStream();
						OutputStream o0 = client0.getOutputStream();
						DataInputStream in0 = new DataInputStream(i0);
						o0.write("got address from sv".getBytes());
						client0.close();
						i0.close();
						o0.close();
						in0.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					send = false;
				}
			}
		}

	}

	@Override
	/*
	 * This method is used to decide the client's next step
	 */
	public void communicate() throws RemoteException, NotBoundException {
		boolean f = true;
		int ch;
		String s = "", fn = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (f) {
			if (!masterDown) {
				System.out.println("Enter 1-Read file,2-Write,3-Append , 4-Create and 5 to leave");
				try {
					ch = Integer.parseInt(br.readLine());
					if (!masterDown) {
						System.out.println("Enter  file name");
						fn = br.readLine();
						if (ch == 1) {
							client.read(fn);
						} else if (ch == 2) {
							client.overWrite(fn);
						} else if (ch == 3) {
							client.append(fn);
						} else if (ch == 4) {
							client.create(fn);
						} else {
							f = false;
						}
					} else {
						System.out.println("master is down");
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("Enter 1-Read file,2-to leave");
				try {
					ch = Integer.parseInt(br.readLine());
					System.out.println("Enter  file name");
					fn = br.readLine();
					if (ch == 1) {
						client.read(fn);
					} else if (ch == 2) {
						f = false;
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}