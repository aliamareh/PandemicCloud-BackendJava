package modele.exceptions;

public class MaladieNonExistanteException extends Exception {
    public MaladieNonExistanteException() {
        super("La maladie est inexistante !");
    }

    public MaladieNonExistanteException(String message) {
        super(message);
    }
}
