package event;

import java.util.UUID;

/**
 * Para mantener la informacion de las emergencias de una forma ordenada 
 */
public class Emergency {

	private String emergencyId; 
	private String requesterId; 
	private String roadId; 
	private String cityId; 
	private String location; 
	private String priority; 
	private boolean inProcess; 
	
	public Emergency(String requesterId, String roadId, String cityId, String location,  String priority){
		this.emergencyId = UUID.randomUUID().toString(); 
		this.requesterId = requesterId; 
		this.roadId = roadId; 
		this.cityId = cityId; 
		this.location = location;
		this.priority = priority; 
		this.inProcess = true; 
	}
	
	public Emergency(){
		/* Dummy constructor for Jackson */ 
	}

	public String getEmergencyId(){
		return emergencyId; 
	}
	
	public String getRequesterId() {
		return requesterId;
	}

	public String getRoadId() {
		return roadId;
	}

	public String getCityId() {
		return cityId;
	}

	public String getLocation(){
		return location; 
	}
	
	public String getPriority() {
		return priority;
	}
	
	public boolean isInProcess() {
		return inProcess; 
	}
	
}
