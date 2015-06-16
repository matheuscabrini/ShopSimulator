package trab4poo.shopserver;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


// Singleton!!
public class ShopManager {

	private static ShopManager shopMan;
	
	private ArrayList<ClientHandler> clientList;
	
	private ShopManager() throws IOException {
		// TODO Auto-generated constructor stub
	}
	
	public static synchronized ShopManager getInstance() throws IOException {
		if (shopMan == null) 
			shopMan = new ShopManager();
		return shopMan;
	}
	
	public void listenForClients() {
		new Thread(() -> {
			try (ServerSocket server = new ServerSocket(3700)) {
				while (true) {
					Socket sock = server.accept();
					ClientHandler clHandler = new ClientHandler(sock);
					clientList.add(clHandler);
					new Thread(clHandler).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	/*
	 * 
	 * ***** MÉTODOS PRIVADOS ******** 
	 * 
	 */
	
	// Passa os dados do programa os arquivo
	@SuppressWarnings("unused")
	private void writeRecordsToFile(URL fileName, ArrayList<? extends Record> list) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(fileName.getPath(), false),
			',', CSVWriter.NO_QUOTE_CHARACTER, 
			CSVWriter.NO_ESCAPE_CHARACTER, 
			System.getProperty("line.separator"));

		// Montando o vetor de dados sobre o registro, e
		// escrevendo-o no arquivo:
		for (Record rec : list) {
			writer.writeNext(rec.getData());
		}			
		writer.close();
	}
	
	// Passa os dados do arquivo ao programa
	@SuppressWarnings("unused")
	private void getRecordsFromFile(URL fileName, ArrayList<? extends Record> list) {
		CSVReader reader;
		String[] data;
		try {
			reader = new CSVReader(new FileReader(fileName.getPath()), ',', 
				CSVWriter.NO_QUOTE_CHARACTER, 
				CSVWriter.NO_ESCAPE_CHARACTER);
			for (Record rec : list) {
				data = reader.readNext();
				rec.setData(data);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void sendMail(String userName, String userEmail, String product) {
        String from = "noreply.shop.bot";
        String pass = "shop.bot";	
  		
        // Setup do servidor de email
        Properties properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
  		properties.put("mail.smtp.user", from);
	    properties.put("mail.smtp.password", pass);
	    properties.put("mail.smtp.port", "587");
	    properties.put("mail.smtp.auth", "true");

	    Session session = Session.getDefaultInstance(properties);
    	MimeMessage message = new MimeMessage(session);

	    try {
	    	message.setFrom(new InternetAddress(from));
	    	message.addRecipient(Message.RecipientType.TO, 
	    			new InternetAddress(userEmail));
	    	message.setSubject(product + " is now available!");
	    	message.setText("Hello "+userName+","
	    			+ "\n\nYou can now buy \""+product+"\" from our shop!"
	    			+ "\nMake sure to check it out later.\nThanks!"
	    			+ "\n\n----------\nThis is an automated message.");

	    	// Enviando mensagem
	    	Transport transport = session.getTransport("smtp");
	    	transport.connect("smtp.gmail.com", from, pass);
	    	transport.sendMessage(message, message.getAllRecipients());
	    	transport.close();
	    } catch (MessagingException e) {
	    	e.printStackTrace();
	    }
	}
}
