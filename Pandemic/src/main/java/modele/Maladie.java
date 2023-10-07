package modele;

import modele.exceptions.MaladieEradiqueException;
import modele.exceptions.PlusDeCubesMaladieDisponible;

public class Maladie {

	private String couleur;		//attribut nommé couleur mais représente bien le nom de la maladie
	private int cubesRestants;	//le nombre de cubes restant pour cette maladie à utiliser
	private boolean remede;//booleen permettant de savoir si le remède a été mis au point

	private boolean eradique;

	/**
	 * Créé une maladie avec un nom (couleur)
	 * @param couleur le nom (couleur) de la maladie
	 */
	public Maladie(String couleur){
		this.couleur = couleur;
		this.cubesRestants = 24; //de base, dans les règles, 24 cubes par maladie (4 maladies)
		this.remede = false; //de base, le remède n'est pas établi
		this.eradique =false;
	}
	
	public String getCouleur() {
		return couleur;
	}
	
	public void setCouleur(String couleur) {
		this.couleur = couleur;
	}
	
	public int getCubesRestants() {
		return cubesRestants;
	}
	
	
	
	public void ajouterCubes(int quantite) throws MaladieEradiqueException {
		if(this.cubesRestants < 24){
			this.cubesRestants += quantite;
		}
		else {
			this.eradique =true;
			throw new MaladieEradiqueException();
		}

	}
	
	public void retirerCubes(int quantite) throws PlusDeCubesMaladieDisponible {
		if(eradique){
			throw new RuntimeException("maladie eradique");
		}
		if(this.cubesRestants - quantite >= 0){
			this.cubesRestants -= quantite;
		}
		else
		{
			throw new PlusDeCubesMaladieDisponible(); //Condition de défaite
		}
	}
	
	public boolean remedeEtabli(){
		return this.remede;
	}
	
	public void remedeEtabli(boolean eta){this.remede = eta;}

	public boolean getRemede() {
		return remede;
	}

	public void setRemede(boolean remede) {
		this.remede = remede;
	}
	
	public boolean isEradique() {
		return eradique;
	}
	
	public void setEradique(boolean eradique) {
		this.eradique = eradique;
	}

	@Override
	public String toString() {
		return this.couleur;
	}



}
