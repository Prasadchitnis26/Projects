
/* 
 * Master2.java 
 * 
 * Version: 
 *     1.0 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
/**
 * This class is used to represent backup master in 
 * Google file system.
 * 
 * @author Prasad Chitnis
 **/
public class Master2 extends UnicastRemoteObject implements MasterInt, Runnable {
	static MasterInt m;
	static int clientPort;
	static ClientInt c;
	static ChunkInt cv[];
	static Registry r;
	int id;
	static String[][] file = new String[100][3];
	String[] chunkS = { "31500", "32500", "33500", "34500", "35500", "36500" };
	// initialize chunk
	LinkedList<String> log = new LinkedList<String>();
	static String[][] fileChunk = new String[100][2];
	static String[][] NoOfChunk = new String[100][2];
	static String[][] fileLock = new String[100][2];
	static String[][] completeFile = new String[100][3];
	static int count1 = 0, count2 = 0, countL = 0, chunkCount = 0, loadb1 = 0, loadb2 = 3;

	protected Master2() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/*
	 * Main method
	 */
	public static void main(String args[]) throws IOException {
		Registry Naming = LocateRegistry.createRegistry(21000);
		cv = new ChunkInt[6];
		Master2 m = new Master2();
		Master2 m2 = new Master2();
		try {
			Naming.bind("Master2", m);
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        m2.setup();
		
	}

	@Override
	/*
	 * This method is used to setup the communication
	 */
	public void setup() throws RemoteException {
		boolean flag=false;
		while(!flag){try {
			r = LocateRegistry.getRegistry("localhost", 20000);
			m = (MasterInt) r.lookup("Master");
			r = LocateRegistry.getRegistry("localhost", 31000);
			cv[0] = (ChunkInt) r.lookup("Chunk1");
			r = LocateRegistry.getRegistry("localhost", 32000);
			cv[1] = (ChunkInt) r.lookup("Chunk2");
			r = LocateRegistry.getRegistry("localhost", 33000);
			cv[2] = (ChunkInt) r.lookup("Chunk3");
			r = LocateRegistry.getRegistry("localhost", 34000);
			cv[3] = (ChunkInt) r.lookup("Chunk4");
			r = LocateRegistry.getRegistry("localhost", 35000);
			cv[4] = (ChunkInt) r.lookup("Chunk5");
			r = LocateRegistry.getRegistry("localhost", 36000);
			cv[5] = (ChunkInt) r.lookup("Chunk6");
            System.out.println("communication established");
			new Thread(new Master2()).start();
			flag=true;

		} catch (RemoteException e) {
			
		} catch (NotBoundException e) {
			
		}
	}
	}

	@Override
	public void message(String msg) throws RemoteException {
		System.out.println("message recieved is :" + msg);

	}

	@Override
	/*
	 * This method is used to ensure that the main master is up
	 */
	public int heartBeat() throws RemoteException {
		return 1;
	}

	@Override
	/*
	 * Run method
	 */
	public void run() {
		boolean flag = true;
		
		// heartbeat mechanism
		while (flag) {
			try {
				int k = m.heartBeat();
				if (k == 1) {
					fileChunk = m.chunk();
					file = m.fList();
					NoOfChunk = m.fileS();
					completeFile = m.cFile();
				}
			} catch (Exception e) {
				flag = false;
			}
		}

		System.out.println("master is down");
		try {
			r = LocateRegistry.getRegistry("localhost", 10000);
			c = (ClientInt) r.lookup("Client");
			c.changeMaster("Master2", 21000);
			r = LocateRegistry.getRegistry("localhost", 19000);
			c = (ClientInt) r.lookup("Client2");
			c.changeMaster("Master2", 21000);
			System.out.println("chnges mades");
		} catch (NotBoundException e) {
			try {
				r = LocateRegistry.getRegistry("localhost", 19000);
				c = (ClientInt) r.lookup("Client2");
				c.changeMaster("Master2", 21000);
			} catch (RemoteException e1) {
				try {
					r = LocateRegistry.getRegistry("localhost", 10000);
					c = (ClientInt) r.lookup("Client");
					c.changeMaster("Master2", 21000);
				} catch (RemoteException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (NotBoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NotBoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c.changeMaster("Master2", 21000);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	/*
	 * This method is not used as this is the backup master
	 */
	public String append(String name, int d) throws RemoteException {
		return "s";
		// TODO Auto-generated method stub

	}

	@Override
	/*
	 * This method is not used as this is the backup master
	 */
	public String overWrite(String name, int d) throws RemoteException {
		return "s";
		// TODO Auto-generated method stub

	}

	@Override
	/*
	 * This method is not used as this is the backup master
	 */
	public void create(String name, String data) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	/*
	 * This method returns information about the chunk servers that store the
	 * file data that the client wants to read
	 */
	public String read(String s, int d) throws RemoteException {
		log.add("read :" + s);
		for (int i = 0; i < 100; i++) {
			if (file[i][0].equals((s + d).hashCode() + "")) {
				return file[i][1] + "," + file[i][2] + "," + (s + d).hashCode() + ",";

			}

		}
		return "nil";
	}

	@Override
	public String[][] chunk() throws RemoteException {
		// TODO Auto-generated method stub
		return fileChunk;
	}

	@Override
	public String[][] fList() throws RemoteException {
		// TODO Auto-generated method stub
		return file;
	}

	@Override
	public String[][] fileS() throws RemoteException {
		// TODO Auto-generated method stub
		return NoOfChunk;
	}

	@Override
	/*
	 * This method is used to check whether the lock is available on the file
	 * that the user wants to edit
	 */
	public boolean isLockAvailable(String name) throws RemoteException {
		// TODO Auto-generated method stub
		for (int i = 0; i < countL; i++) {
			if (fileLock[i][0].equals(name)) {
				if (fileLock[i][1].equals("0")) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	@Override
	/*
	 * This method is used to check get the lock on the file that the user wants
	 * to edit
	 */
	public void getLock(String name) throws RemoteException {
		for (int i = 0; i < countL; i++) {
			if (fileLock[i][0].equals(name)) {
				fileLock[i][1] = "1";
			}
		}
	}

	@Override
	/*
	 * This method is used to release the lock is on the file that the user
	 * wants to edit
	 */
	public void releaseLock(String name) throws RemoteException {
		for (int i = 0; i < countL; i++) {
			if (fileLock[i][0].equals(name)) {
				fileLock[i][1] = "0";
			}
		}
	}

	@Override
	public int nChunk(String f) throws RemoteException {
			System.out.println(f);
		for (int i = 0; i < 100; i++) {
			if (NoOfChunk[i][0].equals(f)) {
				System.out.println(NoOfChunk[i][1]);
				return Integer.parseInt(NoOfChunk[i][1]);
			}
		}
		return 0;
	}

	@Override
	/*
	 * This method is used to read the complete file when the user wants to
	 */
	public String readF(String s, int n) throws RemoteException {

		String t = "";
		for (int i = 0; i < n; i++) {
			String name = (s + i).hashCode() + "";
			for (int j = 0; j < 100; j++) {
				if (completeFile[j][0] != null) {

					if (completeFile[j][0].equals(name)) {
						int k = Integer.parseInt(completeFile[j][1]);
						try {
							t = t + cv[k].read(name);
						} catch (Exception e) {
							try {
								int k2 = Integer.parseInt(completeFile[j][2]);
								t = t + cv[k2].read(name);
								System.out.println("one system is down" + cv[k2].read(name));
							} catch (Exception e2) {
								System.out.println("both systems down");
								return null;
							}
						}
					}
				}
			}
		}
		System.out.println("sending " + t);
		return t;

	}

	@Override
	public String[][] cFile() throws RemoteException {
		// TODO Auto-generated method stub
		return completeFile;
	}
}
