package modele.actions.exceptions;

public class NombreDeStationsDeRecherchesMaxDépasséException extends Exception {
    public NombreDeStationsDeRecherchesMaxDépasséException() {
        super("Nombre de station de recherche max dépassées !");
    }

    public NombreDeStationsDeRecherchesMaxDépasséException(String message) {
        super(message);
    }
}
