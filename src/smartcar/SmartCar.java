package smartcar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;

public class SmartCar implements MqttCallback{

	/* Class attributes */
    private String id;
    private String topic;
    private int myubication;
    private boolean needUpdate; 
    
    /* Communication */
    private MqttClient client;
    private boolean waitingAnswer; 

    /* Constructors */
    public SmartCar(String id){
        /* Initialization */
    	this.id = id;  
    	this.needUpdate = false; 
    	/* this should be set by asking */
        this.topic = "valencia/road/cv30";
        this.myubication = 34; 
        this.waitingAnswer = false; 
        
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
	
	public void setTopic(String t){
		this.topic = t; 
	}
	
	public String getTopic(){
		return this.topic;
	}
	
	public boolean getNeedUpdate(){
		return this.needUpdate;
	}
	/**
     * It subscribes the MqttClient to the topic of the object 
     */
    private void subscribe(){
    	try{
    		this.client.subscribe(this.topic);	
    	}catch(Exception e){
    		System.err.println("SmartCar/subscribe: Something wrong happend.");
    		System.err.println(e);
    	}
    }
	
	/**
	 * Connects itself to the broker. 
	 */
	private void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL,"SmartCar"+this.id);
			this.client.setCallback(this);
			this.client.connect(); 
		}catch(Exception e){
			System.err.println("SmartCar/connect: ERROR");
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
			System.err.println("SmartCar/disconnect: ERROR");
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
		this.needUpdate = false; 
	}
	
	
	/**
	 * Ask the nearest road where it is, and waits the answer. 
	 * Currently, the road is given by default and it's the segment 
	 * which is "discovered". 
	 * 
	 * In a real context, the car doesn't know about the road. 
	 */
	public void WhereIAm(){
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Request", "1000");
			jsmessage.addProperty("SenderId", this.id);
			jsmessage.addProperty("ReceiverId", "null");
			jsmessage.addProperty("Message", "null");
			jsmessage.addProperty("Ubication", this.myubication);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			/* Push the message */
			this.client.publish(this.topic, mes);
			
			/* Set up the car for waiting an answer */
			this.waitingAnswer = true;
		}catch(Exception e){
			System.err.println("SmartCar/WhereIAm: ERROR");
			e.printStackTrace();
		}
	}

	/**
     * 
     */
    public void sendSOS(){
    	try{
    		/* New message */
    		MqttMessage message = new MqttMessage();
    		message.setPayload(("2000:" + this.id + ":{ubication:blabla}").getBytes());

    		/* Publish the message */
    		this.client.publish(this.topic, message);
    		
    	}catch(Exception e){
    		System.err.println("SmartCar/sendSOS: Algo no ha ido bien al enviar el mensaje.");
    		System.err.println(e);
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
		/* If it is waiting an answer then reads the message */
		if(this.waitingAnswer){
			String text = new String(message.getPayload()); 
			JsonObject js = new JsonParser().parse(text).getAsJsonObject();
			String ReceiverId = js.get("ReceiverId").getAsString();
			
			/* If the message is addressed to me */
			if(ReceiverId.equals(this.id)){
				String request = js.get("Request").getAsString();
				/* THEME */
				switch(request.substring(0,1)){
					/* Answer Info */
					case "5":
						switch(request.substring(1,4)){
							/* Where I Am */
							case "000":
								String ntopic = js.get("Topic").getAsString();
								this.topic = ntopic; 
								this.needUpdate = true; 
								break;
						}
						break;
				}
				/* Answer Received */
				this.waitingAnswer = false; 
			}
		}
	}
    
    /* end MqttInterface */

}
