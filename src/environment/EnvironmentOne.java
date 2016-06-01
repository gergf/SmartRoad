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
		/* Define the map of the city */
		int rows = 6; /* 0 - 5 */
		int columns = 5; /* 0 - 4 */
		
		
		/* New city */
		SmartCity valencia = new SmartCity("C-000", "valencia");
		/* New road */
		SmartRoad road_A = new SmartRoad("R-000", "A", "0", valencia);
		SmartRoad road_B = new SmartRoad("R-001", "B", "1", valencia);
		SmartRoad road_C = new SmartRoad("R-002", "C", "2", valencia);
		SmartRoad road_D = new SmartRoad("R-003", "D", "3", valencia);
		SmartRoad road_E = new SmartRoad("R-004", "E", "4", valencia);; 
		
		/* Create Segments */
		Segment s; 
		String id1, id2, ini, up, right;  
		int count = 0; 
		for(int i = 0; i < columns; i++){
			for(int j = 0; j < rows; j++){
				id1 = "S-" + String.valueOf(count++);
				id2 = "S-" + String.valueOf(count++);
				ini = i + "" + j; 		// Initial node 
				up = i + "" + (j+1); 	// Top arista
				right = (i+1) + "" + j; // Right arista 
				switch(i){
				case 0:
					s = new Segment(id1, road_A, ini, up);
					s = new Segment(id2, road_A, ini, right);
					break;
				case 1: 
					s = new Segment(id1, road_B, ini, up);
					s = new Segment(id2, road_B, ini, right);
					break;
				case 2: 
					s = new Segment(id1, road_C, ini, up);
					s = new Segment(id2, road_C, ini, right);
					break;
				case 3: 
					s = new Segment(id1, road_D, ini, up);
					s = new Segment(id2, road_D, ini, right);
					break;
				case 4:
					s = new Segment(id1, road_E, ini, up);
					s = new Segment(id2, road_E, ini, right);
					break;
				}
				
			}
		}
		
		/* New car*/
		SmartCar ambulance = new SmartCar("V-000","00", "1"); 
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
