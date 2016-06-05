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
		
		case "7001":
			this.notifyEmergencyRequester(args[0], args[1], args[2]); 
			break;
		
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
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Code", "5000");
			jsmessage.addProperty("SenderId", city.getId());
			jsmessage.addProperty("ReceiverId", receiverId);
			jsmessage.addProperty("Message", "CityAnswerRequest: Answer to WhereAmI");
			
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
	 * This method sends a new Quest to a Special Vehicle 
	 * @param receiverId
	 * @param message
	 */
	private void sendQuest(String receiverId, Quest quest){
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Code", "3000");
			jsmessage.addProperty("SenderId", city.getId());
			jsmessage.addProperty("ReceiverId", receiverId);
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
	
	private void notifyEmergencyRequester(String requesterId, String message, String roadTopic){
		try{
			/* Create the message in JSON Format */
			JsonObject jsmessage = new JsonObject();
			jsmessage.addProperty("Code", "7001");
			jsmessage.addProperty("SenderId", city.getId());
			jsmessage.addProperty("ReceiverId", requesterId);
			jsmessage.addProperty("Message", message);
			

			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			// Publish the message  
			this.client.publish(roadTopic, mes);
			
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " CityAnswerRequest/notifyEmergencyRequester ERROR");
			e.printStackTrace(); 
		} 
	}
	
	
}
