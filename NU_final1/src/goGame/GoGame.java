package goGame;

import java.util.HashSet;
import java.util.Set;

import com.nedap.go.gui.GoGUIIntegrator;

import Protocol.ProtocolMessages;
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
	 * @param colour The colour to be set ("white"/"black"; true/false respectively)
	 * @throws InvalidColourException
	 */
	public void setColour(String colour) throws InvalidColourException {
		if (colour.equalsIgnoreCase(Character.toString(ProtocolMessages.WHITE))) 
			this.colour = true;
		else if (colour.equalsIgnoreCase(Character.toString(ProtocolMessages.BLACK))) {
			this.colour = false;
		} else {
			throw new InvalidColourException("ERROR: No valid colour provided to player!");
		}
	}

	/**
	 * Sets the colour for this game representation
	 * @param colour The colour to be set (white/black; true/false respectively)
	 */
	public void setColour(boolean colour) {
		this.colour = colour;
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

	/*
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
				g.addStone(getRow(i), getColumn(i), colour);
			}
		}
	}

	/**
	 * Copies the board to a new String instance
	 * @return board A copy of the current board
	 */
	public String deepCopy() {
		String board = new String(currentBoard);
		return board;
	}

	/**
	 * Returns the 2-dimensional column of 1 a given 1-dimensional index
	 * @param move A 1-dimensional index of the preferred move
	 * @return The 2-dimensional index column
	 */
	public int getColumn(int move) {
		return (int) Math.floor(move/dimension);
	}

	/**
	 * Returns the 2-dimensional row of a given 1-dimensional index
	 * @param move A 1-dimensional index of the preferred move
	 * @return The 2-dimensional index row
	 */
	public int getRow(int move) {
		return move % dimension;
	}

	/**
	 * Returns the mark at a requested index
	 * @param i Index of which the colour stone is requested
	 * @return 
	 */
	public char getField(int i) {
		return currentBoard.charAt(i);
	}
	
	/**
	 * Checks whether the provided index is indeed a valid field at the board
	 * @param index A 1-dimensional index of a move
	 * @return true if the index is a valid index of the board
	 */
	public boolean isField(int index) {
		return index >= 0 && index < currentBoard.length();
	}

	public void reset() {
		currentBoard = Character.toString(ProtocolMessages.UNOCCUPIED).repeat(dimension*dimension);
	}

	public void updateBoard(String board) {
		this.currentBoard = board;
		if (useGUI) {boardToGUI(board);}
	}

	public boolean isEmpty(int i) {
		return currentBoard.charAt(i) == ProtocolMessages.UNOCCUPIED;
	}

	public char getColourChar() {
		return (colour) ? ProtocolMessages.WHITE : ProtocolMessages.BLACK;
	}

	public String getBoard() {
		return this.currentBoard;
	}
	
	public GoGUIIntegrator getGUI() {
		return this.g;
	}

	public int getDimension(){
		return this.dimension;
	}
	public static void main (String[] args) {

	}
}