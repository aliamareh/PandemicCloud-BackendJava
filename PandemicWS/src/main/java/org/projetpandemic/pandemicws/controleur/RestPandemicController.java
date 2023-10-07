package org.projetpandemic.pandemicws.controleur;

import jakarta.servlet.http.Part;
import modele.*;
import modele.actions.TypeAction;
import modele.actions.actionsRole.exceptions.*;
import modele.actions.exceptions.*;
import modele.cartes.CarteEvenement;
import modele.cartes.CartePropagation;
import modele.cartes.ICarteJoueur;
import modele.cartes.TypeEvenement;
import modele.exceptions.*;
import org.projetpandemic.pandemicws.modele.FacadePandemicImpl;
import org.projetpandemic.pandemicws.modele.dtos.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.*;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/pandemic")
public class RestPandemicController {

	private static final String TOKEN_PREFIX="Bearer ";
	FacadePandemic facadePandemic;
	PasswordEncoder passwordEncoder;
	private final Function<String,String> genereToken;

	public RestPandemicController(FacadePandemic facadePandemic,
								  PasswordEncoder passwordEncoder,
								  Function<String, String> genereToken)
	{
		this.facadePandemic = facadePandemic;
		this.passwordEncoder = passwordEncoder;
		this.genereToken = genereToken;
	}

	@PostMapping("/connexion")
	public ResponseEntity seConnecter(@RequestBody LoginDTO loginDto) throws LoginDejaUtiliseException, MauvaisLoginException {
		Map<String,String> infoUser;
		try {
			infoUser = facadePandemic.getUserByPseudo(loginDto.pseudo());
		}
		catch (MauvaisLoginException e){
			throw new MauvaisLoginException("Pseudo ou mot de passe incorrect");
		}
		if(!passwordEncoder.matches(loginDto.password(),infoUser.get("password"))){
			throw new MauvaisLoginException("Pseudo ou mot de passe incorrect");
		}

		facadePandemic.connexion(infoUser.get("pseudo"), infoUser.get("password"));
		String token = this.genereToken.apply(infoUser.get("pseudo"));
		return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION,TOKEN_PREFIX+token).build();
	}


	@PostMapping("/inscription")
	public ResponseEntity<String> inscription(@RequestBody LoginDTO loginDto) throws LoginDejaUtiliseException,
			MauvaisLoginException
	{
		try {
			facadePandemic.inscription(loginDto.pseudo(), passwordEncoder.encode(loginDto.password()));
		} catch(MauvaisLoginException e){
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Format de pseudo invalide !");
		} catch(LoginDejaUtiliseException e){
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur déjà inscrit !");
		}

		Map<String,String> infoUser = facadePandemic.getUserByPseudo(loginDto.pseudo());
		facadePandemic.connexion(infoUser.get("pseudo"), infoUser.get("password"));
		String token = this.genereToken.apply(infoUser.get("pseudo"));
		return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.AUTHORIZATION,TOKEN_PREFIX+token).build();
	}

	@PostMapping("/deconnexion")
	public ResponseEntity<String> deconnexion(Authentication authentication) throws MauvaisLoginException {
			facadePandemic.deconnexion(authentication.getName());
			return ResponseEntity.status(HttpStatus.OK).build();
	}

	
	@PostMapping(value = "/partie/create")
	public ResponseEntity creerPartie(@RequestParam int nbJoueurs) throws NombreDeJoueursIncorrectException {
			String id = String.valueOf(facadePandemic.creerPartie(nbJoueurs));
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("idPartie",id));
	}
	
	@PostMapping(value = "/partie/{id}/join")
	public ResponseEntity<String> rejoindrePartie(@PathVariable("id") long idPartie,Authentication authentication)
			throws JoueurDejaPresentException, JoueurNonConnecteException,
			MauvaisLoginException, ListeJoueursCompletetException, PartieNonExistanteException
	{
			facadePandemic.rejoindrePartie(idPartie, authentication.getName());
			return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/partie/{id}/quitter")
	public ResponseEntity<String> quitterPartie(@PathVariable("id") long idPartie, Authentication authentication)
			throws MauvaisLoginException, PartieNonExistanteException
	{

		facadePandemic.estConnecte(authentication.getName());
		if(facadePandemic.getPartieDemarree(idPartie)){
			//Si la partie est en cours on décrémente juste le nombre de joueurs
			int nbJoueur = facadePandemic.getParties().get(idPartie).getNbJoueur()-1;
			facadePandemic.getParties().get(idPartie).setNbJoueur(nbJoueur);

			// Si il y'a plus aucun joueur, on supprime la partie de la facade
			// en supposant que l'etat de la partie est sauvegarde en Database
			if(nbJoueur <= 0 ){
				facadePandemic.getParties().remove(idPartie);
			}
		}
		else {
			facadePandemic.getParties().get(idPartie).getJoueurs()
					.removeIf(joueur -> joueur.getPseudo().equals(authentication.getName()));
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/partie/{id}/joueurs-connectes")
	public ResponseEntity getJoueursConnectes(@PathVariable("id") long idPartie, Authentication authentication) throws
			PartieNonExistanteException, MauvaisLoginException
	{
		if(!facadePandemic.getJoueursConnectes(idPartie).contains(authentication.getName())){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		List<String> joueursConnectesPartie = facadePandemic.getJoueursConnectes(idPartie);
		int nb = facadePandemic.getNbJoueurs(idPartie);
		return ResponseEntity.ok().body(
				Map.of("joueursConnectes",joueursConnectesPartie,
						"nbJoueursTotal",nb));
	}

	@PostMapping("/partie/{id}/start")
	public ResponseEntity<String> demarrerPartie(@PathVariable("id") long idPartie, Authentication authentication) throws PartieNonExistanteException,
			PiocheJoueurVideException, VilleNonTrouveeException,
			VilleMaladiesDejaInitialiseesException, StationRechercheExisteException,
			ListeJoueursNonCompletetException, VilleVoisineAElleMemeException,
			PartieTermineeException, PartieDejaDemarreeException,
			VilleDoublonVoisinException, PlusDeCubesMaladieDisponible, MauvaisLoginException, PartieNonDemarreeException {
		List<String> joueursConnectesPartie = facadePandemic.getJoueursConnectes(idPartie);
		if(!joueursConnectesPartie.contains(authentication.getName())){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		facadePandemic.demarrerPartie(idPartie);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/partie/{id}")
	public ResponseEntity<EtatPartieDTO> getPartie(@PathVariable("id") long idPartie, Authentication authentication)
			throws PartieNonExistanteException, JoueurNonConnecteException
	{

		if(!facadePandemic.getJoueursConnectes(idPartie).contains(authentication.getName())){
			throw new JoueurNonConnecteException("Le joueur n'est pas inscrit dans la partie !");
		}

		Partie p = facadePandemic.getParties().get(idPartie);
		EtatPartieDTO dto = EtatPartieDTO.toDTO(p);

		return ResponseEntity.ok().body(dto);
	}
	
	@GetMapping("/partie/{id}/encours")
	public ResponseEntity verifierSiPartieEnCours(@PathVariable("id") long idPartie, Authentication authentication)
			throws MauvaisLoginException, PartieNonExistanteException
	{
		if(!facadePandemic.getJoueursConnectes(idPartie).contains(authentication.getName())){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		try {
			facadePandemic.verifierPartieEnCours(idPartie);
			return ResponseEntity.ok().build();
		} catch (PartieNonExistanteException | PartieTermineeException | PartieNonDemarreeException e) {
			return ResponseEntity.status(NO_CONTENT).build();
		}
	}

	@GetMapping("/partie/{id}/{pseudo}/mescartes")
	public ResponseEntity<List<CarteDTO>> mescartes(@PathVariable("id") long idPartie,Authentication authentication) throws PartieNonDemarreeException,
			PartieNonExistanteException, PartieTermineeException, MauvaisLoginException
	{
		facadePandemic.verifierPartieEnCours(idPartie);
		Partie p = facadePandemic.getParties().get(idPartie);
		Joueur joueur = p.getJoueurByPseudo(authentication.getName());
		if(Objects.isNull(joueur)){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		List<CarteDTO> cartesJoueur = new ArrayList<>();
		joueur.getCartes().forEach( carteJoueur -> cartesJoueur.add(CarteDTO.toDTO(carteJoueur)));
		return ResponseEntity.ok().body(cartesJoueur);
	}

	@GetMapping("/partie/{id}/{pseudo}/mesinfos")
	public ResponseEntity<InfosJoueurDTO> mesinfos(@PathVariable("id") long idPartie, Authentication authentication)
			throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, MauvaisLoginException
	{
		facadePandemic.verifierPartieEnCours(idPartie);
		Partie p = facadePandemic.getParties().get(idPartie);
		Joueur joueur = p.getJoueurByPseudo(authentication.getName());
		if(Objects.isNull(joueur)){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		InfosJoueurDTO infosJoueur = InfosJoueurDTO.toDTO(joueur);
		return ResponseEntity.ok().body(infosJoueur);
	}

	@GetMapping("/partie/{id}/{ville}/infos")
	public ResponseEntity<VilleInfosDTO> infosVille(@PathVariable("id") long idPartie, @PathVariable String ville, Authentication authentication)
			throws PartieNonDemarreeException, PartieNonExistanteException, PartieTermineeException, VilleNonTrouveeException, MauvaisLoginException
	{
		if(!facadePandemic.getJoueursConnectes(idPartie).contains(authentication.getName())){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		facadePandemic.verifierPartieEnCours(idPartie);
		Partie p = facadePandemic.getParties().get(idPartie);
		Ville v = p.getPlateau().getVilleByNom(ville);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@GetMapping("/partie/{id}/villes")
	public List<String> getVilles(@PathVariable("id") long idPartie,Authentication authentication) throws PartieNonExistanteException, MauvaisLoginException {
		if(!facadePandemic.getJoueursConnectes(idPartie).contains(authentication.getName())){
			throw new MauvaisLoginException("Le joueur n'est pas inscrit dans la partie !");
		}
		List<String> villes = new ArrayList<>();
		facadePandemic.getParties().get(idPartie).getPlateau().getLesVilles()
				.forEach(v -> villes.add(v.getNom()));
		return villes;
	}

	@PostMapping("/partie/{id}/jouerActionVille")
	public ResponseEntity<VilleInfosDTO> jouerActionSurVille(@PathVariable("id") long idPartie,@RequestParam String action,
															 @RequestParam String ville, Authentication authentication)
			throws PiocheJoueurVideException, RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException,
			PartieNonExistanteException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException,
			MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException,
			RolePasExpertAuxOperationsException, VilleNonTrouveeException,
			CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException,
			VilleSansCetteMaladieException, StationRechercheExisteException, JoueurNonConnecteException,
			PartageConnaissancesException, VillePasAssezDeMaladieException, VilleSansStationDeRechercheException,
			PartieNonDemarreeException, RolePasMedecinException, JoueurIntrouvablePartie, PartieTermineeException,
			MauvaisLoginException, VillePasVoisineException, PlusDeCubesMaladieDisponible, JoueurNonCourantException,
			NombresDeCartesMaxAtteindsException
	{
		facadePandemic.jouerActionSurVille(idPartie,authentication.getName(),action,ville);
		Ville v = facadePandemic.getParties().get(idPartie).getPlateau().getVilleByNom(ville);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok()
				.body(villeInfos);
	}

	@PostMapping("/partie/{id}/deplacer-station")
	public ResponseEntity<VilleInfosDTO> deplacerStationDeRecherche(@PathVariable("id") long idPartie,
														   @RequestParam String source,
															 @RequestParam String destination,
															 Authentication authentication)
			throws VilleNonTrouveeException, PartieNonDemarreeException, PartieNonExistanteException,
			PartieTermineeException, StationRechercheNonExistanteException, PiocheJoueurVideException,
			RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException,
			RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException,
			MaladieEradiqueException, JoueurIntrouvablePartie, MaladiesNonIntialiseesException,
			NombreDeStationsDeRecherchesMaxDépasséException, MauvaisLoginException,
			RolePasExpertAuxOperationsException, VillePasVoisineException, CarteIntrouvableException,
			NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException,
			StationRechercheExisteException, JoueurNonConnecteException, PartageConnaissancesException,
			VillePasAssezDeMaladieException, PlusDeCubesMaladieDisponible, JoueurNonCourantException,
			NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException
	{
		if(! facadePandemic.getJoueurCourant(idPartie).getPseudo().equals(authentication.getName())){
			throw new JoueurNonCourantException();
		}
		facadePandemic.retirerStationRecherche(idPartie,source);
		facadePandemic.jouerActionSurVille(idPartie,authentication.getName(),
				String.valueOf(TypeAction.CONSTRUIRESTATIONRECHERCHE),
				destination);
		Ville v = facadePandemic.getParties().get(idPartie).getPlateau().getVilleByNom(destination);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerPartagerConnaissance")
	public ResponseEntity<String> jouerActionPartagerConnaissance(@PathVariable("id") long idPartie,
																  @RequestParam String emetteur,
																  @RequestParam String recepteur,
																  @RequestParam int carte,
																  Authentication authentication)
			throws PartieNonDemarreeException, PartieTermineeException, MauvaisLoginException,
						   RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException,
						   RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException,
						   JoueurIntrouvablePartie, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException,
						   RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException,
						   CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException,
						   StationRechercheExisteException, VillePasAssezDeMaladieException,
						   PlusDeCubesMaladieDisponible, JoueurNonCourantException, NombresDeCartesMaxAtteindsException,
						   VilleSansStationDeRechercheException, PartieNonExistanteException, PartageConnaissancesException, PiocheJoueurVideException, JoueurNonConnecteException {
		if(!authentication.getName().equals(emetteur) && !authentication.getName().equals(recepteur)){
			throw new PartageConnaissancesException("Joueur pas émmeteur ni récepteur !");
		}

		try{
			facadePandemic.jouerActionPartagerConnaissance(idPartie, emetteur, recepteur,carte);
		}
		catch (PartageConnaissancesException e){
			throw new PartageConnaissancesException("La carte choisit ne correspond pas à la ville !");
		}

		return ResponseEntity.ok().build();
	}

	@PostMapping("/partie/{id}/jouerContreMaladie")
	public ResponseEntity<VilleInfosDTO> jouerActionContreMaladie(@PathVariable("id") long idPartie,
														   @RequestParam String maladie,
														   @RequestParam List<Integer> cartes,
														   @RequestParam String typeAction,
														   Authentication authentication)
			throws PiocheJoueurVideException, RolePasPlanificateurUrgenceException, MaladieNonExistanteException,
			CarteVilleNonPossedeException, PartieNonExistanteException, PlanificateurUrgenceCarteDejaPriseException,
			MaladieEradiqueException, MaladiesNonIntialiseesException,NombresDeCartesMaxAtteindsException,
			NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException,
			VilleNonTrouveeException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException,
			VilleSansCetteMaladieException, StationRechercheExisteException, JoueurNonConnecteException,
			PartageConnaissancesException, VillePasAssezDeMaladieException, VilleSansStationDeRechercheException,
			PartieNonDemarreeException, RolePasMedecinException, JoueurIntrouvablePartie, PartieTermineeException,
			MauvaisLoginException, VillePasVoisineException, PlusDeCubesMaladieDisponible, JoueurNonCourantException
	{
		int nbActionsAvant = facadePandemic.getJoueurCourant(idPartie).getNbActions();
		facadePandemic.jouerActionContreMaladie(idPartie, authentication.getName(), maladie,cartes, typeAction);
		// l'action découvrir remede n'a pas abouti
		if( typeAction.equals(TypeAction.DECOUVRIRREMEDE.toString()) &&
				nbActionsAvant == facadePandemic.getJoueurCourant(idPartie).getNbActions() ){
			throw new CarteIntrouvableException("Les cartes choisi ne permettent pas d'effectuer l'action !");
		}
		Ville v = facadePandemic.getJoueurCourant(idPartie).getEmplacement();
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/defausserJoueur")
	public ResponseEntity<String> defausserCarteJoueur(@PathVariable("id") long idPartie,
													   @RequestParam int carte,
													   Authentication authentication)
			throws PartieNonDemarreeException, PartieNonExistanteException, JoueurIntrouvablePartie, PartieTermineeException,
			MauvaisLoginException, NombresDeCartesMaxAtteindsException
	{
		facadePandemic.defausserCarteJoueur(idPartie, authentication.getName(), carte);
		facadePandemic.getParties().get(idPartie).mAJJoueurCourantApresDefausseCarteJoueur();
		return ResponseEntity.ok().build();
	}

	@PostMapping("/partie/{id}/jouerEvenement/pontAerien")
	public ResponseEntity<VilleInfosDTO> jouerEvenementPontAerien(@PathVariable("id") long idPartie,
														   @RequestParam int carte,
														   @RequestParam String cible,
														   @RequestParam String destination,
														   Authentication authentication)
			throws VilleNonTrouveeException, CarteIntrouvableException, PartieNonDemarreeException, PartieNonExistanteException,
			JoueurIntrouvablePartie, PartieTermineeException
	{
		facadePandemic.jouerEvenementPontAerien(idPartie, authentication.getName(), carte, cible, destination);
		Partie p = facadePandemic.getParties().get(idPartie);
		Ville v = p.getPlateau().getVilleByNom(destination);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerEvenement/subventionpublique")
	public ResponseEntity<VilleInfosDTO> jouerEvenementSubventionPublique(@PathVariable("id") long idPartie,
																   @RequestParam int carte,
																   @RequestParam String ville,
																   Authentication authentication)
			throws VilleNonTrouveeException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException,
			PartieNonDemarreeException, StationRechercheExisteException, PartieNonExistanteException, JoueurIntrouvablePartie,
			PartieTermineeException
	{
		facadePandemic.jouerEvenementSubventionPublique(idPartie, authentication.getName(), carte, ville);
		Partie p = facadePandemic.getParties().get(idPartie);
		Ville v = p.getPlateau().getVilleByNom(ville);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerEvenement/subventionpublique2")
	public ResponseEntity<VilleInfosDTO> jouerEvenementSubventionPublique2(@PathVariable("id") long idPartie,
																	@RequestParam int carte,
																	@RequestParam String ville,
																	@RequestParam String villeaenlever,
																		   Authentication authentication)
			throws VilleNonTrouveeException, CarteIntrouvableException, NombreDeStationsDeRecherches6AtteindsException,
			PartieNonDemarreeException, StationRechercheExisteException, PartieNonExistanteException, JoueurIntrouvablePartie,
			StationRechercheNonExistanteException, PartieTermineeException
	{
		facadePandemic.jouerEvenementSubventionPublique2(idPartie, authentication.getName(), carte, ville, villeaenlever);
		Partie p = facadePandemic.getParties().get(idPartie);
		Ville v = p.getPlateau().getVilleByNom(ville);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerEvenement/parUneNuitTranquille")
	public ResponseEntity<String> jouerEvenementParUneNuitTranquille(@PathVariable("id") long idPartie,
																	 @RequestParam int carte,
																	 Authentication authentication)
			throws CarteIntrouvableException, PartieNonDemarreeException, PartieNonExistanteException,
			JoueurIntrouvablePartie, PartieTermineeException
	{
		facadePandemic.jouerEvenementParUneNuitTranquille(idPartie, authentication.getName(), carte);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/partie/{id}/jouerEvenement/popResil")
	public ResponseEntity<EtatPartieDTO> jouerEvenementPopResil(@PathVariable("id") long idPartie,
														 		@RequestParam int carte,
																@RequestParam int prop,
																Authentication authentication
																)
			throws CarteIntrouvableException, CartePropagationPasDansDefaussePropagationException,
			PartieNonDemarreeException, PartieNonExistanteException, JoueurIntrouvablePartie, PartieTermineeException
	{
		facadePandemic.jouerEvenementPopulationResiliente(idPartie, authentication.getName(), carte, prop);
		Partie p = facadePandemic.getParties().get(idPartie);
		EtatPartieDTO dto = EtatPartieDTO.toDTO(p);
		return ResponseEntity.ok().body(dto);
	}

	@PostMapping("/partie/{id}/jouerEvenement/prevision1")
	public ResponseEntity<List<CarteDTO>> jouerEvenementPrevisionPhase1(@PathVariable("id") long idPartie,
																@RequestParam int carte,
																Authentication authentication)
			throws CarteIntrouvableException, PartieNonDemarreeException, PiochePropagationVideException,
			PartieNonExistanteException, JoueurIntrouvablePartie, PartieTermineeException, EvenementDejaEnCoursException
	{
		facadePandemic.jouerEvenementPrevisionPhase1(idPartie, authentication.getName(), carte);
		Partie p = facadePandemic.getParties().get(idPartie);
		List<CarteDTO> cartePrevision = new ArrayList<>();
		p.getCartesEntrepPrevision().forEach(c -> cartePrevision.add(CarteDTO.toDTO(c)));
		return ResponseEntity.ok().body(cartePrevision);
	}

	@PostMapping("/partie/{id}/jouerEvenement/prevision2")
	public ResponseEntity<EtatPartieDTO> jouerEvenementPrevisionPhase2(@PathVariable("id") long idPartie,
																Authentication authentication,
																@RequestParam  List<Integer> indexCartes
																)
			throws PartieNonDemarreeException, PartieNonExistanteException, EvenementPasEnCoursException, PartieTermineeException
	{
		Partie p = facadePandemic.getParties().get(idPartie);
		if(Objects.isNull(p)){
			throw new PartieNonExistanteException();
		}
		List<CartePropagation> cartesPropagations = p.getCartesEntrepPrevision();
		List<CartePropagation> nouvReorganisationCartes = new ArrayList<>();
		for ( int index : indexCartes){
			nouvReorganisationCartes.add(cartesPropagations.get(index));
		}
		p.setCartesEntrepPrevision(nouvReorganisationCartes);
		facadePandemic.jouerEvenementPrevisionPhase2(idPartie);

		EtatPartieDTO dto = EtatPartieDTO.toDTO(p);
		return ResponseEntity.ok().body(dto);
	}

	@PostMapping("/partie/{id}/jouerActionDeplacerPionRepartiteur")
	public ResponseEntity<VilleInfosDTO> jouerActionDeplacerPionParRepartiteur(@PathVariable("id") long idPartie,
																			   Authentication authentication,
																			   @RequestParam String pseudojcontrole,
																			   @RequestParam String typeAction,
																			   @RequestParam String villeDest)
			throws RolePasPlanificateurUrgenceException, PartieNonDemarreeException, CarteVilleNonPossedeException, RolePasMedecinException,
			PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException,
			JoueurIntrouvablePartie, PartieTermineeException, MaladiesNonIntialiseesException,
			NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException,
			VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException,
			PartieNonExistanteException, RolePasRepartiteurException,
			NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException,
			StationRechercheExisteException, PartageConnaissancesException,
			VillePasAssezDeMaladieException, PlusDeCubesMaladieDisponible,
			VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException
	{
		facadePandemic.jouerActionDeplacerPionParRepartiteur(idPartie, authentication.getName(), pseudojcontrole, typeAction, villeDest);
		Partie p = facadePandemic.getParties().get(idPartie);
		Ville v = p.getPlateau().getVilleByNom(villeDest);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerActionDeplacerVersJoueurParRepartiteur")
	public ResponseEntity<VilleInfosDTO> jouerActionDeplacerVersJoueurParRepartiteur(@PathVariable("id") long idPartie,
																					 Authentication authentication,
																					 @RequestParam String jadeplacer,
																					 @RequestParam String jarejoindre)
			throws RolePasPlanificateurUrgenceException, CarteVilleNonPossedeException, PlanificateurUrgenceCarteDejaPriseException,
			MaladieEradiqueException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException,
			RolePasExpertAuxOperationsException, VilleNonTrouveeException, CarteIntrouvableException,
			PartieNonExistanteException, RolePasRepartiteurException, NombreDeStationsDeRecherches6AtteindsException,
			VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException,
			VillePasAssezDeMaladieException, VilleSansStationDeRechercheException, PartieNonDemarreeException,
			RolePasMedecinException, JoueurIntrouvablePartie, PartieTermineeException, VillePasVoisineException,
			PlusDeCubesMaladieDisponible, JoueurNonCourantException, NombresDeCartesMaxAtteindsException
	{
		try {
			facadePandemic.jouerActionDeplacerVersJoueurParRepartiteur(idPartie, authentication.getName(), jadeplacer, jarejoindre);
		}
		catch (JoueurIntrouvablePartie e){
			throw new JoueurIntrouvablePartie("Un des 3 joueurs n'est pas présent dans la partie !");
		}
		Partie p = facadePandemic.getParties().get(idPartie);
		Ville v = p.getJoueurByPseudo(jarejoindre).getEmplacement();
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(v);

		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerActionPiocherEventPlanificateur")
	public ResponseEntity<String> jouerActionPiocherEventPlanificateur(@PathVariable("id") long idPartie, Authentication authentication,@RequestParam String evenement) throws RolePasPlanificateurUrgenceException,
			PartieNonDemarreeException, CarteVilleNonPossedeException,
			RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException,
			MaladieEradiqueException, PartieTermineeException, MaladiesNonIntialiseesException,
			NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException,
			VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, PartieNonExistanteException,
			NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException,
			PartageConnaissancesException, VillePasAssezDeMaladieException, PlusDeCubesMaladieDisponible,
			NombresDeCartesMaxAtteindsException, VilleSansStationDeRechercheException, JoueurNonCourantException
	{
		facadePandemic.jouerActionPiocherCarteEvenementParPlanificateur(idPartie, authentication.getName(),evenement);
		return ResponseEntity.ok().build();
	}


	@PostMapping("/partie/{id}/jouerActionConstruireStationParExpertOpe")
	public ResponseEntity<VilleInfosDTO> jouerActionConstruireStationParExpertOpe(@PathVariable("id") long idPartie,
																				  Authentication authentication
																				  )
			throws VilleNonTrouveeException, PartieNonExistanteException, PartieNonDemarreeException,
			StationRechercheExisteException, JoueurNonCourantException, PartieTermineeException,
			RolePasExpertAuxOperationsException, NombresDeCartesMaxAtteindsException {
		facadePandemic.jouerActionConstruireStationParExpertOpe(idPartie, authentication.getName());
		Partie p = facadePandemic.getParties().get(idPartie);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(p.getJoueurCourant().getEmplacement());
		return ResponseEntity.ok().body(villeInfos);
	}
	@PostMapping("/partie/{id}/jouerActionDeplacerStationParExpertOpe")
	public ResponseEntity<VilleInfosDTO> jouerActionDeplacerStationParExpertOpe(@PathVariable("id") long idPartie,
																				Authentication authentication,
																				@RequestParam String ville)
			throws VilleNonTrouveeException, PartieNonExistanteException, PartieNonDemarreeException,
			StationRechercheExisteException, StationRechercheNonExistanteException,
			JoueurNonCourantException, PartieTermineeException, RolePasExpertAuxOperationsException, NombresDeCartesMaxAtteindsException {
		facadePandemic.jouerActionDeplacerStationParExpertOpe(idPartie, authentication.getName(),ville);
		Partie p = facadePandemic.getParties().get(idPartie);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(p.getJoueurCourant().getEmplacement());
		return ResponseEntity.ok().body(villeInfos);
	}

	@PostMapping("/partie/{id}/jouerActionStationVersVilleExpertOpe")
	public ResponseEntity<VilleInfosDTO> jouerActionStationVersVilleExpertOpe(@PathVariable("id") long idPartie,
																				Authentication authentication,
																				@RequestParam int carte,
																				@RequestParam String ville)
			throws CarteIntrouvableException, VilleNonTrouveeException, PartieNonExistanteException, PartieNonDemarreeException,
			JoueurNonCourantException, PartieTermineeException, RolePasExpertAuxOperationsException, StationRechercheNonExistanteException,
			NombresDeCartesMaxAtteindsException
	{
		try {
			facadePandemic.jouerActionStationVersVilleExpertOpe(idPartie, authentication.getName(),ville,carte);
		}
		catch (StationRechercheNonExistanteException e){
			throw new StationRechercheNonExistanteException("la ville de départ ne contient pas de station de recherche !");
		}
		Partie p = facadePandemic.getParties().get(idPartie);
		VilleInfosDTO villeInfos = VilleInfosDTO.toDTO(p.getJoueurCourant().getEmplacement());
		return ResponseEntity.ok().body(villeInfos);
	}

	@GetMapping("/partie/{id}/peutjouer")
	public ResponseEntity<Boolean> verifierSiPeutJouer(@PathVariable("id") long idPartie, Authentication authentication){
		try{
			facadePandemic.verifierSiPeutJouer(authentication.getName(), idPartie);
			return ResponseEntity.ok(true);
		} catch (PartieNonDemarreeException | JoueurNonConnecteException | PartieNonExistanteException |
				 JoueurIntrouvablePartie | JoueurNonCourantException | PartieTermineeException | MauvaisLoginException e) {
			return ResponseEntity.ok(false);
		}
	}



	
	@GetMapping("/partie/{id}/joueurcourant")
	public ResponseEntity<Joueur> getJoueurCourant(@PathVariable("id") long idPartie) throws PartieNonDemarreeException,
			PartieNonExistanteException, PartieTermineeException
	{
			Joueur jc = facadePandemic.getJoueurCourant(idPartie);
			return ResponseEntity.ok(jc);
	}
	
	@GetMapping("/partie/{id}/nbjoueurs")
	public ResponseEntity<Integer> getNbJoueurs(@PathVariable("id") long idPartie) throws PartieNonExistanteException {
			int nb = facadePandemic.getNbJoueurs(idPartie);
			return ResponseEntity.ok(nb);

	}

	
	@PostMapping("/partie/{id}/joueursuivant")
	public ResponseEntity<String> joueurSuivant(@PathVariable("id") long idPartie) throws PartieNonExistanteException,
			PartieNonDemarreeException, PartieTermineeException
	{
		try {
			Joueur njc = facadePandemic.joueurSuivant(idPartie);
			return ResponseEntity.ok(njc.getPseudo());
		}
		catch (NombresDeCartesMaxAtteindsException e) {
			return ResponseEntity.status(NOT_ACCEPTABLE).body("Le joueur actuel a trop de cartes en main !");
		}
	}

	
	@GetMapping("/parties")
	public ResponseEntity<String> getParties(){
		String result="[";
		List<Partie> cp = new ArrayList<>(facadePandemic.getParties().values());
		
		for(int i=0;i<cp.size();i++){

			if(i!=0)
			{
				result+=",\n";
			}

			Partie p = cp.get(i);
			result+= EtatPartieDTO.toDTO(p).toString();
		}

		result+="]";

		return ResponseEntity.ok(result);
	}

	@GetMapping("/parties/terminees")
	public ResponseEntity<Collection<PartieTermineeDTO>> getPartiesTermineesJoueur(Authentication authentication){
		List<Partie> cp = new ArrayList<>(facadePandemic.getPartiesTermineesJoueur(authentication.getName()));
		List<PartieTermineeDTO> pto = new ArrayList<>();
		for(Partie pr : cp){
			pto.add(PartieTermineeDTO.toDTO(pr));
		}
		return ResponseEntity.ok().body(pto);
	}

	@GetMapping("/parties/non-terminees")
	public ResponseEntity<Collection<PartieNonTermineeDTO>> getPartiesNonTerminees(Authentication authentication) {

		List<Partie> cp = new ArrayList<>(facadePandemic.getPartiesNonTerminees());
		List<PartieNonTermineeDTO> pto = new ArrayList<>();
		for (Partie pr : cp) {
			if (!Objects.isNull(pr.getJoueurByPseudo(authentication.getName())))
			{
				pto.add(PartieNonTermineeDTO.toDTO(pr));
			}
		}
		return ResponseEntity.ok().body(pto);
	}
	
	@PostMapping("/partie/{id}/piocherJoueur")
	public ResponseEntity<String> piocherCarteJoueur(@PathVariable("id") long idPartie, @RequestParam String pseudo,
											 @RequestParam int nbCartes)
			throws PiocheJoueurVideException, PartieNonDemarreeException, PartieNonExistanteException, JoueurIntrouvablePartie,
			PlusDeCubesMaladieDisponible, PartieTermineeException, NombresDeCartesMaxAtteindsException
	{
			facadePandemic.piocherCarteJoueur(idPartie, pseudo, nbCartes);
			return ResponseEntity.ok("Cartes piochées.");
	}

	@PostMapping("/partie/{id}/jouerActionMedecin")
	public ResponseEntity<String> jouerActionRetirerCubeMedecin(@PathVariable("id") long idPartie, @RequestParam String pseudo) throws RolePasPlanificateurUrgenceException, PartieNonDemarreeException, CarteVilleNonPossedeException, RolePasMedecinException, PlanificateurUrgenceCarteDejaPriseException, MaladieEradiqueException, JoueurIntrouvablePartie, PartieTermineeException, MaladiesNonIntialiseesException, NombreDeStationsDeRecherchesMaxDépasséException, RolePasExpertAuxOperationsException, VilleNonTrouveeException, VillePasVoisineException, CarteIntrouvableException, PartieNonExistanteException, NombreDeStationsDeRecherches6AtteindsException, VilleSansCetteMaladieException, StationRechercheExisteException, PartageConnaissancesException, VillePasAssezDeMaladieException, PlusDeCubesMaladieDisponible, VilleSansStationDeRechercheException, NombresDeCartesMaxAtteindsException {
		facadePandemic.jouerActionRetirerCubeParMedecin(idPartie, pseudo);
		return ResponseEntity.ok("Action effectuée.");
	}

}
