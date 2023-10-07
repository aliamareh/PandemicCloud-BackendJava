package modele.exceptions;

public class JoueurDejaPresentException extends Exception {
    public JoueurDejaPresentException() {
        super("Ce joueur est déjà dans la partie !");
    }

    public JoueurDejaPresentException(String message) {
        super(message);
    }
}
