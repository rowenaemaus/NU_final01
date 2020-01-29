package player;

import java.util.Scanner;

public class HumanPlayer implements Player{

	private String colour;
	
	@Override
	public String getName() {
		return "player human";
	}

	@Override
	public String determineMove() {
		Scanner keyboard = new Scanner(System.in);
		String answer = keyboard.next();
		int move = -1;
			
		try {
			move = Integer.valueOf(answer);
		} catch (NumberFormatException e) {
			System.out.println("ERROR: Invalid number provided for move!");
			e.printStackTrace();
		}
		
		return ((Integer)move).toString();
	}

	@Override
	public void setColour(String string) {
		// TODO Auto-generated method stub
		
	}

}
