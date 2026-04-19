package game.engine.cells;

import game.engine.monsters.Monster;

public class MonsterCell extends Cell {
	private final Monster cellMonster;

	 public Monster getCellMonster() {
		 return cellMonster;
	 }

	 public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	 }
	 
	 public void onLand(Monster landingMonster, Monster opponentMonster) {
		 super.onLand(landingMonster, opponentMonster);
		 
		 if(landingMonster.getRole() == this.cellMonster.getRole())
			 landingMonster.executePowerupEffect(landingMonster);
		 else {
			 if(landingMonster.getEnergy() > cellMonster.getEnergy()) {
				 if(!landingMonster.isShielded()) {
					 int temp = landingMonster.getEnergy();
					 landingMonster.setEnergy(cellMonster.getEnergy());
					 cellMonster.setEnergy(temp);
				 }else
					 cellMonster.setEnergy(landingMonster.getEnergy());
					 
			 }
		 }
			 
			
	 }

}
