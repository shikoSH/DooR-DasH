package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
	
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
		allMonsters.remove(player);
		allMonsters.remove(opponent);
		board.setStationedMonsters(allMonsters);
		board.initializeBoard(DataLoader.readCells());
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
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	 private Monster getCurrentOpponent() {
		 if(this.getCurrent()==this.getOpponent()) {return this.getPlayer();}
		 else return this.getOpponent();
	 }
	 private int rollDice() {int x= (int) ((Math.random()*6)+1);
	 return x;}
	 public void usePowerup() throws OutOfEnergyException{
		 if (this.getCurrent().getEnergy()>=Constants.POWERUP_COST) {this.getCurrent().executePowerupEffect(getCurrentOpponent());
		 getCurrent().setEnergy(getCurrent().getEnergy()-Constants.POWERUP_COST);
		 }
		 else {OutOfEnergyException e= new OutOfEnergyException();
		 throw e;}
	 }
	 public void playTurn() throws InvalidMoveException{
		 if(this.getCurrent().isFrozen()==true) 
		 {this.getCurrent().setFrozen(false);this.switchTurn();}
		 else 
		 {  board.moveMonster(this.getCurrent(), rollDice(),this.getCurrentOpponent());
		 }
	 }
	 private void switchTurn() {
		 setCurrent(this.getCurrentOpponent());
	 }
	 private boolean checkWinCondition(Monster monster) {
		 if (monster.getPosition()==99 && monster.getEnergy()>=1000) {return true;}
		 else return false;
			 } 
	 public Monster getWinner() {
		 if(this.checkWinCondition(getPlayer())) {return getPlayer();}
		 else if (this.checkWinCondition(getOpponent())) {return getOpponent();}
		 else return null;
	 }
	 
	
}