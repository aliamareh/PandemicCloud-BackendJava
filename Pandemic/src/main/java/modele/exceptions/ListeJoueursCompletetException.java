package modele.exceptions;

public class ListeJoueursCompletetException extends Exception {
    public ListeJoueursCompletetException() {
        super("La partie est déjà complète !");
    }

    public ListeJoueursCompletetException(String message) {
        super(message);
    }
}
