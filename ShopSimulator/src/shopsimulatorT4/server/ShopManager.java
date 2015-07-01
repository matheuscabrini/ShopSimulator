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

// Gerencia as operações feitas sobre o sistema da loja: compras e requisições feitas 
// por clients pela rede; recupera e escreve os dados nos arquivos .csv; reestoque de produtos e 
// cadastro de usuários e requisições.
public class ShopManager {

	private static int SERVER_PORT; // setado no construtor privado
	
	// Instancia unica de ShopManager (Singleton Pattern)
	private static ShopManager shopMan;
	
	// Instancia de ClientListener (classe interna (Runnable) que espera e lida com novas conexoes de clientes)
	private ClientListener clientListener;
	
	// Lista de instancias de ClientHandlers sendo atualmente executadas em Threads.
	private ArrayList<ClientHandler> activeHandlers;
		
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
	
	// ShopManager segue o Singleton Pattern, logo so
	// obtemos ele via um metodo getInstance() 
	public static synchronized ShopManager getInstance() throws IOException {
		if (shopMan == null) 
			shopMan = new ShopManager();
		return shopMan;
	}
	
	// Construtor privado, conforme Singleton Pattern
	private ShopManager() throws IOException {		
				
		// Se o arquivo com as quantidades de registros existir, tais valores 
		// sao trazidos ao programa. 
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
		} catch (IOException e) {
			e.printStackTrace();
			throw e;		
		}
			
		// Populando as listas com registros dos arquivos, caso existirem:
		
		prodList = new ArrayList<Product>();
		if (nOfProducts > 0) {
			prodList.ensureCapacity(nOfProducts);
			for (int i = 0; i < nOfProducts; i++) 
				prodList.add(new Product());
			
			getRecordsFromFile(productsFileName, prodList);
		}
		
		userList = new ArrayList<User>();
		if (nOfUsers > 0) {
			userList.ensureCapacity(nOfUsers);
			for (int i = 0; i < nOfUsers; i++) 
				userList.add(new User());
			
			getRecordsFromFile(usersFileName, userList);
		}
		
		reqList = new ArrayList<Requisition>();
		if (nOfReqs > 0) {
			reqList.ensureCapacity(nOfReqs);
			for (int i = 0; i < nOfReqs; i++) 
				reqList.add(new Requisition());
			
			getRecordsFromFile(reqsFileName, reqList);
			restartObservers(); // reassociando requisitions aos
								// seus respectivos produtos
		}
		
		clientListener = null;	// esse atributo ser null indica que a escuta por
								// novas conexoes de clientes nao esta ativa
		activeHandlers = new ArrayList<ClientHandler>();
	}
	
	// Inicia o listening do servidor por clients em uma thread
	// separada, pois accept() e blocking.
	public synchronized void listenForClients(int serverPort) {
		SERVER_PORT = serverPort;

		if (clientListener != null)
			return;		// nesse caso, a escuta por clientes ja esta sendo feita e nao e necessario reinicia-la
		
		clientListener = new ClientListener();
		new Thread(clientListener).start();
	}
	
	// Finaliza o listening por novas conexoes de clientes, terminando 
	// propriamente a execucao da thread responsavel
	public synchronized void stopListening() {
		if (clientListener == null)
			return;	// nesse caso, o listening nao esta sendo feito - nada a fazer
		
		clientListener.halt();
		clientListener = null;
	}
	
	// Metodo para liberar os recursos utilizados por ShopManager:
	// fechar o servidor e parar as threads e conexoes dos clients.
	// Além disso, salva os registros de volta para os arquivos.
	public synchronized void close() throws IOException {
		stopListening();
		
		for (ClientHandler ch : activeHandlers)
			ch.halt();		
		
		saveChangesToFiles();
	}
	
	// Obtendo as listas do sistema:	
	public synchronized ArrayList<Product> getProducts() {return prodList;}
	public synchronized ArrayList<User> getUsers() {return userList;}
	public synchronized ArrayList<Requisition> getRequisitions() {return reqList;}

	// Metodo para adicionar um produto. Nao pode haver codigo repetido
	public synchronized boolean addProduct(Product p) {
		if (p == null) return false;
		
		// Codigo do produto eh a quantidade atual de produtos
		p.setCode(nOfProducts);
		
		for (Product existingProduct : prodList) { // Checando se codigo ja existe
			if (existingProduct.getCode() == p.getCode())
				return false;
		}
		
		prodList.add(p);
		nOfProducts++;
		return true;
	}
	
	// Metodo para atualizar estoque de produto por codigo
	public synchronized boolean addProductAmount(int prodCode, int amount) {
		Product p = getProductByCode(prodCode);
		if (p == null) return false;
		p.addAmount(amount);
		return true;
	}
	
	// Metodo para adicionar um usuario. Nao pode haver ID repetido
	public synchronized boolean addUser(User u) {
		if (u == null) return false;
		
		for (User existingUser : userList) { // Checando se ID ja existe
			if (existingUser.getID().equals(u.getID()))
				return false;
		}
		
		userList.add(u); 
		nOfUsers++;
		return true;
	}
	
	// Metodo para adicionar uma requisicao. Nao pode haver requisition repetida
	public synchronized boolean addRequisition(Requisition r) {
		if (r == null || reqList.contains(r)) return false;
		
		Product p = getProductByCode(r.getProductCode());
		if (p == null) return false; // caso o produto da requisition nao exista

		// Setando a requisition como observer do produto desejado pelo usuario
		p.addObserver(r);
		
		reqList.add(r);
		nOfReqs++;
		
		return true;
	}
	
	// Metodo para remover uma requisition. Chamado pela propria 
	// requisition apos enviar o email ao usuario
	public synchronized boolean removeRequisition(Requisition r) {
		if (r == null) return false;
		if (reqList.remove(r) == false) return false;
		nOfReqs--;
		return true;
	}
	
	// Metodos de consulta no sistema
	
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
	
	// Esta funcao atualiza, nos respectivos arquivos, os dados 
	// contidos no programa sobre produtos, usuarios, requisitions
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
	 * ***** METODOS DEFAULT ******** 
	 * 
	 */

	// Recebe o carrinho de compras de um client e processa tais compras.
	// Retorna-se uma lista de codigos de produtos desejados que estavam 
	// indisponíveis no sistema, para poder avisar o comprador da falha 
	// na transacao. Caso contrário, retorna null.
	synchronized List<Integer> processPurchases(ShoppingCart cart) {		
		Iterator<ShoppingCart.Purchase> it = cart.getPurchases();
		ArrayList<Integer> failList = null; 
		
		while (it.hasNext()) {
			ShoppingCart.Purchase purchase = it.next();
			Product prod = getProductByCode(purchase.getProdCode());
			if (prod == null) continue;
			
			if (prod.getAmount() < purchase.getAmountPurchased()) { // produto indisponivel
				if (failList == null) failList = new ArrayList<>();
				failList.add(prod.getCode());				
			}
			else // produto disponivel: efetua-se a transacao
				prod.removeAmount(purchase.getAmountPurchased());
		}
		
		return failList;
	}
	
	/*
	 * 
	 * ***** METODOS PRIVADOS ******** 
	 * 
	 */
	
	// Passa os dados de uma lista do programa ao seu respectivo arquivo
	private void writeRecordsToFile(String fileName, List<? extends Record> rList) throws IOException {
		FileWriter FW;
		try {
			FW = new FileWriter(fileName);
		}
		catch (IOException e) {
			System.out.println("Erro de escrita!");
			return;
		}
		
		CSVWriter writer = new CSVWriter(FW, ',', '"', '\\',  
			System.getProperty("line.separator"));

		// Obtendo o vetor com dados do registro, e
		// escrevendo-o no CSV:
		for (Record record : rList) {
			String [] dataList = record.getData();
			writer.writeNext(dataList);
		}			
		
		writer.close();
	}
	
	// Passa os dados de um dos arquivos para sua respectiva lista dentro do programa
	private void getRecordsFromFile(String fileName, List<? extends Record> rList) throws IOException {
		FileReader FR;
		try {
			FR = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			return;
		}
		
		CSVReader reader = new CSVReader(FR, ',', '"', '\\');
		
		// Lemos um conjunto de dados do CSV e populamos cada elemento
		// da lista de registros com tais dados
		for (Record record : rList) {
			String [] dataList = reader.readNext();
			record.setData(dataList);
		}
		
		reader.close();
	}
	
	// Metodo a ser chamado logo apos a leitura inicial dos CSVs,
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
	
	class ClientListener implements Runnable {
		private boolean haltFlag;
		
		ClientListener() {
			haltFlag = false;
		}
		
		@Override
		public void run() {
			try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
				server.setSoTimeout(10000);
				
				while (!haltFlag) {
					Socket sock;
					try {
						sock = server.accept();
					} catch (SocketTimeoutException e) {
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
		
		public void halt() {
			haltFlag = true;
		}
	}
}
