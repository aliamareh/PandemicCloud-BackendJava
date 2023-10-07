package modele.actions.exceptions;

public class PartageConnaissancesException extends Exception {
    public PartageConnaissancesException() {
        super("Problème de partage de connaissances !");
    }

    public PartageConnaissancesException(String message) {
        super(message);
    }
}
