package modele;

import donnees.BibDonneesMongoDB;
import modele.cartes.CartePropagation;
import modele.cartes.ICarteJoueur;
import modele.exceptions.*;

import java.util.*;

public class Plateau {
	//Stations de recherche restantes
	private int stationsRecherche;
	//Compteur du nombre d'éclosions ayant eu lieu (à 8 = défaite)
	private int compteurEclosion;
	/*indice à incrémenter quand le niveau de propagation augmente (il sera en
	corrélation avec le niveauPropagation*/
	private int indicePropagation;
	private boolean evntParUneNuitTranquille;
	
	private boolean evntPrevision;
	private int[] niveauPropagation; //les différents niveau de propagation
	private List<Maladie> maladies;
	
	private List<CartePropagation> piochePropagation;
	private List<CartePropagation> defaussePropagation;
	private List<ICarteJoueur> piocheJoueur;
	private List<ICarteJoueur> defausseJoueur;

	private List<Ville> lesVilles;

	/**
	 * Créer un nouveau plateau de début de partie
	 */
	public Plateau(){
		this.evntParUneNuitTranquille = false;
		this.evntPrevision = false;
		this.stationsRecherche = 6;
		this.compteurEclosion = 0;
		this.niveauPropagation = new int[]{2,2,2,3,3,4,4};
		this.indicePropagation = 0;
		this.maladies = new ArrayList<>();
		this.piochePropagation = new ArrayList<>();
		this.defaussePropagation = new ArrayList<>();
		this.piocheJoueur = new ArrayList<>();
		this.defausseJoueur = new ArrayList<>();
		this.lesVilles = new ArrayList<>();
	}
	
	/**
	 * Méthode permettant de remplir la pioche de cartes de propagation
	 * @param cartes les cartes de propagation à ajouter
	 * @return le nombre de cartes propagation dans la pioche
	 */
	public int ajouterCartesPropagation(List<CartePropagation> cartes){
		this.piochePropagation.addAll(cartes);
		return this.piochePropagation.size();
	}

	public List<Ville> getLesVilles() {
		return lesVilles;
	}

	public void setLesVilles(List<Ville> villes){
		this.lesVilles.addAll(villes);
		for(Ville v : villes){
			v.setPlateau(this);
		}
	}


	/**
	 * Méthode permettant de remplir la pioche de cartes de propagation
	 * @param cartes les cartes de propagation à ajouter
	 * @return le nombre de cartes propagation dans la pioche
	 */
	public int ajouterCartesJoueur(List<ICarteJoueur> cartes){
		this.piocheJoueur.addAll(cartes);
		return this.piocheJoueur.size();
	}
	
	/**
	 * Permet à un joueur de piocher des cartes (lorsque son tour débute / au début du jeu)
	 * @param nbCartes le nombre de cartes que le joueur va piocher
	 * @return une ArrayList des cartes piochées
	 */
	public List<ICarteJoueur> piocherJoueur(int nbCartes) throws PiocheJoueurVideException {
		List<ICarteJoueur> cartesPiochees = new ArrayList<>();
		
		if(this.piocheJoueur.size() >= nbCartes) {
			for (int i = 0; i < nbCartes; i++) {
				ICarteJoueur carte = this.piocheJoueur.get(0);
				
				cartesPiochees.add(carte);
				
				this.piocheJoueur.remove(0);
			}
		}
		else
		{
			throw new PiocheJoueurVideException(); //Défaite: car plus de cartes
		}
		
		return cartesPiochees;
	}
	
	/**
	 * Méthode utilisée pour placer des cartes d'un joueur dans la défausse du plateau
	 * @param cartes la liste des cartes à défausser
	 */
	public void defausserJoueur(List<ICarteJoueur> cartes){
			
			this.defausseJoueur.addAll(0,cartes);
	}
	
	/**
	 * Permet de connaître le nombre de cartes restantes dans la pioche joueur
	 * @return un entier
	 */
	public int getNbCartesPiocheJoueur(){
		return this.piocheJoueur.size();
	}
	
	/**
	 * Permet de connaître le nombre de cartes restantes dans la défausse joueur
	 * @return un entier
	 */
	public int getNbCartesDefausseJoueur(){
		return this.defausseJoueur.size();
	}
	
	/**
	 * Un joueur piochera une carte propagation dont l'effet sera appliqué puis elle sera placée dans la défausse
	 * @return la carte de propagation piochée
	 * @throws PlusDeCubesMaladieDisponible plus de cubes de la maladie en réserve
	 */
	public CartePropagation piocherPropagation(int nbCube) throws PlusDeCubesMaladieDisponible {
		CartePropagation carte = null;
		
		if (!this.getEvntParUneNuitTranquille()){
			carte = this.piochePropagation.get(0);
			Ville villeTouchee = carte.getLaVille();
			
			this.piochePropagation.remove(0);
			
			villeTouchee.propagation(nbCube);
			
			this.defaussePropagation.add(0, carte);
		}
		else
		{
			this.evntParUneNuitTranquille = false;
		}
		
		return carte;
	}
	
	/**
	 * Permet de connaître le nombre de cartes restantes dans la pioche propagation
	 * @return un entier
	 */
	public int getNbCartesPiochePropagation(){
		return this.piochePropagation.size();
	}
	
	/**
	 * Permet de connaître le nombre de cartes restantes dans la défausse propagation
	 * @return un entier
	 */
	public int getNbCartesDefaussePropagation(){
		return this.defaussePropagation.size();
	}
	
	/**
	 * Méthode utilisée pour l'étape 1 de l'épidémie (Accélération)
	 * @return le niveau de propagation actuel
	 */
	public int acceleration(){
		this.indicePropagation++;
		return this.niveauPropagation[this.indicePropagation];
	}
	
	/**
	 * Méthode utilisée pour l'étape 2 de l'épidémie (Infection)
	 * @return la carte de propagation piochée en dessous
	 * @throws PlusDeCubesMaladieDisponible plus de cubes de la maladie en réserve
	 */
	public CartePropagation infection() throws PlusDeCubesMaladieDisponible {
		int keyCarte = this.piochePropagation.size()-1;
		CartePropagation carteDessous = this.piochePropagation.get(keyCarte);
		this.piochePropagation.remove(keyCarte);
		
		carteDessous.getLaVille().propagation(3);
		
		this.defaussePropagation.add(carteDessous);
		
		return carteDessous;
	}
	
	/**
	 * Méthode utilisée pour l'étape 3 de l'épidémie (Intensification)
	 */
	public void intensification(){
		melanger(this.defaussePropagation);
		
		for(CartePropagation c : this.defaussePropagation){
			this.piochePropagation.add(0, c);
		}
		
		this.defaussePropagation.clear();
	}

	public void resetEclosion(){
		for (Ville v: this.lesVilles){
			v.setaEclos(false);
		}
	}

	public int getCompteurEclosion() {
		return compteurEclosion;
	}
	
	public int addEclosion(){ return this.compteurEclosion++; }

	public int getIndicePropagation() {
		return indicePropagation;
	}
	
	public int[] getNiveauxPropagation(){
		return this.niveauPropagation;
	}
	
	public int getNiveauPropagation(){
		return this.niveauPropagation[this.indicePropagation];
	}

	public List<Maladie> getMaladies() {
		return maladies;
	}

	public void ajouterMaladies(List<Maladie> maladies){
		for(Maladie ma : maladies){
			this.maladies.add(ma);
		}
	}

	public int getStationsRechercheRestantes() {
		return stationsRecherche;
	}
	
	public void setStationsRecherche(int stationsRecherche) {
		this.stationsRecherche = stationsRecherche;
	}
	
	public boolean checkVictoire(){
		boolean victoire = true;
		
		for(Maladie m : maladies){
			if(!m.remedeEtabli()){
				victoire = false; //vérifier que tous les remèdes ont été établis, si oui, on déclare victoire
			}
		}
		
		return victoire;
	}

	public List<CartePropagation> getPiochePropagation() {
		return this.piochePropagation;
	}

	public void melanger(List<?> list) {
		Collections.shuffle(list);
	}

	public List<ICarteJoueur> getPiocheJoueur() {
		return this.piocheJoueur;
	}
	
	public Maladie getMaladieByNom(String nom) throws MaladieNonExistanteException {
		Maladie m = null;
		
		for(Maladie ms : this.maladies){
			if(ms.getCouleur().equals(nom)){
				m = ms;
			}
		}
		
		if(Objects.isNull(m)){
			throw new MaladieNonExistanteException();
		}
	
		return m;
	}
	
	public Ville getVilleByNom(String nom) throws VilleNonTrouveeException {
		Ville v = null;
		
		for(Ville vs : this.lesVilles){
			if(vs.getNom().equals(nom)){
				v = vs;
			}
		}
		
		if(Objects.isNull(v)){
			throw new VilleNonTrouveeException();
		}
		
		return v;
	}

	public List<CartePropagation> getDefaussePropagation() {
		return defaussePropagation;
	}

	public List<ICarteJoueur> getDefausseJoueur() {
		return defausseJoueur;
	}
	
	public void setEvntParUneNuitTranquille(boolean actif){this.evntParUneNuitTranquille = actif;}
	
	public boolean getEvntParUneNuitTranquille(){return this.evntParUneNuitTranquille; }
	
	public boolean getEvntPrevision() {
		return evntPrevision;
	}
	
	public void setEvntPrevision(boolean evntPrevision) {
		this.evntPrevision = evntPrevision;
	}
}
