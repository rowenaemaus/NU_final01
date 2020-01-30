package goGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.nedap.go.gui.GoGUIIntegrator;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;

public class GoGame {

	private GoGUIIntegrator g;
	private Integer dimension = null;
	private boolean colour = false; // true = white
	private Set<String> prevBoards = new HashSet<String>();
	private String currentBoard;
	private boolean useGUI = false;

	private List<Integer> indicesWhite = new ArrayList<Integer>();
	private List<Integer> indicesBlack = new ArrayList<Integer>();
	private Set<Integer> stonesToKeep = new HashSet<Integer>();
	private Set<Integer> stonesToRemove = new HashSet<Integer>();
	private Set<Integer> captureCheck = new HashSet<Integer>();

	private List<Integer> colourToCheck;

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
		String updatedBoardString = computeResult(updatedBoard.toString());
		if (useGUI) {boardToGUI(updatedBoard.toString());}
		return updatedBoardString;
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
	public String computeResult(String board) {
		stonesToKeep.clear();
		stonesToRemove.clear();
		captureCheck.clear();
		getIndices();		
		colourToCheck = getColourChar() == ProtocolMessages.WHITE ? indicesWhite : indicesBlack;

		if (!indicesWhite.isEmpty() && !indicesBlack.isEmpty()) {
			int curStone = colourToCheck.get(0);
			while ((stonesToKeep.size()+stonesToRemove.size()) < colourToCheck.size()) {
				checkCaptured(curStone);
				curStone = nextIndex(curStone);
				if (curStone == -1) {
					break;
				}
			}
			return removeStones();
		}
		return board;
	}

	/**
	 * Checks if a stone is captured between other the other colour or whether there
	 * is still an escape (Unoccupied)
	 * @param curStone The stone to check for
	 */
	public void checkCaptured(int curStone) {
		char colour = getColourChar();
		captureCheck.add(curStone);
		HashMap<Character, Set<Integer>> neighbours = getNeighbours(curStone);

		System.out.println("Neighbours is: " + neighbours.toString());

		if (!neighbours.isEmpty()) {
			if (neighbours.keySet().contains(ProtocolMessages.UNOCCUPIED)) {
				stonesToKeep.addAll(captureCheck);
				captureCheck.clear();
				return;
			} else if (neighbours.keySet().size() == 1) {
				stonesToRemove.add(curStone);
				if (neighbours.containsKey(colour)) {
					stonesToKeep.addAll(neighbours.get(colour));
				}
				return;
			} else if (neighbours.keySet().contains(colour)) {
				for (Integer i : neighbours.get(colour)) {
					checkCaptured(i);
				}
			} else {
				return;
			}
		}
	}

	/**
	 * Finds the next index (stone) to investigate whether it is captured. Ignores
	 * stones that are already investigated.
	 * @param index
	 * @return The next stone to check whether it is captured
	 */
	public int nextIndex(int curStone) {
		int stoneIndex = colourToCheck.indexOf(curStone);

		int nextStoneIndex = stoneIndex+1;

		while (nextStoneIndex < colourToCheck.size()) {
			if (!stonesToKeep.contains(colourToCheck.get(nextStoneIndex)) && !stonesToRemove.contains(colourToCheck.get(nextStoneIndex))) {
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
				if (!indicesBlack.contains(i)) {
					this.indicesBlack.add(i);
				}
			} else if (currentBoard.charAt(i) == ProtocolMessages.WHITE) {
				if (!indicesWhite.contains(i)) {
					this.indicesWhite.add(i);
				}
			} else {
				continue;
			}
		}

		System.out.println("White indices: " + indicesWhite);
		System.out.println("Black indices: " + indicesBlack);
	}

	public HashMap<Character, Set<Integer>> getNeighbours(int index){
		HashMap<Character, Set<Integer>> neighbours = new HashMap<Character, Set<Integer>>();

		if (index == 0 || index == dimension-1 || index == dimension*dimension-dimension || index == dimension*dimension-1) {
			neighbours = getCornerNeighbours(index);
		} else if (index > 0 && index < dimension-1 ) {
			neighbours = getEdgeNeighbours(index);
		} else if (index % 10 == 0) {
			neighbours = getEdgeNeighbours(index);
		} else if (index % dimension == dimension-1) {
			neighbours = getEdgeNeighbours(index);
		} else if (index > (dimension*dimension-dimension) && index < (dimension*dimension-1)){
			neighbours = getEdgeNeighbours(index);
		} else {
			neighbours = getDefaultNeighbours(index);
		}

		return removeChecked(neighbours);
	}

	public HashMap<Character, Set<Integer>> getDefaultNeighbours(int index){
		HashMap<Character, Set<Integer>> neighbours = new HashMap<Character, Set<Integer>>();

		Set<Integer> locationChar1 = new HashSet<Integer>();
		Set<Integer> locationChar2 = new HashSet<Integer>();
		Set<Integer> locationChar3 = new HashSet<Integer>();

		int indexUpper = index-dimension;
		char charUpper = currentBoard.charAt(indexUpper);

		locationChar1.add(indexUpper);
		neighbours.put(charUpper, locationChar1);

		int indexLeft = index-1;
		char charLeft = currentBoard.charAt(indexLeft);
		if (charLeft == charUpper) {
			neighbours.get(charUpper).add(indexLeft);
		} else {
			locationChar2.add(indexLeft);
			neighbours.put(charLeft, locationChar2);
		}

		int indexRight = index+1;
		char charRight = currentBoard.charAt(indexRight);
		if (charRight == charUpper) {
			neighbours.get(charUpper).add(indexRight);
		} else if (charRight == charLeft) {
			neighbours.get(charLeft).add(indexRight);
		} else {
			locationChar3.add(indexRight);
			neighbours.put(charRight, locationChar3);
		}

		int indexBelow = index+dimension;
		char charBelow = currentBoard.charAt(indexBelow);
		if (charBelow == charUpper) {
			neighbours.get(charUpper).add(indexBelow);
		} else if (charBelow == charLeft) {
			neighbours.get(charLeft).add(indexBelow);
		} else if (charBelow == charRight) {
			neighbours.get(charRight).add(indexBelow);
		}
		System.out.println(neighbours);
		return neighbours;
	}

	public HashMap<Character, Set<Integer>> getEdgeNeighbours(int index){
		HashMap<Character, Set<Integer>> neighbours = new HashMap<Character, Set<Integer>>();

		Set<Integer> locationChar1 = new HashSet<Integer>();
		Set<Integer> locationChar2 = new HashSet<Integer>();
		Set<Integer> locationChar3 = new HashSet<Integer>();

		if (index > 0 && index < dimension-1 ) {							// upper edge
			int indexLeft = index-1;
			char charLeft = currentBoard.charAt(indexLeft);

			locationChar1.add(indexLeft);
			neighbours.put(charLeft, locationChar1);

			int indexRight = index+1;
			char charRight = currentBoard.charAt(indexRight);
			if (charRight == charLeft) {
				neighbours.get(charLeft).add(indexRight);
			} else {
				locationChar2.add(indexRight);
				neighbours.put(charRight, locationChar2);
			}

			int indexBelow = index + dimension;
			char charBelow = currentBoard.charAt(indexBelow);
			if (charBelow == charLeft) {
				neighbours.get(charLeft).add(indexBelow);
			} else if (charBelow == charRight) {
				neighbours.get(charRight).add(indexBelow);
			} else {
				locationChar3.add(indexBelow);
				neighbours.put(charBelow, locationChar3);
			}			
		} else if (index % 10 == 0) {										// left edge
			int indexUpper = index-dimension;
			char charUpper = currentBoard.charAt(indexUpper);

			locationChar1.add(indexUpper);
			neighbours.put(charUpper, locationChar1);

			int indexRight = index+1;
			char charRight = currentBoard.charAt(indexRight);
			if (charRight == charUpper) {
				neighbours.get(charUpper).add(indexRight);
			} else {
				locationChar2.add(indexRight);
				neighbours.put(charRight, locationChar2);
			}

			int indexBelow = index + dimension;
			char charBelow = currentBoard.charAt(indexBelow);
			if (charBelow == charUpper) {
				neighbours.get(charUpper).add(indexBelow);
			} else if (charBelow == charRight) {
				neighbours.get(charRight).add(indexBelow);
			} else {
				locationChar3.add(indexBelow);
				neighbours.put(charBelow, locationChar3);
			}
		} else if (index % dimension == dimension-1) {						// right edge 
			int indexUpper = index-dimension;
			char charUpper = currentBoard.charAt(indexUpper);

			locationChar1.add(indexUpper);
			neighbours.put(charUpper, locationChar1);

			int indexLeft = index-1;
			char charLeft = currentBoard.charAt(indexLeft);
			if (charLeft == charUpper) {
				neighbours.get(charUpper).add(indexLeft);
			} else {
				locationChar2.add(indexLeft);
				neighbours.put(charLeft, locationChar2);
			}

			int indexBelow = index + dimension;
			char charBelow = currentBoard.charAt(indexBelow);
			if (charBelow == charUpper) {
				neighbours.get(charBelow).add(indexBelow);
			} else if (charBelow == charLeft) {
				neighbours.get(charLeft).add(indexBelow);
			} else {
				locationChar3.add(indexBelow);
				neighbours.put(charBelow, locationChar3);
			}
		} else if (index > (dimension*dimension-dimension) && index < (dimension*dimension-1)){
			int indexUpper = index-dimension;
			char charUpper = currentBoard.charAt(indexUpper);

			locationChar1.add(indexUpper);
			neighbours.put(charUpper, locationChar1);

			int indexLeft = index-1;
			char charLeft = currentBoard.charAt(indexLeft);
			if (charLeft == charUpper) {
				neighbours.get(charUpper).add(indexLeft);
			} else {
				locationChar2.add(indexLeft);
				neighbours.put(charLeft, locationChar2);
			}

			int indexRight = index+1;
			char charRight = currentBoard.charAt(indexRight);
			if (charRight == charUpper) {
				neighbours.get(charUpper).add(indexRight);
			} else if (charRight == charLeft) {
				neighbours.get(charLeft).add(indexRight);
			} else {
				locationChar3.add(indexRight);
				neighbours.put(charRight, locationChar3);
			}
		}
		return neighbours;
	}

	public HashMap<Character, Set<Integer>> getCornerNeighbours(int index){
		HashMap<Character, Set<Integer>> neighbours = new HashMap<Character, Set<Integer>>();

		Set<Integer> locationChar1 = new HashSet<Integer>();
		Set<Integer> locationChar2 = new HashSet<Integer>();

		if (index == 0) {
			int indexLower = dimension;
			char charLower = currentBoard.charAt(indexLower);						// get neighbour below

			locationChar1.add(indexLower);											// add
			neighbours.put(charLower, locationChar1);								// add this neighbour to the set of neighbours

			int indexRight = index+1;
			char charRight = currentBoard.charAt(indexRight);						// get right neighbour
			if (neighbours.containsKey(charRight)) {								// if this char is already in the neighbour set
				neighbours.get(charLower).add(indexRight);							// add the location to the right as occurrence of same char
			} else {																// if this char is different
				locationChar2.add(indexRight);										// add
				neighbours.put(currentBoard.charAt(indexRight), locationChar2);		// add this combi of char and location to neighbour set
			}

		} else if (index == dimension-1) {									// upper right corner
			int indexLower = index+dimension;
			char charLower = currentBoard.charAt(indexLower);

			locationChar1.add(indexLower);									
			neighbours.put(charLower, locationChar1);					

			int indexLeft = index-1;
			char charLeft = currentBoard.charAt(indexLeft);

			if (neighbours.containsKey(charLeft)) {				
				neighbours.get(charLower).add(indexLeft);					
			} else {															
				locationChar2.add(indexLeft);										
				neighbours.put(currentBoard.charAt(indexLeft), locationChar2);	
			}
		} else if (index == (dimension*dimension-dimension)) { 				// lower left corner
			int indexHigher = index-dimension;
			char charHigher = currentBoard.charAt(indexHigher);

			locationChar1.add(indexHigher);
			neighbours.put(charHigher, locationChar1);

			int indexRight = index+1;
			char charRight = currentBoard.charAt(indexRight);
			if (neighbours.containsKey(charRight)) {
				neighbours.get(charHigher).add(indexRight);
			} else {
				locationChar2.add(indexRight);
				neighbours.put(charRight, locationChar2);
			}
		} else if (index == (dimension*dimension-1)) {						// lower right corner
			int indexHigher = index-dimension;
			char charHigher = currentBoard.charAt(indexHigher);

			locationChar1.add(indexHigher);
			neighbours.put(charHigher, locationChar1);

			int indexLeft = index-1;
			char charLeft = currentBoard.charAt(indexLeft);
			if (neighbours.containsKey(charLeft)) {
				neighbours.get(charHigher).add(indexLeft);
			} else {
				locationChar2.add(indexLeft);
				neighbours.put(charLeft, locationChar2);
			}
		}

		return neighbours;
	}


	public HashMap<Character, Set<Integer>> removeChecked(HashMap<Character, Set<Integer>> neighbours) {
		Iterator<Entry<Character, Set<Integer>>> entrySet = neighbours.entrySet().iterator();

		while (entrySet.hasNext()) {
			Entry<Character, Set<Integer>> character = entrySet.next();
			for (int i : character.getValue()) {
				if (stonesToKeep.contains(i) || stonesToRemove.contains(i)) {
					character.getValue().remove(i);
					neighbours.get(character.getKey()).remove(i);
				}
				if (character.getValue().isEmpty()) {
					neighbours.remove(character.getKey());
				}
			}	
		}
		return neighbours;
	}

	/**
	 * Remove the stones from the current board
	 * @return the board after removing the stones
	 */
	public String removeStones() {
		System.out.println("Removing: " + stonesToRemove.toString());

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