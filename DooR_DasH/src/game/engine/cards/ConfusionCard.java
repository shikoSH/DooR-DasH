package game.engine.cards;

	//child of Card


public class ConfusionCard extends Card{

	private final int duration;
	
	
	public ConfusionCard(String name, String description, int rarity, int duration) {
		super(name, description, rarity, false);
		this.duration=duration;
	}
	
	public int getDuration() {
		return duration;
	}
	
	
}
