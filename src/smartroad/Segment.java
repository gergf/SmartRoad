
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
    private String ini; 
    private String end; 
    private String topic; 
    private boolean open; 
    
    /* Connection */
    private MqttClient client; 
    
    /* Constructor */
    public Segment(String id, SmartRoad smartroad, String ini, String end){
        /* Initialization */
    	this.id = id; 
        this.myroad = smartroad; 
        this.topic = myroad.getTopic();
        this.ini = ini; 
        this.end = end; 
        this.setOpen(true);
        
        /* Add myself to the road. The topic attribute should be 
         * filled by the SmartRoad */
        this.myroad.addSegment(this);
        
        /* Start listening */
        this.connect();
        this.subscribe();
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

    public String getIni() {
        return ini;
    }

    public void setIni(String ini) {
        this.ini = ini;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
    
    /* Methods */
    
    public void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL, this.id);
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
	
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	 /* end MqttInterface */
    
}
