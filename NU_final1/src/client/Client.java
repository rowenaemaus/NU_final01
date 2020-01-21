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
	private String protocolVersion;
	private String name;

	private boolean connected;
	private boolean colourSet;

	public Client() {
		host = "10.48.8.101";
		port = 8070;
		socket = null;
		connected = false;
		colourSet = false;
		player = new HumanPlayer();
		protocolVersion = "1.0";
		name = "Rowena";
	}

	// start listening continuously for ending the game from server side

	// loop over this:
	// when turn pass to player
	// call getMove();
	// validate input
	// methode om opnieuw te proberen : hou bij hoe vaak

	// zodra valide; stuur naar de handler.


	@Override
	public void run() {

		try {
			boolean socketConnect = createConnection();
			try {
				doHandshake();
				printMessage("Handshake complete!");
				connected = true;
			} catch (Exception e) {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			sendMessage("Hi van de client groetjes");
		} catch (ServerUnavailableException e) {
			e.printStackTrace();
		}

		printMessage("Setting up GO game handler.");
		go = new GoGame(10, this);
		System.out.println("client.java flag1");
		new Thread(go).start();
		System.out.println("client flag2");
		player = new HumanPlayer();
		String msg = null;
		try {
			msg = in.readLine();
			while (msg != null) {
				printMessage("> Server message: " + msg);
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

		if (serverHS == 'H' && protocolVersion.equals(serverProtocolVersion)) {
			printMessage("Handshake correct! Connection established.");
		} else {
			throw new HandShakeException("Wrong handshake provided!");
		}
	}

	public void handleCommand(String msg) {
		char command = (msg.length() > 0) ? msg.charAt(0) : null;
		if (msg.length() > 1) {	
			if (!(msg.substring(1,1).equals(ProtocolMessages.DELIMITER))) {
				printMessage("Invalid command character");
				return;
			}
		}

		String[] cmds = msg.split(ProtocolMessages.DELIMITER);
		//		String firstCmd = (cmds.length > 1) ? cmds[1] : null;
		//		String secondCmd = (cmds.length > 2) ? cmds[2] : null;

		switch(command) { 
		case ProtocolMessages.GAME :
			startGame();
			break;
		case ProtocolMessages.TURN :
			doTurn();
			break;
		case ProtocolMessages.RESULT :
			handleResult();
			break;
		case ProtocolMessages.END :
			handleEnd();
			break;
		}
	}

	// start game
	// PROTOCOL.game + PROTOCOL.delimiter + bord + PROTOCOL.delimiter + PROTOCOL.white/black
	// colour command
	// stuur naar de player jo je krijgt deze kleur
	// stuur naar GoGame jo je krijgt deze kleur
	public void startGame() {		
	}

	// srv geeft beurt
	// PROTOCOL.turn + PROTOCOL.delimiter + bord
	public void doTurn() {
		boolean valid = false;
		Integer move = null;

		do {
			move = player.determineMove();
			valid = go.checkValidity(move);
		} while (!valid);
	}

	// srv stuurt bij valid move
	// PROTOCOL.result + PROTOCOL.delimiter + PROTOCOL.valid + PROTOCOL.delimiter + bord (updated)	
	// srv stuurt bij invalid move
	// PROTOCOL.result + PROTOCOL.delimiter + PROTOCOL.invalid + PROTOCOL.delimiter + message (string)
	public void handleResult() {
	}

	// game end
	// PROTOCOL.end + PROTOCOL.delimiter + PROTOCOL.reasonEnd + PROTOCOL.delimiter + winner (als PROTOCOL.W/B) + PROTOCOL.delimiter + scoreBlack (String, parse to integer) + PROTOCOL.delimiter + scoreWhite (String, parse to integer)
	// andere client stopt:
	// game end + exit
	// verbinging verloren:
	// game end: disconnect
	public void handleEnd() {
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

	public void setIP(String IP) {
		this.host = IP;
	}

	public static void main (String[] args) {
		new Thread(new Client()).start();
	}
}
