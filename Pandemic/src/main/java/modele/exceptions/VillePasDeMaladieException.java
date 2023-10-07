package modele.exceptions;

public class VillePasDeMaladieException extends Exception {
    public VillePasDeMaladieException() {
        super("Il n y a pas de cube maladie dans la ville !");
    }

    public VillePasDeMaladieException(String message) {
        super(message);
    }
}
