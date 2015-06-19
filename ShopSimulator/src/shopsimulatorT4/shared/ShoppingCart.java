package shopsimulatorT4.shared;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

// Um carrinho de compras, que cont�m as compras realizadas pelo usu�rio al�m
// de suas requisi��es por notifica��o no reestoque de produto indispon�vel
public class ShoppingCart implements Serializable {
	
	// Conjunto de dados necess�rios para representar uma compra:
	public class Purchase implements Serializable {
		private int prodCode; // c�digo do produto
		private int amountPurchased; // quanto foi comprado dele
		private String prodPrice; // pre�o dele
		
		public Purchase(int prodCode, int amountPurchased, String prodPrice) {
			this.prodCode = prodCode;
			this.amountPurchased = amountPurchased;
			this.prodPrice = prodPrice;
		}
		
		public int getProdCode() {return prodCode;}
		public int getAmountPurchased() {return amountPurchased;}
		public String getProdPrice() {return prodPrice;}
	}

	private ArrayList<Purchase> purchaseList;
	private ArrayList<Requisition> reqList;
	
	public ShoppingCart() {
		purchaseList = new ArrayList<>();
		reqList = new ArrayList<>();
	}
	
	public void addPurchase(int prodCode, int amountPurchased, String prodPrice) {
		purchaseList.add(new Purchase(prodCode, amountPurchased, prodPrice));
	}
	
	public void addRequisition(Requisition r) {
		reqList.add(r);
	}
	
	public Iterator<Purchase> getPurchases() {
		return purchaseList.iterator();
	}
	
	public Iterator<Requisition> getRequisitions() {
		return reqList.iterator();
	}
	
	// Retorna pre�o total da compra
	public String getTotalPrice() {
		double total = 0;
		
		for (Purchase purchase : purchaseList) {
			String priceStr = purchase.getProdPrice().replaceFirst(",", ".");
			double priceD = Double.parseDouble(priceStr);
			total += priceD * purchase.getAmountPurchased();
		}
		
		DecimalFormat format = new DecimalFormat("0.00"); // pra imprimir 2 casas decimais
		return "" + Double.parseDouble(format.format(total));
	}
}
