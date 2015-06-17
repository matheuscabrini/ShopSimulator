package shopsimulatorT4.client;

// Protocolo de comunicações entre client e server é dado
// pelas seguintes constantes:
public class ClientRequest {
	public static final byte SIGN_UP = 0;
	public static final byte SIGN_IN = 1;
	public static final byte PRODUCTS = 2;
	public static final byte PURCHASE = 3;
	public static final byte REQUISITION = 4;
	public static final byte HANDSHAKE = 5;
}
