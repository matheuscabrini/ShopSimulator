package shopsimulatorT4.server;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// TODO Decidir se amountNeeded ser� needed (pun not intended).
// TODO colocar amountNeeded no email tamb�m?
class Requisition extends Record implements Observer {

	// Dados necess�rios para poder notificar o usu�rio
	int prodCode = 0;
	String userEmail = "";
	String userName = "";
	int amountNeeded = 0; // quantia do produto desejada pelo usu�rio 
	
	public Requisition() {}; // usado por ShopManager
	
	public Requisition(int prodCode, String userEmail, String userName, int amountNeeded) {
		this.prodCode = prodCode;
		this.userEmail = userEmail;
		this.userName = userName;
		this.amountNeeded = amountNeeded;
	}
	
	// Quando houver mudan�a no observable product (ou seja, produto agora est� disponivel)
	// devemos mandar email ao usu�rio notificando-o disto
	@Override
	public void update(Observable product, Object arg) {
		Product prod = (Product) product; 
		
		// checando se h� a quantidade desejada pelo usu�rio
		if (prod.getAmount() >= amountNeeded)
			sendEmail(prod.getName());
	}
	
	private void sendEmail(String prodName) {
		String from = "noreply.shop.bot"; // gmail do servidor
		String pass = "shop.bot"; // senha do gmail
		
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
	    	message.setSubject(prodName + " is now available!");
	    	message.setText("Hello "+userName+","
	    			+ "\n\nYou can now buy \""+prodName+"\" from our shop!"
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
	
	@Override
	String[] getData() {
		ArrayList<String> dataList = new ArrayList<>();

		dataList.add(""+prodCode);
		dataList.add(userEmail);
		dataList.add(userName);
		dataList.add(""+amountNeeded);

		String[] retDataList = dataList.toArray(new String[dataList.size()]);
		return retDataList;
	}

	@Override
	void setData(String[] dataList) {
		int i = 0;
		prodCode = Integer.parseInt(dataList[i]);
		userEmail = dataList[i++];
		userName = dataList[i++];
		amountNeeded = Integer.parseInt(dataList[i]);
	}

	public int getProductCode() {
		return prodCode;
	}
}
