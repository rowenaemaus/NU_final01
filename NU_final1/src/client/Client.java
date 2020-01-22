package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import Protocol.ProtocolMessages;
import exceptions.HandShakeException;
import exceptions.ServerUnavailableException;
import goGame.GoGame;
import player.HumanPlayer;
import player.Player;

public class Client implements Runnable{
	private String host;
	private int port;
	private Socket socket;

	private BufferedReader in;
	private BufferedWriter out;

	private Player player;
	private GoGame go;
	private final String protocolVersion;
	private String name;

	private boolean connected;
	private boolean colourSet;

	public Client() {
		host = "10.7.16.57";
		port = 8070;
		socket = null;
		connected = false;
		colourSet = false;
		player = new HumanPlayer();
		protocolVersion = "1.0";
		name = "Rowena";
	}

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

		try {
			sendMessage("Hi van de client groetjes");
		} catch (ServerUnavailableException e) {
			e.printStackTrace();
		}

		//		printMessage("Setting up GO game handler.");
		//		go = new GoGame(10, this);
		//		System.out.println("client.java flag1");
		//		new Thread(go).start();
		//		System.out.println("client flag2");
		//		player = new HumanPlayer();

		String msg = null;
		try {
			msg = in.readLine();
			while (msg != null) {
				printMessage(String.format("Incoming message: \'%s\'", msg));
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

	public void handleCommand(String msg) {
		try {
			sendMessage(player.getInput());
		} catch (ServerUnavailableException e) {
			System.out.println(e);
			e.printStackTrace();
		}

		Character command = (msg.length() > 0) ? msg.charAt(0) : null;
		if (command == null) return;

		if (msg.length() > 1) {	
			if (!(msg.substring(1,1).equals(ProtocolMessages.DELIMITER))) {
				printMessage("Invalid command character");
				return;
			}
		}

		switch(command) { 
		case ProtocolMessages.GAME :
			startGame(msg);
			break;
		case ProtocolMessages.TURN :
			doTurn(msg);
			break;
		case ProtocolMessages.RESULT :
			handleResult(msg);
			break;
		case ProtocolMessages.END :
			handleEnd(msg);
			break;
		default:
			printMessage("Incoming message: \'msg\'");
		}
	}
	
	public void startGame(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String board = cmds.length > 1 ? cmds[1] : null;
		String colour = cmds.length > 2 ? cmds[2] : null;

		if (board == null || colour == null) {
			printMessage("ERROR: Invalid colour and board arguments!");
		} else {
			go.newBoard(board);
			go.setColour(colour);
			player.setColour(colour);
		}
	}

	public void doTurn(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String board = cmds.length > 1 ? cmds[1] : null;
		
		if (board == null) {
			printMessage("ERROR: Invalid board arguments!");
			return;
		}
		
		boolean valid = false;
		String move = null;

		do {
			printMessage(String.format("%s, please determine a move!", name));
			move = player.determineMove();
			valid = go.checkValidity(move);
			printMessage("Move valid: " + valid);
		} while (!valid);

		try {
			sendMessage(ProtocolMessages.MOVE + ProtocolMessages.DELIMITER + move.toString());
		} catch (ServerUnavailableException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public void handleResult(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		String valid = cmds.length > 1 ? cmds[1] : null;
		String board = cmds.length > 2 ? cmds[2] : null;
		
		if (valid == null || board == null) {
			printMessage("ERROR: Invalid result message received from server!");
			return;
		}
		
		if (valid.equals(Character.toString(ProtocolMessages.VALID))) {
			printMessage("Server approved your move.");
			go.updateBoard(board);	
		} else if (valid.equals(Character.toString(ProtocolMessages.INVALID))){
			printMessage("Server disapproves your move, very disappointing.");
		}	
	}

	// game end
	// PROTOCOL.end + PROTOCOL.delimiter + PROTOCOL.reasonEnd + PROTOCOL.delimiter + winner (als PROTOCOL.W/B) + PROTOCOL.delimiter + scoreBlack (String, parse to integer) + PROTOCOL.delimiter + scoreWhite (String, parse to integer)
	// andere client stopt:
	// game end + exit
	// verbinging verloren:
	// game end: disconnect
	public void handleEnd(String msg) {
		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		
	}

	public void printMessage(String s) {
		System.out.println(s);
	}

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


	/* Connection methods */

	public boolean createConnection() {
		clearConnection();

		while (socket == null) {
			try { 
				InetAddress addr = InetAddress.getByName(host);
				printMessage(String.format("Attempting to connect to port %d on %s", port, host));
				socket = new Socket(addr, port);
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
		new Thread(new Client()).start();
	}
}
