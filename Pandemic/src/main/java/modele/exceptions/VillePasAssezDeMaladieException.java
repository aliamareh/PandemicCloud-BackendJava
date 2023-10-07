package modele.exceptions;

public class VillePasAssezDeMaladieException extends Exception {
    public VillePasAssezDeMaladieException() {
        super("Il n'y a pas assez de cube maladie sur cette ville !");
    }

    public VillePasAssezDeMaladieException(String message) {
        super(message);
    }
}
