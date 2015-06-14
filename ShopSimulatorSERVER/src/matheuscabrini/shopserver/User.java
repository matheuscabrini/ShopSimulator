package matheuscabrini.shopserver;

import java.util.ArrayList;

// Representação de um usuário
public class User extends Record {	
	
	// Campos presentes nos arquivos:
	private String name;
	private String address;
	private String phone;
	private String email;
	private String ID;
	private String passHash;
	private String passSalt;
	
	public User() {
		
	}

	@Override
	String[] getData() {
		ArrayList<String> dataList = new ArrayList<>();
		
		dataList.add(""+code);
		dataList.add(name);
		dataList.add(address);
		dataList.add(phone);
		dataList.add(email);
		dataList.add(ID);
		dataList.add(passHash);
		dataList.add(passSalt);
		
		String[] retDataList = dataList.toArray(new String[dataList.size()]);
		return retDataList;
	}

	@Override
	void setData(String[] dataList) {
		int i = 0;
		code = Integer.parseInt(dataList[i++]);
		name = dataList[i++];
		address = dataList[i++];
		phone = dataList[i++];
		email = dataList[i++];
		ID = dataList[i++];
		passHash = dataList[i++];
		passSalt = dataList[i];
	}
	
	// Setters e getters:

	void setCode(int code) { // utilizado somente por SystemManager
		this.code = code;
	}
	public int getCode() {
		return code;
	}
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
	void setPassSalt(String passSalt) {
		this.passHash = passSalt;
	}
	String getPassSalt() {
		return passSalt;
	}
}
