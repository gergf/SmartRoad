package smartcity;

/**
 * Special class to store the important vehicles for the city. r
 *
 */
public class SpecialVehicle {
	
	private String id; 
	private String type; 
	private String location; 
	private boolean onMision; 
	
	public SpecialVehicle(String id, String type, String location){
		this.id = id; 
		this.type = type; 
		this.location = location; 
		this.onMision = false; 
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isOnMision() {
		return onMision;
	}

	public void setOnMision(boolean onMision) {
		this.onMision = onMision;
	}

}
