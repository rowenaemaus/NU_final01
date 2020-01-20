package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import exceptions.HandShakeException;
import exceptions.ServerUnavailableException;
import player.HumanPlayer;
import player.Player;

public class Client implements Runnable{
	private String host;
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private Player player;
	private int port;

	private boolean connected;
	
	public Client() {
		host = "10.48.8.150";
		port = 8070;
		socket = null;
		connected = false;
	}
	
	// start listening continuously for ending the game from server side

	// loop over this:
	// when turn pass to player
	// call getMove();
	// validate input
	// methode om opnieuw te proberen : hoe bij hoe vaak

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
//		Player player = new HumanPlayer();
//		String msg = null;
//		try {
//			msg = in.readLine();
//			while (msg != null) {
//				printMessage("> Server message: " + msg);
//				handleCommand(msg);
//				out.newLine();
//				out.flush();
//				msg = in.readLine();
//			}
//		} catch (IOException e) {
//			closeConnection();
//		}
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
		String outHS = "H";
		try {
			sendMessage(outHS);
		} catch (ServerUnavailableException e1) {
			e1.printStackTrace();
		}
		printMessage("Outward handshake sent.");

		char serverHS = 0;
		String incomingHS = null;
		try {
			incomingHS = in.readLine();
			printMessage("Received handshake: '" + incomingHS + "'");
			serverHS = incomingHS.charAt(0);
			printMessage("Checking inward handshake...");
		} catch (IOException e) {
			printMessage("ERROR: unable to read incoming handshake from server.");
			e.printStackTrace();
		}

		if (serverHS == 'H') {
			printMessage("Handshake correct! Connection established.");
		} else {
			throw new HandShakeException("Wrong handshake provided!");
		}
	}

	public boolean tryAgain() {
		// TODO ask to try and reconnect again
		return false;
	}
	
	// basically handling 
	public void handleCommand(String msg) {
		// TODO aanpassen according to protocol
		char command = (msg.length() > 0) ? msg.charAt(0) : null;
		char space = 0;
		if (msg.length() > 1) {
			space = msg.charAt(1);
			if (!(space == ' ')) {
				printMessage("Invalid command character");
				return;
			}
		}

		String[] cmds = msg.split(" ");
		String firstCmd = (cmds.length > 1) ? cmds[1] : null;
		String secondCmd = (cmds.length > 2) ? cmds[2] : null;
		
		switch(command) { 
		case 'x' :
			printMessage("Server ends the connection.");
			break;
		case 't' :
			player.determineMove();
			break;
		}
		
		
		
		
		
		// TODO what to do with command
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
	
	public String getMove() {
		String move = player.determineMove();
		return null;
	}	

	public static void main (String[] args) {
		new Thread(new Client()).start();
	}
}
