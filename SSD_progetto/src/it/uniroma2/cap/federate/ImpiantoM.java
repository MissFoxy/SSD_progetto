package it.uniroma2.cap.federate;

import it.uniroma2.cap.events.*;
import it.uniroma2.cap.federate.StatoImpiantoA;
import it.uniroma2.cap.scenario.AttivitaA;
import it.uniroma2.cap.scenario.Pezzo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAvariableArray;
import hla.rti1516e.time.HLAinteger64Time;
import hla.rti1516e.time.HLAinteger64TimeFactory;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.InvalidResignAction;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.OwnershipAcquisitionPending;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;

/**
 * This is an example Federate demonstrating how to use the IEEE 1516-2010 (HLA Evolved)
 * Java interface supplied with the Pitch RTI. The code provided here is intended to illustrate how many
 * most common and relevant actions have to be carried-out. 
 * As such, the scenario and the related implementation have been kept simple enough to focus on
 * HLA issue.
 * 
 * The federate structure is structured as follows:
 * 
 * 	InitFederate Method
 *  1.  Create the RTIambassador
 *  2.  Connect to the RTIamsassador
 *  3.  Try to create the federation
 *  4.  Join the federation
 *  5.  Announce a Synchronization Point
 *  6.  Enable Time Regulation and Constrained
 *  7.  Publish and Subscribe
 *  8.  Wait for the federation to Synchronized on the point 
 *  9.  Register an Object Instance
 *  
 *  
 *  StartFederate Method
 *  10. Simulation Main Loop
 *       Determine the needed time advancing (next event timestamp or fixed timestep)
 *       Ask a Next Message Time Advance
 *       Wait for the Time Advance Grant
 *       Process the first event in the Event List (if available)
 * 
 *  DisplayFederateState Method
 *  11. Summarize Federate final state
 * 
 *  LeaveFederation Method
 *  12. Resign from Federation and
 *  13. Destroy the federation
*/

public class ImpiantoM {

	//ImpiantoM proprietà
	private String nome; //codice impianto A
	
	private SortedSet<EventoLocaleM> eventsList; //ordered list of events to be processed
	private ArrayList<String> listaPezzi = new ArrayList<String>();
	
	//simulation properties
	private int _seed;
	private long _simulationEndTime;
	
	//HLA-related properties
	protected RTIambassador rtiAmb;
	protected final String FEDERATION_NAME = "CAP Simulation";
	protected FederateAmbassadorImplM fedAmbassador;
	protected String federateName;
	protected HLAinteger64TimeFactory timeFactory; // set when we join
	protected EncoderFactory encoderFactory;
	
	// handles types - set once we join a federation
	
	protected InteractionClassHandle icRemoteEventAHandle;
	protected ParameterHandle timeHandle;
	protected ParameterHandle pezzoHandle;
	protected ParameterHandle typeHandle;
	
	public ImpiantoM(String nome, int seed, long endTime) {
		this.nome = nome;
		
		eventsList = new TreeSet<EventoLocaleM>(); 
		federateName = nome+"_ImpiantoM";
		this._seed = seed;
		this._simulationEndTime = endTime;

		this.listaPezzi = new ArrayList<String>();
	}

	public void addEvent(EventoLocaleM e) {
		this.eventsList.add(e);
	}
	
	private EventoLocaleM getNextEvent() {
		return this.eventsList.first();
	}

	
	
	/**
	 * @return federation name
	 */
	public String getFederateName() {
		return federateName;
	}
	
	/**
	 * @return codice dell'impianto
	 */
	public String getName() {
		return nome;
	}

	/**
	 * @param nome il codice dell'impianto da inserire
	 */
	public void setName(String nome) {
		this.nome = nome;
	}
	
	public void startFederate(String host) {
		//boolean simulationEnd = false;
		EventoLocaleM event;
		long nextEventTimestamp; 	// timestamp of the next event to be processed
		long currentTime;			// current logical time
		long nextTime;				// timestamp of the next event to be schedulated
		long timeStep = 50;			// timestamp used for determining the nextTime value
		
		//Federate Initialization (items 1-9)
		initFederate(host);
		
		//---------- 10 Simulation Main Loop ---------------
		System.out.println("\n" +"___________________________________________");
		System.out.println("Simulation Begins....");
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Simulation Start ");
		
		
		try {
			while(fedAmbassador.getFederateTime()<_simulationEndTime) {
				currentTime = fedAmbassador.getFederateTime();
				
				if (!eventsList.isEmpty()) {
				
					nextEventTimestamp = getNextEvent().getTime();	
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Next Message Time: " + nextEventTimestamp);
					
				
					
						/* Federate must advance its logical time before processing the event.
						/* A NMR Request ask RTI to advance time to T. The result is
						 * T, if any messages with timestamp T'<T has to be delivered
						 * T' otherwise. In case, appropriate callbacks are invoked for
						 * handling the received event and updating the event list accordingly
						 */
						fedAmbassador.isAdvancing = true;
						System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Ask a Time Advance");
						rtiAmb.nextMessageRequest(timeFactory.makeTime(nextEventTimestamp));
						
				
					//}
					while(fedAmbassador.isAdvancing)
						Thread.sleep(10);
					System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Time Advance Grant");
					
					event = getNextEvent();
					
					//process event
					process(event);
					eventsList.remove(event);
				} 
				else {
					// no local events. Time is advanced by a fixed step
					nextTime = currentTime + timeStep;
					fedAmbassador.isAdvancing = true;
					rtiAmb.nextMessageRequest(timeFactory.makeTime(nextTime));
					while(fedAmbassador.isAdvancing)
						Thread.sleep(10);		
				}
			
			}
			System.out.println("Simulation Completed");
			System.out.println("___________________________________________" + "\n");
			
			leaveSimulationExecution();
			displayFederateState();
			
		}catch (Exception e) {e.printStackTrace();}		
			
	}
	
	private void initFederate(String host) {
		String settings;

		System.out.println( "Starting of " + getFederateName());
		
		
		try {
			//---------- 1 & 2 create RTIambassador and Connect---------------
			
			rtiAmb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
			encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
			
			fedAmbassador = new FederateAmbassadorImplM(this);
			settings = "crcAddress=" + host;
	        rtiAmb.connect(fedAmbassador, CallbackModel.HLA_IMMEDIATE, settings);
	        
	        System.out.println( "Connected to RTI" );
	        
	      //---------------- 3 create Federation Execution -------------------
	      
	        URL[] fom = new URL[]{
	        		(new File("fom/atc_fom.xml")).toURI().toURL(),
			};//FOM
	        	
		    rtiAmb.createFederationExecution(FEDERATION_NAME, fom, "HLAinteger64Time" );
	        System.out.println( "Created Federation" );

		}catch(FederationExecutionAlreadyExists e) {
			//Exception is ignored
			System.out.println("Connecting to an existing Federation Execution");
		}
		catch(ErrorReadingFDD | InconsistentFDD | CouldNotCreateLogicalTimeFactory |
                 CouldNotOpenFDD  | NotConnected | CallNotAllowedFromWithinCallback | 
                 RTIinternalError | MalformedURLException e ) {e.printStackTrace();} 
		catch (ConnectionFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidLocalSettingsDesignator e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCallbackModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyConnected e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    try {
			
	        //---------------------- 4 Join Federation -----------------------
	        rtiAmb.joinFederationExecution( getFederateName(), FEDERATION_NAME);
	        System.out.println( "Joined Federation as " + getFederateName());

			//-------------- 5 Sync Points Registering ----------------
		
			/*
			 * We need to artificially block the federate execution here for showing
			 * how synchronization points RTI call and Federate callbacks work.
			 * Specifically a registered sync point is announced only to federates who 
			 * actually joined the federation.
			 * */
	        FederateHandle f=null;
	        
	        while(f==null) {
        		try {
        			f = rtiAmb.getFederateHandle("Impianto_M");
        		}
        	catch(FederateNotExecutionMember | NameNotFound ignored) {}
        	}
	        
			// Both federates register and achieve the sync point (see FOM)

			rtiAmb.registerFederationSynchronizationPoint("ReadyToRun",null);
			
			
			while(!fedAmbassador.isAnnounced) //semaforo posto inizialmente a false, viene messo a true quando l'rti invoca la callback federateSynch
				Thread.sleep(10);
			
			//---------------------- 6 Time Management -------------------------
			
			// Federates are both time-regulated and time-constrained			
			timeFactory = (HLAinteger64TimeFactory)rtiAmb.getTimeFactory();
			HLAinteger64Interval lookahead = timeFactory.makeInterval(fedAmbassador.federateLookahead);
			rtiAmb.enableTimeRegulation(lookahead);
					
			while(!fedAmbassador.isRegulating) {
				Thread.sleep(10);
			}
			System.out.println(federateName + " is Time Regulated");	
				
			
			//---------------------- 7 Publish & Subscribe -------------------------
			
			//Interactions and parameters
			this.icRemoteEventAHandle = rtiAmb.getInteractionClassHandle("HLAinteractionRoot.RemoteEvent");
			this.pezzoHandle = rtiAmb.getParameterHandle(icRemoteEventAHandle, "pezzo");
			this.typeHandle = rtiAmb.getParameterHandle(icRemoteEventAHandle, "type");
		
			//both federates publish and subscribe the interaction
			rtiAmb.publishInteractionClass(icRemoteEventAHandle);
			rtiAmb.subscribeInteractionClass(icRemoteEventAHandle);
			
			//--------------------- 8 Synchronization Before Running -----------------------
			
			// Sync point reached. The federate must wait other federates
			rtiAmb.synchronizationPointAchieved("ReadyToRun");
			while(!fedAmbassador.isReadyToRun) 
				Thread.sleep(10);	
			System.out.println(federateName + " All Federates achieved READY_TO_RUN Sync Point");
			
		}
		catch ( FederationExecutionDoesNotExist |  SaveInProgress | RestoreInProgress | FederateAlreadyExecutionMember | NotConnected
                | CallNotAllowedFromWithinCallback | RTIinternalError e) 
        {
            System.err.println("Cannot connect to the Federation");
            e.printStackTrace();
        }	
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void process(EventoLocaleM event) {
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Processing Event " + event.getEventType());

		long currentTime;		//current logical time
		long nextEventTime; 	//time to schedule the next event 
		 						//(according to the current event in process)
		int timeStep = 50;
		
		Random rand = new Random(_seed);
				
		currentTime = fedAmbassador.getFederateTime();		
		
		Pezzo pezzo;
		
		switch(event.getEventType()) {
		
			case PRODUCTION_COMPONENT_M:
				pezzo = event.getPezzo();
				System.out.println("[" + fedAmbassador.federateTime + "] Impianto M inizia la produzione di" + pezzo.getPezzo() + "al tempo " + currentTime);
				nextEventTime = currentTime + (long)pezzo.getDurata();
			    //System.out.println("[" + fedAmbassador.federateTime + "] Impianto M ha finito di produrre" + pezzo.getPezzo() + "al tempo " + nextEventTime);
			    EventoRemotoM eventoRemotoM = new EventoRemotoM(EventType.DELIVERY_COMPONENT, nextEventTime, pezzo.getPezzo());
				sendRemoteEvent(eventoRemotoM);
				break;
				
			case STARTING_REQUEST:
				listaPezzi = event.getListaPezzi();
				for(String p : listaPezzi) {
					if( p.equals("M1")){
						Pezzo pezzoM1 = new Pezzo("M1", setDurata(60,5), 7000);
						System.out.println("[" + fedAmbassador.federateTime + "] Costruzione pezzo M1 " + currentTime );
						addEvent(new EventoLocaleM(EventType.PRODUCTION_COMPONENT_M, currentTime, pezzoM1));
					}else{
						Pezzo pezzoM2 = new Pezzo("M2", setDurata(40,2), 4000);
						System.out.println("[" + fedAmbassador.federateTime + "] Costruzione pezzo M2 " + currentTime );
						addEvent(new EventoLocaleM(EventType.PRODUCTION_COMPONENT_M, currentTime, pezzoM2));
					}
				}
				break;
		}
		
	}
	
	/**
	 * @param durata da inserire
	 */
	public float setDurata(double media, double varianza) {
	    double lambda = 1.0 / media; // Calcolo del tasso di decrescita
	    Random random = new Random();
	    double U = random.nextDouble(); // Genera un numero double casuale tra 0 e 1 (estremi inclusi)
	    double durata = -Math.log(U) / lambda; // Applica la formula dell'esponenziale inversa
	    // Adatta la distribuzione in base alla varianza desiderata
	    durata = durata * Math.sqrt(varianza);
	    return (float)durata;
	}
	
	private void sendRemoteEvent(EventoRemotoM e) {
		// Remote events are modeled as interactions to be sent
		try {
			ParameterHandleValueMap parameters = rtiAmb.getParameterHandleValueMapFactory().create(4);
			
			HLAunicodeString pezzoEncoder = encoderFactory.createHLAunicodeString();
			pezzoEncoder.setValue(e.getPezzo());

			//event type
			HLAunicodeString typeEncoder = encoderFactory.createHLAunicodeString();
			typeEncoder.setValue(e.getEventType().name());
			
			parameters.put(this.pezzoHandle, pezzoEncoder.toByteArray());
			parameters.put(this.typeHandle, typeEncoder.toByteArray());
        
			HLAinteger64Time time = timeFactory.makeTime(e.getTime());
        
			rtiAmb.sendInteraction(icRemoteEventAHandle, parameters, null, time);
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Evento Remoto M->A che consegna " + e.getPezzo() + ", tipo evento: "+ e.getEventType() );
		} catch(Exception ex) {ex.printStackTrace();}
	}
	

	private void leaveSimulationExecution() {
		
		//---------- 10 Simulation Main Loop ---------------
		
		try {
			rtiAmb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		} 
		catch(FederateOwnsAttributes ignored) {}
		
		catch (InvalidResignAction | OwnershipAcquisitionPending | FederateNotExecutionMember
				| NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError e) {
			
			e.printStackTrace();
		}
		System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": Resigned from Federation");

		try
		{
			rtiAmb.destroyFederationExecution(this.FEDERATION_NAME);
		}
		catch(FederatesCurrentlyJoined ej) {
			//ignored
			System.out.println("[" + fedAmbassador.federateTime + "] " + federateName + ": did not destroy federation, federates still joined");
		}
		catch( FederationExecutionDoesNotExist  | NotConnected | RTIinternalError e )
		{
			e.printStackTrace();
		}
	}
	
	private void displayFederateState() {
		
		
	}
}
