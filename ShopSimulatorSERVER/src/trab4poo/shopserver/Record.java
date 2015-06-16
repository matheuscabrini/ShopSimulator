package trab4poo.shopserver;

import java.util.Observable;

// Classe abstrata de registro cujos código e métodos são padrões para Book, User e Rental.
// Serve para deixar mais eficiente o processo de escrita e leitura de dados
// nos arquivos, pois são similares para qualquer tipo de item.
abstract class Record extends Observable {
	
	// chave primária; setado pelo sistema quando o item é registrado
	protected int code; 
	
	 // Deve retornar os campos do registro em formato desejável para escrita no .csv
	abstract String[] getData();
	
	// Deve receber dados em formato vindo do .csv e coloca-os nos campos do registro
	abstract void setData(String[] dataList);  
}
