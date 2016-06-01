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
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		/* Test */
		// SetUp.testMessage(this.name, this.id, topic, message);
		
		String text = new String(message.getPayload()); 
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
					String senderId = js.get("SenderId").getAsString();
					String[] args = {senderId, "Check S.O.S", "", "0"};
					new RoadAnswerRequest(this, "2000", args).start();
					
					/* To communicate the city the emergency */
					String location = js.get("Location").getAsString(); 
					/* Si utilizas el mismo array the strings de arriba pasan cosas raras */
					String[] args2 = {this.mycity.getId(), text, location, "1"};
					new RoadAnswerRequest(this, "2000", args2).start(); 
					break;
			}
			break;
		
		/* emergency answer */
		case "6":
			break;
		
		}/* endSwitch*/
		
	}/*endMethod*/
	
	 /* ------------ end MqttInterface ------------ */
    
	
}
