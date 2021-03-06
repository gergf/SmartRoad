package smartroad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;
import smartcar.SmartCar;
import smartcity.SmartCity;

public class SmartRoad implements MqttCallback{
    
	/* Class attributes */
    private String id; 
    private String name; //Should be lower letters 
    private SmartCity mycity;
    private String topic; 
    private String rangeMap; 
    
    /* Lists */
    private ArrayList<Segment> segmentsList; 
    private Set<SmartCar> carsList;
    
    /* Connection */
    private MqttClient client; 
    
    /* Constructors */
    public SmartRoad(String id, String name, String rangeMap, SmartCity city){
    	/* Initialization */
        this.id = id; 
        this.name = name; 
        this.rangeMap = rangeMap; 
        
        this.mycity = city; 
        this.topic = this.mycity.getName() + "/road/" + this.name;
        
        this.segmentsList = new ArrayList<>(); 
        this.carsList = new HashSet<>(); 
        
        /* Add itself to the city */
        this.mycity.addSmartRoad(this);
        
        /*Start listening */
        this.connect();
        this.subscribe(); 
        
        /* Confirm Configuration */
        System.out.println(this.id + "(" + this.name + ") configured!");
    }
    
    /* Getters and Setters */
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getTopic(){
    	return this.topic; 
    }
    
    public String getRangeMap(){
    	return this.rangeMap;
    }
    
    public String getTopicMyCity(){
    	return this.mycity.getCityTopic();
    }
    
    public String getMyCityId(){
    	return this.mycity.getId();
    }

    /* Why? This is already done in the constructor */
    public void setSmartCity(SmartCity c){
    	this.mycity = c; 
    	this.topic = this.mycity.getName() + "/road/" + this.name; 
    }
    
    public ArrayList<Segment> getSegmentsList() {
        return segmentsList;
    }

    /* Methods */
    
    /**
     * It subscribes the MqttClient to the topic of the object 
     */
    private void subscribe(){
    	try{
    		this.client.subscribe(this.topic);
    		/* Maybe this should be a class variable */
    		this.client.subscribe(this.getTopicMyCity() + "/road");
    	}catch(Exception e){
    		System.err.println(this.id + " SmartRoad/subscribe: Something wrong happend.");
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
			System.err.println("SmartRoad/connect: ERROR");
			e.printStackTrace();
		}
	}
    
    /**
     * Adds the segment to the SmartRoad.
     * @param s 
     */
    public void addSegment(Segment s){
        this.segmentsList.add(s); 
    }
    
    public void addSmartCar(SmartCar c){
    	this.carsList.add(c);
    	c.setTopic(this.topic);
    }

    /**
     * Equals method
     * @param o
     * @return 
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof SmartRoad){
            if(this.id == ((SmartRoad)o).getId()){
                return true; 
            }
        }
        return false;
    }

    /* MqttCallback Interface */
    
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage mes) throws Exception {
		/* Test */
		// SetUp.testMessage(this.name, this.id, topic, message);
		String senderId, location, message, requesterId; 
		String text = new String(mes.getPayload()); 
		/* Extract request code */
		JsonObject js = new JsonParser().parse(text).getAsJsonObject();
		String code = js.get("Code").getAsString(); // Y XXX
		String theme = code.substring(0,1); // Y 
		String requestCode = code.substring(1,4); // XXX
		/* THEME */
		switch(theme){
	
		/* info request */
		case "1":
			break;
			
		/* emergency request */
		case "2":
			switch(requestCode){
				/* S.O.S. call */
				case "000":
					
					/* To answer the car that its message has been received */
					senderId = js.get("SenderId").getAsString();
					String[] args = {senderId, "Check S.O.S", "", "0"};
					new RoadAnswerRequest(this, "2000", args).start();
					
					/* To communicate the city the emergency */
					location = js.get("Location").getAsString(); 
					/* Si utilizas el mismo array the strings de arriba pasan cosas raras */
					String[] args2 = {this.mycity.getId(), text, location, "1", senderId};
					
					new RoadAnswerRequest(this, "2000", args2).start(); 
					break;
			}
			break;
		
		case "3":
			String ini, end; 
			String receiverId = js.get("ReceiverId").getAsString(); 
			/* The code 3 represents specific orders, so if the road is not the receiver, 
			 * it must ignore the message */
			if(receiverId.equals(this.id)){
				switch(requestCode){
				/* Open segment */
				case "001":
					ini = js.get("SegmentIni").getAsString();
					end = js.get("SegmentEnd").getAsString(); 
					this.openSegment(ini, end);
					/* Communicate the city that the segment has been opened */
					new RoadAnswerRequest(this, "7001", null).start();
					break;
					
				/* Close segment */
				case "002":
					ini = js.get("SegmentIni").getAsString();
					end = js.get("SegmentEnd").getAsString(); 
					this.closeSegment(ini, end);
					/* Communicate the city that the segment has been closed */
					new RoadAnswerRequest(this, "7002", null).start();
					break;
				}
			}
			break;
		
		/* emergency answer */
		case "6":
			switch(requestCode){
			/* Notify the vehicle */
			case "002":
				/* TODO: Comentar si se deberia mandar el mensaje por todas las carreteras o solo por la 
				carretera donde estaba el requesterId */
				if(js.get("ReceiverId").getAsString().equals(this.getId())){
					message = js.get("Message").getAsString(); 
					requesterId = js.get("RequesterId").getAsString(); // Now is the receiverId
					String[] args = {message, requesterId};
					new RoadAnswerRequest(this, "6002", args).start();
					//TODO: This should be done via REST not MQTT 
				}
				break;
			}
			break;
		
		/* */
		case "7":
			switch(requestCode){
			case "001":
				break;
			}
			break;
		}/* endSwitch*/
		
	}/*endMethod*/
	
	 /* ------------ end MqttInterface ------------ */
	
	private void openSegment(String ini, String end){
		for(Segment s : this.segmentsList){
			if(s.getIni().equals(ini) && s.getEnd().equals(end)){
				s.setOpen(true);
				return;
			}
		}
	}
	
	private void closeSegment(String ini, String end){
		for(Segment s : this.segmentsList){
			if(s.getIni().equals(ini) && s.getEnd().equals(end)){
				s.setOpen(false);
				return;
			}
		}
	}
    
	
}
