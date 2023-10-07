package modele.actions.exceptions;

public class VillePasVoisineException extends Exception {
    public VillePasVoisineException() {
        super("Ville pas voisine !");
    }

    public VillePasVoisineException(String message) {
        super(message);
    }
}
