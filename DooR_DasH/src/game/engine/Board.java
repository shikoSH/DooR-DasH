package game.engine;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.Constants;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.dataloader.DataLoader;
import game.engine.cells.ContaminationSock;
import game.engine.cells.Cell;
import game.engine.cards.Card;
import game.engine.monsters.Monster;
import game.engine.cells.CardCell;
import game.engine.Game;

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
		int row= index/10;
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
	private void setCell(int index, Cell cell) {
		int[] a =indexToRowCol(index);
		int a1= a[0];int a2= a[1];
		boardCells[a1][a2]=cell;
	}
	void initializeBoard(ArrayList<Cell> specialCells) throws IOException {int v=0;int s=0;
	ArrayList<Cell> doors= new ArrayList<Cell>();
	ArrayList<Cell> conv=new ArrayList<Cell>();
	ArrayList<Cell> sock=new ArrayList<Cell>();
	
	for(int i=0;i<specialCells.size();i++) {
		if(specialCells.get(i) instanceof DoorCell) {doors.add(specialCells.get(i));}
		else if(specialCells.get(i) instanceof ConveyorBelt) {conv.add(specialCells.get(i));}
		else if(specialCells.get(i) instanceof ContaminationSock) {sock.add(specialCells.get(i));}
	}
	for(int i=0;i<100;i++) {
		if(i%2==1)
		setCell(i, doors.get(i/2));
		else {
			setCell(i, new Cell("Cell "+i));
		}
	}
	for(int i=0;i<Constants.CONVEYOR_CELL_INDICES.length;i++) {
		setCell(Constants.CONVEYOR_CELL_INDICES[i], conv.get(i));
	}
	for(int i=0;i<Constants.SOCK_CELL_INDICES.length;i++) {
		setCell(Constants.SOCK_CELL_INDICES[i], sock.get(i));
	}
	for(int i=0;i<Constants.CARD_CELL_INDICES.length;i++) {
		setCell(Constants.CARD_CELL_INDICES[i], new CardCell("Cardcell "+i));
	}
	for(int i=0;i<Constants.MONSTER_CELL_INDICES.length;i++) {
		Monster m = stationedMonsters.get(i);
		
		setCell(Constants.MONSTER_CELL_INDICES[i],new MonsterCell(m.getName(), m) );}
	
}
	private static void setCardsByRarity(){


	    ArrayList<Card> newcards = new ArrayList<>();
	    for(int i=0 ; i<originalCards.size(); i++){
	        int j=0;
	        Card Temp = originalCards.get(i);
	        while(j<Temp.getRarity()){
	            newcards.add(Temp);
	            j++;
	            }
	        }
	    cards = (ArrayList<Card>) newcards.clone();

	}
	
	public static void reloadCards(){
		setCardsByRarity();
		Collections.shuffle(Board.getCards());
}
	
	
	public static Card drawCard(){
		ArrayList<Card> temp = Board.getCards();
		if(temp.size()== 0){
			reloadCards();
			temp = Board.getCards();
		}
		Card res= temp.remove(0);
		return res;


	}
	
	
	
}
