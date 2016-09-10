/* 
 * Chunk2.java 
 * 
 * Version: 
 *     1.0 
 * 
 * Revisions: 
 *     $Log$ 
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
/**
 * This class is used to represent one of the chunk server's in 
 * Google file system.
 * 
 * @author Prasad Chitnis
 **/
public class Chunk2 extends UnicastRemoteObject implements ChunkInt,Runnable {
	static MasterInt m;
	static Registry r;
	static ServerSocket s;
	int id;
	static int v=0,counter=0;
	static boolean send=false;
	static int p=000;
	static String filePath=System.getProperty("user.dir");
	static String buffer[][];
	protected Chunk2() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	protected Chunk2(int x) throws RemoteException {
		super();
		this.id=x;
		// TODO Auto-generated constructor stub
	}
	@Override
	/*
	 * This method is used to get the setup complete for the chunk server 
	 * master communication
	 */
	public void setup() throws RemoteException {
		filePath=filePath+"/1/";
		boolean f=false;
		while(!f){
			try{
		r=LocateRegistry.getRegistry("localhost", 20000);
		try {
			m=(MasterInt)r.lookup("Master");
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f=true;
			}catch(Exception e){
				
			}
		}
		System.out.println("communication established");
	}
	public static void main(String args[]) throws IOException, AlreadyBoundException {
		s=new ServerSocket(32500);
		buffer=new String[100][2];
		Registry Naming = LocateRegistry.createRegistry(32000);
		Chunk2 c=new Chunk2();
		Chunk2 c2=new Chunk2();
		Naming.bind("Chunk2", c);
		new Thread(new Chunk2(0)).start();
		//new Thread(new Chunk2(1)).start();
		c2.setup();
		
	}

	/*
	 * This method is used to create a file when requested by the Client
	 * @param: name ,data
	 * name-name of the file
	 * data-data to be stored on the file
	 */
	@Override
	public void create(String name,String data) throws RemoteException {		
		PrintWriter writer;
		try {
			System.out.println(name+" is the name of the chunk that was stored ");
			writer = new PrintWriter(filePath+""+name, "UTF-8");
			writer.println(data);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*
	 * Run method of the Class
	 */
	@Override
	public void run() {
		String reader="",t2="";
		while(true){
	
				Socket client1;
				try {
					client1 = s.accept();
					InputStream i1 = client1.getInputStream();
					DataInputStream in1 = new DataInputStream(i1);
					OutputStream o1 = client1.getOutputStream();
					byte b[] = new byte[64000];
					i1.read(b);
					String s = new String(b).trim();
					String array[]=s.split(",");
					if(array[0].equals("read")){
						//this part is implemented when the Master request's a read operation
						reader="";
						t2="";
						
				            BufferedReader bufferedReader = 
				                new BufferedReader(new FileReader(filePath+""+array[1]));

				            while(( t2=bufferedReader.readLine()) != null) {
				                reader=reader+t2;
				            }   

				            bufferedReader.close();         
				        
						
						
						//lookup the file
				        String array2[]=reader.split("01234567+");    
						o1.write(array2[0].getBytes());
						System.out.println("Data for read operation is sent!!");
					}
					else if(array[0].equals("write")){
						//this part is implemented when the Master request's a write operation
						PrintWriter writer;
						System.out.println();
						array[2]=array[2]+"01234567+";
						while(array[2].length()%10000!=0){
							array[2]=array[2]+"o";
						}
						writer = new PrintWriter(filePath+""+array[1], "UTF-8");
						writer.println(array[2]);
						writer.close();
						o1.write("write done!!".getBytes());
						System.out.println("write operation performed");
					}else if(array[0].equals("append")){
						//this part is implemented when the Master request's a append operation
						BufferedReader bufferedReader = 
			                new BufferedReader(new FileReader(filePath+""+array[1]));

			            while(( t2=bufferedReader.readLine()) != null) {
			                reader=reader+t2;
			            }   

			            bufferedReader.close();   
			            PrintWriter writer;
			            writer = new PrintWriter(filePath+""+array[1], "UTF-8");
			            String array2[]=reader.split("01234567+");
						writer.println(array2[0]+""+array[2]);
						System.out.println(array2[0]+""+array[2]);
						writer.close();
						o1.write("append done".getBytes());
						System.out.println("will send added");}
					else if(array[0].equals("store")){
						//this part is for when the client pushes the data onto the secondary
						buffer[counter][0]=array[1];
						buffer[counter][1]=array[2];
						counter++;
					}
					else if(array[0].equals("writep")){
						//this is used when the client calls the primary replica to make the changes
						PrintWriter writer;
						array[2]=array[2]+"01234567+";
						while(array[2].length()%10000!=0){
							array[2]=array[2]+"o";
						}
						writer = new PrintWriter(filePath+""+array[1], "UTF-8");
						writer.println(array[2]);
						writer.close();
						boolean f=true;
						try{
						Socket client2=new Socket("localhost",Integer.parseInt(array[3]));
						InputStream i2 = client2.getInputStream();
						DataInputStream in2 = new DataInputStream(i2);
						OutputStream o2 = client2.getOutputStream();
						o2.write(("write,"+array[1]+","+array[2]+",").getBytes());
						}catch(Exception e){
							f=false;
						}
						if(f){
						o1.write("ok done!!".getBytes());
						System.out.println("write done on primary");
						}else{
							o1.write("secondary down".getBytes());
							System.out.println("write done on primary");
							
						}
					}else if(array[0].equals("appendp")){
						//this is used when the client calls the primary replica to make the changes
						BufferedReader bufferedReader = 
				                new BufferedReader(new FileReader(filePath+""+array[1]));

				            while(( t2=bufferedReader.readLine()) != null) {
				                reader=reader+t2;
				            }   

				            bufferedReader.close();   
				            PrintWriter writer;
				            writer = new PrintWriter(filePath+""+array[1], "UTF-8");
				            String array2[]=reader.split("01234567+");
							writer.println(array2[0]+""+array[2]);
							System.out.println(array2[0]+""+array[2]);
							writer.close();
							boolean f=true;
							try{
							Socket client2=new Socket("localhost",Integer.parseInt(array[3]));
							InputStream i2 = client2.getInputStream();
							DataInputStream in2 = new DataInputStream(i2);
							OutputStream o2 = client2.getOutputStream();
							o2.write(("append,"+array[1]+","+array[2]+",").getBytes());
							}catch(Exception e){
								f=false;
							}
							if(f){
							o1.write("ok done!!".getBytes());
							System.out.println("write done on primary");
							}else{
								o1.write("secondary down".getBytes());
								System.out.println("write done on primary");
								
							}
						}
					client1.close();
					i1.close();
					o1.close();
					in1.close();
					
					s = s.trim();
					//then check whether it was read write or what
					//send=true;
					p=10500;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		
	}

	/*
	 * This method is used when the master wants to return the complete file
	 */
	@Override
	public String read(String s) throws RemoteException {
		String reader="";
		String t2="";
		
           
			try {
				 BufferedReader bufferedReader;
				bufferedReader = new BufferedReader(new FileReader(filePath+""+s));
				while(( t2=bufferedReader.readLine()) != null) {
				    reader=reader+t2;
				}
				  bufferedReader.close();  
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

     
			String array2[]=reader.split("01234567+");    
             return array2[0];
	}

	
	@Override
	public void storeData(String n, String d) throws RemoteException {
		buffer[counter][0]=n;
		buffer[counter][1]=d;
		counter++;
	}
}


