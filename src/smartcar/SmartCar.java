package smartcar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;
import event.Quest;
import java.time.LocalDateTime; // Jackson needs this 

public class SmartCar implements MqttCallback{

	/* Class attributes */
    private String id;
    private String topic;
    private String mylocation;
    private String type; // 0: Civil, 1: Ambulance, 2: Police...  
    
    /* Communication */
    private MqttClient client; 

    /* Constructors */
    public SmartCar(String id, String location){
        /* Initialization */
    	this.id = id;  
    	this.type = "normal"; 
    	/* SOLUCIONAR: this should be set by asking */
        this.topic = "valencia";
        
        this.mylocation = location;  
        
        /* Connection */
        this.connect();
        this.subscribe();
        
        /* Confirm Configuration */
        System.out.println(this.id + " configured!");
        
        /* Where Am I? */
        System.out.println(this.id + ": Looking for a new topic...");
        this.WhereAmI();
    }
    
    /* Constructor with type */
    public SmartCar(String id, String location, String type){
        /* Initialization */
    	this.id = id;  
    	this.type = type;  
    	
    	/* Special vehicles have a special channel  */
        this.topic = "valencia" + "/" + this.type;
        
        this.mylocation = location;  
        
        /* Connection */
        this.connect();
        this.subscribe();
        
        /* New special vehicle to the city */
        this.notifyCity(); 
        
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
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Code", "1000");
			jsmessage.addProperty("SenderId", this.id);
			jsmessage.addProperty("ReceiverId", "null");
			jsmessage.addProperty("Message", "WhereAmI");
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
	public void notifyCity(){
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Code", "4000");
			jsmessage.addProperty("SenderId", this.id);
			jsmessage.addProperty("ReceiverId", "null");
			jsmessage.addProperty("Message", "new special vehicle");
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
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Code", "2000");
			jsmessage.addProperty("SenderId", this.id);
			jsmessage.addProperty("ReceiverId", "null");
			jsmessage.addProperty("Message", "S.O.S");
			jsmessage.addProperty("Location", this.mylocation);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			/* Push the message */
			this.client.publish(this.topic, mes);
			System.out.println(this.id + ": Call S.0.S sent."); 
			
		}catch(Exception e){
			System.err.println(this.id + " SmartCar/SOS: ERROR");
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
						System.out.println(this.id + ": I've received an answer for my SOS call. ");
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

}
