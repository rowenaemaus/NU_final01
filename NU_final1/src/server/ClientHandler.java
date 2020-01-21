package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler implements Runnable{

	private boolean connected = false;
	private boolean playing = false;

	private BufferedReader in;
	private BufferedWriter out;
	private Socket sock; 
	private Game game;
	private Server srv;
	private String name;

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
			if (shakeIt.charAt(0) == 'H') {
				srv.printMessage(String.format("Correct handshake received from client %s!", name));
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

		try {
			msg = in.readLine();
			while (msg != null) {
				srv.printMessage("> [" + name + "] Incoming: " + msg);
				handleCommand(msg);
				out.newLine();
				out.flush();
				msg = in.readLine();
			}
		} catch (IOException e) {
			shutDown();
		}
	}


	public void handleCommand(String msg) {
		// TODO adjust according to protocol
		String[] messages = msg.split(";");

		char command = msg.charAt(0);

		switch (command) {
		case 'x':
			srv.printMessage(String.format("> [%s] indicated to quit! Shutting down this game.", name));
			shutDown();
			srv.endGame(this.game);
			break;
		case 't':
			break;
		default:
			srv.printMessage("Command hallo doe iets jo");
			break;
		}



		// TODO handle inkomende command

	}

	// continuously listen for quit en hou verbinding in de gaten
	// verdeel kleur
	// stuur een start signaal en wie welke kleur

	// loop hierover:
	// handle timeout
	// stuur turn en luister exclusief naar diegene

	// check validity
	// if niet valid ga naar end
	// als wel valid dan zet de move op bord
	// connect met game wat je wil doen
	// stuur de nieuwe info naar beiden.
	// return naar begin loop


	// wanneer wel quit of verbinding weg

	// endGame();
	// close();
	// shutdown(); (evt niet).


	public String getName() {
		return name;
	}

	public void setGame(Game g) {
		this.game = g;
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
