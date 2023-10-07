package modele.actions.exceptions;

public class CarteVillePasPareilVilleDestinationException extends Exception {
    public CarteVillePasPareilVilleDestinationException() {
        super("La ville sur la carte ville n'est pas pareil que la ville de destination !");
    }

    public CarteVillePasPareilVilleDestinationException(String message) {
        super(message);
    }
}
