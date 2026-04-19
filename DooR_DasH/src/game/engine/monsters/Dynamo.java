package game.engine.monsters;
import game.engine.Role;

//subclass of monster
public class Dynamo extends Monster{
	
	public Dynamo(String name, String description, Role role, int energy){
		super(name, description, role, energy);
	}
	
	public void executePowerupEffect(Monster opponentMonster) {
		
	}
	
	public void setEnergy(int energy) {
		int newEnergy = this.getEnergy()+ 2*energy;
		super.setEnergy(newEnergy);
	}
}
