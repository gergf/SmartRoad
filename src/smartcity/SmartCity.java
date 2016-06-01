package smartcity;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;
import smartcar.SpecialVehicle;
import smartroad.SmartRoad;

public class SmartCity implements MqttCallback{
    
	/* Class attributes */
    private String id; 
    private String name;
    private String cityTopic; 
    
    /* Lists */
    private ArrayList<SpecialVehicle> specialVechicleList; 
    private ArrayList<SmartRoad> SmartRoadList;
    private ArrayList<String> TopicList; 
    
    /* This client listens in the topic of the city  */
    private MqttClient client; 
    
    /* Constructor */
    public SmartCity(String id, String name){
    	/* Initialization */
        this.id = id; 
        this.name = name; 
        this.cityTopic = name;
        
        this.specialVechicleList = new ArrayList<>(); 
        this.SmartRoadList = new ArrayList<>();
        
        /* Prepare topic lists */
        this.TopicList = new ArrayList<>();
        this.TopicList.add(cityTopic);  
        this.TopicList.add(name+"/ambulance");
        
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
		return SmartRoadList;
	}

	public void setSmartRoadList(ArrayList<SmartRoad> smartRoadList) {
		SmartRoadList = smartRoadList;
	}
        
    /* Methods */
	public void addSmartRoad(SmartRoad road){
		/* Check if the road is already inside?? */
		this.SmartRoadList.add(road); 
		/* The city does not listen in this topic,only stores the name */
		this.TopicList.add(this.name+"/road/"+road.getName()); 
		// road.setSmartCity(this);
	}
	
	public void addSpecialVehicle(SpecialVehicle speveh){
		this.specialVechicleList.add(speveh); 
	}
	
	/**
     * It subscribes the MqttClient to the topic of the object 
     */
    private void subscribe(){
    	try{
    		for(String t : TopicList)
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
		//System.err.println(this.id + ": " + new String(message.getPayload()));
		String code, theme, requestCode;
		JsonObject js; 
		try{
			/* Extract request code */
			String text = new String(message.getPayload()); 
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
		/* info */
		case "1":
			switch(requestCode){
				/* Where I am? */
				case "000":
					try{
						/* If it is an special vehicle, add to the city */
						String loc = js.get("Location").getAsString();
						String senderId = js.get("SenderId").getAsString();
						String type = js.get("Type").getAsString();
						
						/* Answer */
						String[] args = {senderId, "", loc};
						new CityAnswerRequest(this, "1000", args).start();
					}catch(Exception e){
						System.err.println(this.id + "messageArrived > Theme 1 > 000: ERROR"); 
					} 
					break;
			}
			break;
			
		/* emergency */
		case "2":
			switch(requestCode){
				/* S.O.S */
				case "000": 
					String emergency_location = js.get("Location").getAsString(); 
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
					/* We do not have any ambulance avaible */
					if(ambulance == null){
						/* Implementar una cola de emergencias */
					}else{
						/* Calculate best route between the ambulance and the emergency */
						ArrayList<String> route = this.calculateBestRoute(ambulance.getLocation(),emergency_location);
						/**/
						System.out.print("Route: -"); 
						for(String s : route)
							System.out.print(s + "-");
						System.out.println(); 
						/* Send the quest to the ambulance */
						String[] args = {ambulance.getId(), "Quest", ""};
						new CityAnswerRequest(this, "3000", args).start();
						
					}
					break;
			}/* end Switch */
			break; 
		
		/* Special Event */
		case "4":
			switch(requestCode){
			/* new Special Vehicle */
			case "000":
				String id = js.get("SenderId").getAsString(); 
				String location = js.get("Location").getAsString();
				String type = js.get("Type").getAsString(); 
				this.addSpecialVehicle(new SpecialVehicle(id, type, location));
				break; 
			}
			break;
			
		/* Answers */
		case "5":
			switch(requestCode){
			case "000":
				/* Nothing, this is an answer to Where Am I? */
				break;
			}
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
	/* ------------  end SmartCity route Methods ------------ */ 

}/* end Class */
