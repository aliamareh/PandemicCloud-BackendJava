package modele.actions;

import modele.Joueur;
import modele.Partie;
import modele.Role;
import modele.Ville;
import modele.actions.exceptions.NombresDeCartesMaxAtteindsException;
import modele.actions.exceptions.PartageConnaissancesException;
import modele.actions.exceptions.VillesPasPareilException;
import modele.cartes.CarteVilleJoueur;
import modele.cartes.ICarteJoueur;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieTermineeException;

public class PartagerConnaissances implements IActions {

    private Joueur joueurEmetteur;
    private Joueur joueurRecepteur;

    private int indexCarte;
    public PartagerConnaissances(Joueur joueurEmetteur, Joueur joueurRecepteur, int indexCarte) {
        this.joueurEmetteur = joueurEmetteur;
        this.joueurRecepteur = joueurRecepteur;
        this.indexCarte = indexCarte;
    }

    /**
     * Cette méthode permet de donner à un autre joueur une CarteVille ou prendre à un autre joueur une CarteVille.
     * Ces deux actions utilisent le meme processus, le fait d'enlever une carteVille d'un joueur et la rajouter aux cartes de l'autre joueur,
     * Un joueur avec le role de Chercheur n'est pas obligé d'utiliser une CarteVille de son emplacement.
     * @param p
     * @throws VillesPasPareilException
     * @throws NombresDeCartesMaxAtteindsException
     */

    @Override
    public void executerAction(Partie p) throws PartieNonDemarreeException, PartieTermineeException, NombresDeCartesMaxAtteindsException, PartageConnaissancesException { // le joueur emetteur n'est pas forcément celui qui joue ce tour
        Ville villeEmetteur = joueurEmetteur.getEmplacement();
        Ville villeRecepteur = joueurRecepteur.getEmplacement();
        boolean carteVilleTrouvee = false;
        ICarteJoueur ic = joueurEmetteur.getCartes().get(this.indexCarte);
        if(ic instanceof CarteVilleJoueur){
            carteVilleTrouvee = true;
        }
        if (joueurRecepteur.getCartes().size() >= 7) {
            throw new NombresDeCartesMaxAtteindsException();
        }

        if (villeEmetteur.getNom().equals(villeRecepteur.getNom()) && carteVilleTrouvee) {
            CarteVilleJoueur carte = (CarteVilleJoueur) joueurEmetteur.getCartes().get(this.indexCarte);
            if (joueurEmetteur.getRole().equals(Role.CHERCHEUSE)) {
                joueurRecepteur.getCartes().add(carte);
                joueurEmetteur.getCartes().remove(carte);
                p.getJoueurCourant().setNbActions(p.getJoueurCourant().getNbActions() - 1);
            }
            else if (carte.getLaVille().getNom().equals(villeEmetteur.getNom())) {
                joueurRecepteur.getCartes().add(carte);
                joueurEmetteur.getCartes().remove(carte);
                p.getJoueurCourant().setNbActions(p.getJoueurCourant().getNbActions() - 1);
            }
            else {
                throw new PartageConnaissancesException();
            }
        }
        else {
            throw new PartageConnaissancesException();
        }
    }
}

