package modele.exceptions;

public class MaladiesNonIntialiseesException extends Exception {
    public MaladiesNonIntialiseesException() {
        super("Maladies non initialisées !");
    }

    public MaladiesNonIntialiseesException(String message) {
        super(message);
    }
}
