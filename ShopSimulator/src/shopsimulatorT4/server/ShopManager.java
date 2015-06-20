package shopsimulatorT4.server;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import shopsimulatorT4.shared.Product;
import shopsimulatorT4.shared.Requisition;
import shopsimulatorT4.shared.ShoppingCart;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

// TODO devo ver se d� pra fazer o programa escrever os csvs fora do bin (classpath, ver txt)
// TODO pensar sobre se o programa est� thread-safe (se n pode ocorrer deadlocks, conflitos etc)

public class ShopManager {

	// Inst�ncia �nica de ShopManager (Singleton Pattern)
	private static ShopManager shopMan;
	
	//Instância de ClientListener (classe interna (Runnable) que espera e lida com novas conexões de clientes)
	private ClientListener clientListener;
	
	//Lista de instâncias de ClientHandlers sendo atualmente executadas em Threads.
	private ArrayList<ClientHandler> activeHandlers;
	
	private final static int SERVER_PORT = 3700;
	
	// Caminhos para os arquivos:
    private final String productsFileName = "CSVs/Products.csv";
    private final String usersFileName = "CSVs/Users.csv";
    private final String reqsFileName = "CSVs/Requisitions.csv";
    private final String countersFileName = "CSVs/Record_Counters.txt";
    
	// Quantias de items registrados no sistema:
	private int nOfProducts = 0;
	private int nOfUsers = 0;
	private int nOfReqs = 0;
	
    // Listas de registros:
	private ArrayList<User> userList;
	private ArrayList<Product> prodList;
	private ArrayList<Requisition> reqList;
	
	// ShopManager segue o Singleton Pattern, logo s�
	// obtemos ele via um m�todo getInstance() 
	public static synchronized ShopManager getInstance() throws IOException {
		if (shopMan == null) 
			shopMan = new ShopManager();
		return shopMan;
	}
	
	// Construtor privado, conforme Singleton Pattern
	private ShopManager() throws IOException {		
		
		// Se o arquivo com as quantidades de registros existir, tais valores 
		// s�o trazidos ao programa. Tamb�m � esperado que exista os arquivos de
		// registros, cujos dados logo ser�o copiados ao programa.	
		
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(countersFileName));

			nOfProducts = Integer.parseInt(br.readLine());
			nOfUsers = Integer.parseInt(br.readLine());
			nOfReqs = Integer.parseInt(br.readLine());
			br.close();
		} catch (FileNotFoundException e) {
			nOfProducts = 0;
			nOfUsers = 0;
			nOfReqs = 0;
		} catch(IOException e) {
			e.printStackTrace();
			throw e;		
		}
			
		
		// Populando as listas com registros preexistentes:
		
		prodList = new ArrayList<>(nOfProducts);
		for (int i = 0; i < nOfProducts; i++) 
			prodList.add(new Product());
		getRecordsFromFile(productsFileName, prodList);
		
		userList = new ArrayList<User>(nOfUsers);
		for (int i = 0; i < nOfUsers; i++) 
			userList.add(new User());
		getRecordsFromFile(usersFileName, userList);

		reqList = new ArrayList<Requisition>(nOfReqs);
		for (int i = 0; i < nOfReqs; i++) 
			reqList.add(new Requisition());
		getRecordsFromFile(reqsFileName, reqList);
		restartObservers(); // reassociando requisitions aos
							// seus respectivos produtos
		
		clientListener = null;	//esse atributo ser null indica que a escuta por novas conexões de clientes não está ativa
		activeHandlers = new ArrayList<ClientHandler>();
	}
	
	// Inicia o listening do servidor por clients em uma thread
	// separada, pois accept() � blocking.
	public synchronized void listenForClients() {
		
		if (clientListener != null)
			return;		//nesse caso, a escuta por clientes já está sendo feita e não é necessário reiniciá-la
		
		clientListener = new ClientListener();
		new Thread(clientListener).start();
	}
	
	//Finaliza o listening por novas conexões de clientes, terminando propriamente a execução da thread responsável
	public synchronized void stopListening()
	{
		if (clientListener == null)
			return;		//nesse caso, o listening não está sendo feito - nada a fazer
		
		clientListener.halt();
		clientListener = null;
	}
	
	public synchronized void close() throws IOException
	{
		stopListening();
		
		for(ClientHandler ch : activeHandlers)
			ch.halt();
		
		saveChangesToFiles();
	}
	
	// Obten��o da lista de produtos	
	public synchronized List<Product> getProducts() {
		return prodList;
	}

	// M�todo para adicionar um produto. N�o pode haver c�digo repetido
	public synchronized boolean addProduct(Product p) {
		if (p == null) return false;
		
		// C�digo do produto � a quantidade atual de produtos
		p.setCode(nOfProducts);
		
		for (Product existingProduct : prodList) { // Checando se c�digo j� existe
			if (existingProduct.getCode() == p.getCode())
				return false;
		}
		
		prodList.add(p);
		nOfProducts++;
		return true;
	}
	
	// M�todo para atualizar estoque de produto por c�digo
	public synchronized void addProductAmount(int prodCode, int amount) {
		Product p = getProductByCode(prodCode);
		p.addAmount(amount);
	}
	
	// M�todo para adicionar um usuário. N�o pode haver ID repetido
	public synchronized boolean addUser(User u) {
		if (u == null) return false;
		
		for (User existingUser : userList) { // Checando se ID j� existe
			if (existingUser.getID().equals(u.getID()))
				return false;
		}
		
		userList.add(u); 
		nOfUsers++;
		return true;
	}
	
	// M�todo para adicionar uma requisição. N�o pode haver requisition repetida
	public synchronized boolean addRequisition(Requisition r) {
		if (r == null || reqList.contains(r)) return false;
		
		Product p = getProductByCode(r.getProductCode());
		if (p == null) return false; // caso o produto da requisition n�o exista

		// Setando a requisition como observer do produto desejado pelo usu�rio
		p.addObserver(r);
		
		reqList.add(r);
		nOfReqs++;
		
		return true;
	}
	
	// M�todo para remover uma requisi��o. Chamado pela pr�pria 
	// requisition ap�s enviar o email ao usu�rio
	public synchronized boolean removeRequisition(Requisition r) {
		if (r == null) return false;
		if (reqList.remove(r) == false) return false;
		nOfReqs--;
		return true;
	}
	
	// M�todos de consulta no sistema
	
	public synchronized Product getProductByCode(int code) {
		if (code < 0) return null;
		for (Product p : prodList) {
			if (p.getCode() == code) return p;
		}
		return null;
	}
	public synchronized User getUserByID(String ID) {
		if (ID == null || ID.isEmpty()) return null;
		for (User u : userList) {
			if (u.getID().equals(ID)) return u;
		}
		return null;
	}
	
	// Esta fun��o atualiza, nos respectivos arquivos, os dados 
	// contidos no programa sobre produtos, usu�rios, requisi��es
	// e suas respectivas quantidades.
	public void saveChangesToFiles() throws IOException {
		writeRecordsToFile(productsFileName, prodList);
		writeRecordsToFile(usersFileName, userList);
		writeRecordsToFile(reqsFileName, reqList);
		
		PrintWriter PW;
		FileWriter FW;
		
		try {
			FW = new FileWriter(countersFileName);
			PW = new PrintWriter(FW);
		} catch (IOException e){
			System.out.println("Erro de escrita!");
			return;
		}
		PW.print(nOfProducts+"\n"+nOfUsers+"\n"+nOfReqs);
		PW.close();
	}

	/*
	 * 
	 * ***** M�TODOS DEFAULT ******** 
	 * 
	 */
	
	// Recebe o carrinho de compras de um client e processa tais compras
	synchronized void processPurchases(ShoppingCart cart) {
		// TODO logging/pdf?
		
		Iterator<ShoppingCart.Purchase> it = cart.getPurchases();
		
		while (it.hasNext()) {
			ShoppingCart.Purchase purchase = it.next();
			Product prod = getProductByCode(purchase.getProdCode());
			if (prod == null) continue;
			prod.removeAmount(purchase.getAmountPurchased());
		}
	}
	
	/*
	 * 
	 * ***** M�TODOS PRIVADOS ******** 
	 * 
	 */
	
	// Passa os dados de uma lista do programa ao seu respectivo arquivo
	private void writeRecordsToFile(String fileName, List<? extends Record> rList) throws IOException {
	    
		FileWriter FW;
		
		try
		{
			FW = new FileWriter(fileName);
		}
		catch(IOException E)
		{
			System.out.println("Erro de escrita!");
			return;
		}
		
		CSVWriter writer = new CSVWriter(FW,
			',', CSVWriter.NO_QUOTE_CHARACTER, 
			CSVWriter.NO_ESCAPE_CHARACTER, 
			System.getProperty("line.separator"));

		// Obtendo o vetor com dados do registro, e
		// escrevendo-o no CSV:
		for (Record record : rList) {
			String [] dataList = record.getData();
			writer.writeNext(dataList);
		}			
		
		writer.close();
	}
	
	// Passa os dados de um dos arquivos � sua respectiva lista dentro do programa
	private void getRecordsFromFile(String fileName, List<? extends Record> rList) throws IOException {
		
		FileReader FR;
		
		try{
			FR = new FileReader(fileName);
		} catch (FileNotFoundException e){
			return;
		}
		
		CSVReader reader = new CSVReader(FR, ',', 
			CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
		
		// Lemos um conjunto de dados do CSV e populamos cada elemento
		// da lista de registros com tais dados
		for (Record record : rList) {
			String [] dataList = reader.readNext();
			record.setData(dataList);
		}
		
		reader.close();
	}
	
	// M�todo a ser chamado logo ap�s a leitura inicial dos CSVs,
	// o qual reassocia os Requisitions (observers) aos seus respectivos
	// produtos (observables)
	private void restartObservers() {
		for (Requisition req : reqList) {
			for (Product prod : prodList) {
				if (prod.getCode() == req.getProductCode())
					prod.addObserver(req);
			}
		}
	}
	
	/*
	 * 
	 * ***** CLASSES INTERNAS ******** 
	 * 
	 */
	
	class ClientListener implements Runnable
	{
		private boolean haltFlag;
		
		ClientListener()
		{
			haltFlag = false;
		}
		
		public void run()
		{
			try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
				server.setSoTimeout(5000);
				while (!haltFlag) {
					
					Socket sock;
					try{
						sock = server.accept();
					}catch (SocketTimeoutException e){
						continue;
					}
					
					ClientHandler clHandler = new ClientHandler(sock);
					new Thread(clHandler).start();
					activeHandlers.add(clHandler);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void halt()
		{
			haltFlag = true;
		}
	}
}
