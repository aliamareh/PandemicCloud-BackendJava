package modele.actions.actionsRole;

import modele.*;
import modele.actions.IActions;
import modele.actions.actionsRole.exceptions.AttendreProchainTourPourCetteActionException;
import modele.actions.actionsRole.exceptions.RolePasExpertAuxOperationsException;
import modele.actions.exceptions.NombreDeStationsDeRecherches6AtteindsException;
import modele.actions.exceptions.NombreDeStationsDeRecherchesMaxDépasséException;
import modele.actions.exceptions.NombresDeStationDeRechercheInférieurà6Exception;
import modele.actions.exceptions.VilleSansStationDeRechercheException;
import modele.cartes.CarteVilleJoueur;
import modele.exceptions.*;

public class ExpertAuxOperations implements IActions {

    // constructeur pour executerAction()
    public ExpertAuxOperations() {
    }

    Ville villeAuquelOnenleveStationDeRecherche;
    CarteVilleJoueur carteVilleJoueurADefausser;

    Ville villeDestination;

    // constructeur pour executerAction2()
    public ExpertAuxOperations(Ville villeAuquelOnenleveStationDeRecherche, CarteVilleJoueur carteVilleJoueurADefausser) {
        this.villeAuquelOnenleveStationDeRecherche = villeAuquelOnenleveStationDeRecherche;
        this.carteVilleJoueurADefausser = carteVilleJoueurADefausser;
    }

    // constructeur pour executerAction3()
    public ExpertAuxOperations(CarteVilleJoueur carteVilleJoueurADefausser, Ville villeDestination) {
        this.carteVilleJoueurADefausser = carteVilleJoueurADefausser;
        this.villeDestination = villeDestination;
    }

    /** Permet à l'expert aux operations de construire une station de recherche dans la ville où il est sans avoir à defausser une carte ville.
     *
     * @param p
     * @throws PartieNonDemarreeException
     * @throws StationRechercheExisteException
     * @throws NombreDeStationsDeRecherchesMaxDépasséException
     * @throws NombreDeStationsDeRecherches6AtteindsException
     * @throws VilleNonTrouveeException
     * @throws RolePasExpertAuxOperationsException
     */
    @Override
    public void executerAction(Partie p) throws PartieNonDemarreeException, StationRechercheExisteException, NombreDeStationsDeRecherchesMaxDépasséException, NombreDeStationsDeRecherches6AtteindsException, VilleNonTrouveeException, RolePasExpertAuxOperationsException, PartieTermineeException {
        Joueur joueurCourant= p.getJoueurCourant();
        Plateau plateau= p.getPlateau();
        Ville villeCourante= joueurCourant.getEmplacement();
        if ( joueurCourant.getRole().equals(Role.EXPERT_AUX_OPERATIONS)) {
            if (villeCourante.hasStationDeRecherche()) {
                throw new StationRechercheExisteException();
            }
            else if (plateau.getStationsRechercheRestantes() <= -1) {
                throw new NombreDeStationsDeRecherchesMaxDépasséException();
            }

            else if (plateau.getStationsRechercheRestantes() == 0) {
                throw new NombreDeStationsDeRecherches6AtteindsException("Utiliser executerAction2()");            }
            else {
                p.setStationRecherche(villeCourante.getNom());
                joueurCourant.setNbActions(joueurCourant.getNbActions() - 1);
            }

        }
        else {
            throw new RolePasExpertAuxOperationsException();
        }
    }

    /** Permet à l'expert aux operations, si il existe déja 6 stations de recherches sur le plateau, d'en retirer une du plateau et d'en créer
     * là où il est sans avoir à défausser une carte ville.
     *
     * @param p
     * @param v
     * @param c
     * @throws PartieNonDemarreeException
     * @throws StationRechercheExisteException
     * @throws VilleNonTrouveeException
     * @throws StationRechercheNonExistanteException
     * @throws NombreDeStationsDeRecherchesMaxDépasséException
     * @throws NombresDeStationDeRechercheInférieurà6Exception
     * @throws RolePasExpertAuxOperationsException
     */

    public void executerAction2(Partie p,Ville v, CarteVilleJoueur c) throws PartieNonDemarreeException, StationRechercheExisteException, VilleNonTrouveeException, StationRechercheNonExistanteException, NombreDeStationsDeRecherchesMaxDépasséException, NombresDeStationDeRechercheInférieurà6Exception, RolePasExpertAuxOperationsException, PartieTermineeException {
        Joueur joueurCourant= p.getJoueurCourant();
        Plateau plateau= p.getPlateau();
        Ville villeCourante= joueurCourant.getEmplacement();
        if ( joueurCourant.getRole().equals(Role.EXPERT_AUX_OPERATIONS)) {
            boolean carteVilleNonTrouvee = true;
            CarteVilleJoueur carteVilleTemporaire = null;
            v=this.villeAuquelOnenleveStationDeRecherche;
            int index = 0;
            while (index < joueurCourant.getCartes().size() && carteVilleNonTrouvee) {
                if (joueurCourant.getCartes().get(index) instanceof CarteVilleJoueur) {
                    carteVilleTemporaire = (CarteVilleJoueur) joueurCourant.getCartes().get(index); // on assigne la carte à carteVilleTemporaire;
                    if (carteVilleTemporaire.getLaVille().getNom().equals(c.getLaVille().getNom())) {
                        carteVilleNonTrouvee = false;
                    }
                }
                index++;
            }

            if (!carteVilleNonTrouvee && plateau.getStationsRechercheRestantes() == 0) {

                if (villeCourante.hasStationDeRecherche()) {
                    throw new StationRechercheExisteException();
                }

                else {
                    p.removeSationRecherche(v.getNom());
                    p.setStationRecherche(villeCourante.getNom());
                    joueurCourant.setNbActions(joueurCourant.getNbActions() - 1);
                }
            }

            else if (plateau.getStationsRechercheRestantes() <= -1){
                throw new NombreDeStationsDeRecherchesMaxDépasséException();
            }
            else {
                throw new NombresDeStationDeRechercheInférieurà6Exception("Utiliser executerAction()");
            }
        }

        else {
            throw new RolePasExpertAuxOperationsException();
        }

        }

    /** Permet à l'expert aux opérations, une fois par tour, de défausser une carte ville quelquonque en sa possesion pour après se
     * déplacer d'une ville avec une station de recherche vers n'importe quel ville.
     *
      * @param p
     * @param v
     * @param c
     * @throws AttendreProchainTourPourCetteActionException
     * @throws PartieNonDemarreeException
     * @throws VilleSansStationDeRechercheException
     * @throws RolePasExpertAuxOperationsException
     */

    public void executerAction3(Partie p,Ville v, CarteVilleJoueur c) throws AttendreProchainTourPourCetteActionException, PartieNonDemarreeException, VilleSansStationDeRechercheException, RolePasExpertAuxOperationsException, PartieTermineeException {
        Joueur joueurCourant= p.getJoueurCourant();
        Plateau plateau= p.getPlateau();
        Ville villeCourante= joueurCourant.getEmplacement();
        if ( joueurCourant.getRole().equals(Role.EXPERT_AUX_OPERATIONS)) {
            boolean carteVilleNonTrouvee = true;
            CarteVilleJoueur carteVilleTemporaire = null;
            int index = 0;
            while (index < joueurCourant.getCartes().size() && carteVilleNonTrouvee) {
                if (joueurCourant.getCartes().get(index) instanceof CarteVilleJoueur) {
                    carteVilleTemporaire = (CarteVilleJoueur) joueurCourant.getCartes().get(index); // on assigne la carte à carteVilleTemporaire;
                    carteVilleNonTrouvee = false;
                    }
                index++;
            }

            if ( p.getCompteurPourExpertAuxOperations()==0) {
                if ( !carteVilleNonTrouvee) {
                    if( villeCourante.hasStationDeRecherche()) {
                        villeCourante.enleverJoueur(joueurCourant);
                        joueurCourant.setEmplacement(villeDestination);
                        villeDestination.rajouterJoueur(joueurCourant);
                        joueurCourant.setNbActions(joueurCourant.getNbActions() - 1);
                        p.defaussercarteJoueur(joueurCourant,index-1);
                        p.setCompteurPourExpertAuxOperations(1);
                    }
                    else {
                        throw new VilleSansStationDeRechercheException();
                    }
                }
            }
          else {
              throw new AttendreProchainTourPourCetteActionException();
            }
    }
        else {
            throw new RolePasExpertAuxOperationsException();
        }
}
}



