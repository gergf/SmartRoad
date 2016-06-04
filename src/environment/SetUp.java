package environment;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class only contains static info. 
 *
 */
public class SetUp {
	
	/* STATIC VARIABLES */
	public static final String BROKER_URL = "tcp://localhost:1883"; 
	private static ObjectMapper mapper = null; 
	
	public static void testMessage(String name, String id, String topic, MqttMessage message){
		System.out.println(id + "(" + name + ") I've listened a message.");
		System.out.println("Topic: " + topic);
		System.out.println("Message: " + new String(message.getPayload()));
	}
	
	/* Jackson
	 * Datatype module to make Jackson recognize Java 8 Date & Time API data types (JSR-310).
	 * https://github.com/FasterXML/jackson-datatype-jsr310 */
	public static ObjectMapper getMapper(){
		if(mapper == null){
			mapper = new ObjectMapper(); 
			mapper.findAndRegisterModules();  
		}
		return mapper; 
	}
}
