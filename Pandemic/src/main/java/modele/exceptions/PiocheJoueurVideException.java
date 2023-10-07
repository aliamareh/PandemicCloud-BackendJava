package modele.exceptions;

public class PiocheJoueurVideException extends Exception {
    public PiocheJoueurVideException() {
        super("La pioche joueur est vide !");
    }

    public PiocheJoueurVideException(String message) {
        super(message);
    }
}
