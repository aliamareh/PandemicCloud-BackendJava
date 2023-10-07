package modele.exceptions;

public class PartieNonExistanteException extends Exception {
    public PartieNonExistanteException() {
        super("Aucune partie n'a été trouvée via cet id !");
    }

    public PartieNonExistanteException(String message) {
        super(message);
    }
}
