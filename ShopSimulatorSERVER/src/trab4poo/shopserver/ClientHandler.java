package trab4poo.shopserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

// TODO Fazer nested class em ShopManager? p ter acesso a lista de prods. etc. acho q n precisa

public class ClientHandler implements Runnable {
	private Socket client;
	
	// client handler precisa saber dados do usuário que está na conexão client
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
			while (true) {
				 request = dataIn.readByte();
				 processRequest(request);
			}
		} catch (IOException e) {
			System.out.println(Thread.currentThread().toString());
			e.printStackTrace();
		}
	}
	
	private void processRequest(byte request) throws IOException {
		
		switch (request) {
		case ClientRequest.SIGN_IN:
			
			break;

		default:
			break;
		}
	}
	
	// Método para receber cadastro usuário
	private void receiveSignUp(/*?*/) {
		
	}
	
	// Método para receber login de usuário no sistema
	private void receiveSignIn(/*?*/) {
		
	}
	
	// Método para enviar ao client lista de produtos
	private void sendProducts() {
		
	}
	
	// Método para enviar ao client lista de produtos
	private void receiveRequisition(/*? product*/) {
	
	}
	
	// Método para receber do client uma compra
	private void receivePurchase(/*? product*/) {
		
	}
}
