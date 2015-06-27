package shopsimulatorT4.client;

// Possíveis valores de retorno para os métodos
// de sign up / sign in (login) na classe Client:

public enum ReturnValues
{
	SUCCESS,
	ALREADY_IN_USE_ID, 
	NO_SUCH_ID, 
	WRONG_PASSWORD,
	UNKNOWN_ERROR
}