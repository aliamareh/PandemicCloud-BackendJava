package modele.actions.exceptions;

public class NombresDeStationDeRechercheInférieurà6Exception extends Exception {
    String phrase;

    public NombresDeStationDeRechercheInférieurà6Exception() {
        super("Nombres de station de recherche inférieur à 6 !");
    }

    public NombresDeStationDeRechercheInférieurà6Exception(String phrase) {
        this.phrase = phrase;
    }
}
