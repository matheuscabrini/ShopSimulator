package trab4poo.shopserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

// TODO Fazer nested class em ShopManager? p ter acesso a lista de prods. etc. acho q n precisa

class ClientHandler implements Runnable {
	private Socket client;
	
	// client handler precisa saber dados do usu�rio que est� na conex�o client
	private int userCode;
	
	enum ClientRequest {
		SIGN_UP,
		SIGN_IN,
		PRODUCTS,
		PURCHASE,
		REQUISITION
	}
	
	public ClientHandler(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		byte request;
		
		try (DataInputStream dataIn = new DataInputStream(client.getInputStream());) {	
			
			/* antes de processar requests, devemos validar o user */
			validateUser(/*?*/);
			
			while (true) {
				 request = dataIn.readByte();
				 processRequest(request);
			}
		} catch (IOException e) {
			System.out.println(Thread.currentThread().toString());
			e.printStackTrace();
		}
	}
	
	private boolean validateUser(/*?*/) {
		return false;
	}
	
	private void processRequest(byte request) throws IOException {
		
		ClientRequest req = ClientRequest.values()[request];
		switch (req) {
		
		case SIGN_UP:
			break;
			
		case SIGN_IN:
			break;
		
		case PRODUCTS:
			break;
			
		case PURCHASE:
			break;
			
		case REQUISITION:
			break;

		default:
			//cliente mandou uma request inv�lida...
			break;
		}
	}
	
	// M�todo para receber cadastro usu�rio
	private void receiveSignUp(/*?*/) {
		
	}
	
	// M�todo para receber login de usu�rio no sistema
	private void receiveSignIn(/*?*/) {
		
	}
	
	// M�todo para enviar ao client lista de produtos
	private void sendProducts() {
		
	}
	
	// M�todo para enviar ao client lista de produtos
	private void receiveRequisition(/*? product*/) {
	
	}
	
	// M�todo para receber do client uma compra
	private void receivePurchase(/*? product*/) {
		
	}
}
