package server;

import Protocol.ProtocolMessages;

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
	public void run() {
		srv.printMessage("________________");
		srv.printMessage("Setting up game...");
		initializeBoard();

		Thread t1 = new Thread(c1, String.format("Handler %s", c1.getName()));
		Thread t2 = new Thread(c2, String.format("Handler %s", c1.getName()));
		
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

	public void handleCommand(ClientHandler caller, String msg) {
		srv.printMessage("[" + caller.getName() + "] Incoming: " + msg);
		Character command = (msg.length() > 0) ? msg.charAt(0) : null;
		if (command == null) return;
		
		if (msg.length() > 1) {	
			if (!(msg.substring(1,1).equals(ProtocolMessages.DELIMITER))) {
				srv.printMessage("Invalid command character");
				return;
			}
		}

		switch (command) {
		case ProtocolMessages.MOVE :
			handleMove(msg);
			break;
		case ProtocolMessages.QUIT :
			srv.printMessage(String.format("[%s] indicated to quit the game!", caller.getName()));
			otherPlayer(caller).toClient(String.format("[%s] indicated to quit the game!", caller.getName()));
			endGame();
			break;			
		default :
			srv.printMessage("Invalid command!");
			break;
		}
	}

	public void handleMove(String msg) {
		// TODO implement
		// PROTOCOL.move + PROTOCOL.delimiter + move
//		otherPlayer(caller).toClient(String.format("> [%s] says: %s", caller.getName(), msg));

	}

	public void endGame() {
		// TODO implement
	}


	public ClientHandler otherPlayer(ClientHandler c) {
		return c.equals(c1) ? c2 :c1;
	}

	public void initializeBoard() {
		// TODO String board = (ProtocolMessages.Unoccupied).repeat(DIMENSION*DIMENSION);
		//		board = "U".repeat(DIMENSION*DIMENSION);
	}

	public String getBoard() {
		return board;
	}

	public ClientHandler getc1() {
		return c1;
	}

	public ClientHandler getc2() {
		return c2;
	}

}
