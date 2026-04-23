package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}
	@Override
	public void executePowerupEffect(Monster opponentMonster) {
		this.setNormalSpeedTurns(2);
	}
	public void move(int distance) {
		if(this.getNormalSpeedTurns() > 0) {
			super.move(distance);
			this.setNormalSpeedTurns(this.getNormalSpeedTurns() - 1);
		}
		else {
			int halfDistance = (int) (distance*0.5);
		super.move(halfDistance);
		}
	}
	
	public void setEnergy(int energy) {
		int change = energy - this.getEnergy();
		int newEnergy = this.getEnergy()+ change+ Constants.MULTITASKER_BONUS;
		super.setEnergy(newEnergy); 
	
	}
}