package goGame;

import java.util.HashSet;
import java.util.Set;

import com.nedap.go.gui.GoGUIIntegrator;

import Protocol.ProtocolMessages;
import client.Client;
import client.PlayerHandler;
import exceptions.InvalidColourException;

public class unconnectedGoGame {

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
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void setUpGUI() {
		g = new GoGUIIntegrator(true, true, dimension);
//		g.startGUI();
		g.setBoardSize(dimension);
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
			boardAfterMove = setStone(nextMove);
			return prevBoards.contains(boardAfterMove) ? false : true;
		} else {
			System.out.println("ERROR: Impossible move!");
			return false;
		}
	}

	public String deepCopy() {
		String board = new String(currentBoard);
		return board;
	}

//	public int index(int row, int col) {
//		return row*dimension + col;
//	}

//	public int getRow(int move) {
//		return (int) Math.floor(move/dimension);
//	}
//
//	public int getColumn(int move) {
//		return move % dimension;
//	}

	public boolean isField(int index) {
		return index >= 0 && index < currentBoard.length();
	}

	public char getField(int i) {
		return currentBoard.charAt(i);
	}

	public boolean isEmptyField(int i) {
		return currentBoard.charAt(i) == ProtocolMessages.UNOCCUPIED;
	}

	public void reset() {
		currentBoard = Character.toString(ProtocolMessages.UNOCCUPIED).repeat(dimension*dimension);
	}

	public String setStone(int move) {
		StringBuilder updatedBoard = new StringBuilder(currentBoard);
		updatedBoard.setCharAt(move, getColourChar());
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
		GoGame g = new GoGame(10, false);;
	}
}

//	
//	
//	
//	
//	private GoGUIIntegrator g;
//	private int dimension;
//	private Client client;
//	private String colour;
//
//	public GoGame (int dimension, Client client) {
//		this.client = client;
//		this.dimension = dimension;
//		client.printMessage("Go game handler set up. Preparing GUI...");
//	}
//
//	public void setUpGUI() {
//		g = new GoGUIIntegrator(true, true, dimension);
//		System.out.println("flag2");
//		g.startGUI();
//		System.out.println("flag5");
//		g.setBoardSize(dimension);
//		System.out.println("flag6");
//		client.printMessage("GUI ready");
//		client.printMessage("-----------------------");
//	}
//
//	public boolean checkValidity(int move) {
//		// TODO implement
//		return false;
//	}
//
//	@Override
//	public void run() {
//		setUpGUI();		
//	}
//
//	public void newBoard(String string) {
//		// TODO Auto-generated method stub
//	}
//
//	public void setColour(String colour) {
//		// TODO Auto-generated method stub
//	}
//
//	public boolean checkValidity(String move) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public void updateBoard(String board) {
//		// TODO Auto-generated method stub
//		// also show new board	
//	}
//	
//	public void showBoard() {
//		// TODO show updated board as it is
//	}
//}
