package shopsimulatorT4.server;

import java.util.Observable;

// Classe abstrata de registro cujos c�digo e m�todos s�o padr�es para Book, User e Rental.
// Serve para deixar mais eficiente o processo de escrita e leitura de dados
// nos arquivos, pois s�o similares para qualquer tipo de item.
abstract public class Record extends Observable {
	
	// M�todo que deve retornar os campos do registro em 
	// formato prop�cio para escrita no .csv
	abstract public String[] getData();
	
	// M�todo que deve receber dados em formato proveniente
	// do .csv e setar eles nos campos do registro
	abstract public void setData(String[] dataList);  
}
