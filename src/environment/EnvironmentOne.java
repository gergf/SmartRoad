package environment;

import smartcar.SmartCar;
import smartcity.SmartCity;
import smartroad.Segment;
import smartroad.SmartRoad;

public class EnvironmentOne {
	
	/**
	 * This environment is designed to test WhereIAm and SOS.
	 */
	
	public static void main(String[] args) {
		/* New city */
		SmartCity valencia = new SmartCity("1100", "valencia");
		/* New road */
		SmartRoad road = new SmartRoad("2100", "cv30", valencia); 
		Segment seg1 = new Segment("2200","seg1", road, 0, 100);
		Segment seg2 = new Segment("2201","seg2", road, 101, 200); 
		
		/* New car*/
		SmartCar car = new SmartCar("3100"); 
		
		System.out.println(car.getTopic());
		
		/* Simulates the car sarching for a topic */
		car.WhereIAm();
		
		/* Waits until the car has new information, and then reload */
		while(!car.getNeedUpdate()){
			//Nothing
		}
		
		/* The car has received the message */
		car.relaunch();
		System.out.println(car.getTopic());
		
		//TODO: El mensaje ha de ser asíncrono. Una vez la carretera la haya añadido hacer el SOS.
		
		road.addSmartCar(car);
		//car.sendSOS();
	}

}
