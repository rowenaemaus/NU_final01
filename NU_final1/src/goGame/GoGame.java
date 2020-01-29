package goGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

	private List<Integer> indicesWhite = new ArrayList<Integer>();
	private List<Integer> indicesBlack = new ArrayList<Integer>();
	private Set<Integer> stonesToKeep = new HashSet<Integer>();
	private Set<Integer> stonesToRemove = new HashSet<Integer>();
	private Set<Integer> captureCheck = new HashSet<Integer>();


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
	 * Sets the board argument as current board and optionally initialises the gui
	 * @param board
	 */
	public void newBoard(String board) {
		if (useGUI) {setUpGUI();}
		this.currentBoard = board;
	}

	public void setBoard(String board) {
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
	 * @param string The one-dimensional index of the move 
	 * @return Whether the move is valid (true if valid, false if invalid)
	 */
	public boolean checkValidity(String move) {
		if (move.equalsIgnoreCase("pass")) {
			return true;
		}  

		int moveInt = -1;
		try { 
			moveInt = Integer.parseInt(move);
		} catch (NumberFormatException e) {
			System.out.println("ERROR: Unable to parse move to integer.");
			return false;
		}

		if(isField(moveInt) && isEmpty(moveInt) && !isFull()) {
			String boardAfterMove = deepCopy();
			boardAfterMove = setStone(moveInt);
			return prevBoards.contains(boardAfterMove) ? false : true;
		} else {
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
	 * Computes the board by processing the capturings.
	 * @return An updated board after the captured stones have been removed
	 */
	public String computeResult() {
		stonesToKeep.clear();
		stonesToRemove.clear();
		captureCheck.clear();
		getIndices();

		int curStone = indicesWhite.get(0);
		while ((stonesToKeep.size()+stonesToRemove.size()) < indicesWhite.size()) {
			checkCaptured(curStone);
			curStone = nextIndex(curStone);
		}
		return removeStones();
	}

	/**
	 * Checks if a stone is captured between other the other colour or whether there
	 * is still an escape (Unoccupied)
	 * @param curStone The stone to check for
	 */
	public void checkCaptured(int curStone) {
		captureCheck.add(curStone);
		HashMap<Character, Set<Integer>> neighbours = getNeighbours(curStone);

		if (neighbours.keySet().contains(ProtocolMessages.UNOCCUPIED)) {
			stonesToKeep.addAll(captureCheck);
			captureCheck.clear();
			return;
		} else if (neighbours.keySet().size() == 1 ) {
			stonesToRemove.add(curStone);
			if (neighbours.containsKey(ProtocolMessages.WHITE)) {
				stonesToKeep.addAll(neighbours.get(ProtocolMessages.WHITE));
			}
			return;
		} else if (neighbours.keySet().contains(ProtocolMessages.WHITE)) {
			for (Integer i : neighbours.get(ProtocolMessages.WHITE)) {
				checkCaptured(i);
			}
		} else {
			return;
		}
	}

	/**
	 * Finds the next index (stone) to investigate whether it is captured. Ignores
	 * stones that are already investigated.
	 * @param index
	 * @return The next stone to check whether it is captured
	 */
	public int nextIndex(int curStone) {
		int stoneIndex = indicesWhite.indexOf(curStone);

		int nextStoneIndex = stoneIndex+1;

		while (nextStoneIndex < indicesWhite.size()) {
			if (!stonesToKeep.contains(indicesWhite.get(nextStoneIndex)) && !stonesToRemove.contains(indicesWhite.get(nextStoneIndex))) {
				return nextStoneIndex;
			} else {
				nextStoneIndex++;
			}
		}

		return -1;
	}

	public void getIndices(){
		for (int i = 0; i < currentBoard.length(); i++) {
			if (currentBoard.charAt(i) == ProtocolMessages.BLACK) {
				this.indicesBlack.add(i);
			} else if (currentBoard.charAt(i) == ProtocolMessages.WHITE) {
				this.indicesWhite.add(i);
			} else {
				continue;
			}
		}
	}

	public HashMap<Character, Set<Integer>> getNeighbours(int index){
		// returns a list of the neighbours of a given index
		// ignores indices of this colour die al geweest zijn en ignores de randen
		// ignore dus de tokeep en toremove ones
		return null;
	}

	public String removeStones() {
		StringBuilder tmpBoard = new StringBuilder(deepCopy());
		for (Integer i : stonesToRemove) {
			tmpBoard.setCharAt(i, ProtocolMessages.UNOCCUPIED);
		}
		return tmpBoard.toString();
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

	public boolean isFull() {
		return !currentBoard.contains(Character.toString(ProtocolMessages.UNOCCUPIED));
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