package modele.exceptions;

public class PlusDeCubesMaladieDisponible extends Exception {
    public PlusDeCubesMaladieDisponible() {
        super("Il n y a plus de cubes maladie !");
    }

    public PlusDeCubesMaladieDisponible(String message) {
        super(message);
    }
}
