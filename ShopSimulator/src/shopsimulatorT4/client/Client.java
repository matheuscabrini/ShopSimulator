package shopsimulatorT4.client;

import shopsimulatorT4.server.Product;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
/*import java.util.Scanner;
import java.net.UnknownHostException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
*/

@SuppressWarnings("unused")
public class Client {

	private Socket clientSocket;
	
	private void sendRequest(byte req) throws IOException
	{
		clientSocket.getOutputStream().write(req);
	}
	
	private void signUp(String name, String address, String phone, 
			String email, String ID, String pass) throws Exception
	{
		DataOutputStream DOS = new DataOutputStream(clientSocket.getOutputStream());
		String passhash = new String(MessageDigest.getInstance("SHA").digest(pass.getBytes()));
		
		sendRequest(ClientRequest.SIGN_UP);		
		DOS.writeUTF(name + "\n");
		DOS.writeUTF(address + "\n");
		DOS.writeUTF(phone + "\n");
		DOS.writeUTF(email + "\n");
		DOS.writeUTF(ID + "\n");
		DOS.writeUTF(passhash + "\n");
		DOS.flush();
	}
	
	private void signIn(String ID, String pass) throws Exception
	{
		DataOutputStream DOS = new DataOutputStream(clientSocket.getOutputStream());
		String passhash = new String(MessageDigest.getInstance("SHA").digest(pass.getBytes()));
		
		sendRequest(ClientRequest.SIGN_IN); 	
		DOS.writeUTF(ID + "\n");
		DOS.writeUTF(passhash + "\n");
		DOS.flush();
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Product> getProducts() throws Exception
	{
		ArrayList<Product> ret;	//verificar se é possível usar List em vez de ArrayList
		sendRequest(ClientRequest.PRODUCTS);
		
		ObjectInputStream OIS = new ObjectInputStream(clientSocket.getInputStream());
		Object read = OIS.readObject();
		
		ret = (ArrayList<Product>) read;
		return ret;
	}
	
	public void connect(String IP, int port) throws IOException
	{
		clientSocket = new Socket(IP, port);
		sendRequest(ClientRequest.HANDSHAKE);
	}
	
	/*
	public static void main(String[] args) {
		System.out.println("digite user: ");
		Scanner scan = new Scanner(System.in);
		String user = scan.next();
		String line;
		
		try {
			Socket socket = new Socket("localhost", 3700);
			PrintWriter pw = new PrintWriter(
				new OutputStreamWriter(socket.getOutputStream()), true);
			
			pw.println(user);
			
			System.out.println("digite mensagens: ");
			while ((line = scan.next()) != null && !line.isEmpty())
				pw.println(line);	
			
			scan.close();
			pw.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
