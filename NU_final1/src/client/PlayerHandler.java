package client;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;
import exceptions.ServerUnavailableException;
import goGame.GoGame;
import player.ComputerPlayer;
import player.HumanPlayer;
import player.Player;

public class PlayerHandler {

	private Client client;
	private Player player;
	private GoGame go;
	private boolean colour;
	private String playerName;

	public PlayerHandler(Client client, String playerType) {
		this.client = client;
		
		if (playerType.equals("human")) {
			player = new HumanPlayer();
		} else {
			player = new ComputerPlayer();
		}		
		playerName = player.getName();
	}

	/**
	 * Starts the game, setting the colour for this player and initialising
	 * the board and GUI
	 * 
	 * @param msg
	 */
	public void startGame(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String board = cmds.length > 1 ? cmds[1] : null;
		String colour = cmds.length > 2 ? cmds[2] : null;

		try {
			if (board == null || colour == null) {
				client.printMessage("ERROR: Invalid colour and board arguments!");
			} else { 
				int dimension = (int) Math.sqrt(board.length());
				go = new GoGame(dimension,false);
				go.newBoard(board);
				go.setColour(colour);
				setColour(colour);
				player.setColour(colour);
				if (player instanceof ComputerPlayer) {((ComputerPlayer) player).sendGame(go);;}
			}
		} catch (InvalidColourException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Asks the player for a move to be returned to the server. A move will be asked
	 * until the user provides a valid move.
	 * @param msg The message received from the server containing the current board.
	 */
	public void doTurn(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String board = cmds.length > 1 ? cmds[1] : null;

		if (board == null) {
			client.printMessage("ERROR: Invalid board arguments!");
			return;
		} else {
			go.updateBoard(board);
		}

		boolean valid = false;
		String move = null;

		do {
			client.printMessage(String.format("%s, please determine a move!", client.getName()));
			move = player.determineMove();
			valid = go.checkValidity(move);
			client.printMessage("Your move: " + move + ", valid: " + valid);
		} while (!valid);

		int moveInt = moveInt = Integer.parseInt(move);
		String updatetBoard = go.setStone(moveInt);
		go.setBoard(updatetBoard);
		try {
			client.sendMessage(ProtocolMessages.MOVE + ProtocolMessages.DELIMITER + move);
		} catch (ServerUnavailableException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Processes the feedback from the server about the previous move; whether the 
	 * move was valid and the updated board after the move. 
	 * @param msg The message received from the server containing 'valid' and the board.
	 */
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
			client.printMessage("Server disapproved your move, very disappointing.");
		}	
	}

	/**
	 * Ends the game. The message can indicate the reason of quitting, as well as the scores.
	 * 	PROTOCOL.end + PROTOCOL.delimiter + PROTOCOL.reasonEnd + PROTOCOL.delimiter + winner (PROTOCOL.W/B) + PROTOCOL.delimiter + scoreBlack + PROTOCOL.delimiter + scoreWhite
	 * @param msg
	 */
	public void handleEnd(String msg) {
		client.printMessage("Game ended!");
		client.closeConnection();
	}

	public void setColour(String colour) throws InvalidColourException {
		if (colour.equalsIgnoreCase(Character.toString(ProtocolMessages.WHITE))) 
			this.colour = true;
		else if (colour.equalsIgnoreCase(Character.toString(ProtocolMessages.BLACK))) {
			this.colour = false;
		} else {
			throw new InvalidColourException("ERROR: No valid colour provided to player!");
		}	
	}

	public Client getClient() {
		return this.client;
	}
	
	public GoGame getGame() {
		return this.go;
	}
	
	public boolean getColour() {
		return this.colour;
	}
	
	public void setGame(GoGame go) {
		this.go = go;
	}

	public static void main (String[] args) {
		PlayerHandler p = new PlayerHandler(new Client(), "computer");
		p.startGame("S;UUUUUUUUU;black");
		p.doTurn("T;UUUUUWUUU");
	}

}
