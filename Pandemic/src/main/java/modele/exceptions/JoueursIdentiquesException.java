package modele.exceptions;

public class JoueursIdentiquesException extends Exception {
    public JoueursIdentiquesException() {
        super("Les joueurs sont identiques !");
    }

    public JoueursIdentiquesException(String message) {
        super(message);
    }
}
