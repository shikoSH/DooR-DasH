package game.engine.dataloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;
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
	        	String name = values[0];
	        	String description = values[1];
	            int rarity = Integer.parseInt(values[2]);
	            boolean lucky = Boolean.parseBoolean(values[3]);

	            Card c = new Card(name,description,rarity,lucky);

	            card.add(s);
	        }

	        br.close();
	        return
		 
	 }
	 public static ArrayList<Cell> readCells() throws IOException{}
	 
	 public static ArrayList<Monster> readMonsters() throws IOException{}
	
	
}

/*
try {
    BufferedReader br = new BufferedReader(new FileReader("students.csv"));
    String line;

    while ((line = br.readLine()) != null) {

        String[] values = line.split(",");

        int id = Integer.parseInt(values[0]);
        String name = values[1];
        int age = Integer.parseInt(values[2]);

        Student s = new Student(id, name, age);

        students.add(s);
    }

    br.close();

} catch (Exception e) {
    e.printStackTrace();
}

System.out.println("Students loaded: " + students.size());
*/

/*


FileReader
BufferedReader
String*/