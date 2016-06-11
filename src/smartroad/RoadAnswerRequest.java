package smartroad;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;

import environment.SetUp;

/*
 * El objetivo de esta clase es liberar de carga al cliente de la SmartRoad, 
 * para que así pueda recibir todas las peticiones sin problemas. 
 * 
 * Una vez se haya enviado el mensaje, el hilo será eliminado.
 */
public class RoadAnswerRequest extends Thread {
	
	/* Data */
	private String code; 
	private String[] args; // It depends of the call  
	private SmartRoad road;
	private String threadId; 
	
	/* Communication */
	MqttClient client; 
	
	public RoadAnswerRequest(SmartRoad road, String code, String[] args){
		this.road = road; 
		this.code = code; 
		this.args = args; 
		this.threadId = MqttClient.generateClientId();
		this.connect();
	}
	
	@Override
	public void run(){
		switch(this.code){
		
		/* args[3] must be a flag ]*/
		case "2000": 
			switch(args[3]){
			/* answer the request 2000 to check the SOS call */
			case "0":	
				this.checkSOScall(args[0], args[1]);
				break;
			/* Send the emergency to the city */
			case "1": 
				if(args.length < 5){
					System.err.println("RoadAnswerRequest ERROR: Args is not completed. Code 2000 with flag 1");
				}else{
					this.notifyCitySOScall(args[0], args[1], args[2], args[4]);
				}
				break;
			}
			break;
		
		case "6002":
			this.notifyVehicleEmergencyAttended(args[0], args[1]); 
			break;
		
		case "7001":
			this.notifyCitySegmentOpened();
			break;
		
		case "7002":
			this.notifyCitySegmentClosed(); 
			break;
		
		}
	}
	
	/* Communication */
	public void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL, this.threadId);
			this.client.connect(); 
		}catch(Exception e){
			System.err.println(args[0] + "RoadAnswerRequest/connect: ERROR");
			e.printStackTrace();
		}
	}
	
	/* Answer methods */
	/**
	 * It sends a message to the car which has sent the S.O.S
	 * @param receiverId
	 * @param message
	 */
	private void checkSOScall(String receiverId, String message ){
		try{
			JsonObject js = SetUp.fillJSBody("6000", this.road.getId(), receiverId, message);
			
			MqttMessage mes = new MqttMessage(); 
			mes.setPayload(js.toString().getBytes());
			
			this.client.publish(road.getTopic(), mes);
		}catch(Exception e){
			System.err.println(road.getId() + ":Thread-" + this.threadId + " ERROR in answer2000");
			e.printStackTrace();
		}
	}
	
	/***
	 * It send a message to the city communicating the emergency. 
	 * @param receiverId
	 * @param message
	 * @param location
	 * @param requesterId
	 */
	private void notifyCitySOScall(String receiverId, String message, String location, String requesterId){
		try{
			JsonObject js = SetUp.fillJSBody("2000", this.road.getId(), receiverId, message);
			js.addProperty("Location",  location);
			js.addProperty("RequesterId", requesterId);
			
			MqttMessage mes = new MqttMessage(); 
			mes.setPayload(js.toString().getBytes());
			
			this.client.publish(road.getTopicMyCity(), mes);
		}catch(Exception e){
			System.err.println(road.getId() + ":Thread-" + this.threadId + "ERROR in communicato2000toCity");
			e.printStackTrace();
		}
	}
	
	/***
	 * It notifies the vehicle which did an emergency call that the emergency has been attended. 
	 * @param message
	 * @param receiverId
	 */
	private void notifyVehicleEmergencyAttended(String message, String receiverId){
		JsonObject js = SetUp.fillJSBody("6002", this.road.getId(), receiverId, message);
		
		MqttMessage mes = new MqttMessage(); 
		mes.setPayload(js.toString().getBytes());
		try{
			this.client.publish(road.getTopic(), mes);
		}catch(Exception e){
			System.err.println(road.getId() + ":Thread-" + this.threadId + "ERROR in notifyVehicleEmergencyAttended");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void notifyCitySegmentOpened(){
		JsonObject js = SetUp.fillJSBody("7001", this.road.getId(), this.road.getMyCityId(), "Segment Opened");
		
		MqttMessage mes = new MqttMessage(); 
		mes.setPayload(js.toString().getBytes());
		
		try{
			this.client.publish(this.road.getTopicMyCity() + "/road", mes);
		}catch(Exception e){
			System.err.println(road.getId() + ":Thread-" + this.threadId + "ERROR in notifyCitySegmentOpened");
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	private void notifyCitySegmentClosed(){
		JsonObject js = SetUp.fillJSBody("7002", this.road.getId(), this.road.getMyCityId(), "Segment Closed");
		
		MqttMessage mes = new MqttMessage(); 
		mes.setPayload(js.toString().getBytes());
		
		try{
			this.client.publish(this.road.getTopicMyCity() + "/road", mes);
		}catch(Exception e){
			System.err.println(road.getId() + ":Thread-" + this.threadId + "ERROR in notifyCitySegmentClosed");
			e.printStackTrace();
		}
	}
}
