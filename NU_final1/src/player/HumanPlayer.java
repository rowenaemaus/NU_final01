package player;

public class HumanPlayer implements Player{

	private String colour;
	
	@Override
	public String getName() {
		return "player human";
	}



	public String getInput() {
//		Scanner keyboard = new Scanner(System.in);
//		System.out.println("Give input pls");
//		return keyboard.next();
		return "Q";
	}



	@Override
	public String determineMove() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setColour(String string) {
		// TODO Auto-generated method stub
		
	}

}
