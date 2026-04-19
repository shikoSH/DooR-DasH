package game.engine;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.Constants;
import game.engine.cells.ConveyorBelt;
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
	private void setCell(int index, Cell cell) {
		int[] a= indexToRowCol(index);
		Cell c= getCell(index);
		c=cell;
	}
	void initializeBoard(ArrayList<Cell> specialCells) throws IOException {int v=0;int s=0;
		for(int i=0;i<specialCells.size();i++) {
			if(i<=49) {
				setCell((2*i)+1, specialCells.get(i));
			}
			else {
				if(specialCells.get(i) instanceof ContaminationSock ) {
					setCell(Constants.SOCK_CELL_INDICES[s],specialCells.get(i));s++;
				}
				else {
					setCell(Constants.CARD_CELL_INDICES[v],specialCells.get(i));v++;
				}
			}
		}
		ArrayList<Cell> mc= new ArrayList<>();
		ArrayList<Cell> cc= new ArrayList<>();
		for(int i=0;i<stationedMonsters.size();i++) {
			
			MonsterCell m= new MonsterCell(stationedMonsters.get(i).getName(),stationedMonsters.get(i));
			mc.add(m);
		}
		for(int i=0;i<cards.size();i++) {
			CardCell m= new CardCell(cards.get(i).getName());
			cc.add(m);
		}
		int ca=0;int mon=0;
		for(int i=0;i<mc.size();i++) {
			setCell(Constants.MONSTER_CELL_INDICES[mon],mc.get(i) );
		}
		for(int i=0;i<cc.size();i++) {
			setCell(Constants.CARD_CELL_INDICES[ca],cc.get(i) );
		}
		
		
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
	    cards = newcards;

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
