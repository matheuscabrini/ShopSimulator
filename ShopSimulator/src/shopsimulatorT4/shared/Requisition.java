package shopsimulatorT4.shared;

import java.io.Serializable;
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

import shopsimulatorT4.server.Record;
import shopsimulatorT4.server.ShopManager;

// Requisição que um client pode realizar a fim de que seja notificado, via email,
// quando o estoque de um certo produto for reposto.
@SuppressWarnings("serial")
public class Requisition extends Record implements Observer, Serializable {

	// Dados necessários para poder notificar o usuário
	int prodCode = 0;
	String userEmail = "";
	String userName = "";
	
	public Requisition() {}; // usado por ShopManager
	
	public Requisition(int prodCode) {this.prodCode = prodCode;}
	
	// Dados do usuário são setados quando a requisition chega no servidor
	public void setUserName(String userName) {this.userName = userName;}
	public void setUserEmail(String userEmail) {this.userEmail = userEmail;}
	
	// Quando houver mudança no observable product (ou seja, produto agora está disponivel)
	// devemos mandar email ao usuário notificando-o disto
	@Override
	public void update(Observable product, Object arg) {
		Product prod = (Product) product; 

		// Se conseguirmos enviar o email, devemos remover esta 
		// requisition do sistema, bem como o observer do produto
		try {
			sendEmail(prod.getName());
			ShopManager.getInstance().removeRequisition(this);
			prod.deleteObserver(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendEmail(String prodName) throws MessagingException {
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
	}
	
	@Override
	public String[] getData() {
		ArrayList<String> dataList = new ArrayList<>();

		dataList.add(""+prodCode);
		dataList.add(userEmail);
		dataList.add(userName);

		String[] retDataList = dataList.toArray(new String[dataList.size()]);
		return retDataList;
	}

	@Override
	public void setData(String[] dataList) {
		int i = 0;
		prodCode = Integer.parseInt(dataList[i++]);
		userEmail = dataList[i++];
		userName = dataList[i++];
	}

	// Getters:
	public int getProductCode() {return prodCode;}
	public String getUserEmail() {return userEmail;}
	public String getUserName() {return userName;}
	
	// equals() é utilizado para remover Requisition do sistema em ShopManager
	// e para adicioná-la também
	@Override
	public boolean equals(Object other) {
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Requisition))return false;
	    
	    Requisition otherReq = (Requisition) other;
	    if (prodCode == otherReq.getProductCode() &&
	    	userEmail == otherReq.getUserEmail() &&
	    	userName == otherReq.getUserName())
	    	return true;
	    else 
	    	return false;
	}
	
	// Para debug
	@Override
	public String toString() {
		return "prodCode: "+prodCode+"\nuserMail: "+userEmail+"\nuserName: "+userName+"\n";
	}
}
