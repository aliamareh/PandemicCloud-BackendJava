package modele.actions.actionsRole;

import modele.Joueur;
import modele.Partie;
import modele.Role;
import modele.Ville;
import modele.actions.FabriqueActionSurVilleConcrete;
import modele.actions.IActions;
import modele.actions.IActionsDeplacement;
import modele.actions.TypeAction;
import modele.actions.actionsRole.exceptions.*;
import modele.actions.exceptions.*;
import modele.exceptions.*;

public class Repartiteur implements IActionsDeplacement {

    private Joueur joueurControle;
    private Joueur joueurADeplacer;
    private Joueur joueurVersLequelleOnDeplace;
    private Ville villeDestination;
    private TypeAction typeAction;

    // constructeur pour executerAction()
    public Repartiteur(Joueur joueurControle,TypeAction typeAction,Ville villeDestination) {
        this.joueurControle=joueurControle;
        this.villeDestination=villeDestination;
        this.typeAction= typeAction;
    }

    // cconstructeur pour executerAction2()
    public Repartiteur(Joueur joueurADeplacer, TypeAction typeAction,Joueur joueurVersLequelleOnDeplace) {
        this.joueurADeplacer = joueurADeplacer;
        this.joueurVersLequelleOnDeplace = joueurVersLequelleOnDeplace;
    }

    /** Permet de deplacer n'importe quel pion vers la ville qu'on veut.
     *
     * @param p
     * @throws RolePasRepartiteurException
     * @throws PartieNonDemarreeException
     */

    public void executerAction(Partie p) throws PlusDeCubesMaladieDisponible, PartieNonDemarreeException, PartieTermineeException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, NombreDeStationsDeRecherchesMaxDépasséException, MaladiesNonIntialiseesException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException, RolePasRepartiteurException {
        Joueur joueurCourant= p.getJoueurCourant();
        if ( joueurCourant.getRole().equals(Role.REPARTITEUR)) {
            FabriqueActionSurVilleConcrete fabriqueActionSurVilleConcrete = new FabriqueActionSurVilleConcrete();
            IActions action = fabriqueActionSurVilleConcrete.creer(this.joueurControle,this.typeAction, this.villeDestination);
            action.executerAction(p);
            joueurCourant.setNbActions(joueurCourant.getNbActions()+1); // faire le déplacement via cette action ne doit pas couter une action
                                                                        // et puisque chaque deplacement coute automatiquement 1 action, en rajoutant une action ici, alors il y'auras pas d'effect sur le nombre d'actions.
        }
        else {
            throw new RolePasRepartiteurException();
        }
    }

    /** Permet de déplacer un pion vers la ville d'un autre pion
     *
     * @param p
     * @throws RolePasRepartiteurException
     * @throws PartieNonDemarreeException
     */
    public void executerAction2(Partie p) throws Exception, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, PartieTermineeException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, NombreDeStationsDeRecherchesMaxDépasséException, MaladiesNonIntialiseesException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException, RolePasRepartiteurException {
        Joueur joueurCourant= p.getJoueurCourant();
        if ( joueurCourant.getRole().equals(Role.REPARTITEUR)) {
            Ville villeDuJoueurVersLequelOnDeplace= joueurVersLequelleOnDeplace.getEmplacement();
            FabriqueActionSurVilleConcrete fabriqueActionSurVilleConcrete = new FabriqueActionSurVilleConcrete();
            IActions action = fabriqueActionSurVilleConcrete.creer(this.joueurControle,this.typeAction, villeDuJoueurVersLequelOnDeplace);
            action.executerAction(p);
        }
        else {
            throw new RolePasRepartiteurException();
        }
    }
}
