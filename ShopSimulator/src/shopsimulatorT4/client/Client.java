package shopsimulatorT4.client;

import shopsimulatorT4.shared.CommunicationProtocol;
import shopsimulatorT4.shared.Product;
import shopsimulatorT4.shared.ShoppingCart;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;

public class Client {

	private Socket clientSocket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	public Client(String IP, int port) throws IOException {
		clientSocket = new Socket(IP, port);
		
		System.out.println("abrindo streams...");
		output = new ObjectOutputStream(clientSocket.getOutputStream());
		output.flush(); // flush no header da stream
		input = new ObjectInputStream(clientSocket.getInputStream());	
		
		System.out.println("enviando hshake...");
		sendRequest(CommunicationProtocol.HANDSHAKE);
		System.out.println("enviado o hshake.");
	}
	
	public void closeConnection() throws IOException {
		output.close();
	}
	
	// Protocolo de comunicao:
	public void sendRequest(byte req) throws IOException {
		output.writeByte(req);
	}
	public byte receiveResponse() throws IOException {
		return input.readByte();
	}
	
	public ReturnValues signUp(String name, String address, String phone, 
			String email, String ID, String pass) throws Exception {
		
		String passHash = new String(MessageDigest.getInstance("SHA").digest(pass.getBytes()));
		
		sendRequest(CommunicationProtocol.SIGN_UP);		
		output.writeUTF(name);
		output.writeUTF(address);
		output.writeUTF(phone);
		output.writeUTF(email);
		output.writeUTF(ID);
		output.writeUTF(passHash);
		output.flush();
		
		byte response = receiveResponse();
		if (response == CommunicationProtocol.SUCCESS)
			return ReturnValues.SUCCESS;
		
		if (response == CommunicationProtocol.INVALID_ID)
			return ReturnValues.ALREADY_IN_USE_ID;	
		
		return ReturnValues.UNKNOWN_ERROR;	//nunca deve acontecer
	}
	
	public ReturnValues signIn(String ID, String pass) throws Exception {
		
		// Criptografamos a senha com uma funcao hash antes de envia-la
		String passHash = new String(MessageDigest.getInstance("SHA").digest(pass.getBytes()));
		
		sendRequest(CommunicationProtocol.SIGN_IN); 	
		output.writeUTF(ID);
		output.writeUTF(passHash);
		output.flush();
		
		byte response = receiveResponse();
		if (response == CommunicationProtocol.SUCCESS)
			return ReturnValues.SUCCESS;
		
		if (response == CommunicationProtocol.INVALID_ID)
			return ReturnValues.NO_SUCH_ID;
		
		if (response == CommunicationProtocol.INVALID_PASS)
			return ReturnValues.WRONG_PASSWORD;
		
		return ReturnValues.UNKNOWN_ERROR;	//nunca deve para acontecer
	}
	
	public ArrayList<Product> getProducts() throws Exception {
		sendRequest(CommunicationProtocol.PRODUCTS_LIST);
		System.out.println("recebendo lista de produtos...");
		ArrayList<Product> ret = (ArrayList<Product>) input.readObject();
		System.out.println("recebida a lista de produtos.");
		return ret;
	}
	
	public void sendShoppingCart(ShoppingCart cart) throws IOException {
		sendRequest(CommunicationProtocol.SHOPPING_CART);
		output.writeObject(cart);
		output.flush();
	}
}
