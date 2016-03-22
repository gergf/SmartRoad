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
    
    /* Lists */
    private ArrayList<Segment> segmentsList; 
    private Set<SmartCar> carsList;
    
    /* Connection */
    private MqttClient client; 
    
    /* Constructors */
    public SmartRoad(String id, String name, SmartCity city){
    	/* Initialization */
        this.id = id; 
        this.name = name; 
        this.mycity = city; 
        this.topic = this.mycity.getName() + "/road/" + this.name;
        this.segmentsList = new ArrayList<>(); 
        this.carsList = new HashSet<>(); 
        
        /* Add itself to the city */
        this.mycity.addSmartRoad(this);
        
        /*Start listening */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getTopic(){
    	return this.topic; 
    }

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
    		System.err.println("SmartRoad/subscribe: Something wrong happend.");
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
        s.setTopic(this.topic + "/" + s.getName());
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
		String text = new String(message.getPayload()); 
		/* Extract request code */
		JsonObject js = new JsonParser().parse(text).getAsJsonObject();
		String request = js.get("Request").getAsString();
		/* THEME */
		switch(request.substring(0, 1)){
			/* info */
			case "1":
				switch(request.substring(1,4)){
					/* Where I am? */
					case "000":
						String ubi = js.get("Ubication").getAsString();
						String senderId = js.get("SenderId").getAsString(); 
						this.info000(ubi, senderId);
						break;
				}
				break;
				
			/* emergency */
			case "2":
				switch(request){
				/* S.O.S */
				case "000":
					break;
			}
				break; 
		}/* endSwitch*/
		
		
	}
	
	 /* end MqttInterface */
    
	/* Request Methods */
	private boolean info000(String ubication, String receiverId){
		//TODO: Calculate where is the car 
		String address = this.segmentsList.get(0).getName();
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Request", "5000");
			jsmessage.addProperty("SenderId", this.id);
			jsmessage.addProperty("ReceiverId", receiverId);
			jsmessage.addProperty("Message", "null");
			
			/*Add topic */
			jsmessage.addProperty("Topic", this.topic + "/" + address);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			
			this.client.publish(this.topic, mes);
		}catch(Exception e){
			System.err.println("SmartRoad/info000 ERROR");
			e.printStackTrace();
		}
		return true; 
	}
}
