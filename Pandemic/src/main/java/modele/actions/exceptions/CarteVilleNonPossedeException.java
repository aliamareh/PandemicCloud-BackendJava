package modele.actions.exceptions;

public class CarteVilleNonPossedeException extends Exception {
    public CarteVilleNonPossedeException() {
        super("Carte ville non possédée !");
    }

    public CarteVilleNonPossedeException(String message) {
        super(message);
    }
}
