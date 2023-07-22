package it.uniroma2.cap.events;

import java.util.ArrayList;

import it.uniroma2.cap.scenario.Pezzo;

public class EventoLocaleM extends Event implements Comparable<EventoLocaleM>{
	
	private Pezzo pezzo; //pezzo generato o influenzato dall'evento
	
	public EventoLocaleM(EventType eventType, Long time, Pezzo pezzo) {
		super(eventType, time);
		this.pezzo = pezzo;
	}

	/**
	 * @return pezzo
	 */
	public Pezzo getPezzo() {
		return pezzo;
	}

	/**
	 * @param pezzo da inserire
	 */
	public void setPezzo(Pezzo pezzo) {
		this.pezzo = pezzo;
	}
	
	
	@Override
	public int compareTo(EventoLocaleM o) {
		if(this.getTime() == o.getTime()) 
			return 0; 
		else if (this.getTime() > o.getTime())
			return 1;
		else return -1;
		
	}
	
	private ArrayList<String> listaPezzi = new ArrayList<String>();
	public ArrayList<String> getListaPezzi() {
		return listaPezzi;
	}

}
