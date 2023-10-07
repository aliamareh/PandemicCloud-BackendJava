package modele.actions.actionsRole;

import modele.*;
import modele.actions.IActions;
import modele.actions.actionsRole.exceptions.CarteIntrouvableException;
import modele.actions.actionsRole.exceptions.PlanificateurUrgenceCarteDejaPriseException;
import modele.actions.actionsRole.exceptions.RolePasMedecinException;
import modele.actions.actionsRole.exceptions.RolePasPlanificateurUrgenceException;
import modele.cartes.CarteEvenement;
import modele.cartes.ICarteJoueur;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieTermineeException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlanificateurUrgence implements IActions {
    private CarteEvenement cartearecup;
    
    public PlanificateurUrgence(CarteEvenement cartearecup){
        this.cartearecup = cartearecup;
    }

    /**
     * Permet au planificateur de piocher une carte évènement de la défausse et de l'entreposer pour la jouer plus tard
     * @param p la partie
     * @throws Exception
     */
    @Override
    public void executerAction(Partie p) throws PartieNonDemarreeException, PartieTermineeException, CarteIntrouvableException, PlanificateurUrgenceCarteDejaPriseException, RolePasPlanificateurUrgenceException {
        Joueur joueurCourant= p.getJoueurCourant();
        if ( joueurCourant.getRole().equals(Role.PLANIFICATEUR_URGENCE)) {
            if(Objects.isNull(p.getCarteEntrepPlanificateurUrgence())) {
                List<ICarteJoueur> defausse = p.getPlateau().getDefausseJoueur();
                if (defausse.remove(cartearecup)) {
                    p.setCarteEntrepPlanificateurUrgence(cartearecup);
                    joueurCourant.setNbActions(joueurCourant.getNbActions()-1);
                } else {
                    throw new CarteIntrouvableException();
                }
            }
            else
            {
                throw new PlanificateurUrgenceCarteDejaPriseException();
            }
        }
        else {
            throw new RolePasPlanificateurUrgenceException();
        }
    }
}

