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
import exceptions.HandShakeException;

class TestClient {
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
		
		
		
//		
//		
//		client.createConnection();
//		assertThat(outContent.toString(), containsString("Attempting to connect to port"));
//		outContent.reset();
//		
//		client.doHandshake();
//		assertThat(outContent.toString(), containsString(" "));
//		outContent.reset();
//		
	}

	@AfterAll
	static void restoreStream() {
		System.setOut(originalOut);
	}
}
