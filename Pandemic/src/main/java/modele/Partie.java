package modele;

import donnees.BibDonneesMongoDB;
import modele.actions.*;
import modele.actions.actionsRole.Medecin;
import modele.actions.actionsRole.PlanificateurUrgence;
import modele.actions.actionsRole.exceptions.*;
import modele.actions.exceptions.*;
import modele.cartes.*;
import modele.exceptions.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Partie {
    private BibDonneesMongoDB bibDonnees;
    private long idPartie;
    private List<Joueur> joueurs;
    private Joueur joueurCourant;
    private final Plateau plateau;
    private int nbJoueur;
    private Boolean partieDemaree;
    //En fonction du nombre de joueurs, le nombre carte a remettez à chacun
    public static  Map<Integer,Integer> nbJoueursCartesJoueurs;
    private CarteEvenement carteEntrepPlanificateurUrgence;
    private List<CartePropagation> cartesEntrepPrevision;

    public CarteEvenement getCarteEntrepPlanificateurUrgence() {
        return carteEntrepPlanificateurUrgence;
    }
    public void setCarteEntrepPlanificateurUrgence(CarteEvenement carteEntrepPlanificateurUrgence) {
        this.carteEntrepPlanificateurUrgence = carteEntrepPlanificateurUrgence;
    }
    
    static {
        nbJoueursCartesJoueurs = new HashMap<>();
        nbJoueursCartesJoueurs.put(2,4);
        nbJoueursCartesJoueurs.put(3,3);
        nbJoueursCartesJoueurs.put(4,2);
    }

    public Partie() {
        this.bibDonnees = new BibDonneesMongoDB();
        this.cartesEntrepPrevision = new ArrayList<>();
        this.partieDemaree = false;
        this.joueurs = new ArrayList<>();
        this.plateau = new Plateau();
    }

    public void setIdPartie(long idPartie) {
        this.idPartie = idPartie;
    }

    public void setNbJoueur(int nbJoueur) {
        this.nbJoueur = nbJoueur;
    }

    public Boolean getPartieDemaree() {
        return partieDemaree;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    /**
     * Méthode pour ajouter un joueur à la partie
     * @param joueur le joueur à ajouter à la partie
     * @throws ListeJoueursCompletetException
     */
    public void setJoueur(Joueur joueur) throws ListeJoueursCompletetException,
            JoueurDejaPresentException
    {
        if(joueurs.contains(joueur)){
            throw new JoueurDejaPresentException();
        }
        for (Joueur j : this.joueurs){
            if (j.getPseudo().equals(joueur.getPseudo())){
                throw new JoueurDejaPresentException();
            }
        }
        if(this.joueurs.size() >= nbJoueur){
            throw new ListeJoueursCompletetException();
        }
        else {
            this.joueurs.add(joueur);
        }
    }
    public Ville setStationRecherche(String nomVille) throws StationRechercheExisteException, VilleNonTrouveeException {
        int i = 0;
        Ville result = null;
        boolean villeTrouvee = false;
        while (i < this.plateau.getLesVilles().size() && ! villeTrouvee){
            Ville v = this.plateau.getLesVilles().get(i);
            if(v.getNom().equals(nomVille)){
                villeTrouvee = true;
                if(v.hasStationDeRecherche()){
                    throw new StationRechercheExisteException();
                }
                v.setStationDeRecherche(true);
                this.plateau.setStationsRecherche(this.plateau.getStationsRechercheRestantes()-1);
                result = v;
            }
            i++;
        }

        if (! villeTrouvee){
            throw new VilleNonTrouveeException();
        }
        return result;

    }

    public Ville removeSationRecherche(String nomVille) throws StationRechercheNonExistanteException, VilleNonTrouveeException, PartieNonDemarreeException {
        int i = 0;
        boolean villeTrouvee = false;
        Ville result = null;
        while (i < this.plateau.getLesVilles().size() && ! villeTrouvee){
            Ville v = this.plateau.getLesVilles().get(i);
            if(v.getNom().equals(nomVille)){
                villeTrouvee = true;
                if(!v.hasStationDeRecherche()){
                    throw new StationRechercheNonExistanteException();
                }
                v.setStationDeRecherche(false);
                this.plateau.setStationsRecherche(this.plateau.getStationsRechercheRestantes()+1);
                result = v;
            }
            i++;
        }

        if (! villeTrouvee){
            throw new VilleNonTrouveeException();
        }
        return result;
    }

    public void mettreEnplacePartie() throws PiocheJoueurVideException, VilleNonTrouveeException,
                                                     StationRechercheExisteException,
                                                     VilleVoisineAElleMemeException,
                                                     VilleDoublonVoisinException, ListeJoueursNonCompletetException, PlusDeCubesMaladieDisponible {
        if(this.joueurs.size() != nbJoueur){
            throw new ListeJoueursNonCompletetException();
        }

        int maxPopulation = 0;

        //initialiser les villes et les maladie
        this.plateau.ajouterMaladies(bibDonnees.getMaladies());
        this.plateau.setLesVilles(bibDonnees.getVilles());
        //ajouter les cartes propagations et les melanger
        this.plateau.ajouterCartesPropagation(bibDonnees.getCartesPropagation());
        this.plateau.melanger(this.plateau.getPiochePropagation());

        //initialisation des cube maladie
        int i = 3;
        while (i > 0 ){
            for (int j = 0 ; j < 3; j++ ){
                this.plateau.piocherPropagation(i);
            }
            i--;
        }
        //poser une station de recherche à Atlanta
        Ville atlanta = this.setStationRecherche("Atlanta");
        //ajouter les cartes evenement et les cartes joueur à la pioche des carte joueurs et melanger
        this.plateau.ajouterCartesJoueur(bibDonnees.getCartesEvenementsJoueur());
        //ajouter  à la pioche des carte joueurs
        this.plateau.ajouterCartesJoueur(bibDonnees.getCartesVilleJoueur());
        this.plateau.melanger(this.plateau.getPiocheJoueur());

        //Donnez à chaque joueur des [2,3,4] cartes Joueur et initialise le joueur courant et les roles
        Role[] roles = Role.values().clone();
        List<Role> rlist = Arrays.asList(roles);
        Collections.shuffle(rlist);
        int val = 0;
        for (Joueur j : this.joueurs) {
            j.setEmplacement(atlanta);
            atlanta.rajouterJoueur(j);
            j.setNbActions(4);
            j.setRole(rlist.get(val));
            val++;
            
            
            try {
                List<ICarteJoueur> cartes = this.piocherCarteJoueur(j, nbJoueursCartesJoueurs.get(nbJoueur));
               for ( ICarteJoueur c : cartes){
                   if (c instanceof CarteVilleJoueur
                           && ((CarteVilleJoueur) c).getLaVille().getPopulation() > maxPopulation ){
                       maxPopulation = ((CarteVilleJoueur) c).getLaVille().getPopulation();
                       this.joueurCourant = j;
                   }
               }
            }
            catch (PiocheJoueurVideException | NombresDeCartesMaxAtteindsException e) {
                throw new PiocheJoueurVideException();
            }
        };

        // inserer les carte Epidemie en fonction du niveau de difficulté
        this.plateau.ajouterCartesJoueur(
                bibDonnees.getCartesEpidemieJoueur()
        );
        this.plateau.melanger(this.plateau.getPiocheJoueur());

        this.partieDemaree = true;
    }

    /**
     * Permet a un joueur
     * @param j
     * @param nbCartes
     * @return
     * @throws PiocheJoueurVideException
     * @throws NombresDeCartesMaxAtteindsException
     * @throws PlusDeCubesMaladieDisponible
     */
    public List<ICarteJoueur> piocherCarteJoueur(Joueur j, int nbCartes) throws PiocheJoueurVideException, NombresDeCartesMaxAtteindsException, PlusDeCubesMaladieDisponible {
        List<ICarteJoueur> cartes = this.plateau.piocherJoueur(nbCartes);
        List<ICarteJoueur> carteADeffausser = new ArrayList<>();
        for (ICarteJoueur carteJoueur: cartes){
            if (carteJoueur instanceof CarteEpidemie){
                this.plateau.acceleration();
                this.plateau.infection();
                this.plateau.intensification();
                carteADeffausser.add(carteJoueur);
            }
        }
        this.plateau.defausserJoueur(carteADeffausser);
        cartes.removeAll(carteADeffausser);
        j.ajouterCartes(cartes);
        return cartes;
    }

    public void defaussercarteJoueur (Joueur j, Integer ... index){
        
        this.plateau.defausserJoueur(j.defausser(index));
    }

    /**
     *
     * @return ID de la partie
     */
    public long getIdPartie() {
        return idPartie;
    }


    /**
     * Cette fonction permet de traiter toute les action concernat une ville
     * @param j  le joueur courant
     * @param typeAction  le type de deplacement ex : [voiture, vol direct,
     *          charter, Navette, construire une station de recherche]
     * @param ville la ville sur laquelle jouer l'action
     */
    public void jouerActionSurVille(Joueur j, TypeAction typeAction, Ville ville) throws PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, NombresDeCartesMaxAtteindsException, JoueurNonCourantException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if(joueurCourant != j){
            throw new JoueurNonCourantException() ;
        }
        if (this.joueurCourant == j && this.joueurCourant.getNbActions() <= 0){
            throw new NombresDeCartesMaxAtteindsException();
        }
        FabriqueActionSurVilleConcrete fabrique = new FabriqueActionSurVilleConcrete();
        IActions actions = fabrique.creer(joueurCourant,typeAction,ville);

        actions.executerAction(this);
        verifierNbAction();
    }

    /**
     *
     * @param j : joueurCourant
     * @param ville : la ville ou se trouve la station de recherche que l'on souhaite retirer
     * @throws Exception
     */
    public void jouerActionDeplacerStationDeRecherche(Joueur j, Ville ville ) throws  PartieTermineeException, PartieNonDemarreeException, JoueurNonCourantException, VilleNonTrouveeException, StationRechercheExisteException, StationRechercheNonExistanteException, NombresDeStationDeRechercheInférieurà6Exception, NombresDeCartesMaxAtteindsException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if(joueurCourant != j){
            throw new JoueurNonCourantException() ;
        }
        if (this.joueurCourant == j && this.joueurCourant.getNbActions() <= 0){
            throw new NombresDeCartesMaxAtteindsException();
        }
        ConstruireStationDeRecherche action = new ConstruireStationDeRecherche(ville);
        action.executerAction2(this,ville);
        verifierNbAction();
    }


    /**
     * Cette fonction permet de traiter les action : traiter une maladie ou trouver un remède
     * @param j joureur courant
     * @param maladie  la maladie
     * @param typeAction le type de deplacement ex : [traiter maladie, trouver remède]
     */
    public void jouerActionContreMaladie(Joueur j, Maladie maladie, List<Integer> cartesVilleJoueurs,TypeAction typeAction) throws PlusDeCubesMaladieDisponible, PartieTermineeException, PartieNonDemarreeException, JoueurNonCourantException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombresDeCartesMaxAtteindsException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if(joueurCourant != j){
            throw new JoueurNonCourantException() ;
        }
        if (this.joueurCourant == j && this.joueurCourant.getNbActions() <= 0){
            throw new NombresDeCartesMaxAtteindsException();
        }

        FabriqueActionContreMaladieConcrete fabrique = new FabriqueActionContreMaladieConcrete();
        IActions actions = fabrique.creer(typeAction, cartesVilleJoueurs,maladie);

        actions.executerAction(this);
        verifierNbAction();
    }


    /**
     * Permet au medecin de retirer tous les cubes d'une maladie guérie dans la ville dans laquelle il est.
     * @param j
     * @throws Exception
     * @throws PlusDeCubesMaladieDisponible
     */
    public void jouerActionRetirerCubeParMedecin(Joueur j) throws PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if (this.joueurCourant == j && this.joueurCourant.getNbActions() <= 0){
            throw new NombresDeCartesMaxAtteindsException();
        }

        IActions actions = new Medecin();

        actions.executerAction(this);

    }

    /**
     * Permet de deplacer n'importe quel joueur vers une autre ville
     * @param joueurControle
     * @param joueurCourant
     * @param villeDestination
     * @throws PlusDeCubesMaladieDisponible
     * @throws Exception
     */
    public void jouerActionDeplacerPionParRepartiteur(Joueur joueurCourant, Joueur joueurControle,TypeAction typeAction,Ville villeDestination) throws
            PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, RolePasRepartiteurException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if (! joueurCourant.getRole().equals(Role.REPARTITEUR)){
            throw new RolePasRepartiteurException();
        }
        FabriqueActionSurVilleConcrete fabrique = new FabriqueActionSurVilleConcrete();
        IActions actions = fabrique.creer(joueurControle, typeAction, villeDestination);
        actions.executerAction(this);
        joueurControle.setNbActions(joueurControle.getNbActions()+1);
        this.joueurCourant.setNbActions(this.getJoueurCourant().getNbActions()-1);
        verifierNbAction();
    }

    public void jouerActionDeplacerVersJoueurParRepartiteur(Joueur joueurC, Joueur joueurADeplacer, Joueur joueurARejoindre) throws
            PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, NombresDeCartesMaxAtteindsException, RolePasRepartiteurException, JoueurNonCourantException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if (  this.joueurCourant!= joueurC) {
            throw new JoueurNonCourantException();
        }
        if(! joueurC.getRole().equals(Role.REPARTITEUR)){
            throw new RolePasRepartiteurException();
        }
        Ville villeCurrent = joueurADeplacer.getEmplacement();
        Ville villeDestination = joueurARejoindre.getEmplacement();

        villeCurrent.enleverJoueur(joueurADeplacer);
        joueurADeplacer.setEmplacement(villeDestination);
        villeDestination.rajouterJoueur(joueurADeplacer);

        joueurC.setNbActions(joueurC.getNbActions()-1);
        verifierNbAction();
    }

    /**
     * Permet au planificateur de piocher une carte évènement de la défausse
     * @param carteEvenement
     * @throws PlusDeCubesMaladieDisponible
     * @throws Exception
     */
    public void jouerActionPiocherCarteEvenementParPlanificateur(CarteEvenement carteEvenement) throws
            PlusDeCubesMaladieDisponible, PartieTermineeException, NombresDeCartesMaxAtteindsException, PartieNonDemarreeException, MaladieEradiqueException, MaladiesNonIntialiseesException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }

        IActions actions = new PlanificateurUrgence(carteEvenement);
        actions.executerAction(this);
        verifierNbAction();
    }

    /**
     * cette fonction permet de traiter l'action partager des connaissances
     * @param j1  joueur courant
     * @param j2 joueur avec lequel on souhaite partager des connaissances
     */
    public void jouerActionPartagerConnaissance(Joueur j1, Joueur j2, int positionCarte) throws PlusDeCubesMaladieDisponible, PartieTermineeException, NombresDeCartesMaxAtteindsException, PartieNonDemarreeException, MaladieEradiqueException, MaladiesNonIntialiseesException, JoueurNonCourantException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException {

        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if(joueurCourant != j1 && joueurCourant != j2 ){
            throw new JoueurNonCourantException() ;
        }
        if ( ( this.joueurCourant == j1 || this.joueurCourant == j2) && this.joueurCourant.getNbActions() <= 0){
            throw new NombresDeCartesMaxAtteindsException();
        }

        IActions a = new PartagerConnaissances(j1,j2,positionCarte);

        a.executerAction(this);
        verifierNbAction();
    }

    public void jouerEvenementParUneNuitTranquille(Joueur j, int positioncarte) throws PartieTermineeException, PartieNonDemarreeException, CarteIntrouvableException {
        peutJouerEvenement(j, positioncarte);

        ICarteJoueur c = j.getCartes().get(positioncarte);
        if(c instanceof CarteEvenement && ((CarteEvenement) c).getTypeEvenement().equals(TypeEvenement.PAR_UNE_NUIT_TRANQUILLE)){
            this.getPlateau().setEvntParUneNuitTranquille(true);
            this.defaussercarteJoueur(j, positioncarte);
        }
        else
        {
            throw new CarteIntrouvableException();
        }
    }

    public void jouerEvenementPontAerien(Joueur j, int positioncarte, Joueur joueuradeplacer, Ville destination) throws PartieTermineeException, PartieNonDemarreeException, CarteIntrouvableException {
        peutJouerEvenement(j, positioncarte);

        ICarteJoueur c = j.getCartes().get(positioncarte);
        if(c instanceof CarteEvenement && ((CarteEvenement) c).getTypeEvenement().equals(TypeEvenement.PONT_AERIEN)){
            joueuradeplacer.getEmplacement().enleverJoueur(joueuradeplacer);
            joueuradeplacer.setEmplacement(destination);
            destination.rajouterJoueur(joueuradeplacer);
            this.defaussercarteJoueur(j, positioncarte);
        }
        else
        {
            throw new CarteIntrouvableException();
        }
    }

    public void jouerEvenementPopulationResiliente(Joueur j, int positioncarte, int cartepropagation) throws CarteIntrouvableException, PartieNonDemarreeException, PartieTermineeException, CartePropagationPasDansDefaussePropagationException {
        peutJouerEvenement(j, positioncarte);

        if(cartepropagation < 0 || cartepropagation >= this.getPlateau().getDefaussePropagation().size()){
            throw new CartePropagationPasDansDefaussePropagationException();
        }

        ICarteJoueur c = j.getCartes().get(positioncarte);
        if(c instanceof CarteEvenement && ((CarteEvenement) c).getTypeEvenement().equals(TypeEvenement.POPULATION_RESILIENTE)){
            this.getPlateau().getDefaussePropagation().remove(cartepropagation);
            this.defaussercarteJoueur(j, positioncarte);
        }
        else
        {
            throw new CarteIntrouvableException();
        }
    }

    public void jouerEvenementPrevisionPhase1(Joueur j, int positioncarte) throws CarteIntrouvableException, PartieNonDemarreeException, PartieTermineeException, PiochePropagationVideException, EvenementDejaEnCoursException {
        peutJouerEvenement(j, positioncarte);

        if(this.getPlateau().getEvntPrevision()){
            throw new EvenementDejaEnCoursException();
        }

        ICarteJoueur c = j.getCartes().get(positioncarte);
        List<CartePropagation> pioche = this.getPlateau().getPiochePropagation();
        if(c instanceof CarteEvenement && ((CarteEvenement) c).getTypeEvenement().equals(TypeEvenement.PREVISION)){
            this.defaussercarteJoueur(j, positioncarte);
            if(pioche.size() > 0) {
                while(pioche.size() > 0 && this.cartesEntrepPrevision.size() < 6){
                    this.cartesEntrepPrevision.add(pioche.get(0));
                    pioche.remove(0);
                }
            }
            else
            {
                throw new PiochePropagationVideException();
            }
        }
        else
        {
            throw new CarteIntrouvableException();
        }

        this.getPlateau().setEvntPrevision(true);
    }

    public void jouerEvenementPrevisionPhase2() throws EvenementPasEnCoursException {
        if(this.getPlateau().getEvntPrevision()){
            List<CartePropagation> pioche = this.getPlateau().getPiochePropagation();
            while(this.cartesEntrepPrevision.size() > 0){
                pioche.add(0,this.cartesEntrepPrevision.get(this.cartesEntrepPrevision.size()-1));
                this.cartesEntrepPrevision.remove(this.cartesEntrepPrevision.size()-1);
            }
            this.getPlateau().setEvntPrevision(false);
        }
        else
        {
            throw new EvenementPasEnCoursException();
        }
    }

    public void jouerEvenementSubventionPublique(Joueur j, int positioncarte, Ville villeChoisie) throws CarteIntrouvableException, PartieNonDemarreeException, PartieTermineeException, StationRechercheExisteException, NombreDeStationsDeRecherches6AtteindsException, VilleNonTrouveeException {
        peutJouerEvenement(j, positioncarte);

        ICarteJoueur c = j.getCartes().get(positioncarte);
        if(c instanceof CarteEvenement && ((CarteEvenement) c).getTypeEvenement().equals(TypeEvenement.SUBVENTION_PUBLIQUE)){
            if ( villeChoisie.hasStationDeRecherche()) {
                throw new StationRechercheExisteException();
            }
            else if ( this.getPlateau().getStationsRechercheRestantes()==0) {
                throw new NombreDeStationsDeRecherches6AtteindsException("Utiliser executerEvenement2()");
            }
            else {
                this.setStationRecherche(villeChoisie.getNom());
                this.defaussercarteJoueur(j, positioncarte);
            }
        }
        else
        {
            throw new CarteIntrouvableException();
        }
    }

    public void jouerEvenementSubventionPublique2(Joueur j, int positioncarte, Ville villeChoisie, Ville villeaenlever) throws CarteIntrouvableException, PartieNonDemarreeException, PartieTermineeException, StationRechercheExisteException, VilleNonTrouveeException, StationRechercheNonExistanteException {
        peutJouerEvenement(j, positioncarte);

        ICarteJoueur c = j.getCartes().get(positioncarte);
        if(c instanceof CarteEvenement && ((CarteEvenement) c).getTypeEvenement().equals(TypeEvenement.SUBVENTION_PUBLIQUE)){
            if (villeChoisie.hasStationDeRecherche()) {
                throw new StationRechercheExisteException();
            } else {
                this.removeSationRecherche(villeaenlever.getNom());
                this.setStationRecherche(villeChoisie.getNom());
                this.defaussercarteJoueur(j, positioncarte);
            }
        }
        else
        {
            throw new CarteIntrouvableException();
        }
    }

    private void peutJouerEvenement(Joueur j, int positioncarte) throws PartieTermineeException, PartieNonDemarreeException, CarteIntrouvableException {
        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        if(!this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        if(positioncarte < 0 || positioncarte >= j.getCartes().size()){
            throw new CarteIntrouvableException();
        }
    }

    /**
     *
     * @return l'etat du plateau
     */
    public Plateau getPlateau() {
        return this.plateau;
    }

    public Joueur getJoueurCourant() throws PartieNonDemarreeException, PartieTermineeException {
        if(! this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        
        if(this.partieTerminee()){
            throw new PartieTermineeException();
        }
        
        return joueurCourant;
    }

    /**
     * Permet de verifier le nombres d'actions du joueur
     * Si nbAction = 0 on piche 2 cartes joueur et 2 carte propagation et on passe au joueur suivant
     * @throws PartieTermineeException
     */

    public void verifierNbAction() throws PartieTermineeException, NombresDeCartesMaxAtteindsException, PartieNonDemarreeException {
        if(this.joueurs.get(this.joueurs.indexOf(this.joueurCourant)).getNbActions() <= 0){
            mAJJoueurCourant();
        }
    }


    public Joueur mAJJoueurCourant() throws NombresDeCartesMaxAtteindsException, PartieTermineeException, PartieNonDemarreeException {
        try {
            piocherCarteJoueur(joueurCourant,2);
            int nivProp = this.plateau.getNiveauPropagation();
        
            for (int i= 1; i<=nivProp; i++){
                this.plateau.piocherPropagation(3);
            }
        }
        catch (PiocheJoueurVideException | PlusDeCubesMaladieDisponible ex){
            throw new PartieTermineeException();
        }
        
        if(joueurCourant.getCartes().size() > 7){
            throw new NombresDeCartesMaxAtteindsException();
        }
        this.joueurs.get(this.joueurs.indexOf(this.joueurCourant)).setNbActions(4);
        this.joueurCourant = this.joueurs.get((this.joueurs.indexOf(this.joueurCourant) + 1) % this.joueurs.size());
        this.setCompteurPourExpertAuxOperations(0);
        this.plateau.resetEclosion();
        bibDonnees.updatePartie(this);
        return this.joueurCourant;
    }

    public Joueur mAJJoueurCourantApresDefausseCarteJoueur() throws NombresDeCartesMaxAtteindsException, PartieNonDemarreeException, PartieTermineeException {
        if(joueurCourant.getCartes().size() > 7){
            throw new NombresDeCartesMaxAtteindsException();
        }
        if(this.joueurs.get(this.joueurs.indexOf(this.joueurCourant)).getNbActions() <= 0){
            this.joueurs.get(this.joueurs.indexOf(this.joueurCourant)).setNbActions(4);
            this.joueurCourant = this.joueurs.get((this.joueurs.indexOf(this.joueurCourant) + 1) % this.joueurs.size());
        }
        bibDonnees.updatePartie(this);
        return this.joueurCourant;
    }

    /**
     *
     * @return nb de joueur pour la partie
     */
    public int getNbJoueur() {
        return this.nbJoueur;
    }

    /**
     *
     * @return  true ou false selon si la partir est terminée ou pas
     */
    public boolean partieTerminee() {
        if(! this.partieDemaree){
            return false;
        }
        //8 éclosions ont eu lieu ou Il n'y a plus assez de carte joueurs
        if(this.plateau.getCompteurEclosion() == 8 || this.plateau.getPiocheJoueur().size() < 2){
            return true;
        }

        for(Maladie m : this.plateau.getMaladies()){
            // il n'y a plus de cube maladie pour une maladie m donnée
            if(m.getCubesRestants() == 0){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return : true si les joueur on gagner la partie, false sinon
     * @throws PartieNonTermineeException  si la partie n'est pas encore terminée
     * @throws PartieNonDemarreeException  si la partie n'a pas encore demarré
     */
    public boolean partieGagnee() throws PartieNonTermineeException, PartieNonDemarreeException {

        if(! this.partieTerminee()){
            throw new PartieNonTermineeException();
        }
        if(! this.partieDemaree){
            throw new PartieNonDemarreeException();
        }
        //Les joueurs ont perdu la partie
        if(this.plateau.getCompteurEclosion() == 8 || this.plateau.getPiocheJoueur().size() < 2){
            return false;
        }

        return this.plateau.checkVictoire();
    }

    public Joueur getJoueurByPseudo(String j) {
        Joueur joueur = null;

        for(Joueur js : this.joueurs){
            if(js.getPseudo().equals(j)){
                joueur = js;
            }
        }

        return joueur;
    }

    // compteur utilisé par le role ExpertAuxOperation
    private int compteurPourExpertAuxOperations = 0;

    public int getCompteurPourExpertAuxOperations() {
        return compteurPourExpertAuxOperations;
    }

    public void setCompteurPourExpertAuxOperations(int compteurPourExpertAuxOperations) {
        this.compteurPourExpertAuxOperations = compteurPourExpertAuxOperations;
    }

    public List<CartePropagation> getCartesEntrepPrevision() {
        return cartesEntrepPrevision;
    }

    public void setCartesEntrepPrevision(List<CartePropagation> cartesEntrepPrevision) {
        this.cartesEntrepPrevision = cartesEntrepPrevision;
    }
}
