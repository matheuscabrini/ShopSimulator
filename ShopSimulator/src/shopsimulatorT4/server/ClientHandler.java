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

// Classe que � instanciada pelo ShopManager para lidar com cada
// client que conecta no servidor
class ClientHandler implements Runnable {

	private Socket client;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ShopManager shopMan;
	
	private boolean haltFlag; // serve para dizer se a conex�o deve terminar 
	
	// client handler precisa saber ID do usu�rio que est� na conex�o (ap�s login)
	private String clientUserID;
	
	public ClientHandler(Socket client) throws IOException {
		this.client = client;
		shopMan = ShopManager.getInstance();
		haltFlag = false;
	}
	
	// Protocolo de comunica��o:
	public void sendResponse(byte res) throws IOException {
		output.writeByte(res); 
		output.flush();
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
			
			// Antes de processar requests, devemos validar a conexao com o handshake
			byte request = receiveRequest();
			if (CommunicationProtocol.HANDSHAKE != request)
				throw new IOException("Invalid connection");
			
			// Lemos request do client at� o fim da conex�o
			while (!haltFlag) {
				 request = receiveRequest();
				 processRequest(request);
			}
			
			output.close(); // terminando conexao
		} catch (IOException e) {
			String errorMsg = "Error on connection to user "+clientUserID+" on ";
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
				this.clientUserID = ID;
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
			output.reset(); // para n�o mandar a mesma lista, da mesma refer�ncia
			ArrayList<Product> list = (ArrayList<Product>) shopMan.getProducts();
			output.writeObject(list);
			output.flush();
		} catch (IOException e) {
			String errorMsg = "Error on sending products to user "+clientUserID+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
	
	// Metodo para receber do client um carrinho de compras.
	// Tais compras s�o processadas por ShopManager, o qual retorna uma lista
	// de c�digos de produtos que n�o estavam dispon�veis, ou null, caso
	// contr�rio. Depois, tratamos as requisi��es do usu�rio por produtos
	// que j� estavam indispon�veis no client.
	private void receiveShoppingCart() {
		try {
			ShoppingCart cart = (ShoppingCart) input.readObject();
			
			// Avaliando se as compras do cliente s�o v�lidas e efetuando-as
			ArrayList<Integer> failList = (ArrayList<Integer>) shopMan.processPurchases(cart);
			if (failList == null)
				sendResponse(CommunicationProtocol.SUCCESS);
			else { 
				// Retornando lista de produtos inv�lidos para o client
				sendResponse(CommunicationProtocol.INVALID_TRANSACTION);
				output.writeObject(failList);
				output.flush();
			}
				
			// Ativando as requisitions por produtos desejados pelo comprador
			Iterator<Requisition> it = cart.getRequisitions();
			while (it.hasNext()) {
				Requisition r = it.next();
				User u = shopMan.getUserByID(clientUserID);
				r.setUserName(u.getName());
				r.setUserEmail(u.getEmail());
				shopMan.addRequisition(r);
			}
		} catch (Exception e) {
			String errorMsg = "Error on receiving shopping cart from: "+clientUserID+" on ";
			System.err.println(errorMsg + Thread.currentThread());
			e.printStackTrace();
		}
	}
}
