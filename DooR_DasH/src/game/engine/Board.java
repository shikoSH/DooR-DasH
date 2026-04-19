package game.engine;
import java.util.ArrayList;

import game.engine.cells.Cell;
import game.engine.cards.Card;
import game.engine.monsters.Monster;

public class Board {
	private final Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters;
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	public Board(ArrayList<Card> readCards){ 
		boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		cards = new ArrayList<Card>();
		originalCards = readCards;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsterss) {
		Board.stationedMonsters = stationedMonsterss;
		/*for(int i =0; i< Board.stationedMonsters.size();i++) {
			Board.stationedMonsters.set(i,stationedMonsters.get(i));
		}*/
	}
	public static ArrayList<Card> getCards() {
		return cards;
	}
	public static void setCards(ArrayList<Card> cardss) {
		Board.cards = cardss;
		/*for(int i =0; i< Board.cards.size();i++) {
			Board.cards.set(i,cards.get(i));
		}*/
	}
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	//testing
	
	private int[] indexToRowCol(int index) {
		int row= (int)index/10;
		int col;
		if(row%2==0)col=index%10;
		else col=9-(index%10);
		int[] s= {row,col};
		return s;
	}
	private Cell getCell(int index) {
		Cell[][] c= getBoardCells();
		int[] a= indexToRowCol(index);
		return c[a[0]][a[1]];
	}
}
