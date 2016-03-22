
package smartroad;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import environment.SetUp;

/**
 *
 */
public class Segment implements MqttCallback {
    
	/* Class attributes */
    private String id; 
    private SmartRoad myroad; 
    private int ini; 
    private int end; 
    private String topic; 
    private String name; 
    
    /* Connection */
    private MqttClient client; 
    
    /* Constructor */
    public Segment(String id,String name, SmartRoad smartroad, int ini, int end){
        /* Initialization */
    	this.id = id; 
        this.name = name; 
        this.myroad = smartroad; 
        this.ini = ini; 
        this.end = end; 
        
        /* Add myself to the road. The topic attribute should be 
         * filled by the SmartRoad */
        this.myroad.addSegment(this);
        
        /* Start listening */
        this.connect();
        this.subscribe();
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
        return id;
    
	}

    public void setId(String id) {
        this.id = id;
    }

    public SmartRoad getMyroad() {
		return myroad;
	}

	public void setMyroad(SmartRoad myroad) {
		this.myroad = myroad;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public SmartRoad getSmartroad() {
        return myroad;
    }

    public void setSmartroad(SmartRoad smartroad) {
        this.myroad = smartroad;
    }

    public int getIni() {
        return ini;
    }

    public void setIni(int ini) {
        this.ini = ini;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
    
    /* Methods */
    
    public void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL, this.name);
			client.setCallback(this);
			client.connect(); 
		}catch(Exception e){
			System.err.println("Segment/connect: ERROR");
			e.printStackTrace();
		}
	}

    /**
     * It subscribes the MqttClient to the topic of the object 
     */
    private void subscribe(){
    	try{
    		this.client.subscribe(this.topic);
    	}catch(Exception e){
    		System.err.println("Segment/subscribe: Something wrong happend.");
    		System.err.println(e);
    	}
    }
    
    /**
     * Equals returns True if both segments are the same one.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Segment){
            if(this.id == ((Segment)o).getId()){
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
		System.out.println("I am " + this.name + " and I've listened this message:");
		System.out.println(topic);
		System.out.println(new String(message.getPayload()));
	}
	
	 /* end MqttInterface */
    
}
