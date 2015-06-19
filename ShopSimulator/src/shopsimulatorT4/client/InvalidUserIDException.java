package shopsimulatorT4.client;

public class InvalidUserIDException extends IllegalArgumentException {

	public InvalidUserIDException() {
	}

	public InvalidUserIDException(String arg0) {
		super(arg0);
	}

	public InvalidUserIDException(Throwable arg0) {
		super(arg0);
	}

	public InvalidUserIDException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
