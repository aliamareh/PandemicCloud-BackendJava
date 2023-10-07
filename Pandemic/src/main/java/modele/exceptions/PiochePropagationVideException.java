package modele.exceptions;

public class PiochePropagationVideException extends Exception {
    public PiochePropagationVideException() {
        super("La pioche propagation est vide !");
    }

    public PiochePropagationVideException(String message) {
        super(message);
    }
}
