package matheuscabrini.shopserver;

import java.util.ArrayList;

public class Product extends Record {
	
	// Campos presentes nos arquivos:
	private String name;
	private String category;
	private String price;
	private int amount;

	public Product(String name, String category, String price, int amount) {
		this.name = name;
		this.category = category;
		this.price = price;
		this.amount = (amount >= 0) ? amount : 0;
	}
	
	@Override
	String[] getData() {
		ArrayList<String> dataList = new ArrayList<>();
		
		dataList.add(""+code);
		dataList.add(name);
		dataList.add(category);
		dataList.add(price);
		dataList.add(""+amount);

		String[] retDataList = dataList.toArray(new String[dataList.size()]);
		return retDataList;
	}

	@Override
	void setData(String[] dataList) {
		int i = 0;
		code = Integer.parseInt(dataList[i++]);
		name = dataList[i++];
		category = dataList[i++];
		price = dataList[i++];
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
