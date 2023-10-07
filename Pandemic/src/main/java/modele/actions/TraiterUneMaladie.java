package modele.actions;

import modele.*;
import modele.actions.exceptions.VilleSansCetteMaladieException;
import modele.exceptions.*;

public class TraiterUneMaladie implements IActions{

    private Maladie maladieATraiter;

    public TraiterUneMaladie(Maladie maladieATraiter) {
        this.maladieATraiter = maladieATraiter;
    }

    /**
     * Permet de trainer une maladie.
     * @param p
     * @throws PartieNonDemarreeException
     * @throws VillePasAssezDeMaladieException
     * @throws VilleSansCetteMaladieException
     */
    @Override
    public void executerAction(Partie p) throws PlusDeCubesMaladieDisponible, PartieNonDemarreeException, PartieTermineeException, MaladiesNonIntialiseesException, VillePasAssezDeMaladieException, VilleSansCetteMaladieException, MaladieEradiqueException {
        Joueur joueurCourant= p.getJoueurCourant();
        Ville villeCourante= joueurCourant.getEmplacement();

        if ( villeCourante.getNiveauxMaladies().containsKey(maladieATraiter) &&
                villeCourante.getNiveauxMaladies().get(maladieATraiter) > 0
        ) {
                // si parmi les maladies existantes dans une ville, il y'a cette maladie
                if ( joueurCourant.getRole().equals(Role.MEDECIN)) { // si le joueur a le role de medecin
                    villeCourante.removeNiveauMaladieGuerie(maladieATraiter);
                }

                else if(maladieATraiter.getRemede()==true){
                    villeCourante.removeNiveauMaladieGuerie(maladieATraiter);
                }
                else {
                    villeCourante.removeNiveauMaladie(maladieATraiter,1); // diminue le niveau d'une maladie d'un cube
                }
                joueurCourant.setNbActions(joueurCourant.getNbActions() - 1);
        }

        else {
            throw new VilleSansCetteMaladieException();
        }
    }
}
