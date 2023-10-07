package org.projetpandemic.pandemicws.modele;

import donnees.BibDonneesMongoDB;
import modele.*;
import modele.actions.TypeAction;
import modele.actions.actionsRole.exceptions.*;
import modele.actions.exceptions.*;
import modele.cartes.CarteEvenement;
import modele.cartes.ICarteJoueur;
import modele.exceptions.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FacadePandemicImpl implements FacadePandemic {
	private TreeSet<String> joueursConnectes;
	private BibDonneesMongoDB bibDonnees;
	private Map<Long, Partie> lesParties;
	private long nextId = 1L;
	
	public FacadePandemicImpl(){
		this.bibDonnees = new BibDonneesMongoDB();
		this.joueursConnectes = new TreeSet<>();
		this.lesParties = new TreeMap<>();
	}
	
	@Override
	public boolean estConnecte(String pseudo) throws MauvaisLoginException {
		Objects.requireNonNull(pseudo);
		boolean result;
		if(pseudo.length() < 3){
			throw new MauvaisLoginException();
		}
		
		if(this.joueursConnectes.contains(pseudo)){
			result = true;
		}
		else
		{
			result = false;
		}
		
		return result;
	}
	
	@Override
	public void connexion(String pseudo,String password) throws MauvaisLoginException, LoginDejaUtiliseException {
		Objects.requireNonNull(pseudo);
		Map<String,String> userInfos = bibDonnees.getUserByPseudo(pseudo);
		if( Objects.isNull(userInfos) || !userInfos.get("password").equals(password)){
			throw new MauvaisLoginException();
		}
		
		if(estConnecte(pseudo)){
			throw new LoginDejaUtiliseException();
		}

		this.joueursConnectes.add(pseudo);
	}
	@Override
	public Map<String,String> getUserByPseudo(String pseudo) throws MauvaisLoginException{
        Map<String,String> userInfos = bibDonnees.getUserByPseudo(pseudo);
        if(Objects.isNull(userInfos)){
            throw new MauvaisLoginException();
        }
		return userInfos;
	}



	@Override
	public void inscription(String pseudo, String password) throws MauvaisLoginException, LoginDejaUtiliseException{
		if( pseudo.length()<3){
			throw new MauvaisLoginException();
		}
		Map<String,String> userInfos = bibDonnees.getUserByPseudo(pseudo);

		if(Objects.nonNull(userInfos)){
			throw new LoginDejaUtiliseException();
		}
		this.bibDonnees.insertUser(pseudo,password);

	}

	@Override
	public void deconnexion(String pseudo) throws MauvaisLoginException{
		if( !joueursConnectes.contains(pseudo)){
			throw new MauvaisLoginException();
		}
		this.joueursConnectes.remove(pseudo);
	}
	
	@Override
	public long creerPartie(int nbJoueurs) throws NombreDeJoueursIncorrectException {
		if(nbJoueurs < 2 || nbJoueurs > 4)
			throw new NombreDeJoueursIncorrectException();
		
		 long id = this.nextId;
		 Partie p = new Partie();
		 p.setIdPartie(id);
		 p.setNbJoueur(nbJoueurs);
		
		this.lesParties.put(id, p);
		
		this.nextId++;
		return id;
	}
	
	@Override
	public void rejoindrePartie(long idPartie, String j) throws MauvaisLoginException, JoueurNonConnecteException, PartieNonExistanteException, JoueurDejaPresentException, ListeJoueursCompletetException {
		Objects.requireNonNull(j);
		if(j.length() < 3){
			throw new MauvaisLoginException();
		}
		if(!estConnecte(j)){
			throw new JoueurNonConnecteException();
		}
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		
		this.lesParties.get(idPartie).setJoueur(new Joueur(j));
	}
	
	@Override
	public void verifierPartieEnCours(long idPartie) throws PartieTermineeException, PartieNonDemarreeException, PartieNonExistanteException {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		Partie p = this.lesParties.get(idPartie);
		if(p.partieTerminee()){
			throw new PartieTermineeException();
		}
		if(!p.getPartieDemaree()){
			throw new PartieNonDemarreeException();
		}
	}
	
	@Override
	public void verifierSiPeutJouer(String j, long idPartie) throws MauvaisLoginException, JoueurNonConnecteException,
	 PartieNonExistanteException, JoueurIntrouvablePartie, PartieNonDemarreeException, PartieTermineeException, JoueurNonCourantException {
		Objects.requireNonNull(j);
		if(j.length() < 3){
			throw new MauvaisLoginException();
		}
		if(!estConnecte(j)){
			throw new JoueurNonConnecteException();
		}
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		Joueur joueur = p.getJoueurCourant();
		if(!joueur.getPseudo().equals(j)){
			throw new JoueurNonCourantException();
		}
	}
	
	@Override
	public void jouerActionContreMaladie(long idPartie, String j, String maladie, List<Integer> cartesVillesJoueurs ,
	 String typeAction) throws PartieNonExistanteException, JoueurIntrouvablePartie, MaladieNonExistanteException, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, JoueurNonConnecteException, JoueurNonCourantException, PartieTermineeException, MauvaisLoginException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException {
		verifierSiPeutJouer(j, idPartie);
		
		Partie p = this.lesParties.get(idPartie);
		Joueur joueur = p.getJoueurByPseudo(j);
		p.jouerActionContreMaladie(joueur, p.getPlateau().getMaladieByNom(maladie), cartesVillesJoueurs ,
		 TypeAction.valueOf(typeAction));
	}
	
	@Override
	public void jouerActionSurVille(long idPartie, String j, String typeAction, String ville) throws PartieNonExistanteException, JoueurIntrouvablePartie, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, JoueurNonConnecteException, JoueurNonCourantException, PartieTermineeException, MauvaisLoginException, VilleNonTrouveeException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException {
		verifierSiPeutJouer(j, idPartie);
		
		Partie p = this.lesParties.get(idPartie);
		Joueur joueur = p.getJoueurByPseudo(j);
		p.jouerActionSurVille(joueur, TypeAction.valueOf(typeAction), p.getPlateau().getVilleByNom(ville));
	}
	
	@Override
	public void jouerActionPartagerConnaissance(long idPartie, String j1, String j2, int carte) throws PartieNonExistanteException, JoueurIntrouvablePartie, PlusDeCubesMaladieDisponible, MauvaisLoginException, JoueurNonConnecteException, PartieNonDemarreeException, JoueurNonCourantException, PartieTermineeException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException {
		Objects.requireNonNull(j2);
		if(j2.length() < 3){
			throw new MauvaisLoginException();
		}
		if(!estConnecte(j2)){
			throw new JoueurNonConnecteException();
		}
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j2))){
			throw new JoueurIntrouvablePartie();
		}
		
		p.jouerActionPartagerConnaissance(p.getJoueurByPseudo(j1), p.getJoueurByPseudo(j2),carte);
	}
	
	@Override
	public boolean getPartieDemarree(long idPartie) throws PartieNonExistanteException {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		
		return this.lesParties.get(idPartie).getPartieDemaree();
	}
	
	@Override
	public void demarrerPartie(long idPartie) throws PartieNonExistanteException, PartieDejaDemarreeException, PiocheJoueurVideException, VilleNonTrouveeException, VilleMaladiesDejaInitialiseesException, StationRechercheExisteException, ListeJoueursNonCompletetException, VilleVoisineAElleMemeException, VilleDoublonVoisinException, PartieTermineeException, PlusDeCubesMaladieDisponible {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		Partie p = this.lesParties.get(idPartie);
		if(p.getPartieDemaree()){
			throw new PartieDejaDemarreeException();
		}
		if(p.partieTerminee()){
			throw new PartieTermineeException();
		}
		p.mettreEnplacePartie();
	}
	
	@Override
	public Joueur getJoueurCourant(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException {
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		
		return p.getJoueurCourant();
	}
	
	@Override
	public int getNbJoueurs(long idPartie) throws PartieNonExistanteException {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}

		return this.lesParties.get(idPartie).getNbJoueur();
	}

	@Override
	public List<String> getJoueursConnectes(long idPartie) throws PartieNonExistanteException {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		List<String> joueurs = new ArrayList<>();
		this.lesParties.get(idPartie).getJoueurs().forEach(j -> joueurs.add(j.getPseudo()));
		return joueurs;
	}
	
	@Override
	public boolean getPartieTerminee(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		Partie p = this.lesParties.get(idPartie);
		if(!p.getPartieDemaree()){
			throw new PartieNonDemarreeException();
		}
		
		return p.partieTerminee();
	}
	
	@Override
	public boolean getPartieGagnee(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException, PartieNonTermineeException {
		if(!this.lesParties.containsKey(idPartie)){
			throw new PartieNonExistanteException();
		}
		Partie p = this.lesParties.get(idPartie);
		if(!p.getPartieDemaree()){
			throw new PartieNonDemarreeException();
		}
		if(!p.partieTerminee()){
			throw new PartieNonTermineeException();
		}
		
		return p.partieGagnee();
	}
	
	@Override
	public Joueur joueurSuivant(long idPartie) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException, NombresDeCartesMaxAtteindsException {
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		
		return p.mAJJoueurCourant();
	}
	
	@Override
	public Map<Long, Partie> getParties() {
		return this.lesParties;
	}
	
	@Override
	public List<ICarteJoueur> piocherCarteJoueur(long idPartie, String j, int nbCartes) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException, PiocheJoueurVideException, JoueurIntrouvablePartie, NombresDeCartesMaxAtteindsException, PlusDeCubesMaladieDisponible {
		Objects.requireNonNull(j);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		
		return p.piocherCarteJoueur(p.getJoueurByPseudo(j), nbCartes);
	}
	
	@Override
	public void defausserCarteJoueur(long idPartie, String j, Integer... index) throws PartieNonExistanteException, PartieNonDemarreeException, PartieTermineeException, JoueurIntrouvablePartie {
		Objects.requireNonNull(j);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		
		p.defaussercarteJoueur(p.getJoueurByPseudo(j), index);
	}
	
	@Override
	public void ajouterStationRecherche(long idPartie, String ville) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, VilleNonTrouveeException, StationRechercheExisteException {
		Objects.requireNonNull(ville);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		p.setStationRecherche(ville);
	}
	
	@Override
	public void retirerStationRecherche(long idPartie, String ville) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, VilleNonTrouveeException, StationRechercheNonExistanteException {
		Objects.requireNonNull(ville);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		p.removeSationRecherche(ville);
	}
	
	@Override
	public void jouerEvenementParUneNuitTranquille(long idPartie, String j, int positioncarte) throws PartieNonExistanteException, JoueurIntrouvablePartie, PartieNonDemarreeException, PartieTermineeException, CarteIntrouvableException {
		Objects.requireNonNull(j);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		p.jouerEvenementParUneNuitTranquille(p.getJoueurByPseudo(j), positioncarte);
	}
	
	@Override
	public void jouerEvenementPontAerien(long idPartie, String j, int positioncarte, String joueuradeplacer, String destination) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, VilleNonTrouveeException, CarteIntrouvableException {
		Objects.requireNonNull(j);
		Objects.requireNonNull(joueuradeplacer);
		Objects.requireNonNull(destination);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j)) || Objects.isNull(p.getJoueurByPseudo(joueuradeplacer))){
			throw new JoueurIntrouvablePartie();
		}

		p.jouerEvenementPontAerien(p.getJoueurByPseudo(j),positioncarte, p.getJoueurByPseudo(joueuradeplacer), p.getPlateau().getVilleByNom(destination));
	}
	
	@Override
	public void jouerEvenementPopulationResiliente(long idPartie, String j, int positioncarte, int cartepropagation) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, CarteIntrouvableException, CartePropagationPasDansDefaussePropagationException {
		Objects.requireNonNull(j);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		p.jouerEvenementPopulationResiliente(p.getJoueurByPseudo(j), positioncarte, cartepropagation);
	}
	
	@Override
	public void jouerEvenementPrevisionPhase1(long idPartie, String j, int positioncarte) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, CarteIntrouvableException, PiochePropagationVideException, EvenementDejaEnCoursException {
		Objects.requireNonNull(j);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}

		p.jouerEvenementPrevisionPhase1(p.getJoueurByPseudo(j), positioncarte);
	}
	
	@Override
	public void jouerEvenementPrevisionPhase2(long idPartie) throws PartieNonDemarreeException,
			PartieTermineeException,
			EvenementPasEnCoursException, PartieNonExistanteException
	{
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);

		p.jouerEvenementPrevisionPhase2();
	}
	
	@Override
	public void jouerEvenementSubventionPublique(long idPartie, String j, int positioncarte, String villeChoisie) throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, JoueurIntrouvablePartie, VilleNonTrouveeException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, StationRechercheExisteException {
		Objects.requireNonNull(j);
		Objects.requireNonNull(villeChoisie);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		p.jouerEvenementSubventionPublique(p.getJoueurByPseudo(j), positioncarte, p.getPlateau().getVilleByNom(villeChoisie));
	}
	
	@Override
	public void jouerEvenementSubventionPublique2(long idPartie, String j, int positioncarte, String villeChoisie, String villeaenlever) throws PartieNonDemarreeException, PartieTermineeException, JoueurIntrouvablePartie, VilleNonTrouveeException, CarteIntrouvableException, StationRechercheExisteException, StationRechercheNonExistanteException, PartieNonExistanteException {
		Objects.requireNonNull(j);
		Objects.requireNonNull(villeChoisie);
		Objects.requireNonNull(villeaenlever);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p.getJoueurByPseudo(j))){
			throw new JoueurIntrouvablePartie();
		}
		p.jouerEvenementSubventionPublique2(p.getJoueurByPseudo(j), positioncarte, p.getPlateau().getVilleByNom(villeChoisie), p.getPlateau().getVilleByNom(villeaenlever));
	}
	
	@Override
	public void jouerActionRetirerCubeParMedecin(long idPartie, String j) throws JoueurIntrouvablePartie, PartieNonExistanteException, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException {
		Objects.requireNonNull(j);
		verifierPartieEnCours(idPartie);
		Partie p = this.lesParties.get(idPartie);
		Joueur joueur = p.getJoueurByPseudo(j);
		if(Objects.isNull(joueur)){
			throw new JoueurIntrouvablePartie();
		}
		p.jouerActionRetirerCubeParMedecin(joueur);
	}
	
	@Override
	public void jouerActionDeplacerPionParRepartiteur(long idPartie, String jcourant, String jcontrole, String typeAction, String villeDest) throws JoueurIntrouvablePartie, PartieNonExistanteException, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, RolePasRepartiteurException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException {
		Objects.requireNonNull(jcourant);
		Objects.requireNonNull(jcontrole);
		Objects.requireNonNull(typeAction);
		Objects.requireNonNull(villeDest);
		verifierPartieEnCours(idPartie);
		
		Partie p = this.lesParties.get(idPartie);
		
		Ville v = p.getPlateau().getVilleByNom(villeDest);
		
		Joueur jc = p.getJoueurByPseudo(jcourant);
		Joueur jctrl = p.getJoueurByPseudo(jcontrole);
		
		if(Objects.isNull(jc) || Objects.isNull(jctrl)){
			throw new JoueurIntrouvablePartie();
		}
		
		p.jouerActionDeplacerPionParRepartiteur(jc, jctrl, TypeAction.valueOf(typeAction), v);
	}
	
	@Override
	public void jouerActionDeplacerVersJoueurParRepartiteur(long idPartie, String jcourant, String jadeplacer, String jarejoindre) throws PartieNonExistanteException, JoueurIntrouvablePartie, PlusDeCubesMaladieDisponible, PartieNonDemarreeException, MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException, NombresDeCartesMaxAtteindsException, RolePasRepartiteurException, JoueurNonCourantException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException, RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException, VilleSansStationDeRechercheException {
		Objects.requireNonNull(jcourant);
		Objects.requireNonNull(jadeplacer);
		Objects.requireNonNull(jarejoindre);
		verifierPartieEnCours(idPartie);
		
		Partie p = this.lesParties.get(idPartie);
		
		Joueur jc = p.getJoueurByPseudo(jcourant);
		Joueur jad = p.getJoueurByPseudo(jadeplacer);
		Joueur jar = p.getJoueurByPseudo(jarejoindre);
		
		if(Objects.isNull(jc) || Objects.isNull(jad) || Objects.isNull(jar)){
			throw new JoueurIntrouvablePartie();
		}
		
		p.jouerActionDeplacerVersJoueurParRepartiteur(jc, jad, jar);
	}


	@Override
	public void jouerActionPiocherCarteEvenementParPlanificateur(long idPartie, String jcourant,String evenement)
			throws PartieNonExistanteException, PartieTermineeException,
			PartieNonDemarreeException, CarteIntrouvableException, JoueurNonCourantException, RolePasPlanificateurUrgenceException,
			PlusDeCubesMaladieDisponible, PartieTermineeException, NombresDeCartesMaxAtteindsException, PartieNonDemarreeException,
			MaladieEradiqueException, MaladiesNonIntialiseesException, VilleNonTrouveeException, NombreDeStationsDeRecherches6AtteindsException,
			VilleSansCetteMaladieException, StationRechercheExisteException, CarteVilleNonPossedeException, VillePasAssezDeMaladieException,
			NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException,
			RolePasPlanificateurUrgenceException, RolePasMedecinException, PartageConnaissancesException, PlanificateurUrgenceCarteDejaPriseException,
			VilleSansStationDeRechercheException
	{
		Objects.requireNonNull(evenement);
		Objects.requireNonNull(jcourant);
		verifierPartieEnCours(idPartie);

		Partie p = this.lesParties.get(idPartie);
		CarteEvenement carteEvenement = null;
		for (ICarteJoueur iCarteJoueur : p.getPlateau().getDefausseJoueur()){
			if(iCarteJoueur instanceof CarteEvenement){
				CarteEvenement tmp = (CarteEvenement) iCarteJoueur;
				if(tmp.getTypeEvenement().getNomEvenement().equals(evenement)){
					carteEvenement = tmp;
				}
			}
		}
		if(Objects.isNull(carteEvenement)){
			throw new CarteIntrouvableException();
		}
		p.jouerActionPiocherCarteEvenementParPlanificateur(carteEvenement);
	}

	@Override
	public void jouerActionConstruireStationParExpertOpe(long idPartie,String jc) throws PartieNonDemarreeException, PartieTermineeException, VilleNonTrouveeException, StationRechercheExisteException, JoueurNonCourantException, PartieNonExistanteException, RolePasExpertAuxOperationsException,NombresDeCartesMaxAtteindsException {
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p)){
			throw new PartieNonExistanteException();
		}
		if(!p.getJoueurCourant().getPseudo().equals(jc)){
			throw new JoueurNonCourantException();
		}
		if(!p.getJoueurCourant().getRole().equals(Role.EXPERT_AUX_OPERATIONS)) throw new RolePasExpertAuxOperationsException();
		p.setStationRecherche(p.getJoueurCourant().getEmplacement().getNom());
		p.getJoueurCourant().setNbActions(p.getJoueurCourant().getNbActions()-1);
		p.verifierNbAction();
	}

	@Override
	public void jouerActionDeplacerStationParExpertOpe(long idPartie,String jc, String ville) throws PartieNonDemarreeException, PartieTermineeException, VilleNonTrouveeException, StationRechercheExisteException, JoueurNonCourantException, PartieNonExistanteException, RolePasExpertAuxOperationsException, StationRechercheNonExistanteException, NombresDeCartesMaxAtteindsException{
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p)){
			throw new PartieNonExistanteException();
		}
		if(!p.getJoueurCourant().getPseudo().equals(jc)){
			throw new JoueurNonCourantException();
		}
		if(!p.getJoueurCourant().getRole().equals(Role.EXPERT_AUX_OPERATIONS)) throw new RolePasExpertAuxOperationsException();
		p.removeSationRecherche(ville);
		p.setStationRecherche(p.getJoueurCourant().getEmplacement().getNom());
		p.getJoueurCourant().setNbActions(p.getJoueurCourant().getNbActions()-1);
		p.verifierNbAction();
	}

	@Override
	public void jouerActionStationVersVilleExpertOpe(long idPartie,String jc,String ville, int carte) throws PartieNonExistanteException, JoueurNonCourantException,
			StationRechercheNonExistanteException,PartieNonDemarreeException,
			PartieTermineeException, RolePasExpertAuxOperationsException, CarteIntrouvableException,
			VilleNonTrouveeException, NombresDeCartesMaxAtteindsException
	{
		Partie p = this.lesParties.get(idPartie);
		if(Objects.isNull(p)){
			throw new PartieNonExistanteException();
		}
		if(!p.getJoueurCourant().getPseudo().equals(jc)){
			throw new JoueurNonCourantException();
		}
		if(!p.getJoueurCourant().getRole().equals(Role.EXPERT_AUX_OPERATIONS)) throw new RolePasExpertAuxOperationsException();
		Joueur j = p.getJoueurCourant();
		if(! j.getEmplacement().hasStationDeRecherche()) throw new StationRechercheNonExistanteException();
		if(p.getCompteurPourExpertAuxOperations() == 1){
			throw new RolePasExpertAuxOperationsException("Cette action ne peut être faite qu'une fois par tour !");
		}
		List<ICarteJoueur> c = j.defausser(carte);
		if(c.size() == 0){
			throw new CarteIntrouvableException();
		}

		Ville villeCurrent = j.getEmplacement();
		Ville villeDestination = p.getPlateau().getVilleByNom(ville);

		villeCurrent.enleverJoueur(j);
		j.setEmplacement(villeDestination);
		villeDestination.rajouterJoueur(j);

		j.setNbActions(j.getNbActions()-1);
		p.setCompteurPourExpertAuxOperations(1);
		p.verifierNbAction();
	}
	
	@Override
	public List<Partie> getPartiesTermineesJoueur(String pseudo) {
		List<Partie> result = new ArrayList<>();
		
		for(Partie p : this.lesParties.values()){
			if(p.partieTerminee()){
				Joueur j = p.getJoueurByPseudo(pseudo);
				if(!Objects.isNull(j)){
					result.add(p);
				}
			}
		}
	
		return result;
	}
	
	@Override
	public List<Partie> getPartiesNonTerminees() {
		List<Partie> result = new ArrayList<>();
		
		for(Partie p : this.lesParties.values()){
			if(!p.partieTerminee()){
				result.add(p);
			}
		}
		
		return result;
	}
	
	
}
