package smartcity;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import environment.SetUp;
import smartroad.SmartRoad;

public class SmartCity implements MqttCallback{
    
	/* Class attributes */
    private String id; 
    private String name;
    
    /* Lists */
    private ArrayList<SmartRoad> SmartRoadList;
    private ArrayList<String> TopicList; 
    
    /* Connection */
    private MqttClient client; 
    
    /* Constructor */
    public SmartCity(String id, String name){
    	/* Initialization */
        this.id = id; 
        this.name = name; 
        
        this.SmartRoadList = new ArrayList<>();
        this.TopicList = new ArrayList<>();
        this.TopicList.add(name+"/road");
        this.TopicList.add(name+"/"); // Maybe this should be removed. 
        
        /* Connection */
        this.connect();
        this.subscribe();
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

	public ArrayList<SmartRoad> getSmartRoadList() {
		return SmartRoadList;
	}

	public void setSmartRoadList(ArrayList<SmartRoad> smartRoadList) {
		SmartRoadList = smartRoadList;
	}
        
    /* Methods */
	public void addSmartRoad(SmartRoad road){
		this.SmartRoadList.add(road); 
		this.TopicList.add(name+"/road/"+road.getName()); 
		road.setSmartCity(this);
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
    		this.client = new MqttClient(SetUp.BROKER_URL, this.name);
			client.setCallback(this);
			client.connect(); 
		}catch(Exception e){
			System.err.println("SmartCity/connect: ERROR");
			e.printStackTrace();
		}
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
		System.out.println("I am " + this.name + " and I've listened this message:");
		System.out.println(topic);
		System.out.println(new String(message.getPayload()));
	}
	
	 /* end MqttInterface */
}
