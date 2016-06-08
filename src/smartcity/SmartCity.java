package smartcity;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;
import event.Emergency;
import event.Quest;
import smartcar.SpecialVehicle;
import smartroad.SmartRoad;

public class SmartCity implements MqttCallback{
    
	/* Class attributes */
    private String id; 
    private String name;
    private String cityTopic; 
    
    /* Lists */
    private ArrayList<SpecialVehicle> specialVechicleList;
    private ArrayList<Emergency> emergencyQueue;
    private ArrayList<SmartRoad> smartRoadList;
    private ArrayList<String> topicList; 
    
    /* This client listens in the topic of the city  */
    private MqttClient client; 
    
    /* Constructor */
    public SmartCity(String id, String name){
    	/* Initialization */
        this.id = id; 
        this.name = name; 
        this.cityTopic = name;
        
        this.specialVechicleList = new ArrayList<>(); 
        this.smartRoadList = new ArrayList<>();
        this.emergencyQueue = new ArrayList<>();
        
        /* Prepare topic lists */
        this.topicList = new ArrayList<>();
        this.topicList.add(cityTopic);  
        this.topicList.add(name+"/ambulance");
        this.topicList.add(name+"/road");
        
        /* Connection */
        this.connect();
        this.subscribe();
        
        /* Confirm configuration */
        System.out.println(this.id + "(" + this.name + ") configured!");
    }
    
    /* Getters and Setters */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getName(){
		return this.name; 
	}
	
	public String getCityTopic(){
		return this.cityTopic;
	}

	public ArrayList<SmartRoad> getSmartRoadList() {
		return smartRoadList;
	}

	public void setSmartRoadList(ArrayList<SmartRoad> smartRoadList) {
		this.smartRoadList = smartRoadList;
	}
        
    /* Methods */
	public void addSmartRoad(SmartRoad road){
		/* Check if the road is already inside?? (Connection lost) */
		if(!this.smartRoadList.contains(road))
			this.smartRoadList.add(road); 
		/* The city does not listen in this topic,only stores the name */
		this.topicList.add(this.name+"/road/"+road.getName()); 
	}
	
	public void addEmergencyToQueue(Emergency e){
		this.emergencyQueue.add(e); 
	}
	
	public void completeEmergency(String emergencyId){
		for(Emergency em : this.emergencyQueue)
			if(em.getEmergencyId().equals(emergencyId)){
				em.setCompleted(true);
				break;
			}
	}
	
	public void addSpecialVehicle(SpecialVehicle speveh){
		this.specialVechicleList.add(speveh); 
	}
	
	/**
     * It subscribes the MqttClient to the topic of the object 
     */
    private void subscribe(){
    	try{
    		for(String t : topicList)
    			this.client.subscribe(t);
    		
    		
    	}catch(Exception e){
    		System.err.println("SmartCity/subscribe: Something wrong happend.");
    		System.err.println(e);
    	}
    }
	
	public void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL, this.id);
			client.setCallback(this);
			client.connect(); 
		}catch(Exception e){
			System.err.println("SmartCity/connect: ERROR");
			e.printStackTrace();
		}
	}

	/* ------------ MqttCallback Interface ------------ */
	
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String code, theme, requestCode, id, location, type, senderId, description, text;
		JsonObject js; 
		try{
			/* Extract request code */
			text = new String(message.getPayload()); 
			js = new JsonParser().parse(text).getAsJsonObject();
			code = js.get("Code").getAsString(); // Y XXX
			theme = code.substring(0,1); // Y 
			requestCode = code.substring(1,4); // XXX
		}catch(Exception e){
			System.err.println(this.id + ": Error al leer el mensaje JSON");
			return; // Exit method 
		}
		
		/* The message has been read successfully */
		switch(theme){
		/* Info */
		case "1":
			switch(requestCode){
				/* Where I am? */
				case "000":
					try{
						/* If it is an special vehicle, add to the city */
						 location = js.get("Location").getAsString();
						 senderId = js.get("SenderId").getAsString();
						 type = js.get("Type").getAsString();
						
						/* Answer */
						String[] args = {senderId, "", location};
						new CityAnswerRequest(this, "1000", args).start();
					}catch(Exception e){
						System.err.println(this.id + "messageArrived > Theme 1 > 000: ERROR"); 
					} 
					break;
					
				/* Location SpecialVehicle */
				case "100":
					String last_location = js.get("LastLocation").getAsString();
					String current_location = js.get("CurrentLocation").getAsString(); 
					String next_location = js.get("NextLocation").getAsString(); 
					/* Open and close Segments */
					this.openSegment(last_location, current_location); 
					this.closeSegment(current_location, next_location); 
					break;
			}
			break;
			
		/* Emergency */
		case "2": 
			switch(requestCode){
				/* S.O.S */
				case "000": 
					String requesterId = js.get("RequesterId").getAsString(); 
					String roadId = js.get("SenderId").getAsString(); 
					String emergency_location = js.get("Location").getAsString(); 
					/* Create the emergency */
					Emergency emergency = new Emergency(requesterId, roadId, this.id, emergency_location , "SOS" );
					/* The city should ask the the nearest ambulance to calculate this */
					SpecialVehicle ambulance = null; 
					
					/* Search one ambulance */
					for (SpecialVehicle s : this.specialVechicleList){
						/* Ambulance */
						if((!s.isOnMision()) && (s.getType().equals("ambulance"))){
							ambulance = s; 
							break; 
						}
					}
					if(ambulance != null){
						/* Calculate best route between the ambulance and the emergency */
						ArrayList<String> route = this.calculateBestRoute(ambulance.getLocation(),emergency.getLocation());
						/* Create the quest */
						description = "Attend to a call of S.O.S by a normal car."; 
						Quest quest = new Quest(description, "ambulance" ,2, route, emergency); 
						/* Send the quest to the ambulance */
						String[] args = {ambulance.getId(), "Quest", ""};
						new CityAnswerRequest(this, "3000", args, quest).start();
						emergency.setIsInProcess(true);
					}
					/* TODO: Las emergencias se deberian revisar cada x tiempo */
					this.addEmergencyToQueue(emergency);
					break;
			}/* end Switch */
			break;
			
		/* Quests */
		case "3": 
			switch(requestCode){
			case "000":
				/* Nothing, there is no quest for the city */
				break;
			}
			break;
		
		/* Special Event */
		case "4":
			switch(requestCode){
			/* new Special Vehicle */
			case "000":
				senderId = js.get("SenderId").getAsString(); 
				location = js.get("Location").getAsString();
				type = js.get("Type").getAsString(); 
				this.addSpecialVehicle(new SpecialVehicle(senderId, type, location));
				String[] args = {senderId, "You have been added to Special Vehicles of " + this.name + " city."};
				new CityAnswerRequest(this, "8000", args).start();
				break; 
			}
			break;
			
		/* Answers */
		case "5":
			/* Nothing for now */
			break;
		
		case "6":
			/* Nothing for now  */
			break; 
			
		case "7": 
			switch(requestCode){
			/* Quest completed */
			case "000":
				/* Search the specialVehicle which has completed the quest */
				id = js.get("SenderId").getAsString(); 
				for (SpecialVehicle sv : this.specialVechicleList)
					if(sv.getId().equals(id))
						sv.setOnMision(false);
				
				/* Send a message to the initial requester */
				Quest quest = SetUp.getMapper().readValue(js.get("Quest").getAsString(), Quest.class); 
				/* Search road Name */
				String[] args = {quest.getEmergency().getRoadId(), "Your emergency has been attended", quest.getEmergency().getRequesterId()};
				new CityAnswerRequest(this, "6002", args).start();
				this.completeEmergency(quest.getEmergency().getEmergencyId()); 
				break; 
			
			/* Segment opened */ 
			case "001":
				/* TODO: Link this with a future interface */
				break;
			
			/* Segment closed */
			case "002":
				/* TODO: Link this with a future interface */
				break;
			}
			break;
		
		case "8":
			/* Nothing for now */
			break;
		
		/* Non-valid message (theme)*/
		default:
			System.err.println(this.id + " Non-valid theme."); 
			System.err.println("Theme: " + theme + " RequestCode: " + requestCode);
			break;
		}
		
	}/* end messageArrived */
	/* ------------ end MqttInterface ------------ */

	
	/* ------------ SmartCity route Methods ------------ */ 
	
	public SmartRoad calculatelocation(String location){
		// Calculate correct location here 
		String carRangeMap = location.substring(0,1); 
		for(SmartRoad r : this.getSmartRoadList()){
			if(r.getRangeMap().equals(carRangeMap)){
				return r;
			}
		}
		return null; 
	}
	
	/**
	 * It calculates the best route between two points 
	 * The map must be a matrix. Special cases are not considered. 
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	private ArrayList<String> calculateBestRoute(String loc1, String loc2){
		ArrayList<String> route = new ArrayList<>();
		/* add first location */
		route.add(loc1); 
		/* String to int */
		int loc1_x = Integer.valueOf(loc1.substring(0,1));
		int loc1_y = Integer.valueOf(loc1.substring(1,2)); 
		int loc2_x = Integer.valueOf(loc2.substring(0,1));
		int loc2_y = Integer.valueOf(loc2.substring(1,2)); 
		
		/* Calculate route */
		while(loc1_x != loc2_x){
			if(loc1_x > loc2_x){
				loc1_x--; 
			}else{
				loc1_x++;
			}
			route.add(String.valueOf(loc1_x) + String.valueOf(loc1_y));
		}
		
		while(loc1_y != loc2_y){
			if(loc1_y > loc2_y){
				loc1_y--;
			}else{
				loc1_y++; 
			}
			route.add(String.valueOf(loc1_x) + String.valueOf(loc1_y));
		}
		
		return route; 
	}
	
	/* Close and Open segments of a route */
	
	private void openSegment(String ini, String end){
		SmartRoad road = this.searchSegment(ini);
		/* Code 3001 */
		if(road != null){
			String[] args = {road.getId(), road.getName(), ini, end};
			new CityAnswerRequest(this, "3001", args).start();
		}else{
			System.err.println(this.id + ": SmartCity/openSegment ERROR: Road is null ");
		}
	}
	
	private void closeSegment(String ini, String end){
		SmartRoad road = this.searchSegment(ini);
		/* Code 3002 */
		if(road != null){
			String[] args = {road.getId(), road.getName(), ini, end};
			new CityAnswerRequest(this, "3002", args).start();
		}else{
			System.err.println(this.id + ": SmartCity/closeSegment ERROR: Road is null ");
		}
	}
	
	/***
	 * It returns the road which contains that segment. 
	 * I use the rangeMap to determine this. 
	 * @param ini
	 * @param end
	 * @return
	 */
	private SmartRoad searchSegment(String ini){
		String rangeMap = ini.substring(0,1);
		for(SmartRoad road : this.smartRoadList){
			if(rangeMap.equals(road.getRangeMap()))
				return road;
		}
		return null; 
	}
	/* ------------  end SmartCity route Methods ------------ */ 

}/* end Class */
