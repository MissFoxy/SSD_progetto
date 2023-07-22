package it.uniroma2.cap.scenario;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import it.uniroma2.sel.atc.events.*;
import it.uniroma2.sel.atc.federate.Airport;

public class CAPSimulation {
	
	//Scenario
	private Airport airportFederate;
	private static int _seed = 67; 
	private static long _simulationEndTIme = 150;
	
	//main simulation method
	void run(String[] args) {
		String airportCode="FCO";
		String code;
		String host="localhost";
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		//instatiating federate
		try {
			System.out.println("Air Traffic Control Simulation");
			System.out.println("_______________________________"+"\n");
			System.out.println("Enter the Airport Code");
			System.out.println("1: FCO - Fiumicino (default)");
			System.out.println("2: LIN - Linate");
		
			code = in.readLine();
			if (code.equals("2")) 
				airportCode = "LIN";
			
			System.out.println("Enter CRC Host or press enter for localhost");
			host = in.readLine();
			if (host.length() == 0)  
				host = "localhost";
		}
		catch(Exception e) {e.printStackTrace();}
		
		airportFederate = new Airport(airportCode, _seed, _simulationEndTIme);
				
		//Scenario Configuration
		initScenario(airportCode);
		
		//Starting of Federate Execution
		airportFederate.startFederate(host);
	}
	
	private void initScenario(String airportCode) {
		/*
		 * for the sake of simplicity the scenario configuration is hardcoded here.
		 * A more effective and flexible implementation should use external file configurations
		 * and/or the use use of a framework e.g. Spring, etc...
		 */
		airportFederate.setRunwayClearance(false);
		if (airportCode == "FCO") {
			//Fiumicino Airport, busy runway, 3 managed planes, 3 initial events	
			Airplane a1 = new Airplane(AirplaneState.LANDED, new String("AZ001"));
			Airplane a2 = new Airplane(AirplaneState.IN_FLIGHT, new String("AZ002"));
			Airplane a3 = new Airplane(AirplaneState.IN_FLIGHT, new String("AZ003"));
		
			airportFederate.addManagedAirplane(a1);
			airportFederate.addManagedAirplane(a2);
			airportFederate.addManagedAirplane(a3);
			
			airportFederate.addEvent(new LocalEvent(EventType.TAKE_OFF_REQUEST,(long)1,a1));
			airportFederate.addEvent(new LocalEvent(EventType.LANDING_REQUEST,(long)12,a2));
			airportFederate.addEvent(new LocalEvent(EventType.LANDING_REQUEST,(long)29,a3));
			
		} 
		else if (airportCode == "LIN") {
			//LINATE Airport, busy runway, 3 managed planes, 3 initial events
			Airplane a4 = new Airplane(AirplaneState.LANDED, "AZ004");
			Airplane a5 = new Airplane(AirplaneState.IN_FLIGHT, "AZ005");
			Airplane a6 = new Airplane(AirplaneState.IN_FLIGHT, "AZ006");
			
			
			airportFederate.addManagedAirplane(a4);
			airportFederate.addManagedAirplane(a5);
			airportFederate.addManagedAirplane(a6);
		
			airportFederate.addEvent(new LocalEvent(EventType.LANDING_REQUEST,(long)8,a4));
			airportFederate.addEvent(new LocalEvent(EventType.TAKE_OFF_REQUEST,(long)2,a5));
			airportFederate.addEvent(new LocalEvent(EventType.LANDING_REQUEST,(long)16,a6));
			
		}
		else {
			System.out.println("Airport Code Unknown");
		}
		System.out.println("Completed Scenario Configuration for Airport " + airportCode);
	}
	
	public static void main(String[] args) {
		new CAPSimulation().run(args);
		
	}
	


}
