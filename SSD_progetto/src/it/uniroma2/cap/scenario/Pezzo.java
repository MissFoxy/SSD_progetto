package it.uniroma2.cap.scenario;

import java.util.Random;

public class Pezzo {
	
	String pezzo;
	float durata;
	int costo;
	
	public Pezzo(String pezzo, float durata, int costo) {
		this.pezzo = pezzo;
		this.durata = durata;
		this.costo = costo;
	}
	
	/**
	 * @return il codice pezzo
	 */
	public String getPezzo() {
		return pezzo;
	}
	
	/**
	 * @param pezzo da inserire
	 */
	public void setPezzo(String pezzo) {
		this.pezzo = pezzo;
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
	public void setDurata(double media, double varianza) {
	    double lambda = 1.0 / media; // Calcolo del tasso di decrescita
	    Random random = new Random();
	    double U = random.nextDouble(); // Genera un numero double casuale tra 0 e 1 (estremi inclusi)
	    double durata = -Math.log(U) / lambda; // Applica la formula dell'esponenziale inversa
	    // Adatta la distribuzione in base alla varianza desiderata
	    durata = durata * Math.sqrt(varianza);
	    
		this.durata = (float)durata;
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
