package smartcar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;
import event.Quest;
import java.util.ArrayList;

public class SmartCar implements MqttCallback{

	/* Class attributes */
    private String id;
    private String topic;
    private String mylocation;
    private String type; // normal, ambulance, police...
    private String current_city; 
    
    /* Communication */
    private MqttClient client; 

    /* Constructors */
    public SmartCar(String id, String location){
        /* Initialization */
    	this.id = id;  
    	this.type = "normal"; 
    	/* The topic is known by GPS location  */
        this.current_city = "valencia/road/E";
    	this.topic = this.current_city;
        
        this.mylocation = location;  
        
        /* Connection */
        this.connect();
        this.subscribe();
        
        /* Confirm Configuration */
        System.out.println(this.id + " configured!");
    }
    
    /* Constructor with type for Special Vehicles */
    public SmartCar(String id, String location, String type){
        /* Initialization */
    	this.id = id;  
    	this.type = type;  
    	
    	/* Special vehicles have a special channel  */
    	this.current_city = "valencia"; 
        this.topic = current_city + "/" + this.type;
        
        this.mylocation = location;  
        
        /* Connection */
        this.connect();
        this.subscribe();
        
        /* New special vehicle to the city */
        this.notifyCityNewSpecialVehicle(); 
        
        /* Confirm Configuration */
        System.out.println(this.id + " configured!");
    }
    
    /* Getters and Setters */
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setTopic(String t){
		this.topic = t; 
	}
	
	public String getTopic(){
		return this.topic;
	}

	/**
     * It subscribes the MqttClient to the topic of the object 
     */
    private void subscribe(){
    	try{
    		this.client.subscribe(this.topic);	
    	}catch(Exception e){
    		System.err.println(this.id + " SmartCar/subscribe: Something wrong happend.");
    		System.err.println(e);
    	}
    }
	
	/**
	 * Connects itself to the broker. 
	 */
	private void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL,this.id);
			this.client.setCallback(this);
			this.client.connect(); 
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/connect: ERROR");
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private void disconnect(){
		try{
			this.client.disconnect();
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/disconnect: ERROR");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method disconnects the client, to re-connect it and 
	 * re-subscribe to the topic of the object
	 */
	public void relaunch(){
		this.disconnect();
		this.connect();
		this.subscribe();
		/* display new configuration */
		System.out.println(this.id + " my new topic is: " + this.topic); 
	}
	
	
	/* Communication methods */
	
	/**
	 * Ask the nearest road where it is, and waits the answer. 
	 * Currently, the road is given by default and it's the segment 
	 * which is "discovered". 
	 * 
	 * In a real context, the car doesn't know about the road. 
	 */
	public void WhereAmI(){
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = SetUp.fillJSBody("1000", this.id, "null", "WhereAmI");
			jsmessage.addProperty("Location", this.mylocation);
			jsmessage.addProperty("Type",  this.type);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			/* Push the message */
			this.client.publish(this.topic, mes);
			
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/WhereIAm: ERROR");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method comunicates to the city that there is a new special 
	 * vehicle 
	 */
	public void notifyCityNewSpecialVehicle(){
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = SetUp.fillJSBody("4000", this.id, "C-000", "New special vehicle");
			jsmessage.addProperty("Location", this.mylocation);
			jsmessage.addProperty("Type",  this.type);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			/* Push the message */
			this.client.publish(this.topic, mes);
			
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/WhereIAm: ERROR");
			e.printStackTrace();
		}
	}
		
	
	/**
     * It sends a call of S.O.S 
     */
    public void sendSOS(){
    	try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = SetUp.fillJSBody("2000", this.id, "null", "S.O.S");
			jsmessage.addProperty("Location", this.mylocation);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			/* Push the message */
			this.client.publish(this.topic, mes);
			System.out.println(this.id + ": Call S.0.S sent. My location is: " + this.mylocation); 
			
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/SOS: ERROR");
			e.printStackTrace();
		}
    }
    
    /**
     * Notify the city that the quest has been completed
     */
    private void notifyCityQuestCompleted(Quest quest){
			/* Create the message in JSON Format */
		JsonObject jsmessage = SetUp.fillJSBody("7000", this.id, "C-000", 
				"The quest (" + quest.getId() + ") has been completed.");
		
		try{	
			ObjectMapper mapper = SetUp.getMapper(); 
			String quest_string = mapper.writeValueAsString(quest);
			jsmessage.addProperty("Quest", quest_string);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			/* Push the message */
			this.client.publish(this.topic, mes);
			
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/notifyCityQuestCompleted: ERROR");
			e.printStackTrace();
		}
    }
    
    /***
     * This method is for Special Vehicles. Where the topic is cityName/type
     * and the city needs to know where will be the car to handle the segments. 
     * @param last
     * @param current
     * @param next
     */
    private void notifyCityMyLocation(String last, String current, String next){
    	/* Send message to the city to handle the segments in my route */
    	JsonObject jsmessage = SetUp.fillJSBody("1100", this.id, "C-000", "My location (last, current and next)");
    	jsmessage.addProperty("LastLocation", last);
    	jsmessage.addProperty("CurrentLocation", current);
    	jsmessage.addProperty("NextLocation", next);
    	
    	/* Create Mqtt message */
    	MqttMessage mes = new MqttMessage(); 
    	mes.setPayload(jsmessage.toString().getBytes());
    	
    	/* Push the message */
    	try {
    		/* TODO: Estudia mejor esta forma de mandar mensajes */
			MqttClient auxClient = new MqttClient(SetUp.BROKER_URL, MqttClient.generateClientId());
			auxClient.connect();
			auxClient.publish(this.topic, mes); // TOPIC: SpecialVehicle ambulance...
			auxClient.disconnect();
		} catch (MqttException e) {
			System.err.println(this.id + ": SmartCar/notifyCityMyLocation ERROR"); 
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
		/* Parse MqttMessage to Json and string*/
		String text = new String(message.getPayload()); 
		JsonObject js = new JsonParser().parse(text).getAsJsonObject();
		String ReceiverId = js.get("ReceiverId").getAsString();
		
		/* If the message is addressed to me */
		if(ReceiverId.equals(this.id)){
			
			String code = js.get("Code").getAsString(); // Y XXX
			String theme = code.substring(0,1); // Y 
			String requestCode = code.substring(1,4); // XXX
			/* THEME */
			switch(theme){
			/* Quests */
			case "3": 
				/* Received Quest */
				switch(requestCode){
				case "000": 
					System.out.println(this.id + ": I have received a new Quest.");
					/* JSON to Quest */
					ObjectMapper mapper = SetUp.getMapper();
					String jsQuest = js.get("Quest").getAsString();
					Quest quest = mapper.readValue(jsQuest, Quest.class);
					/* Do the quest (I should be available) */
					this.doQuest(quest);
					break;
				}
				break;
			
			/* Answer Info */
			case "5":
				switch(requestCode){
					/* Where I Am */
					case "000":
						String new_topic = js.get("Topic").getAsString();
						this.topic = new_topic;  
						break;
				}
				break;
			
			/* Answer to Emergency */
			case "6": 
				switch(requestCode){
					/* S.O.S call */
					case "000":
						break;
					/* Emergency attended */
					case "002":
						System.out.println(this.id + ": Now I am OK!");
						break;
				}
				break;
			
			case "7":
				switch(requestCode){
				case "001":
					break;
				}
				break;
			
			case "8":
				switch(requestCode){
				/* The city has listened my message 4000 */
				case "000": 
					if(js.get("ReceiverId").getAsString().equals(this.id)){
						/* Nothing to do here (for now) */ 
					}
					break;
				}
				break;
			
			/* Non-valid message (theme)*/
			default:
				System.err.println(this.id + ": MessageArrivedERROR Non-valid theme. ");
				break;
			}
		}
	}
    
    /* end MqttInterface */
	
	/* Quests, 
	 * for now, all the quest are "go to somewhere to attend an event"  */
	private void doQuest(Quest quest) {
		/* Read the quest */
		ArrayList<String> route = quest.getRoute();
		/* Go to ... */
		System.out.println(this.id + ": Going from " + route.get(0) + " to " + route.get(route.size()-1)); 
		this.goTo(route);
		/* Tell the city that the quest has been completed */
		this.notifyCityQuestCompleted(quest);
	}
	
	/* Actions */
	private void goTo(ArrayList<String> route){
		boolean arrived = false;
		int index = 0;  
		String last, current, next; /* Location to notify the city */
		/* This simulates the time elapsed during the journey */
		while(!arrived){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
			last = this.mylocation; 
			this.mylocation = route.get(index++); 
			current = this.mylocation; 
			if(this.mylocation.equals(route.get(route.size() - 1))){
				arrived = true; 
				next = current; /* TODO: Mirar esto. Como se finaliza la ruta.  */
			}else{
				next = route.get(index);
			}
			
			/* Notify the city my location */
			this.notifyCityMyLocation(last, current, next); 
		}
		System.out.println(this.id + ": I have arrived to my destiny."); 
	}
	
	/* Actions Car */
	/**
	 * This methods moves the car to a random location inside the map. 
	 */
	private void moveTo(){
		/* TODO: Maybe in JADE...  */
	}
}
