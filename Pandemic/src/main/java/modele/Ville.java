package modele;

import modele.exceptions.*;

import java.util.*;

public class Ville {
	private Plateau plateau;
	private final String nom; 													//Le nom de la ville
	private final int population; 												//permettra aussi de définir le joueur qui commence
	private boolean stationDeRecherche; 										//si une station de recherche est posée sur cette ville
	private boolean aEclos;
	private Map<Maladie, Integer> niveauxMaladies; 								//nombre de cube d'une maladie
	private final Maladie maladieParDefaut; 									//permet de définir les cubes à utiliser pour les propagations
	private List<Ville> villesAlentours;										//liste des villes voisines (déplacements et éclosions)
	private float[] coordonnees = new float[2];									//permettra de placer la ville sur le plateau visuel
	private Set <Joueur > joueursDansLaVille;                                   //les joueurs se trouvant dans une ville

	/**
	 * Créé une ville avec un nom, un nombre d'habitants et la maladie par défaut
	 * @param nom Le nom de la ville
	 * @param population le nombre d'habitants
	 * @param maladieParDefaut la maladie par défaut (détermine le niveau de maladie à augmenter lors d'une
	 * propagation et eclosion)
	 */
	public Ville(String nom, int population, Maladie maladieParDefaut){
		this.nom = nom;
		this.population = population;
		this.stationDeRecherche = false;
		this.niveauxMaladies = new HashMap<>();
		this.villesAlentours = new ArrayList<>();
		this.maladieParDefaut = maladieParDefaut;
		this.joueursDansLaVille = new HashSet<>();
		this.aEclos = false;
	}

	public void setPlateau(Plateau plateau){this.plateau = plateau;};

	public boolean isaEclos() {
		return aEclos;
	}

	public void setaEclos(boolean aEclos) {
		this.aEclos = aEclos;
	}

	/**
	 * Créé une ville avec un nom, un nombre d'habitants et la maladie par défaut
	 * @param nom Le nom de la ville
	 * @param population le nombre d'habitants
	 * @param maladieParDefaut la maladie par défaut (détermine le niveau de maladie à augmenter lors d'une
	 * propagation et eclosion)
	 * @param x la coordonné x pour la placement de la ville sur le plateau
	 * @param y la coordonné y pour le placement de la ville sur le plateau
	 */
	public Ville(String nom, int population, Maladie maladieParDefaut, float x, float y){
		this(nom, population, maladieParDefaut);
		this.coordonnees[0] = x;
		this.coordonnees[1] = y;
	}
	
	/**
	 * Méthode permettant de récupérer le nom de la ville
	 * @return le nom de la ville en chaine de caractères
	 */
	public String getNom() {
		return nom;
	}
	
	/**
	 * Méthode permettant de récupérer le nombre d'habitant de la ville
	 * @return le nombre d'habitant de la ville
	 */
	public int getPopulation() {
		return population;
	}
	
	/**
	 * Méthode utilisée pour savoir si une station de recherche a été construite dans la ville
	 * @return un booleen (true si une station est présente, false sinon)
	 */
	public boolean hasStationDeRecherche() {
		return stationDeRecherche;
	}
	
	/**
	 * Méthode utilisée pour définir si une station de recherche a été construite dans la ville ou si elle a été
	 * déplacée
	 * @param stationDeRecherche true si une station de recherche est présente, false sinon
	 */
	public void setStationDeRecherche(boolean stationDeRecherche) {
		this.stationDeRecherche = stationDeRecherche;
	}
	
	/**
	 * Méthode utilisée lors de l'effet d'une carte propagation
	 * @param niveau niveau de propagation actuel
	 * @throws PlusDeCubesMaladieDisponible il n'y a plus de cubes de la maladie en réserve
	 */
	public void propagation(int niveau) throws PlusDeCubesMaladieDisponible {
		this.addNiveauMaladie(this.getMaladieParDefaut(), niveau);
	}
	
	/**
	 * Méthode permettant d'ajouter un certains nombre de niveau à une maladie de la ville tant qu'il y'a pas de médecin dans cette ville et de remede pour cette maladie
	 * @param m la maladie à laquelle il faut ajouter des niveau (cubes)
	 * @param quantite le nombre de niveau à ajouter (si c'est une propagation et qu'elle est au niveau 2, alors
	 * quantité = 2)
	 */
	public void addNiveauMaladie(Maladie m, int quantite) throws PlusDeCubesMaladieDisponible {
		if(!this.niveauxMaladies.containsKey(m)){
			this.niveauxMaladies.put(m,0);
		}
		
		int niveau = this.niveauxMaladies.get(m)+quantite;

		boolean isMedecin= false;
		boolean isSpeMiseQuarantaine = false;

		for ( Joueur joueur : this.joueursDansLaVille) {
			if ( joueur.getRole().equals(Role.MEDECIN)) {
				isMedecin=true;
			}
			else if(joueur.getRole().equals(Role.SPE_MISE_EN_QUARANTAINE)){
				isSpeMiseQuarantaine=true;
			}
		}
		
		if(!isSpeMiseQuarantaine){
			for(Ville vs : this.villesAlentours){
				Set<Joueur> joueurs = vs.getJoueursDansLaVille();
				for(Joueur js : joueurs){
					if(js.getRole().equals(Role.SPE_MISE_EN_QUARANTAINE)){
						isSpeMiseQuarantaine=true;
					}
				}
			}
		}

		if ( (!isMedecin || !m.getRemede()) && !isSpeMiseQuarantaine) {
			if(niveau > 3){
				this.eclosion(m);
				m.retirerCubes(3-this.niveauxMaladies.get(m));
				this.niveauxMaladies.replace(m, 3);
			}
			else
			{	m.retirerCubes(quantite);
				this.niveauxMaladies.replace(m, niveau);
			}
	}}
	
	/**
	 * Méthode permettant de retirer un niveau à une maladie de la ville
	 * @param m la maladie à laquelle il faut ajouter des niveau (cubes)
	 * @param quantite le nombre de cube à enlever
	 * @throws MaladiesNonIntialiseesException la map des maladies ne contient pas la maladie en question (elle doit
	 * être initialisée en y ajoutant un niveau)
	 * @throws VillePasAssezDeMaladieException la quantité est supérieur au nombre de cubes de maladie sur la ville
	 */
	public void removeNiveauMaladie(Maladie m, int quantite) throws MaladiesNonIntialiseesException, VillePasAssezDeMaladieException, MaladieEradiqueException {
		if(!niveauxMaladies.containsKey(m)){
			throw new MaladiesNonIntialiseesException();
		}
		
		int niveau = this.niveauxMaladies.get(m)-quantite;
		
		if(niveau >= 0){
			m.ajouterCubes(quantite);
			this.niveauxMaladies.replace(m, niveau);
		}
		else
		{
			throw new VillePasAssezDeMaladieException();
		}
	}
	
	/**
	 * Méthode permettant de récupérer le niveau d'une maladie de la ville
	 * @param m la maladie dont on souhaite connaitre le niveau
	 * @return un nombre compris entre 0 et 3
	 * @throws MaladiesNonIntialiseesException la map des maladies ne contient pas la maladie en question (elle doit
	 * être initialisée en y ajoutant un niveau)
	 */
	public int getNiveauMaladie(Maladie m) throws MaladiesNonIntialiseesException {
		if(!niveauxMaladies.containsKey(m)){
			throw new MaladiesNonIntialiseesException();
		}
		return this.niveauxMaladies.get(m);
	}
	
	/**
	 * Méthode permettant, quand une éclosion a lieu dans la ville, d'ajouter un niveau de maladie aux
	 * villes voisines avec la maladie qui a causé l'éclosion
	 * @param md la maladie ayant causé l'éclosion
	 * @throws PlusDeCubesMaladieDisponible plus de cubes de la maladie en réserve
	 */
	public void eclosion(Maladie md) throws PlusDeCubesMaladieDisponible {
			this.aEclos = true;
			this.plateau.addEclosion();
			for(Ville ville : this.villesAlentours){
				if(! ville.isaEclos()){
					ville.addNiveauMaladie(md, 1);
				}
			}
	}
	
	/**
	 * Méthode permettant de retourner la maladie par défaut de la ville (utilisée pour savoir quel niveau de maladie
	 *  incrémenter lors d'une propagation / éclosion)
	 * @return un objet de type Maladie correspondant à la maladie par défaut de la ville
	 */
	public Maladie getMaladieParDefaut() {
		return maladieParDefaut;
	}
	
	/**
	 * Méthode permettant de définir les villes qui sont "reliées" à cette ville (utilisé pour les éclosions et les
	 * déplacements des joueurs)
	 * @param v autant de paramètres Ville que souhaités
	 * @throws VilleVoisineAElleMemeException il est impossible d'avoir une ville voisine à elle-même
	 * @throws VilleDoublonVoisinException ne pas mettre deux fois la même ville dans les voisins
	 */
	public void ajouterVillesAlentours(Ville ...v) throws VilleVoisineAElleMemeException, VilleDoublonVoisinException {
		for(Ville vi : v){
			if(!vi.equals(this)) {
				if(!this.villesAlentours.contains(vi)) {
					this.villesAlentours.add(vi);
				}
				else
				{
					throw new VilleDoublonVoisinException();
				}
			}
			else
			{
				throw new VilleVoisineAElleMemeException();
			}
		}
	}
	
	/**
	 * Méthode simpliste permettant de retourner le nom de toutes les villes
	 * voisines à une certaine ville
	 * @return une chaine de caractères contenant le nom de toutes les villes voisines (séparés par des "/")
	 */
	public String getVillesAlentoursToString(){
		String result = "";
		
		for(Ville v : this.villesAlentours){
			result+=v.toString()+" / ";
		}
		
		return result;
	}
	
	/** On rajoute un joueur aux joueurs se trouvant dans une ville
	 */
	public void rajouterJoueur(Joueur j){
		this.joueursDansLaVille.add(j);
	}
	
	/** On enlève un joueur aux joueurs se trouvant dans une liste.
	 */
	public void enleverJoueur(Joueur j){
		if (joueursDansLaVille.contains(j))
			this.joueursDansLaVille.remove(j);
	}
	
	/** Pour obtenir les joueurs se trouvant dans une ville.
	 */
	public Set<Joueur> getJoueursDansLaVille() {
		return joueursDansLaVille;
	}
	
	/** Pour obtenir les villes alentours aux alentours d(une ville.
	 */
	public List<Ville> getVillesAlentours() {
		return villesAlentours;
	}
	
	/**
	 * Méthode de transformation de l'objet en chaine de caractère
	 * @return le nom de la ville ainsi que la maladie par défaut
	 */
	@Override
	public String toString() {
		return this.nom+" - Maladie par défaut : "+this.maladieParDefaut.toString();
	}

	public Map<Maladie, Integer> getNiveauxMaladies() {
		return niveauxMaladies;
	}

	public void setNiveauxMaladies(Map<Maladie, Integer> niveauxMaladies) {
		this.niveauxMaladies = niveauxMaladies;
	}

	public void removeNiveauMaladieGuerie(Maladie m) throws PlusDeCubesMaladieDisponible, MaladiesNonIntialiseesException {
		if(!niveauxMaladies.containsKey(m)){
			throw new MaladiesNonIntialiseesException();
		}

		int encienNiveau = this.niveauxMaladies.get(m);
		m.retirerCubes(encienNiveau);

		int niveau = 0;
		this.niveauxMaladies.replace(m,niveau);


	}

}
