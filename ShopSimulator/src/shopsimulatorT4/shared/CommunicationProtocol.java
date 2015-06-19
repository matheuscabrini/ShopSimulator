package shopsimulatorT4.shared;

// Protocolo de comunica��es entre client e server � dado
// pelas seguintes possibilidades:

public class CommunicationProtocol {
	public static final byte SIGN_UP = 0; // cadastro de novo usu�rio
	public static final byte SIGN_IN = 1; // login em cadastro existente
	public static final byte PRODUCTS_LIST = 2; // envio da lista de produtos
	public static final byte PURCHASE = 3; // envio do carrinho de compras
	public static final byte HANDSHAKE = 4; // in�cio da comunica��o
	public static final byte INVALID_ID = 5; // erro: signin com ID n�o cadastrado ou signup com ID j� existente
	public static final byte INVALID_PASS = 6; // erro: signin com senha errada
	public static final byte SUCCESS = 7; // signup/signin v�lido
	public static final byte END = 8; // termina conex�o 
}
