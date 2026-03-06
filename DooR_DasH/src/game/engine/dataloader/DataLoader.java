package game.engine.dataloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import game.engine.cards.*;
import game.engine.cells.*;
import game.engine.monsters.*;
import java.io.*;
import java.util.*;

public class DataLoader {
	private static String CARDS_FILE_NAME="cards.csv";
	private static String CELLS_FILE_NAME="cells.csv";
	private static String MONSTERS_FILE_NAME="monsters.csv";
	

	 public static ArrayList<Card> readCards() throws IOException {
		 ArrayList<Card> cards = new ArrayList<>();
		 BufferedReader br = new BufferedReader(new FileReader(CARDS_FILE_NAME));
		 
	        String line;
	        
	        while ((line = br.readLine()) != null) {
	        	String[] values = line.split(",");
	        	String type = values[0];
	        	String name = values[1];
	        	String description = values[2];
	            int rarity = Integer.parseInt(values[3]);
	            Card c;
	        	switch(type) {
	        	case"SWAPPER":{
	        		c = new SwapperCard(name,description,rarity);
	        		break;
	        	}
	        	case"STARTOVER":{
	        		boolean lucky = Boolean.parseBoolean(values[4]);
	        		c= new StartOverCard(name,description,rarity,lucky); 
	        		break;
	        	}
	        	case"ENERGYSTEAL":{
	        		int energy = Integer.parseInt(values[4]);
	        		c= new EnergyStealCard(name,description,rarity,energy); 
	        		break;
	        	}
	        	case"SHIELD":{
	        		c= new ShieldCard(name,description,rarity); 
	        		break;
	        	}
	        	default:{
	        		int duration = Integer.parseInt(values[4]);
	        		c= new ConfusionCard(name,description,rarity,duration); 
	        		break;
	        	}
	        	}
	        	cards.add(c);
	        }
	        br.close();
	        return cards;   
	        }
		 
}
/*
public static ArrayList<Cell> readCells() throws IOException
	 {
		 ArrayList<Cell> cells = new ArrayList<>();
		 BufferedReader br = new BufferedReader(new FileReader(CELLS_FILE_NAME));
		 
	        String line;
	        
	        while ((line = br.readLine()) != null) {
	        	String[] values = line.split(",");
	        	String name = values[0];
	        	String description = values[1];
	            int rarity = Integer.parseInt(values[2]);
	            boolean lucky = Boolean.parseBoolean(values[3]);

	            Cell c = new Cell(name,description,rarity,lucky);

	            cells.add(c);
	        }

	        br.close();
	        return cards;
		 
	 }
	 
	 public static ArrayList<Monster> readMonsters() throws IOException{}
	
	
}

*/