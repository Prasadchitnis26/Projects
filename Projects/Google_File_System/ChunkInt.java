import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 * Chunkserver Interface
 */
public interface ChunkInt extends Remote {
public void setup() throws RemoteException;
public void create(String name,String data) throws RemoteException;
public String read(String s) throws RemoteException;
public void storeData(String n,String d) throws RemoteException;


}
