package shopsimulatorT4.client;

// Poss�veis valores de retorno para os m�todos
// de sign up / sign in (login) na classe Client:

public enum ReturnValues
{
	SUCCESS,
	ALREADY_IN_USE_ID, 
	NO_SUCH_ID, 
	WRONG_PASSWORD,
	UNKNOWN_ERROR
}