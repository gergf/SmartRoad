package smartcity;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonObject;

import environment.SetUp;
import event.Quest;
import smartroad.SmartRoad;

/*
 * El objetivo de esta clase es liberar de carga al cliente de la SmartCity, 
 * para que así pueda recibir todas las peticiones sin problemas. 
 * 
 * Una vez se haya enviado el mensaje, el hilo será eliminado.
 */
public class CityAnswerRequest extends Thread
{
	/* Data */
	private String code; 
	private String[] args; // It depends of the call 
	private SmartCity city;
	private String threadId;
	private Quest quest; 
	
	/* Communication */
	MqttClient client; 
	
	public CityAnswerRequest(SmartCity city, String code, String[] args){
		this.city = city; 
		this.code = code; 
		this.args = args; 
		this.threadId = MqttClient.generateClientId();
		this.connect();
	}
	
	public CityAnswerRequest(SmartCity city, String code, String[] args, Quest quest){
		this.city = city; 
		this.code = code; 
		this.args = args; 
		this.quest = quest; 
		this.threadId = MqttClient.generateClientId();
		this.connect();
	}
	
	@Override
	public void run() {
		/* Identify the request */
		switch(code){
		/* INFO - Where Am I? */
		case "1000":
			answer1000(args[0], args[2]);
			break;
		
		/* Send quest */
		case "3000":
			if(this.quest != null)
				this.sendQuest(args[0], this.quest);
			else
				System.err.println("CityAnswerRequest ERROR: Unable to send the Quest. Quest is null.");
			break; 
		
		/* Open segment */ 
		case "3001":
			this.openSegment(args[0], args[1], args[2], args[3]);
			break;
			
		/* Close segment */
		case "3002":
			this.closeSegment(args[0], args[1], args[2], args[3]);
			break;
		
		/* The emergency has been completed. Notify the requester */
		case "6002":
			this.notifyEmergencyRequester(args[0], args[1], args[2]); 
			break;
		
		/* Answer to new Special Vehicle */
		case "8000":
			this.checkNewSpecialVehicle(args[0], args[1]); 
			break;
		
		default:
			System.err.println("CityAnswerRequest ERROR: The code " + code + " is not handled. ");
		}/*end switch */
		
		/* finish thread */
		return; 
	}
	
	/* Communication */
	public void connect(){
		try{
			/* New client */
    		this.client = new MqttClient(SetUp.BROKER_URL, this.threadId);
			this.client.connect(); 
		}catch(Exception e){
			System.err.println(args[0] + "CityAnswerRequest/connect: ERROR");
			e.printStackTrace();
		}
	}
	
	/* Responses */
	
	private void answer1000(String receiverId, String location){
		/* Awesome code here */
		SmartRoad road = city.calculatelocation(location);
		
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = SetUp.fillJSBody("5000", this.city.getId(), receiverId, "CityAnswerRequest: Answer to WhereAmI");
			
			/*Add topic */
			jsmessage.addProperty("Topic", road.getTopic());
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			// Publish the message  
			this.client.publish(city.getCityTopic(), mes);
			
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/answer1000 ERROR");
			e.printStackTrace(); 
		} 
	}/* end answer1000 */
	
	/**
	 * It notifies the new Special Vehicle that its request has been listened. 
	 * @param receiverId
	 * @param message
	 */
	private void checkNewSpecialVehicle(String receiverId, String message){
		String senderId = this.city.getId(); 
		JsonObject jsmessage = SetUp.fillJSBody("8000", senderId, receiverId, message);
		
		MqttMessage mes = new MqttMessage(); 
		mes.setPayload(jsmessage.toString().getBytes());
		
		try{
			this.client.publish(this.city.getCityTopic() + "/ambulance", mes);
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/checkNewSpecialVehicle ERROR");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method sends a new Quest to a Special Vehicle 
	 * @param receiverId
	 * @param message
	 */
	private void sendQuest(String receiverId, Quest quest){
		/* Create the message in JSON Format */
		JsonObject jsmessage = SetUp.fillJSBody("3000", this.city.getId(), receiverId, "Send Quest");
		
		try{
			/* Quest to JSON */
			ObjectMapper mapper = SetUp.getMapper();
			String quest_string = mapper.writeValueAsString(quest);
			jsmessage.addProperty("Quest", quest_string);
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			// Publish the message  
			this.client.publish(city.getCityTopic() + "/" + quest.getTopic(), mes);
			
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/sendQuest ERROR");
			e.printStackTrace(); 
		} 
	}/* end sendQuest */
	
	/**
	 * 
	 * @param receiverId
	 * @param message
	 * @param requesterId
	 */
	private void notifyEmergencyRequester(String receiverId, String message, String requesterId){
		/* Create the message in JSON Format */
		JsonObject jsmessage = SetUp.fillJSBody("6002", this.city.getId(), receiverId, message);
		jsmessage.addProperty("RequesterId", requesterId);
		
		/* Create a Mqtt message */
		MqttMessage mes = new MqttMessage();
		mes.setPayload((jsmessage.toString()).getBytes());
		
		try{
			// Publish the message  
			this.client.publish(this.city.getCityTopic() + "/road", mes);
			
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/notifyEmergencyRequester ERROR");
			e.printStackTrace(); 
		} 
	}
	
	/**
	 * 
	 * @param receiverId
	 * @param roadName
	 * @param ini
	 * @param end
	 */
	private void openSegment(String receiverId, String roadName, String ini, String end){
		JsonObject jsmessage = SetUp.fillJSBody("3001", this.city.getId(), receiverId, "Open the segment");
		jsmessage.addProperty("SegmentIni", ini);
		jsmessage.addProperty("SegmentEnd", end);
		
		MqttMessage mes = new MqttMessage(); 
		mes.setPayload(jsmessage.toString().getBytes());
		
		try{
			this.client.publish(this.city.getCityTopic() + "/road" , mes);
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/openSegment ERROR");
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param receiverId
	 * @param roadName
	 * @param ini
	 * @param end
	 */
	private void closeSegment(String receiverId, String roadName, String ini, String end){
		JsonObject jsmessage = SetUp.fillJSBody("3002", this.city.getId(), receiverId, "Close the segment");
		jsmessage.addProperty("SegmentIni", ini);
		jsmessage.addProperty("SegmentEnd", end);
		
		MqttMessage mes = new MqttMessage(); 
		mes.setPayload(jsmessage.toString().getBytes());
		
		try{
			this.client.publish(this.city.getCityTopic() + "/road", mes);
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/closeSegment ERROR");
			e.printStackTrace();
		}
	}
}
