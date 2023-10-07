package modele.actions.exceptions;

public class VillesPasPareilException extends Exception {
    public VillesPasPareilException() {
        super("Ville pas pareil !");
    }

    public VillesPasPareilException(String message) {
        super(message);
    }
}
