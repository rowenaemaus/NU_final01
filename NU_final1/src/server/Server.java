package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.MaxGamesException;
import exceptions.NoOpponentException;

public class Server implements Runnable {
	/**
	 * Todo block:
	 * - dat als een game af is notifyen dat er weer een nieuwe kan
	 * - wanneer wil je eruit?
	 * - poort vragen/hardcoden
	 * - protocol integreren
	 * - kleurhandling
	 * 
	 */
	private ServerSocket ssock;

	private List<ClientHandler> handlers;
	private List<Game> games;

	private int maxHandlers;
	private int maxGames;

	private int nextClientNo;
	private static String serverName = "ServeRowena";
	private String IPAddress;
	
	public Server () {
		handlers = new ArrayList<>();
		games = new ArrayList<>();
		nextClientNo = 1;
		maxGames = 1;
		maxHandlers = 2 * maxGames;		
	}

	@Override
	public void run() {
		boolean openNewSocket = true;
		while (openNewSocket) {
			try {
				setupSsock();

				// TODO while wat wil je dit doen? Hoe eruit?
				while (true) {
					Socket sock = ssock.accept();
					String name = "Client " + String.format("%02d", nextClientNo++);
					printMessage("New client [" + name + "] trying to connect to server!");
					ClientHandler handler = new ClientHandler(sock, this, name);
					
					if (handlers.size() < maxHandlers) {
						handlers.add(handler);
						if (handler.getConnected()) {
							printMessage("Trying to start new game for " + name);
							startNewGame(handler);
						} else {
							printMessage("Unable to connect client");
							// TODO en nu?
						}
					} else {
						printMessage(String.format("Max handlers connected. Unable to add handler for [%s] to server right now.", name));
						printMessage("Please wait for a game to end and try to reconnect.");
						printMessage("________________");
						sock.close();
					}
				}
			} catch (Exception e1) {
				System.out.println("server flag1");
				System.out.println(e1);
				e1.printStackTrace();
				openNewSocket = false;
			}
		}
	}

	public void setupSsock() {
		try {
			IPAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			printMessage("Unable to retrieve IP address of localhost!");
			e.printStackTrace();
		}

		ssock = null;
		int maxAttempts = 2;
		int attempts = 0;
		while (ssock == null && attempts < maxAttempts) {
			int port = getPort();
			try {
				printMessage(String.format("Trying to open a socket at %s on port %d", IPAddress, port));
				ssock = new ServerSocket(port, 0, InetAddress.getByName(IPAddress));
				printMessage("Server started at port " + port);
				printMessage("----------------------------------------------");
				printMessage("----------------------------------------------");
			} catch (IOException e) {
				printMessage(String.format("WARNING: Attempt %d: could not create socket on port %d", attempts, port));
				attempts++;
			}
		}
	}
	
	public int getPort() {
		//TODO implement
		return 8070;
	}

	public void startNewGame(ClientHandler c1) throws MaxGamesException, NoOpponentException {
		// TODO game naam geven.
		ClientHandler opponent = null;

		for (ClientHandler c : handlers) {
			if (c.getConnected() && !c.getPlaying() && !c.equals(c1)) {
				printMessage(String.format("Opponent found for %s!", c1.getName())); 
				opponent = c;
				printMessage(String.format("Starting the epic battle: %s vs. %s", c1.getName(), opponent.getName()));
			}
		}

		if (opponent == null) {
			printMessage("No opponent found! Please wait until another user tries to connect.");
			printMessage("...");
			return;
		}
		if (games.size() < maxGames) {
			printMessage("Max number of games not reached, starting a new battle.");
			// TODO notify de spelers ook met een sendMessage ofzo
			Game g = new Game(c1, opponent, this);
			games.add(g);
			new Thread(g).start();
		} else {
			throw new MaxGamesException("Max number of games reached!");
		}
	}
	
	public void endGame(Game g) {
		handlers.remove(g.getc1());
		handlers.remove(g.getc2());
		games.remove(g);
	}

	public void printMessage(String s) {
		System.out.println(s);
	}	

	public char determineColour() {
		// TODO return de kleur die of gewenst is of nog niet bezet
		return 0;
	}

	public char getHello() {
		// TODO	return de string van hello handshake volgens protocol 
		return 'H';
	}


	// _________________ MAIN _______________________________

	public static void main (String[] args) {
		Server srv = new Server();
		System.out.println(String.format("Welcome to %s, ready to host...", serverName));
		try {
			System.out.println("My IP address is " + InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("Unable to retrieve IP");
			e.printStackTrace();
		}
		new Thread(srv).start();
	}

}
