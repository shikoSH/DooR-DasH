package game.engine.cells;

import game.engine.monsters.Monster;

public abstract class TransportCell extends Cell{
	private final int effect;
	//parent of ConveyorBelt, ContaminationSock
	public TransportCell(String name, int effect){
		super(name);
		this.effect = effect;
	}
	public int getEffect() {
		return effect;
	}
	
	public void transport(Monster monster) {
		int newPos = monster.getPosition() + this.effect;
	    monster.setPosition(newPos);
	}
}
