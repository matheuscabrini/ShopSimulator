package shopsimulatorT4.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import shopsimulatorT4.shared.ShoppingCart;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

// TODO DECIDIR sobre retorno das listas
// TODO devo ver se d� pra fazer o programa escrever os csvs fora do bin (classpath, ver txt)
// TODO pensar sobre se o programa est� thread-safe (se n pode ocorrer deadlocks, conflitos etc)
// TODO MAKEPURCHASE() (decidir iterator iterable etc.)

public class ShopManager {

	// Inst�ncia �nica de ShopManager (Singleton Pattern)
	private static ShopManager shopMan;
	
	private final static int SERVER_PORT = 3700;
	
	// Caminhos para os arquivos:
    private final String productsFileName = "/CSVs/Products.csv";
    private final String usersFileName = "/CSVs/Users.csv";
    private final String reqsFileName = "/CSVs/Requisitions.csv";
    private final String countersFileName = "/CSVs/Record_Counters.txt";
    
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
	    URL countersFileURL = this.getClass().getResource(countersFileName);
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(countersFileURL.getPath()));

			nOfProducts = Integer.parseInt(br.readLine());
			nOfUsers = Integer.parseInt(br.readLine());
			nOfReqs = Integer.parseInt(br.readLine());
			br.close();
		} catch (IOException e) {
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
	}
	
	// Inicia o listening do servidor por clients em uma thread
	// separada, pois accept() � blocking.
	public void listenForClients() {
		new Thread(() -> {
			try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
				while (true) {
					Socket sock = server.accept();
					ClientHandler clHandler = new ClientHandler(sock);
					new Thread(clHandler).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
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
	
	// M�todo para adicionar um produto. N�o pode haver ID repetido
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
	
	// M�todo para adicionar um produto. N�o pode haver requisition repetida
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
	synchronized boolean removeRequisition(Requisition r) {
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
		
	    URL countersFileURL = this.getClass().getResource(countersFileName);
		PrintWriter pw = new PrintWriter(
				new FileWriter(countersFileURL.getPath()));
		pw.print(nOfProducts+"\n"+nOfUsers+"\n"+nOfReqs);
		pw.close();
	}

	/*
	 * 
	 * ***** M�TODOS DEFAULT ******** 
	 * 
	 */
	
	synchronized void makePurchase(ShoppingCart cart) {
		// 	TODO 
		
	}
	
	/*
	 * 
	 * ***** M�TODOS PRIVADOS ******** 
	 * 
	 */
	
	// Passa os dados de uma lista do programa ao seu respectivo arquivo
	private void writeRecordsToFile(String fileName, List<? extends Record> rList) throws IOException {
	    URL fileURL = this.getClass().getResource(fileName);
		CSVWriter writer = new CSVWriter(new FileWriter(fileURL.getPath(), false),
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
	    URL fileURL = this.getClass().getResource(fileName);
		CSVReader reader = new CSVReader(new FileReader(fileURL.getPath()), ',', 
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
}
