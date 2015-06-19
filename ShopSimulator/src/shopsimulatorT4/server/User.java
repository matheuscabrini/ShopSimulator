package shopsimulatorT4.server;

import java.util.ArrayList;

// Representação de um usuário
public class User extends Record {	
	
	// Campos presentes nos arquivos:
	private String name = "";
	private String address = "";
	private String phone = "";
	private String email = "";
	private String ID = "";
	private String passHash = "";
	
	User() {} // usado por ShopManager
	
	User(String name, String address, String phone, String email,
			String ID, String passHash) {
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.email = email;
		this.ID = ID;
		this.passHash = passHash;
	}

	@Override
	String[] getData() {
		ArrayList<String> dataList = new ArrayList<>();
		
		dataList.add(name);
		dataList.add(address);
		dataList.add(phone);
		dataList.add(email);
		dataList.add(ID);
		dataList.add(passHash);
		
		String[] retDataList = dataList.toArray(new String[dataList.size()]);
		return retDataList;
	}

	@Override
	void setData(String[] dataList) {
		int i = 0;
		name = dataList[i++];
		address = dataList[i++];
		phone = dataList[i++];
		email = dataList[i++];
		ID = dataList[i++];
		passHash = dataList[i];
	}
	
	// Setters e getters:

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAddress() {
		return address;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPhone() {
		return phone;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	
	public void setID(String ID) {
		this.ID = ID;
	}
	public String getID() {
		return ID;
	}
	
	void setPassHash(String passHash) {
		this.passHash = passHash;
	}
	String getPassHash() {
		return passHash;
	}
	
	// Para debug
	@Override
	public String toString() {
		return "ID: "+ID+"\nname: "+name+"\nemail: "+email+"\naddress: "
				+address+"\n"+"\nphone: "+phone+"\nhash: "+passHash+"\n";
	}
}
