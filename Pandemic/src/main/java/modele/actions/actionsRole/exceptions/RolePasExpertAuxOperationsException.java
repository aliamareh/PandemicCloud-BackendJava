package modele.actions.actionsRole.exceptions;

public class RolePasExpertAuxOperationsException extends Exception {
    public RolePasExpertAuxOperationsException() {
        super("Ce joueur n'est pas expert aux opérations !");
    }

    public RolePasExpertAuxOperationsException(String message) {
        super(message);
    }
}
