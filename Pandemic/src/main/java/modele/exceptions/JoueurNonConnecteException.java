package modele.exceptions;

public class JoueurNonConnecteException extends Exception {
    public JoueurNonConnecteException() {
        super("Le joueur ne s'est pas authentifié !");
    }

    public JoueurNonConnecteException(String message) {
        super(message);
    }
}
