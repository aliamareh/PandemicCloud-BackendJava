package modele;

import modele.actions.TypeAction;
import modele.actions.actionsRole.exceptions.*;
import modele.actions.exceptions.*;
import modele.cartes.CarteEvenement;
import modele.cartes.ICarteJoueur;
import modele.exceptions.*;

import java.util.List;
import java.util.Map;

public interface FacadePandemic {
	/**
	 * Connexion d'une personne pour par la suite le faire joindre une partie
	 * @param pseudo chaine de caractère pour identifier le futur joueur
	 * @throws MauvaisLoginException pseudo ou password incorrect
	 * @throws LoginDejaUtiliseException un pseudo est unique donc on ne peut avoir plusieurs fois le même dans la
	 * facade
	 */
	void connexion(String pseudo,String password) throws MauvaisLoginException, LoginDejaUtiliseException;

	/**
	 * return une map contenant le pseudo et le password de l'utilisateur
	 * qui sont enregistrés dans la bd
	 * @throws MauvaisLoginException le pseudo ne correspond a aucun utilisateur
	 */
	Map<String,String> getUserByPseudo(String pseudo) throws MauvaisLoginException;

	/**
	 * inscription d'une personne
	 * @param pseudo chaine de caractère pour identifier le futur joueur
	 * @throws MauvaisLoginException le pseudo doit faire au moins 3 caractères
	 * @throws LoginDejaUtiliseException un pseudo est unique donc on ne peut avoir plusieurs fois le même dans la
	 * facade
	 */
	void inscription(String pseudo,String password) throws MauvaisLoginException, LoginDejaUtiliseException;

	/**
	 * deconnexion d'une personne
	 * @throws LoginDejaUtiliseException pas de joueur connecté avec le pseudo
	 */
	void deconnexion(String pseudo) throws MauvaisLoginException;

	/**
	 * Savoir si le joueur est bien enregistré dans la facade
	 * @param pseudo chaine de caracètre pour identifier le joueur
	 * @return true si le joueur est bien connecté, false sinon
	 * @throws MauvaisLoginException le pseudo doit être de 3 caractères minimum
	 */
	boolean estConnecte(String pseudo) throws MauvaisLoginException;

	/**
	 * retourne la liste des joueurs ayant rejoin la partie
	 * @throws PartieNonExistanteException l'identifiant de la partie fourni ne correspond à aucune partie
	 */
	List<String> getJoueursConnectes(long idPartie) throws PartieNonExistanteException;
	
	/**
	 * Création d'une partie
	 * @param nbJoueurs le nombre de joueurs nécessaire au lancement de la partie
	 * @return l'id de la partie créée
	 * @throws NombreDeJoueursIncorrectException le nombre de joueurs doit être compris entre 2 et 4
	 */
	long creerPartie(int nbJoueurs) throws NombreDeJoueursIncorrectException;
	
	/**
	 * Permet à un joueur de rejoindre une partie créée précédemment
	 * @param idPartie l'id de la partie à rejoindre
	 * @param j le pseudo du joueur
	 * @throws MauvaisLoginException le pseudo doit faire au minimum 3 caractères
	 * @throws JoueurNonConnecteException le joueur doit s'être connecté auparavant (méthode connexion())
	 * @throws PartieNonExistanteException l'identifiant de la partie fourni ne correspond à aucune partie démarrée actuellement
	 * @throws JoueurDejaPresentException ce joueur est déjà connecté à cette partie
	 * @throws ListeJoueursCompletetException la partie contient déjà le nombre de joueurs requis pour jouer.
	 */
	void rejoindrePartie(long idPartie, String j) throws MauvaisLoginException, JoueurNonConnecteException, PartieNonExistanteException, JoueurDejaPresentException, ListeJoueursCompletetException;
	
	/**
	 * Permet de faire toutes les vérifications pour savoir si la partie est en cours
	 * @param idPartie l'identifiant de la partie
	 * @throws PartieNonExistanteException l'identifiant de la partie ne correspond à aucune partie créée
	 * @throws PartieTermineeException la partie est terminée
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 */
	void verifierPartieEnCours(long idPartie) throws PartieNonExistanteException, PartieTermineeException, PartieNonDemarreeException;
	
	/**
	 * Permet de faire toutes les vérifications pour savoir si un joueur peut effectuer une action dans la partie
	 * @param j le nom du joueur
	 * @param idPartie l'identifiant de la partie
	 * @throws MauvaisLoginException le pseudo doit faire minimum 3 caractères
	 * @throws JoueurNonConnecteException ce joueur n'est pas connecté
	 * @throws PartieNonExistanteException l'identifiant de la partie fourni ne correspond pas à une partie existante
	 * @throws JoueurIntrouvablePartie ce joueur n'est pas présent dans la partie
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarrée
	 * @throws PartieTermineeException la partie est terminée et il n'est plus possible d'effectuer d'action
	 * @throws JoueurNonCourantException ce n'est pas au tour de ce joueur de jouer
	 */
	void verifierSiPeutJouer(String j, long idPartie) throws MauvaisLoginException, JoueurNonConnecteException,
            PartieNonExistanteException, JoueurIntrouvablePartie, PartieNonDemarreeException, PartieTermineeException, JoueurNonCourantException;
	
	/**
	 * Méthode de jeu pour effectuer une action contre une maladie
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joeur qui effectue l'action
	 * @param maladie le nom de la maladie contre laquelle le joueur veut effecuter une action
	 * @param typeAction la chaine de caractères identifiant le type d'action
	 * @throws PartieNonExistanteException l'identifiant de la partie fourni ne correspond pas à une partie existante
	 * @throws JoueurIntrouvablePartie ce joueur n'est pas présent dans la partie
	 * @throws MaladieNonExistanteException le nom de maladie fourni n'a pas permi d'identifier une maladie existante
	 */
	void jouerActionContreMaladie(long idPartie, String j, String maladie, List<Integer> cartesVillesJoueur,
	 String typeAction) throws PartieNonExistanteException, JoueurIntrouvablePartie, MaladieNonExistanteException, PlusDeCubesMaladieDisponible, PiocheJoueurVideException, PartieNonDemarreeException, JoueurNonConnecteException, JoueurNonCourantException, PartieTermineeException, MauvaisLoginException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException;
	
	/**
	 * Méthode de jeu pour effectuer une action sur une ville précise
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui effectue l'action
	 * @param typeAction la chaine de caractères pour le type d'action à effectuer
	 * @param ville le nom de la ville sur laquelle sera effectuée l'action
	 * @throws PartieNonExistanteException l'identifiant de la partie fourni ne correspond pas à une partie existante
	 * @throws JoueurIntrouvablePartie ce joueur n'est pas présent dans la partie
	 */
	void jouerActionSurVille(long idPartie, String j, String typeAction, String ville) throws PartieNonExistanteException, JoueurIntrouvablePartie, PlusDeCubesMaladieDisponible, PiocheJoueurVideException, PartieNonDemarreeException, JoueurNonConnecteException, JoueurNonCourantException, PartieTermineeException, MauvaisLoginException, VilleNonTrouveeException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException;
		
	/**
	 * Méthode de jeu pour effectuer l'action de partager des connaissances avec un autre joueur
	 * @param idPartie l'identifiant de la partie
	 * @param j1 le nom du joueur à qui c'est le tour qui souhaite partager des connaissances
	 * @param j2 le nom du joueur avec qui j1 souhaite partager des connaissances
	 * @param carte la position de la carte dans la main du joueur j1
	 * @throws PartieNonExistanteException l'identifiant de la partie fourni ne correspond pas à une partie existante
	 * @throws JoueurIntrouvablePartie ce joueur n'est pas présent dans la partie
	 */
	void jouerActionPartagerConnaissance(long idPartie, String j1, String j2, int carte) throws PartieNonExistanteException, JoueurIntrouvablePartie, PlusDeCubesMaladieDisponible, PiocheJoueurVideException, MauvaisLoginException, JoueurNonConnecteException, PartieNonDemarreeException, JoueurNonCourantException, PartieTermineeException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException;
	
	/**
	 * Permet de savoir si une partie est démarrée
	 * @param idPartie l'identifiant de la partie
	 * @return true si la partie a démarrée, false sinon
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 */
	boolean getPartieDemarree(long idPartie) throws PartieNonExistanteException;
	
	/**
	 * Lancer une partie
	 * @param idPartie l'identifiant de la partie
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws ListeJoueursNonCompletetException le nombre de joueurs connectés n'est pas égal au nombre spécifié à
	 * la création de la partie
	 * @throws PartieDejaDemarreeException la partie a déjà été lancée
	 * @throws PiocheJoueurVideException sera levée si les données ont mal été initialisées
	 * @throws VilleNonTrouveeException sera levée si les données ont mal été initialisées
	 * @throws VilleMaladiesDejaInitialiseesException sera levée si les données ont mal été initialisées
	 * @throws StationRechercheExisteException sera levée si les données ont mal été initialisées
	 * @throws VilleVoisineAElleMemeException sera levée si les données ont mal été initialisées
	 * @throws VilleDoublonVoisinException sera levée si les données ont mal été initialisées
	 */
	void demarrerPartie(long idPartie) throws PartieNonExistanteException, PartieTermineeException, ListeJoueursNonCompletetException, PiocheJoueurVideException, VilleNonTrouveeException, VilleMaladiesDejaInitialiseesException, StationRechercheExisteException, VilleVoisineAElleMemeException, VilleDoublonVoisinException, PartieDejaDemarreeException, PlusDeCubesMaladieDisponible;
	
	/**
	 * Renvoie le joueur courant de la partie
	 * @param idPartie l'identifiant de la partie
	 * @return un objet Joueur correspondant au joueur courant
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarrée et n'a donc pas encore de joueur courant
	 * @throws PartieTermineeException la partie est terminée
	 */
	Joueur getJoueurCourant(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException;
	
	/**
	 * Renvoie le nombre de joueurs prévu pour la partie
	 * @param idPartie l'identifiant de la partie
	 * @return un entier entre 2 et 4
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 */
	int getNbJoueurs(long idPartie) throws PartieNonExistanteException;
	
	/**
	 * Permet de savoir si une partie est terminée
	 * @param idPartie l'identifiant de la partie
	 * @return true si la partie est terminée, false sinon
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarrée
	 */
	boolean getPartieTerminee(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException;
	
	/**
	 * Permet de savoir si la partie a été gagnée
	 * @param idPartie l'identifiant de la partie
	 * @return true si la partie a été gagnée, false sinon
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarrée
	 * @throws PartieNonTermineeException la partie n'est pas finie
	 */
	boolean getPartieGagnee(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException, PartieNonTermineeException;
	
	/**
	 * Permet de passer au tour du joueur suivant
	 * @param idPartie l'identifiant de la partie
	 * @return le nouveau joueur courant
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond pas à une partie existante
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarrée
	 * @throws PartieTermineeException la partie est déjà finie
	 * @throws NombresDeCartesMaxAtteindsException le joueur courant actuel a plus de 7 cartes en main
	 */
	Joueur joueurSuivant(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException,NombresDeCartesMaxAtteindsException;
	
	/**
	 * Retourne les parties créées
	 * @return une map des identifiants et des parties correspondantes
	 */
	Map<Long, Partie> getParties();
	
	/**
	 * Permet de retirer des cartes de la pioche "joueur" et de les ajouter à la main d'un joueur
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui recevra les cartes
	 * @param nbCartes le nombre de cartes à piocher
	 * @return la liste des cartes piochées
	 * @throws PartieNonExistanteException l'identifiant de partie ne correspond à aucune partie créée
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarrée
	 * @throws PartieTermineeException la partie est finie
	 * @throws PiocheJoueurVideException il n'y a plus de cartes dans la pioche "joueur"
	 * @throws JoueurIntrouvablePartie le joueur n'a pas été trouvé dans cette partie
	 */
	List<ICarteJoueur> piocherCarteJoueur(long idPartie, String j, int nbCartes) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException, PiocheJoueurVideException, JoueurIntrouvablePartie, NombresDeCartesMaxAtteindsException, PlusDeCubesMaladieDisponible;
	
	/**
	 * Permet de retirer des cartes de la main d'un joueur et de les ajouter à la défausse "joueur"
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur à qui on prendra les cartes
	 * @param index les identifiants de cartes à défausser
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie créée
	 * @throws PartieNonDemarreeException la parti n'a pas encore démarré
	 * @throws PartieTermineeException la partie est fini
	 */
	void defausserCarteJoueur(long idPartie, String j, Integer ...index) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException, MauvaisLoginException, JoueurIntrouvablePartie;
	
	/**
	 * Permet d'ajouter une station de recherche à une ville
	 * @param idPartie l'identifiant de la partie
	 * @param ville le nom de la ville
	 * @throws PartieNonDemarreeException
	 * @throws PartieNonExistanteException
	 * @throws PartieTermineeException
	 * @throws VilleNonTrouveeException
	 * @throws StationRechercheExisteException
	 */
	void ajouterStationRecherche(long idPartie, String ville) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, VilleNonTrouveeException, StationRechercheExisteException;
	
	/**
	 * Permet de retirer une station de recherche d'une ville
	 * @param idPartie l'identifiant de la partie
	 * @param ville le nom de la ville de laquelle retirer la station de recherche
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie existante
	 * @throws PartieTermineeException la partie est terminée
	 * @throws VilleNonTrouveeException la ville donnée n'existe pas
	 * @throws StationRechercheNonExistanteException il n'y a pas de station de recherche sur la ville donnée
	 */
	void retirerStationRecherche(long idPartie, String ville) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, VilleNonTrouveeException, StationRechercheNonExistanteException;
	
	/**
	 * Permet de jouer l'événement "Par une nuit tranquille" de la main du joueur
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur
	 * @param positioncarte l'index de la carte dans la main du joueur
	 * @throws PartieNonExistanteException l'identifiant founi n'a trouvé aucune partie existante
	 * @throws JoueurIntrouvablePartie aucun joueur portant ce nom n'a été trouvé dans la partie spécifiée
	 * @throws PartieNonDemarreeException la partie spécifiée n'a pas encore démarré
	 * @throws PartieTermineeException la partie spécifiée est déjà terminée
	 * @throws CarteIntrouvableException la carte demandée n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes du joueur ou la carte n'est pas une carte événement ou la carte événément n'est pas "ParUneNuitTranquille")
	 */
	void jouerEvenementParUneNuitTranquille(long idPartie, String j, int positioncarte) throws PartieNonExistanteException, JoueurIntrouvablePartie, PartieNonDemarreeException, PartieTermineeException, CarteIntrouvableException;
	
	/**
	 * Permet de jouer l'événement "Pont Aérien" de la main du joueur
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui joue la carte
	 * @param positioncarte l'index de la carte dans la main du joueur
	 * @param joueuradeplacer le nom du joueur qui sera déplacé
	 * @param destination le nom de la ville sur laquelle déplacer le joueur
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie en existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws JoueurIntrouvablePartie le joueur effectuant l'action et/ou le joueur à déplacer n'a pas été identifié dans la partie
	 * @throws VilleNonTrouveeException la nom de ville ne correspond à aucune ville existante
	 * @throws CarteIntrouvableException la carte demandée n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes du joueur ou la carte n'est pas une carte événement ou la carte événément n'est pas "PontAerien")
	 */
	void jouerEvenementPontAerien(long idPartie, String j, int positioncarte, String joueuradeplacer, String destination) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, VilleNonTrouveeException, CarteIntrouvableException;
	
	/**
	 * Permet de jouer l'événement "Population Resiliente" de la main du joueur
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui joue la carte
	 * @param positioncarte l'index de la carte dans la main du joueur
	 * @param cartepropagation l'index de la carte propagation a supprimer de la défausse
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie en existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws JoueurIntrouvablePartie le joueur effectuant l'action n'a pas été identifié dans la partie
	 * @throws CarteIntrouvableException la carte demandée n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes du joueur ou la carte n'est pas une carte événement ou la carte événément n'est pas "PontAerien")
	 * @throws CartePropagationPasDansDefaussePropagationException la carte propagation n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes de la défausse propagation)
	 */
	void jouerEvenementPopulationResiliente(long idPartie, String j, int positioncarte, int cartepropagation) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, CarteIntrouvableException, CartePropagationPasDansDefaussePropagationException;
	
	/**
	 * Permet de jouer la première phase l'événement "Prévision" (découvrir les 6 premières cartes de la pioche propagation)
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui joue la carte
	 * @param positioncarte l'index de la carte dans la main du joueur
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie en existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws JoueurIntrouvablePartie le joueur effectuant l'action n'a pas été identifié dans la partie
	 * @throws CarteIntrouvableException la carte demandée n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes du joueur ou la carte n'est pas une carte événement ou la carte événément n'est pas "PontAerien")
	 * @throws PiochePropagationVideException La pioche propagation est vide
	 * @throws EvenementDejaEnCoursException l'événement a déjà été entammé et n'est pas terminé (phase 2 non faite)
	 */
	void jouerEvenementPrevisionPhase1(long idPartie, String j, int positioncarte) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, CarteIntrouvableException, PiochePropagationVideException, EvenementDejaEnCoursException;
	
	/**
	 * Permet de jouer la seconde phase l'événement "Prévision" (replacer les 6 cartes sur la pioche)
	 * @param idPartie l'identifiant de la partie
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie en existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws EvenementPasEnCoursException l'événement n'a pas encore débuté (phase 1 non exécutée)
	 */
	void jouerEvenementPrevisionPhase2(long idPartie) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, EvenementPasEnCoursException;
	
	/**
	 * Permet de jouer l'événement "Subvention publique" de la main du joueur quand il reste des stations de recherches à disposition
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui joue la carte
	 * @param positioncarte l'index de la carte dans la main du joueur
	 * @param villeChoisie le nom de la ville sur laquelle placer une station de recheche
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie en existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws JoueurIntrouvablePartie le joueur effectuant l'action n'a pas été identifié dans la partie
	 * @throws VilleNonTrouveeException la nom fourni ne correspond à aucune ville du plateau
	 * @throws CarteIntrouvableException la carte demandée n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes du joueur ou la carte n'est pas une carte événement ou la carte événément n'est pas "SubventionPublique")
	 * @throws NombreDeStationsDeRecherches6AtteindsException pour ajouter une station de recherche à une ville quand les 6 sont déjà utilisées, veuillez utiliser la méthode n°2
	 * @throws StationRechercheExisteException il y a déjà une station de recherche sur cette ville
	 */
	void jouerEvenementSubventionPublique(long idPartie, String j, int positioncarte, String villeChoisie) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, VilleNonTrouveeException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, StationRechercheExisteException;

	/**
	 * Permet de jouer l'événement "Subvention publique" de la main du joueur quand il ne reste plus de stations de recherches à disposition
	 * @param idPartie l'identifiant de la partie
	 * @param j le nom du joueur qui joue la carte
	 * @param positioncarte l'index de la carte dans la main du joueur
	 * @param villeChoisie le nom de la ville sur laquelle placer une station de recheche
	 * @param villeaenlever le nom de la ville de laquelle on récupère la station de recherche
	 * @throws PartieNonDemarreeException la partie n'a pas encore démarré
	 * @throws PartieNonExistanteException l'identifiant fourni ne correspond à aucune partie en existante
	 * @throws PartieTermineeException la partie est déjà terminée
	 * @throws JoueurIntrouvablePartie le joueur effectuant l'action n'a pas été identifié dans la partie
	 * @throws VilleNonTrouveeException la nom fourni ne correspond à aucune ville du plateau
	 * @throws CarteIntrouvableException la carte demandée n'a pas été trouvée (l'identifiant < 0 ou > nombre de cartes du joueur ou la carte n'est pas une carte événement ou la carte événément n'est pas "SubventionPublique")
	 * @throws StationRechercheExisteException une station de recherche existe déjà sur la ville choisie
	 * @throws StationRechercheNonExistanteException la ville de laquelle on souhaite récuperer la station de recherche n'en possède pas
	 */
	void jouerEvenementSubventionPublique2(long idPartie, String j, int positioncarte, String villeChoisie, String villeaenlever) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, VilleNonTrouveeException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, StationRechercheExisteException, StationRechercheNonExistanteException;
	
	/**
	 * Action du role du médecin
	 * @param idPartie l'id de la partie
	 * @param j le nom du joueur étant médecin
	 * @throws PlusDeCubesMaladieDisponible
	 * @throws PartieNonExistanteException
	 * @throws PartieNonDemarreeException
	 * @throws JoueurIntrouvablePartie
	 * @throws MaladieEradiqueException
	 * @throws PartieTermineeException
	 * @throws MaladiesNonIntialiseesException
	 * @throws VilleNonTrouveeException
	 * @throws NombreDeStationsDeRecherches6AtteindsException
	 * @throws VilleSansCetteMaladieException
	 * @throws StationRechercheExisteException
	 * @throws CarteVilleNonPossedeException
	 * @throws VillePasAssezDeMaladieException
	 * @throws NombreDeStationsDeRecherchesMaxDépasséException
	 * @throws RolePasExpertAuxOperationsException
	 * @throws VillePasVoisineException
	 * @throws CarteIntrouvableException
	 * @throws RolePasPlanificateurUrgenceException
	 * @throws RolePasMedecinException
	 * @throws PartageConnaissancesException
	 * @throws PlanificateurUrgenceCarteDejaPriseException
	 * @throws VilleSansStationDeRechercheException
	 * @throws NombresDeCartesMaxAtteindsException
	 */
	void jouerActionRetirerCubeParMedecin(long idPartie, String j) throws JoueurIntrouvablePartie, PartieNonExistanteException, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException;
	
	/**
	 * Action du role répartiteur
	 * @param idPartie l'id de la partie
	 * @param jcourant le nom du joueur courant
	 * @param jcontrole le nom du joueur controlé
	 * @param typeAction le type d'action à faire effectuer
	 * @param villeDest le nom de la ville de destination
	 * @throws PartieNonExistanteException
	 * @throws JoueurIntrouvablePartie
	 * @throws PlusDeCubesMaladieDisponible
	 * @throws PartieNonDemarreeException
	 * @throws MaladieEradiqueException
	 * @throws PartieTermineeException
	 * @throws MaladiesNonIntialiseesException
	 * @throws RolePasRepartiteurException
	 * @throws VilleNonTrouveeException
	 * @throws NombreDeStationsDeRecherches6AtteindsException
	 * @throws VilleSansCetteMaladieException
	 * @throws StationRechercheExisteException
	 * @throws CarteVilleNonPossedeException
	 * @throws VillePasAssezDeMaladieException
	 * @throws NombreDeStationsDeRecherchesMaxDépasséException
	 * @throws RolePasExpertAuxOperationsException
	 * @throws VillePasVoisineException
	 * @throws CarteIntrouvableException
	 * @throws RolePasPlanificateurUrgenceException
	 * @throws RolePasMedecinException
	 * @throws PartageConnaissancesException
	 * @throws PlanificateurUrgenceCarteDejaPriseException
	 * @throws VilleSansStationDeRechercheException
	 * @throws NombresDeCartesMaxAtteindsException
	 */
	void jouerActionDeplacerPionParRepartiteur(long idPartie, String jcourant, String jcontrole, String typeAction, String villeDest) throws
			JoueurIntrouvablePartie, PartieNonExistanteException, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, RolePasRepartiteurException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException;
	
	/**
	 * Action du role répartiteur
	 * @param idPartie l'id de la partie
	 * @param joueurC le nom du joueur courant
	 * @param joueurADeplacer le nom du joueur à déplacer
	 * @param joueurARejoindre le nom du joueur à rejoindre (par le joueur à déplacer)
	 * @throws PartieNonExistanteException
	 * @throws JoueurIntrouvablePartie
	 * @throws PlusDeCubesMaladieDisponible
	 * @throws PartieNonDemarreeException
	 * @throws MaladieEradiqueException
	 * @throws PartieTermineeException
	 * @throws MaladiesNonIntialiseesException
	 * @throws NombresDeCartesMaxAtteindsException
	 * @throws RolePasRepartiteurException
	 * @throws JoueurNonCourantException
	 * @throws VilleNonTrouveeException
	 * @throws NombreDeStationsDeRecherches6AtteindsException
	 * @throws VilleSansCetteMaladieException
	 * @throws StationRechercheExisteException
	 * @throws CarteVilleNonPossedeException
	 * @throws VillePasAssezDeMaladieException
	 * @throws NombreDeStationsDeRecherchesMaxDépasséException
	 * @throws RolePasExpertAuxOperationsException
	 * @throws VillePasVoisineException
	 * @throws CarteIntrouvableException
	 * @throws RolePasPlanificateurUrgenceException
	 * @throws RolePasMedecinException
	 * @throws PartageConnaissancesException
	 * @throws PlanificateurUrgenceCarteDejaPriseException
	 * @throws VilleSansStationDeRechercheException
	 */
	void jouerActionDeplacerVersJoueurParRepartiteur(long idPartie, String joueurC, String joueurADeplacer, String joueurARejoindre) throws
			PartieNonExistanteException, JoueurIntrouvablePartie, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, NombresDeCartesMaxAtteindsException, RolePasRepartiteurException, JoueurNonCourantException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException;
	
	/**
	 * Action pour role planificateur urgence
	 * @param idPartie l'id de la partie
	 * @param event l'événement
	 * @throws PartieNonExistanteException
	 * @throws PartieTermineeException
	 * @throws PartieNonDemarreeException
	 * @throws CarteIntrouvableException
	 * @throws RolePasPlanificateurUrgenceException
	 * @throws JoueurNonCourantException
	 */
	void jouerActionPiocherCarteEvenementParPlanificateur(long idPartie, String jcourant,String event) throws PartieNonExistanteException,
			JoueurNonCourantException,
			PlusDeCubesMaladieDisponible, PartieTermineeException, NombresDeCartesMaxAtteindsException, PartieNonDemarreeException,
			MaladieEradiqueException, MaladiesNonIntialiseesException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException,
			VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException,
			NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException,
			RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException,
			VilleSansStationDeRechercheException;

	/**
	 * Permet de récupérer la liste des parties qu'un joueur a terminé
	 * @param pseudo le nom du joueur
	 * @return une liste de parties (vide si le joueur n'a terminé aucune partie)
	 */
	List<Partie> getPartiesTermineesJoueur(String pseudo);
	
	/**
	 * Permet de récupérer la liste de toutes les parties non terminées
	 * @return une lsite de parties (vide si aucune partie n'éxiste ou si aucune partie n'est terminée)
	 */
	List<Partie> getPartiesNonTerminees();

	void jouerActionConstruireStationParExpertOpe(long idPartie,String jc) throws PartieNonDemarreeException, PartieTermineeException, VilleNonTrouveeException, StationRechercheExisteException, JoueurNonCourantException, PartieNonExistanteException, RolePasExpertAuxOperationsException, NombresDeCartesMaxAtteindsException;
	void jouerActionDeplacerStationParExpertOpe(long idPartie,String jc, String ville) throws PartieNonDemarreeException, PartieTermineeException, VilleNonTrouveeException, StationRechercheExisteException, JoueurNonCourantException, PartieNonExistanteException, RolePasExpertAuxOperationsException, StationRechercheNonExistanteException, NombresDeCartesMaxAtteindsException;
	void jouerActionStationVersVilleExpertOpe(long idPartie,String jc,String ville, int carte) throws StationRechercheNonExistanteException, PartieNonExistanteException, JoueurNonCourantException, PartieNonDemarreeException, PartieTermineeException, RolePasExpertAuxOperationsException, CarteIntrouvableException, VilleNonTrouveeException, NombresDeCartesMaxAtteindsException;
}

