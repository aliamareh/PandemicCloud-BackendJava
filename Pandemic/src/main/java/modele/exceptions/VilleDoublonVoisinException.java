package modele.exceptions;

public class VilleDoublonVoisinException extends Exception {
    public VilleDoublonVoisinException() {
        super("Erreur mise en place du jeu : ville doublon !");
    }

    public VilleDoublonVoisinException(String message) {
        super(message);
    }
}
