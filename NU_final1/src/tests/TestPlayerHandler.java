package tests;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import Protocol.ProtocolMessages;
import client.Client;
import client.PlayerHandler;
import exceptions.InvalidColourException;
import goGame.GoGame;

public class TestPlayerHandler {
	private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private static PlayerHandler handler;
	private static String board;
	private static int dimension;
	private static GoGame go;

	@BeforeAll
	public static void setUp() {
		System.setOut(new PrintStream(outContent));

		handler = new PlayerHandler(new Client());
		handler.getClient().createConnection();
		dimension = 10;
		board = Character.toString(ProtocolMessages.UNOCCUPIED).repeat(dimension*dimension);
		go = new GoGame(dimension,false);
		go.setColour(false);
		handler.setGame(go);
	}

	@Test
	public void testBoardSetup() {
		handler.startGame(ProtocolMessages.GAME+ProtocolMessages.DELIMITER+board+ProtocolMessages.DELIMITER+ProtocolMessages.BLACK);
		assertTrue(handler.getGame() != null);
		assertEquals(handler.getGame().getDimension(), dimension);
	}

	@Test
	public void testHandleResult() {
		handler.handleResult(ProtocolMessages.RESULT+ProtocolMessages.DELIMITER+ProtocolMessages.VALID+ProtocolMessages.DELIMITER+board);
		assertThat(outContent.toString(), containsString("Server approved your move"));
		outContent.reset();
		
		handler.handleResult(ProtocolMessages.RESULT+ProtocolMessages.DELIMITER+ProtocolMessages.INVALID+ProtocolMessages.DELIMITER+board);
		assertThat(outContent.toString(), containsString("Server disapproved your move"));
		outContent.reset();
	}

	@Test
	public void testGetGame() {
		assertEquals(handler.getGame(), go);
	}
	
	@Test
	public void testSetColour() throws InvalidColourException {
		handler.setColour(Character.toString(ProtocolMessages.BLACK));
		assertEquals(handler.getColour(), false);
		
		handler.setColour(Character.toString(ProtocolMessages.WHITE));
		assertEquals(handler.getColour(), true);	
	}
	
	
	// For now not testing as it involves setting up a complete client object
	@Test
	public void testTurn() {
/*		assertTrue(handler.getGame() != null);
		assertEquals(handler.getGame().getBoard(), board);
		handler.doTurn(ProtocolMessages.TURN+ProtocolMessages.DELIMITER+board);
		assertNotEquals(handler.getGame().getBoard(), board);*/
	}
	
	// For now not testing as it involves setting up a complete client object and server connection
	@Test
	public void testHandleEnd() {
/*		handler.handleEnd("");
		assertThat(outContent.toString(), containsString("Game ended!"));
		outContent.reset();*/
	}
}
