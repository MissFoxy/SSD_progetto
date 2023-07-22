package it.uniroma2.cap.scenario;

public class AttivitaA {
	
	String attivita;
	float durata;
	int costo;
	
	public AttivitaA(String attivita, float durata, int costo) {
		this.attivita = attivita;
		this.durata = durata;
		this.costo = costo;
	}
	

	/**
	 * @return il codice dell'attività
	 */
	public String getAttivita() {
		return attivita;
	}
	
	/**
	 * @param attività da inserire
	 */
	public void setAttivita(String attivita) {
		this.attivita = attivita;
	}

	/**
	 * @return la durata
	 */
	public float getDurata() {
		return durata;
	}
	
	/**
	 * @param durata da inserire
	 */
	public void setDurata(float durata) {
		this.durata = durata;
	}
	
	/**
	 * @return il costo
	 */
	public int getCosto() {
		return costo;
	}
	
	/**
	 * @param costo da inserire
	 */
	public void setCosto(int costo) {
		this.costo = costo;
	}
	
}
