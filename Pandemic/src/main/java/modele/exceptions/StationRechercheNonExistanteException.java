package modele.exceptions;

public class StationRechercheNonExistanteException extends Exception {
    public StationRechercheNonExistanteException() {
        super("Station de recherche inexistante !");
    }

    public StationRechercheNonExistanteException(String message) {
        super(message);
    }
}
