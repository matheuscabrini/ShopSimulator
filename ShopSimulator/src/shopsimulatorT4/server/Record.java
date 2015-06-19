package shopsimulatorT4.server;

import java.util.Observable;

// Classe abstrata de registro cujos código e métodos são padrões para Book, User e Rental.
// Serve para deixar mais eficiente o processo de escrita e leitura de dados
// nos arquivos, pois são similares para qualquer tipo de item.
abstract public class Record extends Observable {
	
	// Método que deve retornar os campos do registro em 
	// formato propício para escrita no .csv
	abstract public String[] getData();
	
	// Método que deve receber dados em formato proveniente
	// do .csv e setar eles nos campos do registro
	abstract public void setData(String[] dataList);  
}
