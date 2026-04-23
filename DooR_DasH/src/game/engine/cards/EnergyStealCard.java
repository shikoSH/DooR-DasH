package game.engine.cards;

import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class EnergyStealCard extends Card implements CanisterModifier {
	private int energy;

	public EnergyStealCard(String name, String description, int rarity, int energy) {
		super(name, description, rarity, true);
		this.energy = energy;
	}
	
	public int getEnergy() {
		return energy;
	}
	@Override
	public void performAction(Monster player, Monster opponent) {
		
		//initialising variables
		int player_original_energy = player.getEnergy();
		int opponent_original_energy = opponent.getEnergy();
		int change_in_energy = this.getEnergy();
		
		if(opponent.isShielded()) {
			opponent.setShielded(false);
			return;
		}
		//case that opponent energy is greater than the change in energy 
		if(opponent_original_energy>=change_in_energy) {
			
			modifyCanisterEnergy(player,change_in_energy);
			int negativeChange = 0 - change_in_energy;
			modifyCanisterEnergy(opponent,negativeChange);
		}
		else {
			//case that the opponent energy is less than the change in energy
			modifyCanisterEnergy(player,opponent.getEnergy());
			modifyCanisterEnergy(opponent,0);
		}
}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		monster.alterEnergy(canisterValue);
		
	}
}
