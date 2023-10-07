package modele.exceptions;

public class VilleVoisineAElleMemeException extends Exception {
    public VilleVoisineAElleMemeException() {
        super("Erreur mise en place du jeu : ville voisine à elle même !");
    }

    public VilleVoisineAElleMemeException(String message) {
        super(message);
    }
}
