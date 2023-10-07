package modele.actions.actionsRole;

import modele.*;
import modele.actions.IActions;
import modele.actions.actionsRole.exceptions.RolePasMedecinException;
import modele.exceptions.MaladiesNonIntialiseesException;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieTermineeException;
import modele.exceptions.PlusDeCubesMaladieDisponible;

import java.util.Map;

public class Medecin implements IActions {
    /** Permet au medecin de retirer automatiquement tous les cubes d'une maladie guérie dans la ville dans laquelle il est.
     *
     * @param p
     * @throws Exception
     */
    @Override
    public void executerAction(Partie p) throws PlusDeCubesMaladieDisponible, PartieNonDemarreeException, PartieTermineeException, MaladiesNonIntialiseesException, RolePasMedecinException {
        Joueur joueurCourant= p.getJoueurCourant();
        if ( joueurCourant.getRole().equals(Role.MEDECIN)) {
            Ville villeCourante= joueurCourant.getEmplacement();
            Map<Maladie,Integer> maladiesVilleMedecin= villeCourante.getNiveauxMaladies();
            for ( Maladie maladie: maladiesVilleMedecin.keySet()) {
                if ( maladie.getRemede()== true) {
                    villeCourante.removeNiveauMaladieGuerie(maladie);
                }
            }
        }
        else {
            throw new RolePasMedecinException();
        }
    }
}


