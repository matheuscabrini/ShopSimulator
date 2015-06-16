package trab4poo.shopserver;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//TODO WIP
class Requisition implements Observer {

	String userEmail;
	String userName;
	String product; //?
	
	public Requisition(String userEmail, String userName) {
		this.userEmail = userEmail;
		this.userName = userName;
	}
	
	// Quando houver mudança no observable product (ou seja, produto agora está disponivel)
	// devemos mandar email ao usuário notificando-o disto
	@Override
	public void update(Observable product, Object arg) {
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
