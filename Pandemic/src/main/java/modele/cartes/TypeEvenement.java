package modele.cartes;

public enum TypeEvenement {
    PONT_AERIEN("PONT AÉRIEN", "Déplacez un pion quelconque sur la ville de votre choix. " +
            "Vous devez avoir la permission du propriétaire du pion qui sera déplacé."),
    SUBVENTION_PUBLIQUE("SUBVENTION PUBLIQUE", "Placez 1 station de recherche dans la ville de votre choix " +
            "(sans avoir à défausser une carte Ville)."),
    PREVISION("PRÉVISION", "Piochez, consultez et réorganisez dans l'ordre de votre choix les 6 premières cartes du paquet Propagation. " +
            "Replacez-les ensuite sue le dessus du paquet."),
    PAR_UNE_NUIT_TRANQUILLE("PAR UNE NUIT TRANQUILLE", "Ne faites pas la prochaine phase Propagation des maladies " +
            "(ne dévoilez aucune carte Propagation)."),
    POPULATION_RESILIENTE("POPULATION RÉSILIENTE", "Retirez du jeu 1 carte de votre choix de la défausse Propagation." +
            "(Vous pouvez jouer Population Résiliente entre les étapes Infection et Intensification d'une carte Épidémie.)");

    private final String nomEvenement ;
    
    private final String description;

    TypeEvenement(String nomEvenement, String description) {
        this.nomEvenement = nomEvenement ;
        this.description = description;
    }

    public String getNomEvenement() {return this.nomEvenement ;}
    
    public String getDescription(){ return this.description; }
    
    
    @Override
    public String toString() {
        return this.nomEvenement+" description : "+this.description;
    }
}
