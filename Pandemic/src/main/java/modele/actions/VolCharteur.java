package modele.actions;

import modele.Joueur;
import modele.Partie;
import modele.Ville;
import modele.actions.exceptions.CarteVilleNonPossedeException;
import modele.actions.exceptions.CarteVillePasPareilVilleDestinationException;
import modele.cartes.CarteVille;
import modele.cartes.CarteVilleJoueur;
import modele.cartes.ICarteJoueur;
import modele.exceptions.PartieNonDemarreeException;

public class VolCharteur implements IActions, IActionsDeplacement {
    private Joueur joueur;
    private Ville villeDestination;
    private CarteVille carteVilleTemporaire;
    public VolCharteur(Joueur joueur,Ville ville) {

        this.joueur = joueur;
        this.villeDestination = ville;
    }

    /** Permet au joueur de se déplacer à la ville de son choix que si la carte ville qu'il aura defaussé est pareil à la ville ou il est
     * localisé, et dimunier le nombre d'actions restantes.
     *
     * @param p
     * @throws CarteVilleNonPossedeException
     * @throws PartieNonDemarreeException
     */
    public void executerAction(Partie p) throws CarteVilleNonPossedeException,PartieNonDemarreeException {
        boolean carteVilleNonTrouvee = true;
        CarteVilleJoueur carteVilleTemporaire=null;
        Joueur joueurCurrent = joueur;
        Ville villeCurrent = joueurCurrent.getEmplacement();
        int index = 0;
        while(index < joueurCurrent.getCartes().size() && carteVilleNonTrouvee){
            if (joueurCurrent.getCartes().get(index) instanceof CarteVilleJoueur) {
                carteVilleTemporaire=(CarteVilleJoueur) joueurCurrent.getCartes().get(index); // on assigne la carte à carteVilleTemporaire;
                if(carteVilleTemporaire.getLaVille().getNom().equals(villeCurrent.getNom())){
                    carteVilleNonTrouvee = false;
                }
            }
            index ++;
        }
        if(!carteVilleNonTrouvee){
            villeCurrent.enleverJoueur(joueurCurrent);
            joueurCurrent.setEmplacement(villeDestination);
            villeDestination.rajouterJoueur(joueurCurrent);
            joueurCurrent.setNbActions(joueurCurrent.getNbActions() - 1);
            p.defaussercarteJoueur(joueurCurrent,index-1);
        }
        else {
            throw new CarteVilleNonPossedeException();
        }

    }

    public String toString(){
        return "Se deplacer dans "+villeDestination.getNom().toString();
    }

}
