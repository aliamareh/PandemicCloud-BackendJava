package modele.actions.actionsRole.exceptions;

public class PlanificateurUrgenceCarteDejaPriseException extends Exception {
    public PlanificateurUrgenceCarteDejaPriseException() {
        super("La carte planification d'urgence est déjà prise !");
    }

    public PlanificateurUrgenceCarteDejaPriseException(String message) {
        super(message);
    }
}
