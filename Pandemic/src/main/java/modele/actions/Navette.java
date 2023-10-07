package modele.actions;

import modele.Joueur;
import modele.Partie;
import modele.Ville;
import modele.actions.exceptions.VilleSansStationDeRechercheException;
import modele.exceptions.PartieNonDemarreeException;

public class Navette implements IActions, IActionsDeplacement {


    private Joueur joueur;
    private Ville villeDestination;


    public Navette(Joueur joueur, Ville villeDestination) {
        this.villeDestination = villeDestination;
        this.joueur=joueur;
    }

    /** Si la ville de destination a une station de recherche, s'y déplacer et dimunier le nombre d'actions restant au joueur.
     *
     * @param p
     * @throws VilleSansStationDeRechercheException
     * @throws PartieNonDemarreeException
     */
    public void executerAction (Partie p) throws VilleSansStationDeRechercheException, PartieNonDemarreeException {
        if (villeDestination.hasStationDeRecherche()) {
            Joueur joueurCurrent = joueur;
            Ville villeCurrent = joueurCurrent.getEmplacement();
            villeCurrent.enleverJoueur(joueurCurrent);
            joueurCurrent.setEmplacement(villeDestination);
            villeDestination.rajouterJoueur(joueurCurrent);
            joueurCurrent.setNbActions(joueurCurrent.getNbActions() - 1);
        } else {
            throw new VilleSansStationDeRechercheException();
        }

    }

    public String toString(){
        return "Se deplacer dans "+villeDestination.getNom().toString();
    }

}
