package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import Protocol.ProtocolMessages;
import exceptions.HandShakeException;
import exceptions.ServerUnavailableException;

public class Client implements Runnable{
	private String host;
	private int port;
	private Socket socket;

	private BufferedReader in;
	private BufferedWriter out;

	private PlayerHandler handler;
	private final String protocolVersion;
	private String name;
	private String playertype;

	private boolean connected;
	private boolean playing;
	private boolean askSetup = false;

	public Client() {
		System.out.println("Welcome, anonymous client, let me set stuff up...");
		try {
			host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		port = 8070;
		socket = null;
		connected = false;
		protocolVersion = "1.0";
		name = "Rowena";
		playertype = "human";
		playing = false;

		if (askSetup) {askSetup();}
	}


	public void askSetup() {
		Scanner keyboard = new Scanner(System.in);
		System.out.println("First, what is your name?");
		this.name = keyboard.next();

		System.out.println(name + ", what IP-address would you like to connect to?");
		this.host = keyboard.next();

		System.out.println(name + ", what port do you want to connect to?");
		this.port = keyboard.nextInt();

		System.out.println("Do you want to play with (1).human or (2).computer player?");
		playertype = keyboard.next();

		System.out.println("Aight leggo >>>>");
		System.out.println(">>>>>>>>>>>>>>>>");
	}

	/**
	 * Determine what type of player should be instantiated
	 * @param answer
	 */
	public void determinePlayer(String answer) {
		if (answer.equalsIgnoreCase("human") || answer.contains("1")) {
			playertype = "human";
		} else {
			System.out.println("Invalid playertype, default is computer player.");
			playertype = "computer";
		}
	}

	/**
	 * Checks the handshake and handles the game communication with 
	 * the server side as long as it lasts.
	 */
	@Override
	public void run() {
		try {
			boolean socketConnect = createConnection();
			try {
				doHandshake();
				printMessage("Handshake complete!");
				connected = true;
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

		handler = new PlayerHandler(this, playertype);

		setPlaying(true);
		printMessage(name + " starting the game");

		String msg = null;
		try {
			msg = in.readLine();
			while (msg != null) {
				printMessage(String.format("Incoming server-message: \'%s\'", shorter(msg)));
				handleCommand(msg);
				out.newLine();
				out.flush();
				msg = in.readLine();
			}
		} catch (IOException e) {
			System.out.println(e);
			closeConnection();
		}
	}

	/**
	 * Handles the commands coming from the server side and forwards the message 
	 * to the right method depending on the command
	 * @param msg The message coming from the server-side
	 */
	public void handleCommand(String msg) {
		Character command = (msg.length() > 0) ? msg.charAt(0) : null;
		if (command == null) return;

		switch(command) { 
		case ProtocolMessages.GAME :
			handler.startGame(msg);
			break;
		case ProtocolMessages.TURN :
			handler.doTurn(msg);
			break;
		case ProtocolMessages.RESULT :
			handler.handleResult(msg);
			break;
		case ProtocolMessages.END :
			handler.handleEnd(msg);
			break;
		}
	}

	public void printMessage(String s) {
		System.out.println(s);
	}

	public String shorter(String s) {
		return (s.length() > 50) ? (s.substring(0,50)+"...") : s; 
	}

	/**
	 * Sends a message to the server side over the outputstream
	 * @param msg The message to be sent over the stream
	 * @throws ServerUnavailableException
	 */
	public void sendMessage(String msg) throws ServerUnavailableException {
		if (out != null) {
			try {
				out.write(msg);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new ServerUnavailableException("ERROR: Server unavailable");
		}

	}

	/*--------------------------------------------*/
	/* Connection methods */

	public boolean createConnection() {
		clearConnection();

		while (socket == null) {
			try { 
				printMessage(String.format("Attempting to connect to port %d on %s", port, host));
				socket = new Socket(host, port);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				printMessage("I/O set up for client!");
				return true;
			} catch (IOException e) {
				printMessage("ERROR: Unable to set up socket and I/O streams on port " + port + " at " + host);
				return false;
			}
		}
		return (socket != null);
	}

	public void doHandshake() throws HandShakeException {
		String outHS = ProtocolMessages.HANDSHAKE + ProtocolMessages.DELIMITER + protocolVersion + ProtocolMessages.DELIMITER + name; //+ PROTOCOL.delimiter + message (string)
		try {
			sendMessage(outHS);
		} catch (ServerUnavailableException e1) {
			e1.printStackTrace();
		}
		printMessage("Outward handshake sent.");

		char serverHS = 0;
		String serverProtocolVersion = null;
		try {
			String incomingHS = in.readLine();
			printMessage("Received handshake: '" + incomingHS + "'");
			String[] messages = incomingHS.split(ProtocolMessages.DELIMITER);
			serverHS = messages[0].charAt(0);
			serverProtocolVersion = messages[1];
			printMessage("Checking inward handshake...");
		} catch (IOException e) {
			printMessage("ERROR: unable to read incoming handshake from server.");
			e.printStackTrace();
		}

		if (serverHS == ProtocolMessages.HANDSHAKE && protocolVersion.equals(serverProtocolVersion)) {
			printMessage("Handshake correct! Connection established.");
		} else {
			throw new HandShakeException("Wrong handshake provided!");
		}
	}

	public void clearConnection() {
		socket = null;
		in = null;
		out = null;
	}

	public void closeConnection() {
		System.out.println("Closing the connection...");
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/* Getters setters */
	public boolean getPlaying() {
		return this.playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public String getName() {
		return this.name;
	}

	public void setIP(String IP) {
		this.host = IP;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	/* Main */

	public static void main (String[] args) {
		Client c = new Client();
		new Thread(c).start();
	}
}
