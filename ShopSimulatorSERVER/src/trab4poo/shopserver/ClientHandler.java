package trab4poo.shopserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

// TODO Fazer nested class em ShopManager? p ter acesso a lista de prods. etc

public class ClientHandler implements Runnable {
	private Socket client;
	
	// client handler precisa saber dados do usu�rio que est� na conex�o client
	private int userCode;
	
	private enum ClientRequest {
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
		int line;
		String user;
		
		try {
			DataInputStream dataIn = new DataInputStream(client.getInputStream());
			
			user = br.readLine();
			System.out.println(user+" has entered the room!!!");
			
			while ((line = dataIn.readInt()) != null) {
				if (!line.isEmpty()) 
					System.out.println("["+user+"] "+line);
			}
			
			System.out.println(user+" has left the room!!!");
			dataIn.close();
			client.close();
		} catch (IOException e){
			e.printStackTrace();
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
