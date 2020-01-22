package goGame;

import com.nedap.go.gui.GoGUIIntegrator;

import client.Client;

public class GoGame implements Runnable {

	private GoGUIIntegrator g;
	private int dimension;
	private Client client;
	private String colour;

	public GoGame (int dimension, Client client) {
		this.client = client;
		this.dimension = dimension;
		client.printMessage("Go game handler set up. Preparing GUI...");
		System.out.println("flag3");
	}

	public void setUpGUI() {
		System.out.println("flag1");
		g = new GoGUIIntegrator(true, true, dimension);
		System.out.println("flag2");
		g.startGUI();
		System.out.println("flag5");
		g.setBoardSize(dimension);
		System.out.println("flag6");
		client.printMessage("GUI ready");
		client.printMessage("-----------------------");
	}

	public boolean checkValidity(int move) {
		// TODO implement
		return false;
	}

	@Override
	public void run() {
		setUpGUI();		
	}

	public void newBoard(String string) {
		// TODO Auto-generated method stub
	}

	public void setColour(String colour) {
		// TODO Auto-generated method stub
	}

	public boolean checkValidity(String move) {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateBoard(String board) {
		// TODO Auto-generated method stub
		// also show new board	
	}
	
	public void showBoard() {
		// TODO show updated board as it is
	}
}
