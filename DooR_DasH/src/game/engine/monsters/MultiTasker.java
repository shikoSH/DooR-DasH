package game.engine.monsters;

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
		// TODO Auto-generated method stub
		
	}
	public void move(int distance) {
		super.move((int)0.5*distance);
	}
	
	public void setEnergy(int energy) {
		int newEnergy = this.getEnergy()+ energy+ 200;
		super.setEnergy(newEnergy);
	
	}
}