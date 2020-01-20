package exceptions;

public class ServerUnavailableException extends Exception {

	private static final long serialVersionUID = -7089936052410448797L;

	public ServerUnavailableException(String msg) {
		super(msg);
	}
	
}
