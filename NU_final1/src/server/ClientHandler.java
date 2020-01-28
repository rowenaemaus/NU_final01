package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import Protocol.ProtocolMessages;
import exceptions.InvalidColourException;

public class ClientHandler implements Runnable{

	private boolean connected = false;
	private boolean playing = false;

	private BufferedReader in;
	private BufferedWriter out;
	private Socket sock; 
	private Game game;
	private Server srv;
	private String name;
	private boolean colour;

	public ClientHandler(Socket sock, Server srv, String name) throws IOException {
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			this.sock = sock;
			this.srv = srv;
			this.name = name;

			doHandshake();
		} catch (IOException e) {
			shutDown();
		}
	}

	public void doHandshake() {
		String shakeIt = null;

		try {
			shakeIt = in.readLine();
			if (shakeIt.charAt(0) == ProtocolMessages.HANDSHAKE) {
				srv.printMessage(String.format("Correct handshake received from %s!", name));
				out.write(srv.getHello());
				out.newLine();
				out.flush();
				srv.printMessage("Outward handshake to client sent.");
			}
		} catch (IOException e) {
			shutDown();
		}
		srv.printMessage("Handshake completed [" + name + "] connected." );
		this.connected = true;
	}

	@Override
	public void run() {
		srv.printMessage("Handler ready for [" + name + "]");
		String msg = null;
		srv.printMessage("____________");
		
		try {
			msg = in.readLine();
			while (msg != null) {
				game.handleCommand(this, msg);
				msg = in.readLine();
			}
		} catch (IOException e) {
			shutDown();
		}
	}

	public void sendToClient(String s) {
		try {
			out.write(s);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public void setGame(Game g) {
		this.game = g;
	}

	public void setColour(char colour) throws InvalidColourException {
		if (colour == ProtocolMessages.WHITE) 
			this.colour = true;
		else if (colour == ProtocolMessages.BLACK) {
			this.colour = false;
		} else {
			throw new InvalidColourException("ERROR: No valid colour provided to player!");
		}
	}
	
	public boolean getColour() {
		return this.colour;
	}
	
	public boolean getConnected() {
		return this.connected;
	}

	public boolean getPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	private void shutDown() {
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
