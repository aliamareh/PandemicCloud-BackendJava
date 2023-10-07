package modele.cartes;

import modele.Ville;

public abstract class CarteVille {
	private Ville laVille;
	
	public CarteVille(Ville laVille){
		this.laVille = laVille;
	}

	/**
	 * @return : la ville inscrite sur la carte
	 */
	public Ville getLaVille() {
		return laVille;
	}

	/**
	 *
	 * @param laVille : la ville qu'on souhaite ajouter a la carte
	 */
	public void setLaVille(Ville laVille) {
		this.laVille = laVille;
	}

	@Override
	public String toString() {
		return "CarteVille :"+
		"laVille : "+laVille.toString();
	}
}
