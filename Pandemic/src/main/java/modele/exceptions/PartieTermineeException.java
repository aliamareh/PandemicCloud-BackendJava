package modele.exceptions;

public class PartieTermineeException extends Exception {
    public PartieTermineeException() {
        super("Cette partie est terminée !");
    }

    public PartieTermineeException(String message) {
        super(message);
    }
}
