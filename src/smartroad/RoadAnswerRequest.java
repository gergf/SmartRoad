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
	/* Values in args
	 * 0: ReceiverId
	 * 1: Message 
	 * 2: Location
	 * 3: Flag Code - En el caso de que en una contestacion se deban 
	 * enviar varios mensajes, para distinguir que mensaje se quiere enviar.
	 */
	private String[] args; 
	private SmartRoad road;
	private String threadId; 
	private String flag; 
	
	/* Communication */
	MqttClient client; 
	
	public RoadAnswerRequest(SmartRoad road, String code, String[] args){
		this.road = road; 
		this.code = code; 
		this.args = args; 
		this.flag = args[3];
		this.threadId = MqttClient.generateClientId();
		this.connect();
	}
	
	@Override
	public void run(){
		switch(this.code){
		case "2000": 
			switch(this.flag){
			/* answer the request 2000 to check the SOS call */
			case "0":	
				this.answer2000(args[0], args[1]);
				break;
			/* Send the emergency to the city */
			case "1": 
				break;
			}
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
	private void answer2000(String receiverId, String message ){
		try{
			JsonObject js = new JsonObject(); 
			js.addProperty("Code" , "6000");
			js.addProperty("SenderId", road.getId());
			js.addProperty("ReceiverId",  receiverId);
			js.addProperty("Message", message);
			
			MqttMessage mes = new MqttMessage(); 
			mes.setPayload(js.toString().getBytes());
			
			this.client.publish(road.getTopic(), mes);
			
		}catch(Exception e){
			System.err.println(road.getId() + ":Thread-" + this.threadId + " ERROR in answer2000");
			e.printStackTrace();
		}
	}

}
