package shopsimulatorT4.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import shopsimulatorT4.client.ClientRequest;

class ClientHandler implements Runnable {
	private Socket client;
	
	// client handler precisa saber dados do usuário que está na conexão client
	private String userID;
	
	public ClientHandler(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		byte request;
		
		try (DataInputStream dataIn = new DataInputStream(client.getInputStream());) {	
			
			// Antes de processar requests, devemos validar a conexão com o handshake
			request = dataIn.readByte();
			if (ClientRequest.HANDSHAKE != request)
				throw new IOException("Invalid connection");
			
			while (true) {
				 request = dataIn.readByte();
				 processRequest(request, dataIn);
			}
		} catch (IOException e) {
			System.out.println(Thread.currentThread().toString());
			e.printStackTrace();
		}
	}
	
	private void processRequest(byte request, DataInputStream dataIn) {
		
		try {
			switch (request) {			
				case ClientRequest.SIGN_UP:
					receiveSignUp();
					break;
					
				case ClientRequest.SIGN_IN:
					receiveSignIn();
					break;
				
				case ClientRequest.PRODUCTS:
					sendProducts();
					break;
					
				case ClientRequest.PURCHASE:
					receivePurchase();
					break;
					
				case ClientRequest.REQUISITION:
					receiveRequisition();
					break;

				default:
					// request inválida (nunca deve acontecer)
					break;
			}
		} catch (IOException e) {
			System.out.println(Thread.currentThread().toString());
			e.printStackTrace();
		}
	}
	
	// Método para receber cadastro usuário
	private void receiveSignUp(/*?*/) {
		
	}
	
	// Método para receber login de usuário no sistema
	private void receiveSignIn(/*?*/) {
		
	}
	
	// Método para enviar ao client lista de produtos
	private void sendProducts() throws IOException {
		
	}
	
	// Método para enviar ao client lista de produtos
	private void receiveRequisition(/*? product*/) {
		// TODO new requisition aqui mesmo, ou manda pro shopman criar??
	 // product.addObserver(requisition)
	}
	
	// Método para receber do client uma compra
	private void receivePurchase(/*? product*/) {
	// TODO receiveCarrinho() ?????????	
	}
}
