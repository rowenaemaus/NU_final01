package tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.Client;
import client.PlayerHandler;
import exceptions.HandShakeException;
import goGame.GoGame;
import server.Game;

/**
 * This test class was a first draft for testing the client server connection. In the 
 * end I did not really work this out, and constructed other testclasses for more 
 * specific testing.
 *  
 * @author rowena.emaus
 *
 */
class TestGo {
	private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final static PrintStream originalOut = System.out;

	private Client client;

	@BeforeAll
	public static void setUpStream() {
		System.setOut(new PrintStream(outContent));
	}
	
	@BeforeEach 
	public void setUp() {
		client = new Client();
	}

	@Test
	public void testIOConnect() throws HandShakeException {
		String test = "test string";
		
		client.printMessage(test);
		assertThat(outContent.toString(), containsString(test));
		outContent.reset();
		
		String host = "localhost";
		client.setHost(host);
		assertEquals(client.getHost(), host);
		
		
		
		PlayerHandler p = new PlayerHandler(new Client(), "human");
		p.startGame("S;UUUUUUUUU;black");


		GoGame g = new GoGame(10, false);

	}

	@AfterAll
	static void restoreStream() {
		System.setOut(originalOut);
	}
}
