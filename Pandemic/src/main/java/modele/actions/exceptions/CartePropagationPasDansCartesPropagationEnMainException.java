package modele.actions.exceptions;

public class CartePropagationPasDansCartesPropagationEnMainException extends Exception{
    public CartePropagationPasDansCartesPropagationEnMainException() {
        super("Carte propagation pas dans cartes propagation en main !");
    }

    public CartePropagationPasDansCartesPropagationEnMainException(String message) {
        super(message);
    }
}
