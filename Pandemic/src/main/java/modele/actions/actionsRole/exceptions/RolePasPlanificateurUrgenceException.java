package modele.actions.actionsRole.exceptions;

public class RolePasPlanificateurUrgenceException extends Exception {
    public RolePasPlanificateurUrgenceException() {
        super("Ce joueur n'est pas planificateur urgence !");
    }

    public RolePasPlanificateurUrgenceException(String message) {
        super(message);
    }
}
