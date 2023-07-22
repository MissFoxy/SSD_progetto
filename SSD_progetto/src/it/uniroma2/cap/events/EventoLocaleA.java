package it.uniroma2.cap.events;

import it.uniroma2.cap.scenario.AttivitaA;

public class EventoLocaleA extends Event implements Comparable<EventoLocaleA>{
	
	private AttivitaA attivita; //attività generata o influenzata dall'evento
	
	public EventoLocaleA(EventType eventType, Long time, AttivitaA attivita) {
		super(eventType, time);
		this.attivita = attivita;
	}

	/**
	 * @return attività
	 */
	public AttivitaA getAttivitaA() {
		return attivita;
	}

	/**
	 * @param attività da inserire
	 */
	public void setAttivitaA(AttivitaA attivita) {
		this.attivita = attivita;
	}
	
	@Override
	public int compareTo(EventoLocaleA o) {
		if(this.getTime() == o.getTime()) 
			return 0; 
		else if (this.getTime() > o.getTime())
			return 1;
		else return -1;
		
	}
	

}
