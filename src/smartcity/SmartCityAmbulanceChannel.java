package smartcity;

import org.eclipse.paho.client.mqttv3.MqttClient;

import environment.SetUp;

public class SmartCityAmbulanceChannel {
	
	private MqttClient client;
	private String topic; 
	private SmartCity mycity; 
	
	public SmartCityAmbulanceChannel(SmartCity city){
		/* Constructor here */
		this.mycity = city; 
		this.topic = this.mycity.getCityTopic() + "/ambulance"; 
		
		
	}
}
