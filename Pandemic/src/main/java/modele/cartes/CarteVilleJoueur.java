package modele.cartes;

import modele.Ville;
import modele.cartes.CarteVille;

public class CarteVilleJoueur extends CarteVille implements ICarteJoueur {
	public CarteVilleJoueur(Ville laVille){
		super(laVille);
	}
}
