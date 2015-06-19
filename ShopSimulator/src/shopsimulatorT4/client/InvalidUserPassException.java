package shopsimulatorT4.client;

public class InvalidUserPassException extends IllegalArgumentException {

	public InvalidUserPassException() {
	}

	public InvalidUserPassException(String s) {
		super(s);
	}

	public InvalidUserPassException(Throwable cause) {
		super(cause);
	}

	public InvalidUserPassException(String message, Throwable cause) {
		super(message, cause);
	}

}
