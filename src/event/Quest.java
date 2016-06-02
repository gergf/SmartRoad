package event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Esta clase representa una mision dentro de la simulacion, 
 * el objetivo es hacer mucho mas intuitivo y visible el funcionamiento
 * del sistema al tratar eventos
 * @author ger
 *
 */
public class Quest {

	private String id; 
	private String topic; 
	private String description; 
	/* Priority level 
	 * 1: Low 
	 * 2: Normal 
	 * 3: High 
	 * 4: Extreme */
	private int priority; 
	private ArrayList<String> route; 
	private LocalDateTime date; 
	
	public Quest(String description, String topic, int priority, ArrayList<String> route){
		this.id = UUID.randomUUID().toString(); // Random ID for the quests
		this.description = description; 
		this.topic = topic; 
		this.priority = priority; 
		this.route = route; 
		date = LocalDateTime.now(); 
	}
	
	public String getId() {
		return this.id; 
	}
	
	public String getTopic() {
		return this.topic; 
	}

	public String getDescription() {
		return description;
	}

	public int getPriority() {
		return priority;
	}

	public ArrayList<String> getRoute() {
		return route;
	}

	public LocalDateTime getDate() {
		return date;
	}
}
