package goGame;

import java.util.HashSet;
import java.util.Set;

import com.nedap.go.gui.GoGUIIntegrator;

import Protocol.ProtocolMessages;
import client.Client;
import client.PlayerHandler;
import exceptions.InvalidColourException;

public class GoGame {

	private GoGUIIntegrator g;
	private int dimension;
	private boolean colour = false; // true = white
	private Set<String> prevBoards = new HashSet<String>();
	private String currentBoard;
	private boolean useGUI = false;

	public GoGame (int dimension, boolean useGUI) {
		this.dimension = dimension;
		this.useGUI = useGUI;
		try {
			setColour("black");
		} catch (InvalidColourException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the GUI for this Go game
	 */
	public void setUpGUI() {
		g = new GoGUIIntegrator(true, true, dimension);
		g.startGUI();
		g.setBoardSize(dimension);
		System.out.println("GUI ready");
	}

	/**
	 * Creates a new board for a new game
	 * @param board
	 */
	public void newBoard(String board) {
		if (useGUI) {setUpGUI();}
		this.currentBoard = board;
	}

	/**
	 * Sets the colour for this game representation
	 * @param colour The colour to be set ("black"/"white")
	 * @throws InvalidColourException
	 */
	public void setColour(String colour) throws InvalidColourException {
		if (colour.equalsIgnoreCase("white")) 
			this.colour = true;
		else if (colour.equalsIgnoreCase("black")) {
			this.colour = false;
		} else {
			throw new InvalidColourException("ERROR: No valid colour provided to player!");
		}
	}

	/**
	 * Checks whether a move is valid to make given the board and 
	 * previous boards.
	 * @param move The one-dimensional index of the move 
	 * @return Whether the move is valid (true if valid, false if invalid)
	 */
	public boolean checkValidity(int move) {
		if(isField(move) && isEmpty(move)) {
			String boardAfterMove = deepCopy();
			boardAfterMove = setStone(move);
			return prevBoards.contains(boardAfterMove) ? false : true;
		} else {
			System.out.println("ERROR: Impossible move!");
			return false;
		}
	}

	/**
	 * Sets a stone at the desired spot
	 * @param move The one-dimensional index to put the stone
	 * @return Returns the resulting board after the move has taken place
	 */
	public String setStone(int move) {
		StringBuilder updatedBoard = new StringBuilder(currentBoard);
		updatedBoard.setCharAt(move, getColourChar());
		if (useGUI) {boardToGUI(updatedBoard.toString());}
		return updatedBoard.toString();
	}

	/**
	 * Adjusts the GUI to match the provided board
	 * @param board The board to be translate to a GUI representation
	 */
	public void boardToGUI(String board) {
		char[] boardChar = board.toCharArray();

		for (int i = 0; i < board.length(); i++) {
			char curChar = boardChar[i];
			if (curChar == ProtocolMessages.BLACK || curChar == ProtocolMessages.WHITE) {
				boolean colour = boardChar[i] == ProtocolMessages.BLACK ? false : true;
				System.out.println("row, column: " + getRow(i) + getColumn(i));
				g.addStone(getRow(i), getColumn(i), colour);
			}
		}
	}

	public String deepCopy() {
		String board = new String(currentBoard);
		return board;
	}

	public int getColumn(int move) {
		return (int) Math.floor(move/dimension);
	}

	public int getRow(int move) {
		return move % dimension;
	}

	public boolean isField(int index) {
		return index >= 0 && index < currentBoard.length();
	}

	public char getField(int i) {
		return currentBoard.charAt(i);
	}

	public boolean isEmpty(int i) {
		return currentBoard.charAt(i) == ProtocolMessages.UNOCCUPIED;
	}

	public void reset() {
		currentBoard = Character.toString(ProtocolMessages.UNOCCUPIED).repeat(dimension*dimension);
	}

	public char getColourChar() {
		return (colour) ? ProtocolMessages.WHITE : ProtocolMessages.BLACK;
	}

	public void updateBoard(String board) {
		this.currentBoard = board;
		if (useGUI) {boardToGUI(board);}
	}

	public boolean emptySpot(int move) {
		return currentBoard.charAt(move) == ProtocolMessages.UNOCCUPIED ? true : false; 
	}


	public static void main (String[] args) {
		
	}
}