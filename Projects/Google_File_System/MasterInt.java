import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 * Master server Interface
 */
public interface MasterInt extends Remote {
	public void setup() throws RemoteException;
	public  void message(String msg) throws RemoteException;
	public int heartBeat() throws RemoteException;
	public String append(String name,int id) throws RemoteException;
	public String overWrite(String name,int id) throws RemoteException;
	public void create(String name,String data) throws RemoteException;
	public String read(String name,int id) throws RemoteException;
	public boolean isLockAvailable(String name) throws RemoteException;
	public void getLock(String name) throws RemoteException;
	public void releaseLock(String name) throws RemoteException;
	public String[][] chunk( ) throws RemoteException;
	public String[][] fList( ) throws RemoteException;
	public String[][] fileS( ) throws RemoteException;
	public String[][] cFile( ) throws RemoteException;
	public int nChunk(String f) throws RemoteException;
	public String readF(String s,int n) throws RemoteException;

}
