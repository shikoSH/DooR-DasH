package game.engine.cells;

import game.engine.monsters.Monster;

public class ConveyorBelt extends TransportCell {

	public ConveyorBelt(String name, int effect) {
		super(name, effect);
	}
	public void transport(Monster monster) {
		int newPos = monster.getPosition() + Math.abs(super.getEffect());
	    monster.setPosition(newPos);
	}
	
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		
		this.transport(landingMonster);
	}
}
