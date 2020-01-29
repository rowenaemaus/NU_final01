package player;

import java.util.Random;

import goGame.GoGame;

public class ComputerPlayer implements Player {

	private int dimension;
	private int min;
	private int max;
	private GoGame g;

	public ComputerPlayer() {
	}

	public void sendGame(GoGame g) {
		dimension = g.getDimension();
		min = 0;
		max = dimension*dimension;
		this.g = g;
	}
	
	@Override
	public String getName() {
		return "computer player";
	}

	@Override
	public String determineMove() {
		Random r = new Random();
		int move = -1;
		do {
			move = r.nextInt(max);
		} while (!g.checkValidity(((Integer)move).toString()));

		return ((Integer) move).toString();
	}

	@Override
	public void setColour(String string) {
		
	}

	public static void main(String[] args) {
		ComputerPlayer p = new ComputerPlayer();
		p.determineMove();
	}
}
