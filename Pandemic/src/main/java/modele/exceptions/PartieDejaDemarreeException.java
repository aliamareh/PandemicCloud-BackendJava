package modele.exceptions;

public class PartieDejaDemarreeException extends Exception {
    public PartieDejaDemarreeException() {
        super("Cette partie est déjà en cours !");
    }

    public PartieDejaDemarreeException(String message) {
        super(message);
    }
}
