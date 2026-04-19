package game.engine.cells;

import game.engine.monsters.Monster;

public class ConveyorBelt extends TransportCell{

	
	
	//child of Transport cell which is a child of cell
	// TAke care the effect must be positive might change later 
	public ConveyorBelt(String name, int effect) {
		super(name, effect);

	}
	
	public void transport(Monster monster) {
		int newPos = monster.getPosition() + Math.abs(super.getEffect());
	    monster.setPosition(newPos);
	}
	
}