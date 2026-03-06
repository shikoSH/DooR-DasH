package game.engine.cells;

import game.engine.monsters.Monster;

public class MonsterCell extends Cell {
	 Monster cellMonster;

	 public Monster getCellMonster() {
		 return cellMonster;
	 }

	 public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	 }

}
