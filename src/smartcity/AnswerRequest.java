package smartcity;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;

import environment.SetUp;
import smartroad.SmartRoad;

/*
 * El objetivo de esta clase es liberar de carga al cliente de la SmartCity, 
 * para que así pueda recibir todas las peticiones sin problemas. 
 * 
 * El objetivo único de esta clase es RESPONDER, es decir, una vez se haya 
 * enviado el mensaje, el hilo será eliminado.
 */
public class AnswerRequest extends Thread
{
	/* Data */
	private String code; 
	/* Values in args
	 * 0: ReceiverId
	 * 1: Message 
	 * 2: Location
	 */
	private String[] args; 
	private SmartCity city;
	private String threadId; 
	
	/* Communication */
	MqttClient client; 
	
	public AnswerRequest(SmartCity city, String code, String[] args){
		this.city = city; 
		this.code = code; 
		this.args = args; 
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
		}
		
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
			System.err.println(args[0] + "AnswerRequest/connect: ERROR");
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
			jsmessage.addProperty("Message", "Thread");
			
			/*Add topic */
			jsmessage.addProperty("Topic", road.getTopic());
			
			/* Create a Mqtt message */
			MqttMessage mes = new MqttMessage();
			mes.setPayload((jsmessage.toString()).getBytes());
			// Publish the message  
			this.client.publish(city.getCityTopic(), mes);
			
		}catch(Exception e){
			System.err.println(city.getId()+"-Thread:" + this.threadId + " AnswerRequest/answer1000 ERROR");
			e.printStackTrace(); 
		} 
	}/* end answer1000 */
	
}
