package modele;

import modele.cartes.ICarteJoueur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Joueur {
	private String pseudo;
	private Role role;
	private Ville emplacement;
	private List<ICarteJoueur> cartes;

	private int nbActions;

	private int compteurEvenementReorganisation=0;

	public Joueur(String pseudo){
		this.pseudo = pseudo;
		this.cartes = new ArrayList<>();
	}
	
	public String getPseudo() {
		return pseudo;
	}
	
	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public Ville getEmplacement() {
		return emplacement;
	}
	
	public void setEmplacement(Ville emplacement) {
		this.emplacement = emplacement;
	}

	public List<ICarteJoueur> getCartes() {
		return cartes;
	}

	public void ajouterCartes(List<ICarteJoueur> cartes) {
		this.cartes.addAll(cartes);
	}

	public void enleverCartes(List<ICarteJoueur> cartes) {
		this.cartes.removeAll(cartes);
	}

	public List<ICarteJoueur> defausser(Integer ... index){
		List<ICarteJoueur> cartesDefausses = new ArrayList<>();
		List<ICarteJoueur> carteASeppr = new ArrayList<>();

		Arrays.stream(index).forEach( i -> {
			cartesDefausses.add(cartes.get(i));
			carteASeppr.add(cartes.get(i));
		});
		cartes.removeAll(carteASeppr);

		return cartesDefausses;
	}

	public int getNbActions() {
		return nbActions;
	}

	public void setNbActions(int nbActions) {
		this.nbActions = nbActions;
	}

	public int nombreCartesPourTraiterMaladie() {
		if ( role.equals(Role.SCIENTIFIQUE)) {
			return 4;
		} else {
			return 5;
		}
	}
	
	@Override
	public String toString() {
		return this.pseudo;
	}
}
