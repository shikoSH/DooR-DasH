package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		setCardsByRarity();
		reloadCards();
	} 
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}
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
	public void initializeBoard(ArrayList<Cell> specialCells) throws IOException {int v=0;int s=0;
	ArrayList<Cell> doors= new ArrayList<Cell>();
	ArrayList<Cell> conv=new ArrayList<Cell>();
	ArrayList<Cell> sock=new ArrayList<Cell>();
	
	for(int i=0;i<specialCells.size();i++) {
		if(specialCells.get(i) instanceof DoorCell) {DoorCell d= (DoorCell) specialCells.get(i);doors.add(d);}
		else if(specialCells.get(i) instanceof ConveyorBelt) {ConveyorBelt cv = (ConveyorBelt) specialCells.get(i);conv.add(cv);}
		else if(specialCells.get(i) instanceof ContaminationSock) {ContaminationSock cs= (ContaminationSock) specialCells.get(i);sock.add(cs);}
	}
	
	int doorIndex = 0;
	for(int i=0;i<Constants.BOARD_SIZE;i++) {
		if(i%2==1)
		setCell(i, doors.get(doorIndex++));
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
	for (int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
        if (i >= stationedMonsters.size()) break; 
        Monster m = stationedMonsters.get(i);
        m.setPosition(Constants.MONSTER_CELL_INDICES[i]);
        setCell(Constants.MONSTER_CELL_INDICES[i], new MonsterCell(m.getName(), m));
    }
	}
	private void setCardsByRarity(){


	    ArrayList<Card> newcards = new ArrayList<>();
	    for(int i=0 ; i<originalCards.size(); i++){
	        int j=0;
	        Card Temp = originalCards.get(i);
	        while(j<Temp.getRarity()){
	            newcards.add(Temp);
	            j++;
	            }
	        }
	    originalCards = (ArrayList<Card>) newcards.clone();

	}
	
	public static void reloadCards(){
		setCards(getOriginalCards());
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
	
	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException
	{
		int currentMonster_position= currentMonster.getPosition();		
		int opponentMonster_position= opponentMonster.getPosition();
		currentMonster.move(roll);
		int final_position = currentMonster.getPosition();
		//case that final position classes with oppoenent's position
		if(final_position==opponentMonster_position) {
			currentMonster.setPosition(currentMonster_position);
			throw new InvalidMoveException("Landing position is occupied by the opponent!");
		}
		//calling on land to effect
		Cell currentCell= getCell(final_position);
		currentCell.onLand(currentMonster, opponentMonster);
		//decrementing confusion			
		currentMonster.decrementConfusion();
		opponentMonster.decrementConfusion();
		//refreshing the cells 
		updateMonsterPositions(currentMonster, opponentMonster);
	}
	
	private void updateMonsterPositions(Monster player, Monster opponent) {
		
		//the problem is that when swapper card is used the monster position updates but the cells still have the original monster refrence so you should wynchronise it every time
		for(int i=0;i<Constants.BOARD_SIZE; i++) {
				Cell cell =getCell(i);
				cell.setMonster(null);
		}
		Cell player_cell=  getCell(player.getPosition());
		player_cell.setMonster(player);
		Cell opponent_cell = getCell(opponent.getPosition());
		opponent_cell.setMonster(opponent);
		}
}
		
