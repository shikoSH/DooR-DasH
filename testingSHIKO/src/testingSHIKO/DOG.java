package testingSHIKO;

public class DOG {
	private String colour;
	private int age;
	private String name;
	private Breed breed;
	
	public DOG(String colour, int age, String name, Breed breed) {
		this.colour = colour;
		this.name = name;
		this.age = age;
		this.breed = breed;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getAge() {
		return this.age;
	}
	
	public String getColour() {
		return this.colour;
	}
	
	public Breed getBreed() {
		return this.breed;
	}
	
}
