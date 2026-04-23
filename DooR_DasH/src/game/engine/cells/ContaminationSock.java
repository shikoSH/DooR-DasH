package game.engine.cells;

import game.engine.Constants;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier {

	public ContaminationSock(String name, int effect) {
		super(name, effect);
	}
	
	// ContaminationSock

	@Override
	public void transport(Monster monster) {
	    monster.setPosition(monster.getPosition() + getEffect()); // effect is already negative
	    modifyCanisterEnergy(monster, -Constants.SLIP_PENALTY);   // energy penalty here, inside transport
	}

	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
	    monster.alterEnergy(canisterValue);
	}

	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
	    super.onLand(landingMonster, opponentMonster);
	    transport(landingMonster); // transport already handles both position + energy
	}
}

