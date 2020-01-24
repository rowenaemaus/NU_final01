package server;

import Protocol.ProtocolMessages;
import goGame.GoGame;

public class Game implements Runnable {

	public ClientHandler c1 = null;
	public ClientHandler c2 = null;

	private GoGame g;
	private int boardDimension = 10; 

	private Server srv;
	
	private ClientHandler turn;

	public Game() {
		
	}
	
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

	/**
	 * Starts the game by creating a thread for each clienthandler and starting them.
	 */
	@Override
	public synchronized void run() {
		srv.printMessage("________________");
		srv.printMessage("Setting up game...");

		setUpGame();

		Thread t1 = new Thread(c1, String.format("Handler %s", c1.getName()));
		Thread t2 = new Thread(c2, String.format("Handler %s", c1.getName()));

		srv.printMessage("Game starting!");
		t1.start();
		t2.start();
		
		c1.sendToClient("G;UUUUUUUUU;black");
		c2.sendToClient("G;UUUUUUUUU;white");
		c1.sendToClient("T;UUUUUUUUU");
		// TODO stuur start game naar beide clients
		this.turn = c1;
		// doe magic van turn
		this.turn = otherPlayer(turn);
		// TODO om en om een beurt sturen en die verwerken
	}

	/**
	 * Sets up the Go game class, which controls the GUI.
	 */
	public void setUpGame() {
		g = new GoGame(boardDimension, true);
		g.newBoard(Character.toString(ProtocolMessages.UNOCCUPIED).repeat(boardDimension*boardDimension));
	}
	

	/**
	 * Handles the incoming commands from the client and passes it according to the 
	 * provided command.
	 * @param caller The clienthandler the message comes from
	 * @param msg The message as received from the client
	 */
	public void handleCommand(ClientHandler caller, String msg) {
		srv.printMessage("[" + caller.getName() + "] Incoming: " + msg);
		Character command = (msg.length() > 0) ? msg.charAt(0) : null;
		if (command == null) {
			srv.printMessage("Invalid command provided to server from [" + caller.getName() + "]");
			return;
		}

		switch (command) {
		case ProtocolMessages.MOVE :
			srv.printMessage("[" + caller.getName() + "] making a move");
			handleMove(caller, msg);
			break;
		case ProtocolMessages.QUIT :
			srv.printMessage(String.format("[%s] indicated to quit the game!", caller.getName()));
			otherPlayer(caller).sendToClient(String.format("[%s] indicated to quit the game!", caller.getName()));
			endGame();
			break;			
		default :
			srv.printMessage("Unhandled command. Received message: '" + msg + "'");
			break;
		}
	}

	/**
	 * Handles the move as sent by the client. 
	 * @param msg The message sent to indicate the move
	 */
	public void handleMove(ClientHandler caller, String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String move = cmds.length > 1 ? cmds[1] : null;
		
		if (move == null) {
			srv.printMessage("Not possible to read move!");
			caller.sendToClient("Not able to read move!");
			return;
		}
		
		int moveInt = Integer.parseInt(move);
		
		boolean valid = g.checkValidity(moveInt);
		
		if (!valid) {
			// TODO protocol proof maken
			caller.sendToClient(ProtocolMessages.END + ProtocolMessages.DELIMITER + "Sorry, invalid move, you lose!");
			otherPlayer(caller).sendToClient(ProtocolMessages.END + ProtocolMessages.DELIMITER + "Sorry, the other player did an invalid move, you win!");
			endGame();
			srv.endGame(this);
		} else {
			//beeindigen
		}
		
		//		otherPlayer(caller).toClient(String.format("> [%s] says: %s", caller.getName(), msg));

	}

	/**
	 * Ends the game
	 */
	public void endGame() {
		srv.printMessage("Game over!");
		srv.endGame(this);
	}

	public ClientHandler otherPlayer(ClientHandler c) {
		return c.equals(c1) ? c2 :c1;
	}

	public ClientHandler getc1() {
		return c1;
	}

	public ClientHandler getc2() {
		return c2;
	}

	public static void main(String[] a) {
		Game game = new Game();
		game.setUpGame();
	}
	
	
}
