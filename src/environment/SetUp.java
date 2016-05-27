package environment;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * This class only contains static info. 
 *
 */
public class SetUp {
	
	/* STATIC VARIABLES */
	public static final String BROKER_URL = "tcp://localhost:1883"; 
	
	public static void testMessage(String name, String id, String topic, MqttMessage message){
		System.out.println(id + "(" + name + ") I've listened a message.");
		System.out.println("Topic: " + topic);
		System.out.println("Message: " + new String(message.getPayload()));
	}
	
}
