package org.projetpandemic.pandemicws.modele;

import modele.FacadePandemic;
import modele.Joueur;
import modele.Partie;
import modele.actions.actionsRole.exceptions.CarteIntrouvableException;
import modele.actions.exceptions.CartePropagationPasDansDefaussePropagationException;
import modele.actions.exceptions.VillePasVoisineException;
import modele.actions.exceptions.VilleSansStationDeRechercheException;
import modele.cartes.CarteEvenement;
import modele.cartes.CarteVilleJoueur;
import modele.cartes.TypeEvenement;
import modele.exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestFacadePandemicImpl {
	
	private FacadePandemic facade;

	private static String maxime = "Maxime";
	private static String ali = "Ali";
	private static String nizar = "Nizar";
	private static String emmanuel = "Emmanuel";

	@BeforeEach
	void setUp() {
		this.facade = new FacadePandemicImpl();
	}
	
	@Test
	void testConnexion(){
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertThrows(MauvaisLoginException.class, ()->facade.connexion("ts","test"));
		Assertions.assertThrows(MauvaisLoginException.class, ()->facade.connexion(maxime,"test"));
		
		Assertions.assertDoesNotThrow(()->facade.estConnecte("Atlas"));
		Assertions.assertThrows(MauvaisLoginException.class, ()->facade.estConnecte("ts"));
	}
	
	@Test
	void testRejoindrePartie() throws NombreDeJoueursIncorrectException {
		long id = facade.creerPartie(4);
		
		//Mauvais login
		Assertions.assertThrows(MauvaisLoginException.class, ()->facade.rejoindrePartie(id, "ts"));
		//Joueur non connecté
		Assertions.assertThrows(JoueurNonConnecteException.class, ()->facade.rejoindrePartie(id, maxime));

		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.rejoindrePartie(28L, maxime));
		
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id, maxime));
		//Joueur déjà présent
		Assertions.assertThrows(JoueurDejaPresentException.class, ()->facade.rejoindrePartie(id, maxime));

		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id, ali));

		Assertions.assertDoesNotThrow(()->facade.inscription(nizar,nizar));
		Assertions.assertDoesNotThrow(()->facade.connexion(nizar,nizar));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id, nizar));

		Assertions.assertDoesNotThrow(()->facade.inscription(emmanuel,emmanuel));
		Assertions.assertDoesNotThrow(()->facade.connexion(emmanuel,emmanuel));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id, emmanuel));

		Assertions.assertDoesNotThrow(()->facade.inscription("UnAutreJoueur","test"));
		Assertions.assertDoesNotThrow(()->facade.connexion("UnAutreJoueur","test"));
		//Liste de joueurs complete
		Assertions.assertThrows(ListeJoueursCompletetException.class,()->facade.rejoindrePartie(id, "UnAutreJoueur"));
	}
	
	@Test
	void testDemarrerPartie() throws NombreDeJoueursIncorrectException {
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.demarrerPartie(28L));
		
		long id = facade.creerPartie(2);
		//Liste joueurs incomplete
		Assertions.assertThrows(ListeJoueursNonCompletetException.class, ()->facade.demarrerPartie(id));

		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//Démarrage ok
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
	}
	
	@Test
	void testGetSurPartie() throws PartieNonExistanteException, PartieNonDemarreeException, NombreDeJoueursIncorrectException {
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.getNbJoueurs(28L));
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.getPartieDemarree(28L));
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.getPartieTerminee(28L));
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.getPartieGagnee(28L));
		Assertions.assertThrows(PartieNonExistanteException.class, ()->facade.getJoueurCourant(28L));
	
		//---Préparatifs------------------
		long id = facade.creerPartie(2);

		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		Assertions.assertEquals(2, facade.getNbJoueurs(id));
		
		Assertions.assertFalse(facade.getPartieDemarree(id));
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.getPartieTerminee(id));
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.getPartieGagnee(id));
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.getJoueurCourant(id));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		Assertions.assertTrue(facade.getPartieDemarree(id));
		Assertions.assertFalse(facade.getPartieTerminee(id));
		Assertions.assertThrows(PartieNonTermineeException.class, ()->facade.getPartieGagnee(id));
		Assertions.assertDoesNotThrow(()->facade.getJoueurCourant(id));
	}
	
	@Test
	void testJoueurSuivant() throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, NombreDeJoueursIncorrectException {
		Assertions.assertThrows(PartieNonExistanteException.class, ()-> facade.joueurSuivant(28L));
	
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		Assertions.assertThrows(PartieNonDemarreeException.class, ()-> facade.joueurSuivant(id));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		Joueur j = facade.getJoueurCourant(id);
		
		Assertions.assertDoesNotThrow(()-> facade.joueurSuivant(id));
		
		Assertions.assertNotEquals(j, facade.getJoueurCourant(id));
	}

	@Test
	void testJouerActionPartagerConnaissance() throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, VilleNonTrouveeException, NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);

		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Mauvais login
		Assertions.assertThrows(MauvaisLoginException.class,()->facade.jouerActionPartagerConnaissance(28L, "j1", ali,0));
		//Joueur non connecté
		Assertions.assertThrows(JoueurNonConnecteException.class,()->facade.jouerActionPartagerConnaissance(28L, "j123",
		 ali,0));
		//Partie Non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerActionPartagerConnaissance(28L, ali, maxime,0));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class,()->facade.jouerActionPartagerConnaissance(id, ali, maxime,0));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		//Joueur n'a pas rejoins la partie
		Assertions.assertThrows(JoueurIntrouvablePartie.class,()->facade.jouerActionPartagerConnaissance(id, jx, ali,0));
		
		String jCourant = facade.getJoueurCourant(id).getPseudo();
		String jNCourant = maxime.equals(jCourant) ? ali : maxime;
		
		//Joueur non courant
		Assertions.assertThrows(JoueurNonCourantException.class,()->facade.jouerActionPartagerConnaissance(id, jNCourant, jCourant,0));
		
		facade.getJoueurCourant(id).getCartes().add(0,new CarteVilleJoueur(facade.getParties().get(id).getPlateau().getVilleByNom("Atlanta")));
		//Echange
		Assertions.assertDoesNotThrow(()->facade.jouerActionPartagerConnaissance(id, jCourant, jNCourant,0));
	}
	
	@Test
	void testJouerActionSurVille() throws PartieNonDemarreeException, VilleNonTrouveeException, StationRechercheExisteException, PartieTermineeException, NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Mauvais login
		Assertions.assertThrows(MauvaisLoginException.class,()->facade.jouerActionSurVille(28L, "j1", "test", "ville"));
		//Joueur non connecté
		Assertions.assertThrows(JoueurNonConnecteException.class,()->facade.jouerActionSurVille(28L, "j123", "test",
				"ville"));
		//Partie Non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerActionSurVille(28L, maxime, "test", "ville"));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class,()->facade.jouerActionSurVille(id, maxime,
				"test","ville"));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		//Joueur n'a pas rejoins la partie
		Assertions.assertThrows(JoueurIntrouvablePartie.class,()->facade.jouerActionSurVille(id, jx, "test", "ville"));
		
		Partie p = facade.getParties().get(id);
		String j = p.getJoueurCourant().getPseudo();
		
		//Type d'action inconnu
		Assertions.assertThrows(IllegalArgumentException.class,()->facade.jouerActionSurVille(id, j, "test","ville"));
		//Ville inconnnue
		Assertions.assertThrows(VilleNonTrouveeException.class,()->facade.jouerActionSurVille(id, j, "NAVETTE",
		"ville"));
		//Ville inconnnue
		Assertions.assertThrows(VilleNonTrouveeException.class,()->facade.jouerActionSurVille(id, j, "NAVETTE",
				"ville"));
		//Ville sans station de recherche (NAVETTE)
		Assertions.assertThrows(VilleSansStationDeRechercheException.class,()->facade.jouerActionSurVille(id, j, "NAVETTE", "Mexico"));
		
		p.setStationRecherche("Mexico");
		//Déplacement (NAVETTE)
		Assertions.assertDoesNotThrow(()->facade.jouerActionSurVille(id, j, "NAVETTE", "Mexico"));
		//Villes non voisines (VOITUREOUTRANSBORDEUR)
		Assertions.assertThrows(VillePasVoisineException.class,()->facade.jouerActionSurVille(id,j, "VOITUREOUTRANSBORDEUR", "Paris"));
		//Déplacement (VOITUREOUTRANSBORDEUR)
		Assertions.assertDoesNotThrow(()->facade.jouerActionSurVille(id,j, "VOITUREOUTRANSBORDEUR", "Miami"));
		//Station de recherche (CONSTRUIRESTATIONRECHERCHE) CARTE NON POSSEDEE
		/*Assertions.assertDoesNotThrow(()->facade.jouerActionSurVille(id,j, "CONSTRUIRESTATIONRECHERCHE",
		p.getJoueurCourant().getEmplacement().getNom()));*/
		
		//suite
	}
	
	@Test
	void testPiocherCarteJoueur() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie introuvable
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.piocherCarteJoueur(28L,"testj",1));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.piocherCarteJoueur(id, maxime,1));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		//Joueur introuvable
		Assertions.assertThrows(JoueurIntrouvablePartie.class, ()->facade.piocherCarteJoueur(id, "t1", 1));
		
		Partie p = facade.getParties().get(id);
		int nbCartesAvant = p.getJoueurByPseudo(maxime).getCartes().size();
		//OK
		//TODO : ici des fois on pioche une carte epidemie
		Assertions.assertDoesNotThrow(()->facade.piocherCarteJoueur(id, maxime,2));
		Assertions.assertEquals(nbCartesAvant+2, p.getJoueurByPseudo(maxime).getCartes().size());
	}
	
	//test defausser carte joueur
	
	@Test
	void testStationRecherche() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.ajouterStationRecherche(28L, "test"));
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.retirerStationRecherche(28L, "test"));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.ajouterStationRecherche(id, "test"));
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.retirerStationRecherche(id, "test"));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		//Ville non trouvée
		Assertions.assertThrows(VilleNonTrouveeException.class, ()->facade.ajouterStationRecherche(id, "test"));
		Assertions.assertThrows(VilleNonTrouveeException.class, ()->facade.retirerStationRecherche(id, "test"));
		//Station déjà existante
		Assertions.assertThrows(StationRechercheExisteException.class,()->facade.ajouterStationRecherche(id, "Atlanta"));
		//Pas de station existante
		Assertions.assertThrows(StationRechercheNonExistanteException.class,()->facade.retirerStationRecherche(id,
		"Tokyo"));
		//Ajout station
		Assertions.assertDoesNotThrow(()->facade.ajouterStationRecherche(id, "Tokyo"));
		//Suppression station
		Assertions.assertDoesNotThrow(()->facade.retirerStationRecherche(id, "Tokyo"));
	}
	
	@Test
	public void jouerEvenementParUneNuitTranquille() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerEvenementParUneNuitTranquille(28L, maxime, 0));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.jouerEvenementParUneNuitTranquille(id, maxime, 0));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		// Carte introuvable
		Assertions.assertThrows(CarteIntrouvableException.class,()->facade.jouerEvenementParUneNuitTranquille(id, maxime, 0));
		
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.PAR_UNE_NUIT_TRANQUILLE));
		// Joueur introuvable
		Assertions.assertThrows(JoueurIntrouvablePartie.class, ()->facade.jouerEvenementParUneNuitTranquille(id, jx, 0));
		// Jouer l'événement
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementParUneNuitTranquille(id, maxime, 0));
	}
	
	@Test
	public void jouerEvenementPontAerien() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerEvenementPontAerien(28L, maxime, 0, ali, "Tokyo"));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.jouerEvenementPontAerien(id, maxime, 0, ali, "Tokyo"));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		// Carte introuvable
		Assertions.assertThrows(CarteIntrouvableException.class,()->facade.jouerEvenementPontAerien(id, maxime, 0, ali, "Tokyo"));
		
		facade.getParties().get(id).getJoueurByPseudo(ali).getCartes().add(0, new CarteEvenement(TypeEvenement.PONT_AERIEN));
		// Joueur introuvable
		Assertions.assertThrows(JoueurIntrouvablePartie.class, ()->facade.jouerEvenementPontAerien(id, jx, 0, maxime, "Tokyo"));
		// Ville introuvable
		Assertions.assertThrows(VilleNonTrouveeException.class, ()->facade.jouerEvenementPontAerien(id, maxime, 0, ali, "Togergergkyo"));
		// Jouer l'événement
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementPontAerien(id, ali, 0, maxime, "Tokyo"));
	}
	
	@Test
	public void jouerEvenementPopulationResiliente() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerEvenementPopulationResiliente(28L, maxime, 0, 2));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.jouerEvenementPopulationResiliente(id, ali, 0, 2));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		// Carte introuvable
		Assertions.assertThrows(CarteIntrouvableException.class,()->facade.jouerEvenementPopulationResiliente(id, maxime, 0, 2));
		
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.POPULATION_RESILIENTE));
		// Joueur introuvable
		Assertions.assertThrows(JoueurIntrouvablePartie.class, ()->facade.jouerEvenementPopulationResiliente(id, jx, 0, 2));
		// Carte pas dans la pioche propagation
		Assertions.assertThrows(CartePropagationPasDansDefaussePropagationException.class, ()->facade.jouerEvenementPopulationResiliente(id, maxime, 0, 20));
		// Jouer l'événement
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementPopulationResiliente(id, maxime, 0, 2));
	}
	
	@Test
	public void jouerEvenementPrevision() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";
		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerEvenementPrevisionPhase1(28L, maxime, 0));
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerEvenementPrevisionPhase2(28L));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.jouerEvenementPrevisionPhase1(id, maxime, 0));
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.jouerEvenementPrevisionPhase2(id));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		// Carte introuvable
		Assertions.assertThrows(CarteIntrouvableException.class,()->facade.jouerEvenementPrevisionPhase1(id, maxime, 0));
		
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.PREVISION));
		// Evenement non démarré
		Assertions.assertThrows(EvenementPasEnCoursException.class, ()->facade.jouerEvenementPrevisionPhase2(id));
		// Jouer événement phase 1
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementPrevisionPhase1(id, maxime, 0));
		// Événement déjà en cours
		Assertions.assertThrows(EvenementDejaEnCoursException.class,()->facade.jouerEvenementPrevisionPhase1(id, maxime, 0));
		// Jouer événement phase 2
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementPrevisionPhase2(id));
	}
	
	@Test
	public void jouerEvenementSubventionPublique() throws NombreDeJoueursIncorrectException {
		//---Préparatifs------------------
		long id = facade.creerPartie(2);
		String jx = "jxxx";

		Assertions.assertDoesNotThrow(()->facade.inscription(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.inscription(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.inscription(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.connexion(maxime,maxime));
		Assertions.assertDoesNotThrow(()->facade.connexion(ali,ali));
		Assertions.assertDoesNotThrow(()->facade.connexion(jx,jx));

		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,maxime));
		Assertions.assertDoesNotThrow(()->facade.rejoindrePartie(id,ali));
		//--------------------------------
		
		//Partie non existante
		Assertions.assertThrows(PartieNonExistanteException.class,()->facade.jouerEvenementSubventionPublique(28L, maxime, 0, "Tokyo"));
		//Partie non démarrée
		Assertions.assertThrows(PartieNonDemarreeException.class, ()->facade.jouerEvenementSubventionPublique(id, maxime, 0, "Tokyo"));
		
		//---Démarrage-----
		Assertions.assertDoesNotThrow(()->facade.demarrerPartie(id));
		//-----------------
		
		// Carte introuvable
		Assertions.assertThrows(CarteIntrouvableException.class,()->facade.jouerEvenementSubventionPublique(id, maxime, 0,"Tokyo"));
		
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.SUBVENTION_PUBLIQUE));
		// Ville introuvable
		Assertions.assertThrows(VilleNonTrouveeException.class, ()->facade.jouerEvenementSubventionPublique(id, maxime, 0, "erufgheiurhg"));
		// Jouer événements normal
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementSubventionPublique(id,maxime,0,"Tokyo"));
		// La ville n'avait pas de station de recherche à enlever
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.SUBVENTION_PUBLIQUE));
		Assertions.assertThrows(StationRechercheNonExistanteException.class, ()->facade.jouerEvenementSubventionPublique2(id,maxime,0,"Paris","Londres"));
		// La ville de destination a déjà une station de recherche
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.SUBVENTION_PUBLIQUE));
		Assertions.assertThrows(StationRechercheExisteException.class, ()->facade.jouerEvenementSubventionPublique2(id,maxime,0,"Tokyo","Atlanta"));
		// Echange de station de recherche OK
		facade.getParties().get(id).getJoueurByPseudo(maxime).getCartes().add(0, new CarteEvenement(TypeEvenement.SUBVENTION_PUBLIQUE));
		Assertions.assertDoesNotThrow(()->facade.jouerEvenementSubventionPublique2(id,maxime,0,"Londres","Tokyo"));
	}
}