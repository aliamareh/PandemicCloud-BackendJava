package modele.actions.actionsRole.exceptions;

public class AttendreProchainTourPourCetteActionException extends Exception {
    public AttendreProchainTourPourCetteActionException() {
        super("Il faut attentre le prochain tour pour cette action !");
    }

    public AttendreProchainTourPourCetteActionException(String message) {
        super(message);
    }
}
