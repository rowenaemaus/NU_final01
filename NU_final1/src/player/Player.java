package player;

public interface Player {

	public String getName();
	
	// determine move, read input and returns as int
	public int determineMove();

	public void setColour(String string);

}
