package modele.actions;

import modele.Joueur;
import modele.Partie;
import modele.Plateau;
import modele.Ville;
import modele.actions.exceptions.CarteVilleNonPossedeException;
import modele.actions.exceptions.NombreDeStationsDeRecherchesMaxDépasséException;
import modele.actions.exceptions.NombreDeStationsDeRecherches6AtteindsException;
import modele.actions.exceptions.NombresDeStationDeRechercheInférieurà6Exception;
import modele.cartes.CarteVilleJoueur;
import modele.exceptions.*;

public class ConstruireStationDeRecherche implements IActions {

    Ville villeAuquelOnenleveStationDeRecherche;

    // constructeur pour l'action executerAction()
    public ConstruireStationDeRecherche() {
    }

    // constructeur pour l'action executerAction()
    public ConstruireStationDeRecherche(Ville villeAuquelOnenleveStationDeRecherche) {
        this.villeAuquelOnenleveStationDeRecherche = villeAuquelOnenleveStationDeRecherche;
    }

    /** Permet de construire une station de recherche si le nombre de stations de recherche totale est inférieur à 6.
     *
     * @param p
     * @throws NombreDeStationsDeRecherches6AtteindsException
     * @throws PartieNonDemarreeException
     * @throws StationRechercheExisteException
     * @throws VilleNonTrouveeException
     * @throws NombreDeStationsDeRecherchesMaxDépasséException
     */

    @Override
    public void executerAction(Partie p) throws NombreDeStationsDeRecherches6AtteindsException, PartieNonDemarreeException, StationRechercheExisteException, VilleNonTrouveeException, NombreDeStationsDeRecherchesMaxDépasséException, CarteVilleNonPossedeException, PartieTermineeException {
        boolean carteVilleNonTrouvee = true;
        CarteVilleJoueur carteVilleTemporaire = null;
        Joueur joueurCurrent = p.getJoueurCourant();
        Ville villeCurrent = joueurCurrent.getEmplacement();
        Plateau plateau = p.getPlateau();
        int index = 0;
        while (index < joueurCurrent.getCartes().size() && carteVilleNonTrouvee) {
            if (joueurCurrent.getCartes().get(index) instanceof CarteVilleJoueur) {
                carteVilleTemporaire = (CarteVilleJoueur) joueurCurrent.getCartes().get(index); // on assigne la carte à carteVilleTemporaire;
                if (carteVilleTemporaire.getLaVille().getNom().equals(villeCurrent.getNom())) {
                    carteVilleNonTrouvee = false;
                }
            }
            index++;
        }


        if (!carteVilleNonTrouvee) {
            if (villeCurrent.hasStationDeRecherche()) {
                throw new StationRechercheExisteException();
            } else if (plateau.getStationsRechercheRestantes() <= -1) {
                throw new NombreDeStationsDeRecherchesMaxDépasséException();
            } else if (plateau.getStationsRechercheRestantes() == 0) {
                throw new NombreDeStationsDeRecherches6AtteindsException("Utiliser executeAction2()");
            } else {
                p.setStationRecherche(villeCurrent.getNom());
                joueurCurrent.setNbActions(joueurCurrent.getNbActions() - 1);
                p.defaussercarteJoueur(joueurCurrent, index - 1);
            }
        }
        else {
            throw new CarteVilleNonPossedeException();
        }
    }


    /**
     * Permet de construire une station de recherche si le nombre de stations de recherches est égal à 6,
     * en l'enlèvant d'une ville et en la mettant dans la ville actuelle du joueur.
      * @param p
     * @param v
     * @throws PartieNonDemarreeException
     * @throws StationRechercheExisteException
     * @throws VilleNonTrouveeException
     * @throws StationRechercheNonExistanteException
     * @throws NombresDeStationDeRechercheInférieurà6Exception
     */
    public void executerAction2( Partie p, Ville v) throws PartieNonDemarreeException, StationRechercheExisteException, VilleNonTrouveeException, StationRechercheNonExistanteException, NombresDeStationDeRechercheInférieurà6Exception, PartieTermineeException {
        boolean carteVilleNonTrouvee = true;
        CarteVilleJoueur carteVilleTemporaire = null;
        Joueur joueurCurrent = p.getJoueurCourant();
        Ville villeCurrent = joueurCurrent.getEmplacement();
        Plateau plateau = p.getPlateau();
        v=this.villeAuquelOnenleveStationDeRecherche;
        int index = 0;
        while (index < joueurCurrent.getCartes().size() && carteVilleNonTrouvee) {
            if (joueurCurrent.getCartes().get(index) instanceof CarteVilleJoueur) {
                carteVilleTemporaire = (CarteVilleJoueur) joueurCurrent.getCartes().get(index); // on assigne la carte à carteVilleTemporaire;
                if (carteVilleTemporaire.getLaVille().getNom().equals(villeCurrent.getNom())) {
                    carteVilleNonTrouvee = false;
                }
            }
            index++;
        }

        if (!carteVilleNonTrouvee && plateau.getStationsRechercheRestantes() == 0) {

            if (villeCurrent.hasStationDeRecherche()) {
                throw new StationRechercheExisteException();
            }

            else {
                p.removeSationRecherche(v.getNom());
                p.setStationRecherche(villeCurrent.getNom());
                joueurCurrent.setNbActions(joueurCurrent.getNbActions() - 1);
                p.defaussercarteJoueur(joueurCurrent, index - 1);
            }
        }
        else {
            throw new NombresDeStationDeRechercheInférieurà6Exception("Utiliser executeAction()");
        }
    }



    }

