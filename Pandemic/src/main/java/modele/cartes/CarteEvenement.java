package modele.cartes;

public class CarteEvenement implements ICarteJoueur {
    private TypeEvenement typeEvenement;

    public CarteEvenement(TypeEvenement typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    /**
     *
     * @return : le type de l'evenement
     */
    public TypeEvenement getTypeEvenement() {
        return typeEvenement;
    }

    /**
     *
     * @param typeEvenement
     */
    public void setTypeEvenement(TypeEvenement typeEvenement) {
        this.typeEvenement = typeEvenement;
    }
    
    @Override
    public String toString() {
        return "CarteEvenement :evenement : "+this.typeEvenement;
    }
}
