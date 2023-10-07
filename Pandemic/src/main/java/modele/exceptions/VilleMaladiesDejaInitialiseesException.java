package modele.exceptions;

public class VilleMaladiesDejaInitialiseesException extends Exception {
    public VilleMaladiesDejaInitialiseesException() {
        super("Erreur mise en place du jeu : ville maladie dèjà initialisée !");
    }

    public VilleMaladiesDejaInitialiseesException(String message) {
        super(message);
    }
}
