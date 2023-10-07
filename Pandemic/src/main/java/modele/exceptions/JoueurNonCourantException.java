package modele.exceptions;

public class JoueurNonCourantException extends Exception {
    public JoueurNonCourantException() {
        super("Ce n'est pas au tour de ce joueur de jouer !");
    }

    public JoueurNonCourantException(String message) {
        super(message);
    }
}
