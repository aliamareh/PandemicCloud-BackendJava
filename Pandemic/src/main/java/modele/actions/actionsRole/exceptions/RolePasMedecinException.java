package modele.actions.actionsRole.exceptions;

public class RolePasMedecinException extends Exception {
    public RolePasMedecinException() {
        super("Ce joueur n'est pas madecin !");
    }

    public RolePasMedecinException(String message) {
        super(message);
    }
}
