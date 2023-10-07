package modele.exceptions;

public class NombreDeJoueursIncorrectException extends Exception {
    public NombreDeJoueursIncorrectException() {
        super("Nombre de joueurs incorrect, choisir un nombre entre 2 et 4 !");
    }

    public NombreDeJoueursIncorrectException(String message) {
        super(message);
    }
}
