package game.engine.cards;

import java.util.ArrayList;

import game.engine.Board;
import game.engine.monsters.Monster;

public class ShieldCard extends Card {
	
	public ShieldCard(String name, String description, int rarity) {
		super(name, description, rarity, true); 
	}
	@Override
	public void performAction(Monster player, Monster opponent) {
		//setting the shield to player and removing from the opponent
		player.setShielded(true);
		if(opponent.isShielded()== true) {
			opponent.setShielded(false);
		}
		ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
		for (int i = 0; i < stationedMonsters.size(); i++) {
		    Monster temp = stationedMonsters.get(i);
		    if(temp.getRole()==player.getRole())
		    {
		    	temp.setShielded(true);
		    }
		    else 
		    {
		    	temp.setShielded(false);
		    }
		}

	}
}
