package shopsimulatorT4.server;

import java.io.Serializable;
import java.util.ArrayList;

// TODO juntar expday, month e year em uma String só? 
public class Product extends Record implements Serializable {
	
	// Campos presentes nos arquivos:
	private int code = 0; // chave primária; setado pelo sistema quando o item é registrado
	private String name = "";
	private String price = "";
	private int expDay = 0;
	private int expMonth = 0;
	private int expYear = 0;
	private String provider = ""; // ????
	private int amount = 0;
	
	Product() {}; // usado por ShopManager

	public Product(String name, String price, int expDay, int expMonth, 
			int expYear, String provider, int amount) {
		this.name = name;
		this.price = price;
		this.expDay = expDay;
		this.expMonth = expMonth;
		this.expYear = expYear;
		this.provider = provider;
		this.amount = (amount >= 0) ? amount : 0;
	}
	
	@Override
	String[] getData() {
		ArrayList<String> dataList = new ArrayList<>();
		
		dataList.add(""+code);
		dataList.add(name);
		dataList.add(price);
		dataList.add(""+expDay);
		dataList.add(""+expMonth);
		dataList.add(""+expYear);
		dataList.add(provider);
		dataList.add(""+amount);

		String[] retDataList = dataList.toArray(new String[dataList.size()]);
		return retDataList;
	}

	@Override
	void setData(String[] dataList) {
		int i = 0;
		code = Integer.parseInt(dataList[i++]);
		name = dataList[i++];
		price = dataList[i++];
		expDay = Integer.parseInt(dataList[i++]);
		expMonth = Integer.parseInt(dataList[i++]);
		expYear = Integer.parseInt(dataList[i++]);
		provider = dataList[i++];
		amount = Integer.parseInt(dataList[i]);
	}
	
	// Getters e setters:
	
	public int getCode() {
		return code;
	}
	void setCode(int code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}

	public int getExpDay() {
		return expDay;
	}
	public int getExpMonth() {
		return expMonth;
	}
	public int getExpYear() {
		return expYear;
	}
	public String getExpDate() {
		return expDay+"/"+expMonth+"/"+expYear;
	}
	public void setExpDate(int d, int m, int y) {
		this.expDay = d;
		this.expMonth = m;
		this.expYear = y;
	}

	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}

	synchronized void addAmount(int amount) { // utilizado somente por ShopManager
		this.amount += amount;
		
		// Alertando a mudança para os observers deste produto:
		setChanged();
		notifyObservers();
	}
	synchronized void removeAmount(int amount) { // utilizado somente por ShopManager
		this.amount -= amount;
		if (this.amount < 0) this.amount = 0;
	}
	public int getAmount() {
		return amount;
	}
}
