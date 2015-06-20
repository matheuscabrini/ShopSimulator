package shopsimulatorT4.server;

import shopsimulatorT4.shared.CommunicationProtocol;
import shopsimulatorT4.shared.Product;
import shopsimulatorT4.shared.Requisition;
import shopsimulatorT4.shared.ShoppingCart;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

class ClientHandler implements Runnable {

	private Socket client;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ShopManager shopMan;
	
	private boolean haltFlag; // serve para dizer se a conex�o deve terminar 
	
	// client handler precisa saber ID do usu�rio que est� na conex�o (ap�s login)
	private String userID;
	
	public ClientHandler(Socket client) throws IOException {
		this.client = client;
		shopMan = ShopManager.getInstance();
		haltFlag = false;
	}
	
	// Protocolo de comuni��o:
	public void sendResponse(byte res) throws IOException {
		output.writeByte(res);
	}
	public byte receiveRequest() throws IOException {
		return input.readByte();
	}
	@Override
	public void run() {
		try {	
			System.out.println("abrindo streams...");
			output = new ObjectOutputStream(client.getOutputStream());
			output.flush(); // flush no header da stream
			input = new ObjectInputStream(client.getInputStream());
			
			System.out.println("recebendo hshake...");
			// Antes de processar requests, devemos validar a conexao com o handshake
			byte request = receiveRequest();
			if (CommunicationProtocol.HANDSHAKE != request)
				throw new IOException("Invalid connection");
			
			System.out.println("lendo requests");
			// Lemos request do client at� o fim da conex�o
			while (!haltFlag) {
				 request = receiveRequest();
				 processRequest(request);
			}
			
			output.close(); // terminando conexao
		} catch (IOException e) {
			String errorMsg = "Error on connection to user "+userID+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	public void halt() {
		haltFlag = true;
	}
	
	// Retorna falso se a conexao deve terminar, true caso contrario
	private void processRequest(byte request) {
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
				
			case CommunicationProtocol.SHOPPING_CART:
				receiveShoppingCart();
				break;
				
			case CommunicationProtocol.END:
				halt();
				return;

			default: // request invalida (nunca deve acontecer)
				break;
		}
	}
	
	// Metodo para receber cadastro de usuario.
	// Lemos os dados do client, digitados pelo usuario,
	// criamos o novo User e tentamos adiciona-lo ao sistema.
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
			String errorMsg = "Error signing up user: "+newUser+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// Metodo para receber login de usuario no sistema
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
			String errorMsg = "Error signing in user: "+ID+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// Metodo para enviar ao client lista de produtos
	private void sendProducts() {
		try {
			ArrayList<Product> list = (ArrayList<Product>) shopMan.getProducts();
			output.writeObject(list);
			output.flush();
		} catch (IOException e) {
			String errorMsg = "Error on sending products to user "+userID+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// Metodo para receber do client um carrinho de compras
	private void receiveShoppingCart() {
		try {
			ShoppingCart cart = (ShoppingCart) input.readObject();
			shopMan.processPurchases(cart);
			
			Iterator<Requisition> it = cart.getRequisitions();
			while (it.hasNext()) {
				shopMan.addRequisition(it.next());
			}
		} catch (Exception e) {
			String errorMsg = "Error on receiving purchases from: "+userID+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
}
