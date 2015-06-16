package trab4poo.shopserver;

import java.util.Observable;

// Classe abstrata de registro cujos c�digo e m�todos s�o padr�es para Book, User e Rental.
// Serve para deixar mais eficiente o processo de escrita e leitura de dados
// nos arquivos, pois s�o similares para qualquer tipo de item.
abstract class Record extends Observable {
	
	// chave prim�ria; setado pelo sistema quando o item � registrado
	protected int code; 
	
	 // Deve retornar os campos do registro em formato desej�vel para escrita no .csv
	abstract String[] getData();
	
	// Deve receber dados em formato vindo do .csv e coloca-os nos campos do registro
	abstract void setData(String[] dataList);  
}
