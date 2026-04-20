package game.engine.monsters;

import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;

import java.util.ArrayList;

import game.engine.Board;
public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	public void executePowerupEffect(Monster opponentMonster) {
		int allGainedEnergy = stealEnergyFrom(opponentMonster);
		ArrayList<Monster> stationed = Board.getStationedMonsters();
		for(int i=0; i<stationed.size();i++) {
			allGainedEnergy += stealEnergyFrom(stationed.get(i));
		}
		this.setEnergy(allGainedEnergy);
	}
	
	private int stealEnergyFrom(Monster target) {
		int opponentEnergy = target.getEnergy();
		int gainedEnergy;
		if(opponentEnergy >= Constants.SCHEMER_STEAL) {
			gainedEnergy = Constants.SCHEMER_STEAL;
			target.setEnergy(target.getEnergy() - Constants.SCHEMER_STEAL);
		}
		else {
			gainedEnergy = opponentEnergy;
			target.setEnergy(0);
		}
		return gainedEnergy;
	}
	
	public void setEnergy(int energy) {
		int newEnergy = this.getEnergy()+ energy+ 10;
		super.setEnergy(newEnergy);
	
	}
}
