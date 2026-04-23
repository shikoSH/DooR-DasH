package game.engine.cells;

import game.engine.Constants;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier {

	public ContaminationSock(String name, int effect) {
		super(name, effect);
	}
	
	public void transport(Monster monster) {
		int newPos = monster.getPosition() - Math.abs(super.getEffect());
	    monster.setPosition(newPos);
		monster.setShielded(false);
	}
	
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
	    monster.alterEnergy(canisterValue);
	}
	
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		this.transport(landingMonster);
		modifyCanisterEnergy(landingMonster, -Constants.SLIP_PENALTY);
	}
}

