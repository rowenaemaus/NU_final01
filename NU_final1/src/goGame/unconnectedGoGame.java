package goGame;

import java.util.HashSet;
import java.util.Set;

import com.nedap.go.gui.GoGUIIntegrator;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;

public class unconnectedGoGame implements Runnable {

	private GoGUIIntegrator g;
	private int dimension;
	private boolean colour = false; // true = white
	private Set<String> prevBoards = new HashSet<String>();
	private String currentBoard;

	public unconnectedGoGame (int dimension) {
		this.dimension = dimension;
		try {
			setColour("black");
		} catch (InvalidColourException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setUpGUI() {
		g = new GoGUIIntegrator(true, true, dimension);
		g.startGUI();
		g.setBoardSize(dimension);
	}

	public void run() {
		
	}

	public void newBoard(String board) {
		setUpGUI();
		this.currentBoard = board;
	}

	public void setColour(String colour) throws InvalidColourException {
		if (colour.equalsIgnoreCase("white")) 
			this.colour = true;
		else if (colour.equalsIgnoreCase("black")) {
			this.colour = false;
		} else {
			throw new InvalidColourException("ERROR: No valid colour provided to player!");
		}
	}

	public boolean checkValidity(String move) {
		int nextMove = -1;

		try {
			nextMove = Integer.valueOf(move);
		} catch (NumberFormatException e) {
			System.out.println("ERROR: Invalid number provided for move!");
			e.printStackTrace();
			return false;
		}

		if(isField(nextMove)) {
			String boardAfterMove = deepCopy();
			boardAfterMove = setStone(getRow(nextMove), getColumn(nextMove));
			return prevBoards.contains(boardAfterMove) ? false : true;
		} else {
			System.out.println("ERROR: Impossible move!");
			return false;
		}
	}

	public boolean previousBoard(String board) {
		return prevBoards.contains(board);
	}

	public String deepCopy() {
		String board = new String(currentBoard);
		return currentBoard;
	}

	public int index(int row, int col) {
		return row*dimension + col;
	}

	public int getRow(int move) {
		return (int) Math.floor(move/dimension);
	}

	public int getColumn(int move) {
		return move % dimension;
	}

	public boolean isField(int index) {
		return index >= 0 && index < currentBoard.length();
	}

	public boolean isField(int row, int col) {
		return isField(index(row, col));
	}

	public char getField(int i) {
		return currentBoard.charAt(i);
	}

	public char getField(int row, int col) {
		return getField(index(row, col));
	}

	public boolean isEmptyField(int i) {
		return currentBoard.charAt(i) == ProtocolMessages.UNOCCUPIED;
	}

	public boolean isEmptyField(int row, int col) {
		return isEmptyField(index(row,col));
	}

	public void reset() {
		currentBoard = Character.toString(ProtocolMessages.UNOCCUPIED).repeat(dimension*dimension);
	}

	public String setStone(int x, int y) {
		StringBuilder updatedBoard = new StringBuilder(currentBoard);
		updatedBoard.setCharAt(index(x,y), getColourChar());
		return updatedBoard.toString();
	}

	public char getColourChar() {
		return (colour) ? ProtocolMessages.WHITE : ProtocolMessages.BLACK;
	}

	public void updateBoard(String board) {
		this.currentBoard = board;
	}

	public void showBoard() {
		// TODO show updated board as it is
	}


	public boolean emptySpot(int move) {
		return currentBoard.charAt(move) == ProtocolMessages.UNOCCUPIED ? true : false; 
	}


	public static void main (String[] args) {
		new Thread(new unconnectedGoGame(10)).start();;
	}
}
