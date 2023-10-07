package modele.actions.exceptions;

public class VilleSansCetteMaladieException extends Exception {
    public VilleSansCetteMaladieException() {
        super("La ville spécifiée ne contient pas la maladie spécifiée !");
    }

    public VilleSansCetteMaladieException(String message) {
        super(message);
    }
}
