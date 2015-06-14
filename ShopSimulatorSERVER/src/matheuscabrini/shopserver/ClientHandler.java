package matheuscabrini.shopserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class ClientHandler implements Runnable {
	private Socket client;
	private int code;
	
	public ClientHandler(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
		String line, user;
		
		try {
			BufferedReader br = new BufferedReader(
				new InputStreamReader(client.getInputStream()));
			
			user = br.readLine();
			System.out.println(user+" has entered the room!!!");
			
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) 
					System.out.println("["+user+"] "+line);
			}
			
			System.out.println(user+" has left the room!!!");
			br.close();
			client.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

}
