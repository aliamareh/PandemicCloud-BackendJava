package modele.actions.exceptions;

public class CartePropagationPasDansDefaussePropagationException extends Exception {
    public CartePropagationPasDansDefaussePropagationException() {
        super("Carte propagation pas dans défausse propagation");
    }

    public CartePropagationPasDansDefaussePropagationException(String message) {
        super(message);
    }
}
