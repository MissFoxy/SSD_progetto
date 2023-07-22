package it.uniroma2.cap.events;

public class EventoRemotoM extends Event {
	
	private String pezzo;
	
	public EventoRemotoM(EventType type, Long time, String pezzo) {
		super(type, time);
		this.pezzo = pezzo;
	}	
	
	/**
	 * @return il codice del pezzo
	 */
	public String getPezzo() {
		return pezzo;
	}

	/**
	 * @param codice del pezzo da inserire
	 */
	public void setPezzo(String pezzo) {
		this.pezzo = pezzo;
	}

}
