package modele.exceptions;

public class LoginDejaUtiliseException extends Exception {
    public LoginDejaUtiliseException() {
        super("Utilisateur déjà connecté !");
    }

    public LoginDejaUtiliseException(String message) {
        super(message);
    }
}
