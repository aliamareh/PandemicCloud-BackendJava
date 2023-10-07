package modele.actions.actionsRole.exceptions;

public class CarteIntrouvableException extends Exception {
    public CarteIntrouvableException() {
        super("carte instrouvale !");
    }

    public CarteIntrouvableException(String message) {
        super(message);
    }
}
