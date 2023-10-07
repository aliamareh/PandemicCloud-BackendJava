package modele.exceptions;

public class StationRechercheExisteException extends Exception {
    public StationRechercheExisteException() {
        super("Il existe déjà une station de rechrche !");
    }

    public StationRechercheExisteException(String message) {
        super(message);
    }
}
