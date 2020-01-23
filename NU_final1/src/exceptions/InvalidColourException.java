package exceptions;

public class InvalidColourException extends Exception {

	private static final long serialVersionUID = -2929441556463858418L;
	
	public InvalidColourException(String msg) {
		super(msg);
	}

}
