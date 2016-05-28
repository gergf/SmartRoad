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
		SmartCity valencia = new SmartCity("C-000", "valencia");
		/* New road */
		SmartRoad road_A = new SmartRoad("R-000", "A", "0", valencia);
		SmartRoad road_B = new SmartRoad("R-001", "B", "1", valencia);
		SmartRoad road_C = new SmartRoad("R-002", "C", "2", valencia);
		SmartRoad road_D = new SmartRoad("R-003", "D", "3", valencia);
		SmartRoad road_E = new SmartRoad("R-004", "E", "4", valencia);; 
		
		/* New car*/
		SmartCar ambulance = new SmartCar("V-000", "00"); 
		SmartCar car_1 = new SmartCar("V-001", "43");
		
		
		/* Simulates call between devices */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Relaunch the car with the new information
		car_1.relaunch(); 
		ambulance.relaunch();

		// Car S-O-S 
		car_1.sendSOS();
	}

}
