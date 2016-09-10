import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
/*
 * Client Interface
 */
public interface ClientInt extends Remote {
public  void setup() throws RemoteException, NotBoundException ;
public  void communicate() throws RemoteException, NotBoundException ;
public  void message(String msg) throws RemoteException;
public  void changeMaster(String name,int p) throws RemoteException;
public void append(String s) throws UnknownHostException, RemoteException;
public void overWrite(String s) throws UnknownHostException,RemoteException;
public void create(String s) throws UnknownHostException,RemoteException;
public void read(String s) throws UnknownHostException,RemoteException;

}
