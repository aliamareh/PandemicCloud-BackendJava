package modele.exceptions;

public class EvenementDejaEnCoursException extends Exception {
    public EvenementDejaEnCoursException() {
        super("Cet evenement est déjà en cours !");
    }

    public EvenementDejaEnCoursException(String message) {
        super(message);
    }
}
