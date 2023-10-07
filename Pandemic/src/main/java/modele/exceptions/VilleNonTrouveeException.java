package modele.exceptions;

public class VilleNonTrouveeException extends Exception {
    public VilleNonTrouveeException() {
        super("Ville introuvable !");
    }

    public VilleNonTrouveeException(String message) {
        super(message);
    }
}
