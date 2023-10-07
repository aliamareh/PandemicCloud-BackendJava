package modele.exceptions;

public class MaladieEradiqueException extends Exception {
    public MaladieEradiqueException() {
        super("Cette maladie est éradiquée !");
    }

    public MaladieEradiqueException(String message) {
        super(message);
    }
}
