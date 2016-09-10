
/* 
 * Master.java 
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
/**
 * This class is used to represent one of the master in 
 * Google file system.
 * 
 * @author Prasad Chitnis
 **/
public class Master extends UnicastRemoteObject implements MasterInt {
	static ClientInt c;
	static MasterInt m2;
	static ChunkInt cv[];
	static Registry r;
	static int clientPort;
	int id;
	String[][] file = new String[100][3];
	String[] chunkS = { "31500", "32500", "33500", "34500", "35500", "36500" };
	// initialize chunk
	LinkedList<String> log = new LinkedList<String>();
	static String[][] fileChunk = new String[100][2];// used to store which
														// chunk is from which
														// file
	static String[][] NoOfChunk = new String[100][2];// used to store the number
														// of chunks a file has
	static String[][] fileLock = new String[100][2];// used to check whether any
													// other client is editing
													// the same file
	static String[][] completeFile = new String[100][3];
	static int count1 = 0, count2 = 0, countL = 0, chunkCount = 0, loadb1 = 0, loadb2 = 3;

	protected Master() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	protected Master(int x) throws RemoteException {
		super();
		this.id = x;
		// TODO Auto-generated constructor stub
	}

	@Override
	/*
	 * This method is used to setup the communication
	 */
	public void setup() throws RemoteException {

		for (int i = 0; i < 100; i++) {
			fileLock[i][0] = "";
			fileLock[i][1] = "0";
			for (int j = 0; j < 3; j++) {
				file[i][j] = "";
			}
			for (int k = 0; k < 2; k++) {
				fileChunk[i][k] = "";
				NoOfChunk[i][k] = "";

			}
		}
		boolean f = false;
		while (!f) {
			try {
				try {
					// if(n!=1){
					r = LocateRegistry.getRegistry("localhost", 10000);
					c = (ClientInt) r.lookup("Client");
					// }
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
					f = true;
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
				}
				
			} catch (Exception e) {

			}
		}
		System.out.println("communication established");
	}

	/*
	 * Main method
	 */
	public static void main(String args[]) throws IOException, AlreadyBoundException {

		Registry Naming = LocateRegistry.createRegistry(20000);
		cv = new ChunkInt[6];
		Master m = new Master();
		Master m2 = new Master();
		Naming.bind("Master", m);
		m2.setup();

	}

	@Override
	public void message(String msg) throws RemoteException {
		System.out.println("message recieved is :" + msg);

	}

	@Override
	public int heartBeat() throws RemoteException {
		return 1;
	}
	// thread will run constanly updating the file...

	@Override
	/*
	 * This method returns information about the chunk servers that store the
	 * file data that the client wants to append on
	 */
	public String append(String s, int d) throws RemoteException {
		log.add("append :" + s);
		for (int i = 0; i < count1; i++) {
			if (file[i][0].equals((s + d).hashCode() + "")) {
				return file[i][1] + "," + file[i][2] + "," + (s + d).hashCode() + ",";
			}
		}
		return "nil";
	}

	@Override
	/*
	 * This method returns information about the chunk servers that store the
	 * file data that the client wants to overWrite
	 */
	public String overWrite(String s, int d) throws RemoteException {
		log.add("overWrite :" + s);
		for (int i = 0; i < count1; i++) {
			if (file[i][0].equals((s + d).hashCode() + "")) {
				return file[i][1] + "," + file[i][2] + "," + (s + d).hashCode() + ",";
			}
		}
		return "nil";
	}

	@Override
	/*
	 * This method is called when the client wants to create a new file
	 */
	public void create(String name, String data) throws RemoteException {
		// create file and write data onto it...
		// split into data
		// for every chunk do the following
		log.add("create :" + name);
		fileLock[countL][0] = name;
		countL++;
		int c = 0;
		if (data.length() < 50000) {

			while (data.length() < 50000) {
				data = data + "01234567+";
				while (data.length() % 10000 != 0) {
					data = data + "o";
				}
			}
		} else {
			if (data.length() % 10000 != 0) {
				data = data + "01234567+";
				while (data.length() % 10000 != 0) {
					data = data + "o";
				}
			}
		}

		// while(data.length()<100000){
		// data=data+"o";
		// }

		int noOfChunks = data.length() / 10000;

		NoOfChunk[chunkCount][0] = name;
		NoOfChunk[chunkCount][1] = noOfChunks + "";
		chunkCount++;
		String m[] = new String[noOfChunks];
		for (int i = 0; i < noOfChunks; i++) {
			m[i] = data.substring(10000 * i, 10000 * (1 + i));
		}

		System.out.println("Number of chunks of the current file" + noOfChunks);
		for (int i = 0; i < noOfChunks; i++) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileChunk[count2][0] = name;
			String chunN = (name + i).hashCode() + "";
			fileChunk[count2][1] = chunN;
			count2++;
			Random r = new Random();
			boolean k = true;
			while (k) {
				try {
					cv[loadb1].create(chunN, m[i]);
					k = false;
				} catch (Exception e) {
					loadb1 = (loadb1 + 1) % 6;
				}
			}
			k = true;
			while (k) {
				try {
					cv[loadb2].create(chunN, m[i]);
					k = false;
				} catch (Exception e) {
					loadb2 = (loadb2 + 1) % 6;
				}
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			file[count1][0] = chunN;
			file[count1][1] = chunkS[loadb1];
			file[count1][2] = chunkS[loadb2];
			completeFile[count1][0] = chunN;
			completeFile[count1][1] = loadb1 + "";
			completeFile[count1][2] = loadb2 + "";
			count1++;
			loadb1 = (loadb1 + 1) % 6;
			loadb2 = (loadb2 + 1) % 6;
		}

	}

	@Override
	/*
	 * This method returns information about the chunk servers that store the
	 * file data that the client wants to read
	 */
	public String read(String s, int d) throws RemoteException {
		log.add("read :" + s);
		for (int i = 0; i < count1; i++) {
			if (file[i][0].equals((s + d).hashCode() + "")) {
				return file[i][1] + "," + file[i][2] + "," + (s + d).hashCode() + ",";
			}
		}
		return "nil";
	}

	/*
	 * This method returns the complete file when the client wants to read the
	 * complete file
	 */
	public String readF(String s, int n) throws RemoteException {

		String t = "";
		for (int i = 0; i < n; i++) {
			String name = (s + i).hashCode() + "";
			for (int j = 0; j < count1; j++) {
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
		return t;

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
		for (int i = 0; i < chunkCount; i++) {
			if (NoOfChunk[i][0].equals(f)) {
				return Integer.parseInt(NoOfChunk[i][1]);
			}
		}
		return 0;
	}

	@Override
	public String[][] cFile() throws RemoteException {
		// TODO Auto-generated method stub
		return completeFile;
	}
}
