package modele.exceptions;

public class ListeJoueursNonCompletetException extends Exception {
    public ListeJoueursNonCompletetException() {
        super( "Impossible de jouer à la partie. Des joueurs sont manquants !");
    }

    public ListeJoueursNonCompletetException(String message) {
        super(message);
    }
}
