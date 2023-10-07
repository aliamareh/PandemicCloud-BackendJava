package modele.exceptions;

public class NombreDeStationsDeRechercheIncorrect extends Exception {
    public NombreDeStationsDeRechercheIncorrect() {
        super("Le nombre de stations de recherches est incorrect !");
    }

    public NombreDeStationsDeRechercheIncorrect(String message) {
        super(message);
    }
}
