package server;

public class Game implements Runnable {

	public ClientHandler c1 = null;
	public ClientHandler c2 = null;

	private final int DIMENSION = 5;
	private String board = null;
	
	private Server srv;

	/**
	 * Setting up a new game, with two provided clienthandlers (so one handler per client)
	 * These handlers are now coupled to this game, and their status 'playing' is set to 
	 * true as they are now assigned to a game. This way the handlers (and thus clients) 
	 * cannot join another game.
	 * @param c1 clienthandler 1
	 * @param c2 clienthandler 2
	 */
	public Game(ClientHandler c1, ClientHandler c2, Server srv) {
		this.c1 = c1;
		this.c2 = c2;
		this.srv = srv;
		c1.setGame(this);
		c2.setGame(this);
		c1.setPlaying(true);
		c2.setPlaying(true);
	}
	
	@Override
	public synchronized void run() {
		srv.printMessage("________________");
		srv.printMessage("Setting up game board...");
		initializeBoard();
		
		Thread t1 = new Thread(c1);
		Thread t2 = new Thread(c2);
		// TODO notify?
		srv.printMessage("Game starting!");
		t1.start();
		t2.start();
			
		try {
			this.wait(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		srv.printMessage("Game over!");
		srv.endGame(this);
	}

	public void initializeBoard() {
		// TODO String board = (ProtocolMessages.Unoccupied).repeat(DIMENSION*DIMENSION);
		board = "U".repeat(DIMENSION*DIMENSION);
	}
	
	public String getBoard() {
		return board;
	}
}
