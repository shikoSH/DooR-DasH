package game.engine.cells;

import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier{
	// the effect value is always negative 
	public ContaminationSock(String name, int effect){ 
		 super(name,effect);
	 }
	
	public void modifyEnergy(Monster monster) {
		
	}
}
