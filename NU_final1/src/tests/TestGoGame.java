package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;
import goGame.GoGame;

public class TestGoGame {

	private static GoGame go;
	private static String board;

	@BeforeAll
	public void setUp() {
		int dimension = 10;
		go = new GoGame(dimension, false);
		
		board = Character.toString(ProtocolMessages.UNOCCUPIED).repeat(dimension*dimension);
	}

	@Test
	public void testSetUpGUI() {
		assertTrue(go.getGUI() == null);
	}
	
	@Test
	public void testNewBoard() {
		go.newBoard(board);
		assertEquals(go.getBoard(), board);
	}
	
	@Test
	public void testSetColour() throws InvalidColourException {
		go.setColour(Character.toString(ProtocolMessages.BLACK));
		assertEquals(go.getColourChar(), ProtocolMessages.BLACK);
		
		go.setColour(true);
		assertEquals(go.getColourChar(), ProtocolMessages.WHITE);
	}

	@Test
	public void testCheckValidity() {
		go.newBoard(board);
		assertFalse(go.checkValidity(-1));		
		assertFalse(go.checkValidity(go.getBoard().length()+1));
		assertTrue(go.checkValidity(go.getBoard().length()-1));
	}
	
	@Test
	public void testSetStone() {
		int index = 0;
		go.newBoard(board);
		assertEquals(go.getField(index), ProtocolMessages.UNOCCUPIED);
		
		String newBoard = go.setStone(index);
		go.newBoard(newBoard);
		assertEquals(go.getBoard(), newBoard);
		assertNotEquals(go.getField(index), ProtocolMessages.UNOCCUPIED);
		assertEquals(go.getField(index), ProtocolMessages.WHITE); // TODO why is it white?
	}
}
