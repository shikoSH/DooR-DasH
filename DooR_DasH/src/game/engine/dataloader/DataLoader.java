package game.engine.dataloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;

public class DataLoader {
	private String CARDS_FILE_NAME="cards.csv";
	private String CELLS_FILE_NAME="cells.csv";
	private String MONSTERS_FILE_NAME="monsters.csv";
	
	
	
	import java.io.*;
	import java.util.*;

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
	    }
*/
	 public static ArrayList<Card> readCards() throws IOException {
		 ArrayList<Card> cards = new ArrayList<>();
		 BufferedReader br = new BufferedReader(new FileReader(CARDS_FILE_NAME));
		 
	        String line;

	        while ((line = br.readLine()) != null) {
	            System.out.println(line);
	        }

	        br.close();
	        cards.add();
		 
	 }
	 public static ArrayList<Cell> readCells() throws IOException{}
	 
	 public static ArrayList<Monster> readMonsters() throws IOException{}
	
	

}


/*


FileReader
BufferedReader
String*/