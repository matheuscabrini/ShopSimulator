package shopsimulatorT4.client;

import shopsimulatorT4.shared.CommunicationProtocol;
import shopsimulatorT4.shared.Product;
import shopsimulatorT4.shared.ShoppingCart;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.List;

// Realiza as operações de comunicação com o servidor
public class Client {

	private Socket clientSocket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	public Client(String IP, int port) throws IOException {
		clientSocket = new Socket(IP, port);
		
		output = new ObjectOutputStream(clientSocket.getOutputStream());
		output.flush(); // flush no header da stream
		input = new ObjectInputStream(clientSocket.getInputStream());	
		
		sendRequest(CommunicationProtocol.HANDSHAKE); // para validar a conexão
	}
	
	// Protocolo de comunicao:
	private void sendRequest(byte req) throws IOException {
		output.writeByte(req);
		output.flush();
	}
	private byte receiveResponse() throws IOException {
		return input.readByte();
	}
	
	public void closeConnection() throws IOException {
		sendRequest(CommunicationProtocol.END);
		output.close(); // automaticamente fecha as outras
	}
	
	// Manda cadastro de novo user para o server
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
		
		return ReturnValues.UNKNOWN_ERROR;	// nunca deve acontecer
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
		
		return ReturnValues.UNKNOWN_ERROR;	// nunca deve para acontecer
	}
	
	@SuppressWarnings("unchecked")
	public List<Product> getProducts() throws IOException, ClassNotFoundException {
		sendRequest(CommunicationProtocol.PRODUCTS_LIST);
		List<Product> ret = (List<Product>) input.readObject();
		return ret;
	}
	
	// Envia o carrinho de compras do usuário ao servidor. Retorna
	// uma lista de códigos de produtos que não estavam disponíveis
	// no servidor, se esta falha ocorrer. Caso contrário, retorna null.
	@SuppressWarnings("unchecked")
	public List<Integer> sendShoppingCart(ShoppingCart cart) throws IOException, ClassNotFoundException {
		sendRequest(CommunicationProtocol.SHOPPING_CART);
		output.writeObject(cart);
		output.flush();
		
		// Recebendo o resultado das transações do carrinho:
		byte response = receiveResponse();
		if (response == CommunicationProtocol.INVALID_TRANSACTION) {
			List<Integer> ret = (List<Integer>) input.readObject();
			return ret;		
		}
		else // sucesso
			return null;
	}
}
