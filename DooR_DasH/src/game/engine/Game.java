package game.engine;

import java.io.IOException;
import java.util.ArrayList;

import game.engine.dataloader.DataLoader;
import game.engine.monsters.Monster;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters;
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException{
		board = new Board(DataLoader.readCards());
		allMonsters = DataLoader.readMonsters();
		player = selectRandomMonsterByRole(playerRole);
		Role opponentRole = (playerRole == Role.SCARER)? Role.LAUGHER: Role.SCARER;
		opponent = selectRandomMonsterByRole(opponentRole);
		this.current = player;
		
	}
	
	
	private Monster selectRandomMonsterByRole(Role role) {
		/*while(true) {
			double rand = Math.random();
			int i= (int) (rand*allMonsters.size());
			Monster m = allMonsters.get(i);
			if(m.getRole() == role) {
				return m;
			}
		} */
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		for(int i = 0; i < allMonsters.size();i++) {
	            Monster m = allMonsters.get(i);
	            if(m.getRole() == role) {
	                candidates.add(m);
	            }
	        }
	        if(candidates.isEmpty()) {
	            return null;
	        }
	        double rand = Math.random();
	        int i= (int) (rand*candidates.size());
	        return candidates.get(i);
	}
	
	public Monster getCurrent() {
		return current;
	}
	public void setCurrent(Monster current) {
		this.current = current;
	}
	public Board getBoard() {
		return board;
	}
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters;
	}
	public Monster getPlayer() {
		return player;
	}
	public Monster getOpponent() {
		return opponent;
	}
	
	
}
