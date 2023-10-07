package modele.actions;

import modele.Maladie;

import java.util.List;

public class FabriqueActionContreMaladieConcrete {
    public IActions creer(TypeAction typeAction,List<Integer> cartesVillesJoueur, Maladie maladie){
        switch (typeAction) {
            case DECOUVRIRREMEDE -> {
                return new DecouvrirRemede(cartesVillesJoueur,maladie);
            }
            case TRAITERMALADIE -> {
                return new TraiterUneMaladie(maladie);
            }
        }
        return null;
    }
}
