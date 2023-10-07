package modele.exceptions;

public class JoueurIntrouvablePartie extends Exception {
    public JoueurIntrouvablePartie() {
        super("Le joueur est introuvable !");
    }

    public JoueurIntrouvablePartie(String message) {
        super(message);
    }
}
