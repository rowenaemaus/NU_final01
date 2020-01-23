package client;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;
import exceptions.ServerUnavailableException;
import goGame.GoGame;
import player.HumanPlayer;
import player.Player;

public class PlayerHandler implements Runnable {

	private Client client;
	private Player player;
	private GoGame go;
	private boolean colour;
	private String playerName;

	public PlayerHandler(Client client) {
		this.client = client;
		colour = false;
		player = new HumanPlayer();
	}

	@Override
	public void run() {
		playerName = player.getName();

		while(client.getPlaying()) {
			boolean validMove = false;
			while(!validMove) {
				toPlayer("Please enter a move: ");
				String move = player.determineMove();
				validMove = go.checkValidity(move); 
			}
		}

		// TODO Auto-generated method stub

	}	

	public void startGame(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String board = cmds.length > 1 ? cmds[1] : null;
		String colour = cmds.length > 2 ? cmds[2] : null;

		try {
			if (board == null || colour == null) {
				client.printMessage("ERROR: Invalid colour and board arguments!");
			} else {
				go = new GoGame((int) Math.sqrt(board.length()), this);
				go.newBoard(board);
				go.setColour(colour);
				player.setColour(colour);
			}
		} catch (InvalidColourException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void doTurn(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String board = cmds.length > 1 ? cmds[1] : null;

		if (board == null) {
			client.printMessage("ERROR: Invalid board arguments!");
			return;
		}

		boolean valid = false;
		String move = null;

		do {
			client.printMessage(String.format("%s, please determine a move!", client.getName()));
			move = player.determineMove();
			valid = go.checkValidity(move);
			client.printMessage("Move valid: " + valid);
		} while (!valid);

		try {
			client.sendMessage(ProtocolMessages.MOVE + ProtocolMessages.DELIMITER + move.toString());
		} catch (ServerUnavailableException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void handleResult(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String valid = cmds.length > 1 ? cmds[1] : null;
		String board = cmds.length > 2 ? cmds[2] : null;

		if (valid == null || board == null) {
			client.printMessage("ERROR: Invalid result message received from server!");
			return;
		}

		if (valid.equals(Character.toString(ProtocolMessages.VALID))) {
			client.printMessage("Server approved your move.");
			go.updateBoard(board);	
		} else if (valid.equals(Character.toString(ProtocolMessages.INVALID))){
			client.printMessage("Server disapproves your move, very disappointing.");
		}	
	}

	// game end
	// PROTOCOL.end + PROTOCOL.delimiter + PROTOCOL.reasonEnd + PROTOCOL.delimiter + winner (als PROTOCOL.W/B) + PROTOCOL.delimiter + scoreBlack (String, parse to integer) + PROTOCOL.delimiter + scoreWhite (String, parse to integer)
	// andere client stopt:
	// game end + exit
	// verbinging verloren:
	// game end: disconnect
	public void handleEnd(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);

	}

	public void toPlayer(String s) {
		System.out.println(s);
	}


	public Client getClient() {
		return this.client;
	}


}
