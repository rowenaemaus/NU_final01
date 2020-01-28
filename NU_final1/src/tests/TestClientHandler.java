package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;
import server.ClientHandler;
import server.Server;

public class TestClientHandler {
	
	private static ClientHandler handler;
	
	@BeforeAll
	public void setUp() throws IOException {
		handler = new ClientHandler(new Socket(), new Server(), "name");
	}
	
	@Test
	public void testSetColour() throws InvalidColourException {
		handler.setColour(ProtocolMessages.BLACK);
		assertEquals(handler.getColour(), ProtocolMessages.BLACK);
	}
	
	@Test
	public void testConnectedPlaying() {
		assertFalse(handler.getConnected());
		assertFalse(handler.getPlaying());
		
		handler.setPlaying(true);
		assertTrue(handler.getPlaying());
	}
}
