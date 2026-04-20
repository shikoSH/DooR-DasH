package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	public void executePowerupEffect(Monster opponentMonster) {
		// TODO Auto-generated method stub
		
	}
	
	private int stealEnergyFrom(Monster target) {
		int opponentEnergy = target.getEnergy();
		int gainedEnergy;
		if(opponentEnergy >= Constants.SCHEMER_STEAL) {
			gainedEnergy = Constants.SCHEMER_STEAL;
			target.setEnergy(gainedEnergy - Constants.SCHEMER_STEAL);
		}
		else if(opponentEnergy < Constants.SCHEMER_STEAL && opponentEnergy > 0) {
			gainedEnergy = opponentEnergy;
			target.setEnergy(0);
		}
		else {
			gainedEnergy = 0;
		}
		return gainedEnergy;
	}
	
	public void setEnergy(int energy) {
		int newEnergy = this.getEnergy()+ energy+ 10;
		super.setEnergy(newEnergy);
	
	}
}
