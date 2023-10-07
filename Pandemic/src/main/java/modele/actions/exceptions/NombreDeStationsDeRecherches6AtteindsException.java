package modele.actions.exceptions;

public class NombreDeStationsDeRecherches6AtteindsException extends Exception {
    String phrase;

    public NombreDeStationsDeRecherches6AtteindsException() {
        super("Vous avez atteint le maximum de station de recherches !");
    }

    public NombreDeStationsDeRecherches6AtteindsException(String phrase) {
        this.phrase = phrase;
    }
}
