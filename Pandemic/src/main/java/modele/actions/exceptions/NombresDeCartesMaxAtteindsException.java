package modele.actions.exceptions;

public class NombresDeCartesMaxAtteindsException extends Exception {
    public NombresDeCartesMaxAtteindsException() {
        super("Vous avez atteint le maximum de carte autorisé !");
    }

    public NombresDeCartesMaxAtteindsException(String message) {
        super(message);
    }
}
