package modele.actions;

import modele.Joueur;
import modele.Maladie;
import modele.Partie;
import modele.Plateau;
import modele.cartes.CarteVilleJoueur;
import modele.cartes.ICarteJoueur;
import modele.exceptions.JoueurNonCourantException;
import modele.exceptions.MaladieEradiqueException;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieTermineeException;

import java.util.List;

public class DecouvrirRemede implements IActions{
    private Joueur joueurCourant;
    private List<Integer> cartesVillesJoueur;
    private Maladie maladieATraiter;

    public DecouvrirRemede(List<Integer> cartesVillesJoueur, Maladie maladieATraiter) {
        this.cartesVillesJoueur = cartesVillesJoueur;
        this.maladieATraiter = maladieATraiter;
    }

    /**
     * Permet de découvrir un remède à une maladie.
     * @param p
     * @throws Exception
     */

    @Override
    public void executerAction(Partie p) throws PartieNonDemarreeException, PartieTermineeException {
        joueurCourant = p.getJoueurCourant();
        if (this.verificationCartes()) {
            Plateau plateau = (Plateau) p.getPlateau();
            List<Maladie> maladies = plateau.getMaladies();
            Maladie maladie = maladies.get(maladies.indexOf(maladieATraiter));

            if(!maladie.remedeEtabli()){
                maladie.setRemede(true);
                joueurCourant.setNbActions(joueurCourant.getNbActions() - 1);
                Integer[] indexes= this.cartesVillesJoueur.toArray(Integer[]::new);
                joueurCourant.defausser(indexes);
            }
        }

    }

    public boolean verificationCartes() {
        if (cartesVillesJoueur.size() < joueurCourant.nombreCartesPourTraiterMaladie()) {
            return false;
        }

        for (int i : this.cartesVillesJoueur) {
            if (this.joueurCourant.getCartes().get(i) instanceof CarteVilleJoueur) {
                CarteVilleJoueur carteTemporaire = (CarteVilleJoueur) this.joueurCourant.getCartes().get(i);
                if (!maladieATraiter.equals(carteTemporaire.getLaVille().getMaladieParDefaut())) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Integer> getCartesVillesJoueur() {
        return cartesVillesJoueur;
    }

    public void setCartesVillesJoueur(List<Integer> cartesVillesJoueur) {
        this.cartesVillesJoueur = cartesVillesJoueur;
    }

    @Override
    public String toString() {
        return "Pour découvrir un remede, " +
                "le joueur courant " + joueurCourant +
                "utilise ces cartes " + cartesVillesJoueur +
                "pour traiter cette maladie " + maladieATraiter;
    }
}
