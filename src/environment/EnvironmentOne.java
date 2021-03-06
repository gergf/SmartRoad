package environment;

import java.io.IOException;
import java.util.Scanner;

import smartcar.Ambulance;
import smartcar.SmartCar;
import smartcity.SmartCity;
import smartroad.Panel;
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
		SmartCity atlantis = new SmartCity("C-000", "atlantis");
		
		/* New road */
		SmartRoad road_A = new SmartRoad("R-000", "A", "0", atlantis);
		SmartRoad road_B = new SmartRoad("R-001", "B", "1", atlantis);
		SmartRoad road_C = new SmartRoad("R-002", "C", "2", atlantis);
		SmartRoad road_D = new SmartRoad("R-003", "D", "3", atlantis);
		SmartRoad road_E = new SmartRoad("R-004", "E", "4", atlantis);; 
		
		/* Create Segments */
		Segment s; 
		Panel p;	// El panel se anyade autom. al segmento 
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
					p = new Panel(s);
					s = new Segment(id2, road_A, ini, right);
					p = new Panel(s);
					break;
				case 1: 
					s = new Segment(id1, road_B, ini, up);
					p = new Panel(s);
					s = new Segment(id2, road_B, ini, right);
					p = new Panel(s);
					break;
				case 2: 
					s = new Segment(id1, road_C, ini, up);
					p = new Panel(s);
					s = new Segment(id2, road_C, ini, right);
					p = new Panel(s);
					break;
				case 3: 
					s = new Segment(id1, road_D, ini, up);
					p = new Panel(s);
					s = new Segment(id2, road_D, ini, right);
					p = new Panel(s);
					break;
				case 4:
					s = new Segment(id1, road_E, ini, up);
					p = new Panel(s);
					s = new Segment(id2, road_E, ini, right);
					p = new Panel(s);
					break;
				}
				
			}
		}
		System.out.println("Segmentes and panels added!");
		
		/* I assume the cars know its topic by GPS location */
		Ambulance ambulance = new Ambulance("A-000","00", "ambulance"); 
		SmartCar car_1 = new SmartCar("V-000", "43");
		
		
		Scanner keyboard = new Scanner(System.in); 
		System.out.print("Press enter to continue..."); 
		keyboard.nextLine();
		
		// Car S-O-S 
		car_1.sendSOS();
	}

}
