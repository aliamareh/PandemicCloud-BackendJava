package modele.actions.actionsRole.exceptions;

public class RolePasRepartiteurException extends Exception{
    public RolePasRepartiteurException() {
        super("Ce joueur n'est pas un répartiteur !");
    }

    public RolePasRepartiteurException(String message) {
        super(message);
    }
}
