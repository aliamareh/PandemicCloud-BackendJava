package modele.actions.exceptions;

public class VilleSansStationDeRechercheException extends Exception {
    public VilleSansStationDeRechercheException() {
        super("Cette ville ne contient pas de station de recherche !");
    }

    public VilleSansStationDeRechercheException(String message) {
        super(message);
    }
}
