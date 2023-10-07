package modele.exceptions;

public class PartieNonDemarreeException extends Exception {
    public PartieNonDemarreeException() {
        super("Cette partie n'a pas encore été démarrée !");
    }

    public PartieNonDemarreeException(String message) {
        super(message);
    }
}
