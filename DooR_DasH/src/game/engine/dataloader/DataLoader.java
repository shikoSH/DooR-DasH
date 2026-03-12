package game.engine.dataloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.*;

import game.engine.Role;
import game.engine.cards.*;
import game.engine.cells.*;
import game.engine.monsters.*;



public class DataLoader {
	private static final String CARDS_FILE_NAME="cards.csv";
	private static final String CELLS_FILE_NAME="cells.csv";
	private static final String MONSTERS_FILE_NAME="monsters.csv";
	

	public static ArrayList<Card> readCards() throws IOException {
		 ArrayList<Card> cards = new ArrayList<>();
		 BufferedReader br = new BufferedReader(new FileReader(CARDS_FILE_NAME));
		 String line;
	        
        while ((line = br.readLine()) != null) {
        	String[] v = line.split(",");
        	switch(v[0]) {
	        	case"SWAPPER":{
	        		cards.add(new SwapperCard(v[1],v[2],Integer.parseInt(v[3])));
	        		break;
	        	}
	        	case"STARTOVER":{
	        		cards.add(new StartOverCard(v[1],v[2],Integer.parseInt(v[3]),Boolean.parseBoolean(v[4]))); 
	        		break;
	        	}
	        	case"ENERGYSTEAL":{
	        		cards.add(new EnergyStealCard(v[1],v[2],Integer.parseInt(v[3]),Integer.parseInt(v[4]))); 
	        		break;
	        	}
	        	case"SHIELD":{
	        		cards.add(new ShieldCard(v[1],v[2],Integer.parseInt(v[3])));
	        		break;
	        	}
	        	default:{
	        		cards.add(new ConfusionCard(v[1],v[2],Integer.parseInt(v[3]),Integer.parseInt(v[4]))); 
	        		break;
	        	}
        	}

        }
        br.close();
        return cards;   
	 }	 

public static ArrayList<Monster> readMonsters() throws IOException{
    ArrayList<Monster> monsters = new ArrayList<>();
    
    BufferedReader br = new BufferedReader(new FileReader(MONSTERS_FILE_NAME));
    String line;
    
    while((line = br.readLine()) != null) {
        String[] v = line.split(",");
        String type = v[0];
        String name = v[1];
        String description = v[2];
        Role role = Role.valueOf(v[3]);
        int energy = Integer.parseInt(v[4]);
        
        switch (type) {
        case "Dasher":
            monsters.add(new Dasher(name, description, role, energy));
            break;
        case "Dynamo":
            monsters.add(new Dynamo(name, description, role, energy));
            break;
        case "MultiTasker":
            monsters.add(new MultiTasker(name, description, role, energy));
            break;
        default:
            monsters.add(new Schemer(name, description, role, energy));
            break;
        }
    }
    br.close();
    return monsters;
}



public static ArrayList<Cell> readCells() throws IOException{
    ArrayList<Cell> cells = new ArrayList<>();
    BufferedReader br = new BufferedReader(new FileReader(CELLS_FILE_NAME));
    
       String line;
       
       while ((line = br.readLine()) != null) {
           String[] v = line.split(",");
           String name=v[0];
           
           if(v.length==3) { Role role;
           if(v[1].equals("LAUGHER"))
               role=Role.LAUGHER;
           else role = Role.SCARER;
           int energy=Integer.parseInt(v[2]);
               Cell c=new DoorCell(name,role, energy);
               cells.add(c);
           }
           else {
        	   
               int effect=Integer.parseInt(v[1]);
               Cell c;
               if(effect>0) {
            	   c= new ConveyorBelt(name, effect);
               }
               else {
            	   c= new ContaminationSock(name, effect);
            	   
               }
               
                cells.add(c);
           }

         
          
       }

       br.close();
       return cells;
}
}