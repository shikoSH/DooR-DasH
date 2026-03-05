package game.engine.monsters;
import game.engine.Role;

public abstract class Monster implements Comparable{
	private String name;
	private String description;
	private Role role;
	private Role originalRole;
	private int energy;
	private int position;
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;
	
	public Monster(String name, String description, Role originalRole, int energy){
		this.name = name;
		this.description = description;
		this.originalRole = originalRole;
		this.energy = energy;
		this.position = 0;
		this.confusionTurns = 0;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Role getOriginalRole() {
		return originalRole;
	}
	
	public int compareTo(Monster o) {
		return this.position - o.position;
	}
	
}
