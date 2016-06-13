package smartcar;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import environment.SetUp;
import event.Quest;

public class Ambulance extends SmartCar {

	private boolean onMision;
	
	public Ambulance(String id, String location, String type) {
		super(id, location, type);
		this.onMision = false;
	}
	
	public boolean isOnMision() {
		return onMision;
	}

	public void setOnMision(boolean onMision) {
		this.onMision = onMision;
	}

	/* this overrides the method of the super class */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		/* Parse MqttMessage to Json and string*/
		String text = new String(message.getPayload()); 
		JsonObject js = new JsonParser().parse(text).getAsJsonObject();
		String ReceiverId = js.get("ReceiverId").getAsString();
		
		/* If the message is addressed to me */
		if(ReceiverId.equals(super.getId())){
			
			String code = js.get("Code").getAsString(); // Y XXX
			String theme = code.substring(0,1); // Y 
			String requestCode = code.substring(1,4); // XXX
			/* THEME */
			switch(theme){
			/* Quests */
			case "3": 
				/* Received Quest */
				switch(requestCode){
				case "000": 
					System.out.println(super.getId() + ": I have received a new Quest.");
					/* JSON to Quest */
					ObjectMapper mapper = SetUp.getMapper();
					String jsQuest = js.get("Quest").getAsString();
					Quest quest = mapper.readValue(jsQuest, Quest.class);
					/* Do the quest (I should be available) */
					this.doQuest(quest);
					break;
				}
				break;
			
			/* Answer Info */
			case "5":
				/* do nothing */ 
				break;
			
			/* Answer to Emergency */
			case "6": 
				switch(requestCode){
					/* S.O.S call */
					case "000":
						break;
					/* Emergency attended */
					case "002":
						System.out.println(super.getId() + ": Now I am OK!");
						break;
				}
				break;
			
			case "7":
				/* do nothing */
				break;
			
			case "8":
				switch(requestCode){
				/* The city has listened my message 4000 */
				case "000": 
					if(js.get("ReceiverId").getAsString().equals(super.getId())){
						/* Nothing to do here (for now) */ 
					}
					break;
				}
				break;
			
			/* Non-valid message (theme)*/
			default:
				System.err.println(super.getId() + ": MessageArrivedERROR Non-valid theme. ");
				break;
			}
		}
	}
	
	/* Quests, 
	 * for now, all the quest are "go to somewhere to attend an event"  */
	private void doQuest(Quest quest) {
		/* Read the quest */
		ArrayList<String> route = quest.getRoute();
		/* Go to ... */
		System.out.println(super.getId() + ": Going from " + route.get(0) + " to " + route.get(route.size()-1)); 
		super.goTo(route);
		/* Tell the city that the quest has been completed */
		super.notifyCityQuestCompleted(quest);
	}
	
}
