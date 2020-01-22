package player;

public interface Player {

	public String getName();	
	
	// determine move, read input and returns as int
	public String determineMove();

	public String getInput();

	public void setColour(String string);

}
