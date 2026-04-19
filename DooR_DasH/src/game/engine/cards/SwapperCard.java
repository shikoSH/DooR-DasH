package game.engine.cards;

import game.engine.monsters.Monster;

public class SwapperCard extends Card{

	public SwapperCard(String name, String description, int rarity) {
		super(name, description, rarity, true);
	}

	@Override
	public void performAction(Monster player, Monster opponent) {
		int player_position = player.getPosition();
		int opponent_position = opponent.getPosition();
		// Case that the player is behind the opponent in position
		if(player_position<opponent_position) {
			player.setPosition(opponent_position);
			opponent.setPosition(player_position);
		}
	}

}
