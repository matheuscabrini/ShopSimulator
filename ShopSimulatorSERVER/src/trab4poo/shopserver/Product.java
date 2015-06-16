package trab4poo.shopserver;

import java.util.ArrayList;
import java.util.Observable;

public class Product extends Record {
	
	// Campos presentes nos arquivos:
	private String name;
	private String price;
	private int expDay;
	private int expMonth;
	private int expYear;
	private String provider; // ????
	private int amount; // TODO product deve representar um conjunto (dado por amount) 
						// de produtos iguais ou representar um único produto?

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
	
	
	void addUnits(int amount) { // utilizado somente por ShopManager
		this.amount += amount;
	}
	
	void removeUnits(int amount) { // utilizado somente por ShopManager
		this.amount -= amount;
		if (this.amount < 0) this.amount = 0;
	}

	public int getAmount() {
		return amount;
	}

}
