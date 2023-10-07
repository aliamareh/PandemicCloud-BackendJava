package modele.actions;

import modele.Joueur;
import modele.Ville;
import modele.actions.actionsRole.Repartiteur;

public class FabriqueActionSurVilleConcrete {
    public IActions creer(Joueur joueur,TypeAction typeAction, Ville ville){
        switch (typeAction){
            case NAVETTE -> {
                return new Navette(joueur,ville);
            }
            case VOLDIRECT -> {
                return new VolDirect(joueur,ville);
            }
            case VOLCHARTEUR -> {
                return new VolCharteur(joueur,ville);
            }
            case VOITUREOUTRANSBORDEUR -> {
                return new VoitureOuTransbordeur(joueur,ville);
            }
            case CONSTRUIRESTATIONRECHERCHE -> {
                return new ConstruireStationDeRecherche(ville);
            }
        }
        return null;
    }
}
