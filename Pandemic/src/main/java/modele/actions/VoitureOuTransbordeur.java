package modele.actions;

import modele.Joueur;
import modele.Partie;
import modele.Ville;
import modele.actions.exceptions.VillePasVoisineException;
import modele.exceptions.PartieNonDemarreeException;

public class VoitureOuTransbordeur implements IActions,IActionsDeplacement {

    private Joueur joueur;
    private Ville villeDestination;

    public VoitureOuTransbordeur(Joueur joueur, Ville villeDestination) {
        this.joueur= joueur;
        this.villeDestination = villeDestination; // on initialise dans la ville
    }

    /** Si la ville de destination est voisine à la ville de départ, s'y déplacer et dimunier le nombre d'actions restant au joueur.
     *
     * @param p
     * @throws VillePasVoisineException
     * @throws PartieNonDemarreeException
     */


    public void executerAction(Partie p) throws VillePasVoisineException, PartieNonDemarreeException {
        Joueur joueurCurrent = joueur;
        Ville villeCurrent= joueurCurrent.getEmplacement();
        if (villeCurrent.getVillesAlentours().contains(villeDestination)) {
            villeCurrent.enleverJoueur(joueurCurrent);
            joueurCurrent.setEmplacement(villeDestination);
            villeDestination.rajouterJoueur(joueurCurrent);
            joueurCurrent.setNbActions(joueurCurrent.getNbActions()-1);
        } else {
            throw new VillePasVoisineException();
        }

    }

    public String toString(){
        return "Se deplacer dans "+villeDestination.getNom();
    }


}


