package game.engine.monsters;
import game.engine.Role;

//Its subclasses are • Dasher• Dynamo• MultiTasker• Schemer

public abstract class Monster implements Comparable<Monster>{
	private String name;
	private String description;
	private Role role;
	private Role originalRole;
	private int energy;//must be >=0 --------------------------------------------------------
	private int position;//must be 0-99 -------------------------------------------------------------------
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;
	
	public Monster(String name, String description, Role originalRole, int energy){
		this.name = name;
		this.description = description;
		this.role=originalRole;
		this.originalRole = originalRole;
		this.energy = energy;
		this.position = 0;
		this.confusionTurns = 0;
		this.frozen=false;
		this.shielded=false;
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
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		  if (energy < 0)
		        this.energy = 0;
		    else
		        this.energy = energy;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		if(position > 99)
			this.position = position-100;
		else
			this.position = position;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public boolean isShielded() {
		return shielded;
	}

	public void setShielded(boolean shielded) {
		this.shielded = shielded;
	}

	public int getConfusionTurns() {
		return confusionTurns;
	}

	public void setConfusionTurns(int confusionTurns) {
		this.confusionTurns = confusionTurns;
	}
	
	public int compareTo(Monster o) {
		return this.position - o.position;
	}
	
}
