package shopsimulatorT4.server;

import shopsimulatorT4.shared.CommunicationProtocol;
import shopsimulatorT4.shared.ShoppingCart;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

// TODO se mandar request invalida deve mandar END (terminar conexao) ou n fazer nada?

class ClientHandler implements Runnable {

	private Socket client;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ShopManager shopMan;
	
	// client handler precisa saber ID do usu�rio que est� na conex�o (ap�s login)
	private String userID;
	
	public ClientHandler(Socket client) throws IOException {
		this.client = client;
		shopMan = ShopManager.getInstance();
	}
	
	// Protocolo de comunica��o:
	public void sendResponse(byte res) throws IOException {
		output.writeByte(res);
	}
	public byte receiveRequest() throws IOException {
		return input.readByte();
	}
	@Override
	public void run() {
		try {	
			output = new ObjectOutputStream(client.getOutputStream());
			output.flush(); // flush no header da stream
			input = new ObjectInputStream(client.getInputStream());
			
			// Antes de processar requests, devemos validar a conex�o com o handshake
			byte request = receiveRequest();
			if (CommunicationProtocol.HANDSHAKE != request)
				throw new IOException("Invalid connection");
			
			// Lemos request do client at� que ele envie sinal de
			// fim de conex�o, ou seja, retorno false de processRequest() 
			while (true) {
				 request = receiveRequest();
				 if (processRequest(request) == false)
					 break;
			}
			
			output.close(); // terminando conex�o
		} catch (IOException e) {
			String errorMsg = "Error on connection to user "+userID+" on thread ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// Retorna falso se a conex�o deve terminar, true caso contr�rio
	private boolean processRequest(byte request) {
		switch (request) {			
			case CommunicationProtocol.SIGN_UP:
				receiveSignUp();
				break;
				
			case CommunicationProtocol.SIGN_IN:
				receiveSignIn();
				break;
			
			case CommunicationProtocol.PRODUCTS_LIST:
				sendProducts();
				break;
				
			case CommunicationProtocol.PURCHASE:
				receivePurchases();
				break;
				
			case CommunicationProtocol.END:
				return false;

			default: // request inv�lida (nunca deve acontecer)
				break;
		}
		return true;
	}
	
	// M�todo para receber cadastro de usu�rio.
	// Lemos os dados do client, digitados pelo usu�rio,
	// criamos o novo User e tentamos adicion�-lo ao sistema.
	private void receiveSignUp() {
		User newUser = new User();
		
		try {
			newUser.setName(input.readUTF());
			newUser.setAddress(input.readUTF());
			newUser.setPhone(input.readUTF());
			newUser.setEmail(input.readUTF());
			newUser.setID(input.readUTF());
			newUser.setPassHash(input.readUTF());
			
			if (shopMan.addUser(newUser) == true)
				sendResponse(CommunicationProtocol.SUCCESS);
			else
				sendResponse(CommunicationProtocol.INVALID_ID);				
		} catch (IOException e) {
			String errorMsg = "Error signing up user: "+newUser+" on thread ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// M�todo para receber login de usu�rio no sistema
	private void receiveSignIn() {
		String ID = null, passHash = null;
		
		try {
			ID = input.readUTF();
			passHash = input.readUTF();
			
			// Validando o login
			User u = shopMan.getUserByID(ID);
			if (u == null) 
				sendResponse(CommunicationProtocol.INVALID_ID);
			if (!u.getPassHash().equals(passHash))
				sendResponse(CommunicationProtocol.INVALID_PASS);
			else { 
				sendResponse(CommunicationProtocol.SUCCESS);			
				this.userID = ID;
			}
		} catch (IOException e) {
			String errorMsg = "Error signing in user: "+ID+" on thread ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// M�todo para enviar ao client lista de produtos
	private void sendProducts() {
		try {
			ArrayList<Product> list = (ArrayList<Product>) shopMan.getProducts();
			output.writeObject(list);
			output.flush();
		} catch (IOException e) {
			String errorMsg = "Error on sending products to user "+userID+" on thread ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// M�todo para receber do client um carrinho de compras
	private void receivePurchases() {
		try {
			ShoppingCart cart = (ShoppingCart) input.readObject();
			shopMan.makePurchase(cart);
		} catch (Exception e) {
			String errorMsg = "Error on receiving purchases from: "+userID+" on thread ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
}
