package modele.exceptions;

public class EvenementPasEnCoursException extends Exception {
    public EvenementPasEnCoursException() {
        super("Cet évenement n'est pas en cours !");
    }

    public EvenementPasEnCoursException(String message) {
        super(message);
    }
}
