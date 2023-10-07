package modele;

public enum Role {

	SCIENTIFIQUE("Scientifique", "Vous n'avez besoin que de 4 cartes de la même couleur pour découvrir un remède."),
	CHERCHEUSE("Chercheuse", "Lorsque vous partagez des connaissances, vous pouvez donner n'importe quelle carte Ville de votre main. La carte n'a pas à correspondre à la ville où vous êtes. " +
			"Durant son tour, un joueur qui partage des connaissances avec vous peut vous prendre n'importe quelle carte."),
	REPARTITEUR("Réparatiteur", "Déplacez le pion d'un autre joueur comme si c'était le vôtre. Pour une action, déplacez un pion sur une ville où se trouve un autre pion. " +
			"Vous devez avoir la permission du propriétaire du pion qui sera déplacé."),
	SPE_MISE_EN_QUARANTAINE("Spécialiste en mise en quarantaine", "Empêchez les éclosions et le placement de cubes dans la ville où " +
			"vous êtes ainsi que dans toutes les villes qui y sont reliées."),
	MEDECIN("Médecin", "Retirez tous les cubes d'une couleur lorsque vous traitez une maladie. " +
			"Dans la ville où vous êtes, retirez automatiquement tous les cubes de la couleur d'une maladie guérie " +
			"(et empêchez d'autres cubes d'une maladie guérie d'y être placé)."),
	EXPERT_AUX_OPERATIONS("Expert aux opérations", "Pour une action, vous pouvez construire une station de recherche dans la ville que vous occupez (sans avoir à défausser). " +
			"Une fois par tour, pour une action, défaussez une carte Ville pour vous déplacer d'une ville " +
			"avec une station de recherche vers n'importe quelle ville."),
	PLANIFICATEUR_URGENCE("Planificateur d'urgence", "Pour une action, prenez une carte Évènement de la défausse et entreposez-la sur cette carte. " +
			"Lorsque vous jouez la carte Évènement entreposée, retirez-la de la partie. " +
			"Limite de 1 carte Évènement sur cette carte. Elle ne fait pas partie de votre main.");
	
	
	private final String nom ;
	private final String description;
	
	Role(String nom, String description) {
		this.nom = nom;
		this.description = description;
	}
	
	public String getNom() {
		return  this.nom ;
	}
	
	public String getDescription(){return this.description; }
}
