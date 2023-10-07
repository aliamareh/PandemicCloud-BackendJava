package modele.actions.exceptions;

public class NombresDeReorganisationMaxAtteindsException extends Exception {
    public NombresDeReorganisationMaxAtteindsException() {
        super("Nombres de réorganisation max atteinds !");
    }

    public NombresDeReorganisationMaxAtteindsException(String message) {
        super(message);
    }
}
