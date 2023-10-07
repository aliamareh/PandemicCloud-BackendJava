package modele.exceptions;

public class MauvaisLoginException extends Exception {
    public MauvaisLoginException() {
        super("Format de pseudo incorrect !");
    }

    public MauvaisLoginException(String message) {
        super(message);
    }
}
