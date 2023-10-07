package modele.exceptions;

public class PartieNonTermineeException extends Exception {
    public PartieNonTermineeException() {
        super("La partie n'est pas encore terminée !");
    }

    public PartieNonTermineeException(String message) {
        super(message);
    }
}
