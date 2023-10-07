package org.projetpandemic.pandemicws;

import com.fasterxml.jackson.databind.ObjectMapper;
import modele.*;
import modele.actions.PartagerConnaissances;
import modele.actions.TypeAction;
import modele.actions.actionsRole.exceptions.CarteIntrouvableException;
import modele.actions.exceptions.*;
import modele.cartes.CarteEvenement;
import modele.cartes.CartePropagation;
import modele.cartes.TypeEvenement;
import modele.exceptions.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.projetpandemic.pandemicws.modele.FacadePandemicImpl;
import org.projetpandemic.pandemicws.modele.dtos.LoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.*;
import java.util.function.Function;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PandemicWsApplicationTests {

	@Autowired
	MockMvc mvc;

	@MockBean
	FacadePandemicImpl facade;

	@MockBean
	PasswordEncoder passwordEncoder;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	Function<String,String> genereToken;
	
	@Test
	void testInscriptionOK() throws Exception {
		String pseudo = "test1";
		String password = "test";
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("pseudo",pseudo);
		userInfo.put("password", passwordEncoder.encode(password));
		LoginDTO loginDTO = new LoginDTO(pseudo,password);
		doReturn(userInfo).when(facade).getUserByPseudo(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/inscription")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO)))
				.andExpect(status().isCreated());
	}
	
	@Test
	void testInscriptionBadLoginKO() throws Exception {
		String pseudo = "te";
		String password = "test";
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("pseudo",pseudo);
		userInfo.put("password", passwordEncoder.encode(password));
		LoginDTO loginDTO = new LoginDTO(pseudo,password);
		doThrow(MauvaisLoginException.class).when(facade).inscription(anyString(), eq(null));
		doReturn(userInfo).when(facade).getUserByPseudo(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/inscription")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO)))
				.andExpect(status().isUnauthorized());
	}
	@Test
	void testInscriptionUserExisteKO() throws Exception {
		String pseudo = "test1";
		String password = "test";
		LoginDTO loginDTO = new LoginDTO(pseudo,password);
		doThrow(LoginDejaUtiliseException.class).when(facade).inscription(anyString(), eq(null));

		mvc.perform(post(URI.create("/pandemic/inscription")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO)))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	void testConnexionOK() throws Exception {
		String pseudo = "test1";
		String password = "test";
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("pseudo",pseudo);
		userInfo.put("password", passwordEncoder.encode(password));
		LoginDTO loginDTO = new LoginDTO(pseudo,password);
		doReturn(userInfo).when(facade).getUserByPseudo(pseudo);
		doReturn(true).when(passwordEncoder).matches(anyString(),eq(null));
		
		mvc.perform(post(URI.create("/pandemic/connexion")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO)))
				.andExpect(status().isOk());
	}
	@Test
	public void testConnexionLoginKO() throws Exception {
		String pseudo = "test1";
		String password = "test";
		LoginDTO loginDTO = new LoginDTO(pseudo,password);
		doThrow(MauvaisLoginException.class).when(facade).getUserByPseudo(pseudo);

		mvc.perform(post(URI.create("/pandemic/connexion"))
						.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO)))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	public void testConnexionPasswordKO() throws Exception {
		String pseudo = "test1";
		String password = "test";
		Map<String,String> userInfo = new HashMap<>();
		userInfo.put("pseudo",pseudo);
		userInfo.put("password", passwordEncoder.encode(password));
		LoginDTO loginDTO = new LoginDTO(pseudo,password);
		doReturn(userInfo).when(facade).getUserByPseudo(pseudo);
		doReturn(false).when(passwordEncoder).matches(password,password);

		mvc.perform(post(URI.create("/pandemic/connexion"))
						.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginDTO)))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	public void testDeconnexionUser() throws Exception {

		String pseudo = "test1";
		String password = "test";
		Map<String,String> userInfo = new HashMap<>();
		userInfo.put("pseudo",pseudo);
		userInfo.put("password", passwordEncoder.encode(password));

		doReturn(userInfo).when(facade).getUserByPseudo(pseudo);
		doReturn(false).when(passwordEncoder).matches(password,userInfo.get("password"));


		String myToken = "Bearer "+genereToken.apply(pseudo);

		doNothing().when(facade).deconnexion(pseudo);

		mvc.perform(post(URI.create("/pandemic/deconnexion")).header("Authorization",myToken))
				.andExpect(status().isOk());

	}
	
	@Test
	void creerPartieOK() throws Exception {
		String myToken = "Bearer "+genereToken.apply("test");
	
		mvc.perform(post(URI.create("/pandemic/partie/create"))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.header("Authorization", myToken)
				.content("nbJoueurs=2"))
				.andExpect(status().isCreated());
	}
	
	@Test
	void creerPartieNbJoueursKO() throws Exception {
		String myToken = "Bearer "+genereToken.apply("test");
		doThrow(NombreDeJoueursIncorrectException.class).when(facade).creerPartie(MockitoHamcrest.intThat(Matchers.greaterThan(4)));
		
		mvc.perform(post(URI.create("/pandemic/partie/create"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken)
							.content("nbJoueurs=5"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void rejoindrePartieJoueurDejaPresentKO() throws Exception {
		String myToken = "Bearer "+genereToken.apply("test");
		doThrow(JoueurDejaPresentException.class).when(facade).rejoindrePartie(1L, "test");
		
		mvc.perform(post(URI.create("/pandemic/partie/1/join"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken))
				.andExpect(status().isConflict());
	}
	
	@Test
	void rejoindrePartieInexistanteKO() throws Exception {
		String myToken = "Bearer "+genereToken.apply("test");
		doThrow(PartieNonExistanteException.class).when(facade).rejoindrePartie(eq(1L), anyString());
		
		mvc.perform(post(URI.create("/pandemic/partie/1/join"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void rejoindrePartieCompleteKO() throws Exception {
		String myToken = "Bearer "+genereToken.apply("test");
		doThrow(ListeJoueursCompletetException.class).when(facade).rejoindrePartie(eq(1L), anyString());
		
		mvc.perform(post(URI.create("/pandemic/partie/1/join"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void rejoindrePartieOK() throws Exception {
		String myToken = "Bearer "+genereToken.apply("test");
		
		mvc.perform(post(URI.create("/pandemic/partie/1/join"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void quitterPartieOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long, Partie> lesParties = new HashMap<>();
		lesParties.put(1L, new Partie());
		doReturn(lesParties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/quitter"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void quitterPartieOK2() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doReturn(true).when(facade).estConnecte(pseudo);
		doReturn(true).when(facade).getPartieDemarree(1L);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);
		doReturn(1).when(p).getNbJoueur();
		doNothing().when(p).setNbJoueur(0);
		mvc.perform(post(URI.create("/pandemic/partie/1/quitter"))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void quitterPartieOK3() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doReturn(true).when(facade).estConnecte(pseudo);
		doReturn(true).when(facade).getPartieDemarree(1L);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);
		doReturn(2).when(p).getNbJoueur();
		doNothing().when(p).setNbJoueur(1);
		mvc.perform(post(URI.create("/pandemic/partie/1/quitter"))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void quitterPartieInexistantKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doThrow(PartieNonExistanteException.class).when(facade).getPartieDemarree(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/quitter"))
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getJoueursConnectesOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		
		mvc.perform(get(URI.create("/pandemic/partie/1/joueurs-connectes"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	@Test
	void getJoueursConnectesJoueurPasInscrit() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of("test1","test2");
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);

		mvc.perform(get(URI.create("/pandemic/partie/1/joueurs-connectes"))
						.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getJoueursConnectesPartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doThrow(PartieNonExistanteException.class).when(facade).getJoueursConnectes(MockitoHamcrest.longThat(Matchers.greaterThan(1L)));
		
		mvc.perform(get(URI.create("/pandemic/partie/2/joueurs-connectes"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void demarrerPartieOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/start"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void demarrerPartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doThrow(PartieNonExistanteException.class).when(facade).getJoueursConnectes(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/start"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void demarrerPartieListeIncompleteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doThrow(ListeJoueursNonCompletetException.class).when(facade).demarrerPartie(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/start"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void demarrerPartieJouerNonInscritKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of("test1","test2");
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);

		mvc.perform(post(URI.create("/pandemic/partie/1/start"))
						.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void demarrerPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doThrow(PartieTermineeException.class).when(facade).demarrerPartie(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/start"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getPartieOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1"))
							.header("Authorization",myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void getPartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long, Partie> parties = new HashMap<>();
		
		doThrow(PartieNonExistanteException.class).when(facade).getJoueursConnectes(1L);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1"))
							.header("Authorization",myToken))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void getPartieJoueurNonInscritKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doReturn(List.of("test1","test2")).when(facade).getJoueursConnectes(1L);

		mvc.perform(get(URI.create("/pandemic/partie/1"))
						.header("Authorization",myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void verifierSiPartieEnCoursOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/encours"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void verifierSiPartieEnCoursInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doThrow(PartieNonExistanteException.class).when(facade).getJoueursConnectes(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/encours"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void verifierSiPartieEnCoursTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doThrow(PartieTermineeException.class).when(facade).verifierPartieEnCours(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/encours"))
							.header("Authorization", myToken))
				.andExpect(status().isNoContent());
	}
	
	@Test
	void verifierSiPartieEnCoursNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doThrow(PartieNonDemarreeException.class).when(facade).verifierPartieEnCours(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/encours"))
							.header("Authorization", myToken))
				.andExpect(status().isNoContent());
	}

	@Test
	void verifierSiPartieEnCoursJoueurNonInscritKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doReturn(List.of("test1","test2")).when(facade).getJoueursConnectes(1L);

		mvc.perform(get(URI.create("/pandemic/partie/1/encours"))
						.header("Authorization",myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesCartesOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Partie p = mock(Partie.class);
		Joueur j = mock(Joueur.class);
		Map<Long,Partie> parties = mock(Map.class);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);
		doReturn(j).when(p).getJoueurByPseudo(pseudo);
		doReturn(List.of()).when(j).getCartes();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mescartes"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	@Test
	void mesCartesJoueurNonInscritOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Partie p = mock(Partie.class);
		Map<Long,Partie> parties = mock(Map.class);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);

		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mescartes"))
						.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesCartesPartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).verifierPartieEnCours(anyLong());
		
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mescartes"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesCartesPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).verifierPartieEnCours(anyLong());
		
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mescartes"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesCartesPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).verifierPartieEnCours(anyLong());
		
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mescartes"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesInfosOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		Partie p = mock(Partie.class);
		Joueur j = mock(Joueur.class);
		Map<Long,Partie> parties = mock(Map.class);
		Ville v = mock(Ville.class);

		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);
		doReturn(j).when(p).getJoueurByPseudo(pseudo);
		doReturn(List.of()).when(j).getCartes();
		doReturn(Role.MEDECIN).when(j).getRole();
		doReturn(v).when(j).getEmplacement();
		doReturn("Tokyo").when(v).getNom();
		doReturn(1).when(j).getNbActions();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mesinfos"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void mesInfosJoueurNonInscritOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		Partie p = mock(Partie.class);
		Map<Long,Partie> parties = mock(Map.class);

		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);

		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mesinfos"))
						.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesInfosPartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).verifierPartieEnCours(anyLong());
		
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mesinfos"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesInfosPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).verifierPartieEnCours(anyLong());
		
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mesinfos"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void mesInfosPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).verifierPartieEnCours(anyLong());
		
		Map<Long, Partie> parties = new HashMap<>();
		parties.put(1L, new Partie());
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlas/mescartes"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void infosVilleOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doNothing().when(facade).verifierPartieEnCours(1L);
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta",999999, new Maladie("test"))));
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlanta/infos"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void infosVilleIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doNothing().when(facade).verifierPartieEnCours(1L);
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlanta/infos"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void infosVillePartieNonExistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doThrow(PartieNonExistanteException.class).when(facade).getJoueursConnectes(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlanta/infos"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void infosVillePartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doThrow(PartieNonDemarreeException.class).when(facade).verifierPartieEnCours(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlanta/infos"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void infosVillePartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		doThrow(PartieTermineeException.class).when(facade).verifierPartieEnCours(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlanta/infos"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void infosVilleJoueurNonExistantKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doReturn(List.of("test1","test2")).when(facade).getJoueursConnectes(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/Atlanta/infos"))
						.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}

	
	@Test
	void getVillesOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		List<String> joueurs = List.of(pseudo);
		doReturn(joueurs).when(facade).getJoueursConnectes(1L);
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta",999999, new Maladie("test"))));
		mvc.perform(get(URI.create("/pandemic/partie/1/villes"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void getVillesPartieNonExistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doThrow(PartieNonExistanteException.class).when(facade).getJoueursConnectes(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/villes"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void getVillesJoueurNonExistantKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		doReturn(List.of("test1","test2")).when(facade).getJoueursConnectes(1L);
		mvc.perform(get(URI.create("/pandemic/partie/1/villes"))
						.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerActionSurVillePartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(PartieNonExistanteException.class).when(facade).jouerActionSurVille(eq(2L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleJoueurIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(JoueurIntrouvablePartie.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVillePartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(PartieNonDemarreeException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleJoueurNonCourantKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(JoueurNonCourantException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVillePartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(PartieTermineeException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlant"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleCarteVilleNonPossedeeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(CarteVilleNonPossedeException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleNonVoisineKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(VillePasVoisineException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("deplacement"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "deplacement")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVillePasCetteMaladieKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(VilleSansCetteMaladieException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleStationRechercheExisteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(StationRechercheExisteException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionSurVilleStationRechercheNonExisteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		doThrow(VilleSansStationDeRechercheException.class).when(facade).jouerActionSurVille(eq(1L),anyString(), eq("test"), eq("Atlanta"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionVille"))
							.header("Authorization", myToken)
							.param("action", "test")
							.param("ville","Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRechercheOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test")), new Ville("Paris", 999998, new Maladie("test2"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		Joueur j = new Joueur(pseudo);
		doReturn(j).when(facade).getJoueurCourant(1L);
		doNothing().when(facade).jouerActionSurVille(eq(1L),anyString(), eq(String.valueOf(TypeAction.CONSTRUIRESTATIONRECHERCHE)), eq("Paris"));;
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isOk());
	}
	
	@Test
	void deplacerStationDeRecherchePartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doThrow(PartieNonExistanteException.class).when(facade).getJoueurCourant(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRecherchePartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).getJoueurCourant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRecherchePartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).getJoueurCourant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRechercheInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test")), new Ville("Paris", 999998, new Maladie("test2"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		Joueur j = new Joueur(pseudo);
		doReturn(j).when(facade).getJoueurCourant(1L);
		doThrow(StationRechercheNonExistanteException.class).when(facade).retirerStationRecherche(1L, "Atlanta");
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRechercheVilleIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test")), new Ville("Paris", 999998, new Maladie("test2"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		Joueur j = new Joueur(pseudo);
		doReturn(j).when(facade).getJoueurCourant(1L);
		doThrow(VilleNonTrouveeException.class).when(facade).retirerStationRecherche(1L, "Atlant");
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlant")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRechercheJoueurIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test")), new Ville("Paris", 999998, new Maladie("test2"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		Joueur j = new Joueur("test2");
		doReturn(j).when(facade).getJoueurCourant(1L);
		doNothing().when(facade).retirerStationRecherche(1L, "Atlanta");
		doThrow(JoueurIntrouvablePartie.class).when(facade).jouerActionSurVille(eq(1L), eq(pseudo), anyString(), eq("Paris"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRechercheJoueurNonCourantKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test")), new Ville("Paris", 999998, new Maladie("test2"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		Joueur j = new Joueur("test2");
		doReturn(j).when(facade).getJoueurCourant(1L);
		doNothing().when(facade).retirerStationRecherche(1L, "Atlanta");
		doThrow(JoueurNonCourantException.class).when(facade).jouerActionSurVille(eq(1L), eq(pseudo), anyString(), eq("Paris"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void deplacerStationDeRechercheExisteDejaKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta", 999999, new Maladie("test")), new Ville("Paris", 999998, new Maladie("test2"))));
		parties.put(1L,partie);
		doReturn(parties).when(facade).getParties();
		Joueur j = new Joueur(pseudo);
		doReturn(j).when(facade).getJoueurCourant(1L);
		doNothing().when(facade).retirerStationRecherche(1L, "Atlanta");
		doThrow(StationRechercheExisteException.class).when(facade).jouerActionSurVille(eq(1L), eq(pseudo), anyString(), eq("Paris"));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/deplacer-station"))
							.header("Authorization", myToken)
							.param("source", "Atlanta")
							.param("destination","Paris"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionPartagerConnaissanceOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
							.header("Authorization", myToken)
							.param("emetteur", "test")
							.param("recepteur","test2")
							.param("carte", "0"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerActionPartagerConnaissanceNiEmetteurNiRecepteurKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
							.header("Authorization", myToken)
							.param("emetteur", "test3")
							.param("recepteur","test2")
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionPartagerConnaissancePartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerActionPartagerConnaissance(eq(1L), eq("test"), eq("test2"), eq(0));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
							.header("Authorization", myToken)
							.param("emetteur", "test")
							.param("recepteur","test2")
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionPartagerConnaissancePartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerActionPartagerConnaissance(eq(1L), eq("test"), eq("test2"), eq(0));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
							.header("Authorization", myToken)
							.param("emetteur", "test")
							.param("recepteur","test2")
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionPartagerConnaissancePartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).jouerActionPartagerConnaissance(eq(1L), eq("test"), eq("test2"), eq(0));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
							.header("Authorization", myToken)
							.param("emetteur", "test")
							.param("recepteur","test2")
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionPartagerConnaissanceJoueurNonCourantKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(JoueurNonCourantException.class).when(facade).jouerActionPartagerConnaissance(eq(1L), eq("test"), eq("test2"), eq(0));
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
							.header("Authorization", myToken)
							.param("emetteur", "test")
							.param("recepteur","test2")
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void jouerActionPartagerConnaissanceCarteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doThrow(PartageConnaissancesException.class).when(facade).jouerActionPartagerConnaissance(eq(1L), eq("test"),
				eq("test2"), eq(-1));

		mvc.perform(post(URI.create("/pandemic/partie/1/jouerPartagerConnaissance"))
						.header("Authorization", myToken)
						.param("emetteur", "test")
						.param("recepteur","test2")
						.param("carte", "-1"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionContreMaladieOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Joueur j = new Joueur(pseudo);
		Ville v = new Ville("Atlanta",999999,new Maladie("maladietest"));
		j.setEmplacement(v);
		doReturn(j).when(facade).getJoueurCourant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerContreMaladie"))
							.header("Authorization", myToken)
							.param("maladie", "maladietest")
							.param("cartes","0,1,2")
							.param("typeAction", "testaction"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerActionContreMaladiePartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).getJoueurCourant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerContreMaladie"))
							.header("Authorization", myToken)
							.param("maladie", "maladietest")
							.param("cartes","0,1,2")
							.param("typeAction", "testaction"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionContreMaladiePartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).getJoueurCourant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerContreMaladie"))
							.header("Authorization", myToken)
							.param("maladie", "maladietest")
							.param("cartes","0,1,2")
							.param("typeAction", "testaction"))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void jouerActionContreMaladieCartesKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Joueur jc = mock(Joueur.class);
		doReturn(jc).when(facade).getJoueurCourant(1L);
		doReturn(3).when(jc).getNbActions();

		mvc.perform(post(URI.create("/pandemic/partie/1/jouerContreMaladie"))
						.header("Authorization", myToken)
						.param("maladie", "maladietest")
						.param("cartes","-1,-1,2")
						.param("typeAction", "DECOUVRIRREMEDE"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionContreMaladiePartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).getJoueurCourant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerContreMaladie"))
							.header("Authorization", myToken)
							.param("maladie", "maladietest")
							.param("cartes","0,1,2")
							.param("typeAction", "testaction"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionContreMaladiePasDansLaVilleKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Joueur j = new Joueur(pseudo);
		Ville v = new Ville("Atlanta",999999,new Maladie("maladietest"));
		j.setEmplacement(v);
		doReturn(j).when(facade).getJoueurCourant(1L);
		doThrow(VilleSansCetteMaladieException.class).when(facade).jouerActionContreMaladie(1L, pseudo, "maladietest2",List.of(0,1,2), "testaction");
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerContreMaladie"))
							.header("Authorization", myToken)
							.param("maladie", "maladietest2")
							.param("cartes","0,1,2")
							.param("typeAction", "testaction"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void defausserJoueurOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Joueur j = mock(Joueur.class);
		doNothing().when(facade).defausserCarteJoueur(1L,pseudo,0);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(1L);
		doReturn(j).when(p).mAJJoueurCourant();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/defausserJoueur"))
							.header("Authorization", myToken)
							.param("carte", "0"))
				.andExpect(status().isOk());
	}
	
	@Test
	void verifierSiPeutJouerOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doNothing().when(facade).verifierSiPeutJouer(pseudo, 1L);
		
		mvc.perform(get(URI.create("/pandemic/partie/1/peutjouer"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void verifierSiPeutJouerKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doThrow(PartieNonExistanteException.class).when(facade).verifierSiPeutJouer(pseudo, 1L);

		mvc.perform(get(URI.create("/pandemic/partie/1/peutjouer"))
						.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void verifierSiPeutJouerKO2() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doThrow(JoueurIntrouvablePartie.class).when(facade).verifierSiPeutJouer(pseudo, 1L);

		mvc.perform(get(URI.create("/pandemic/partie/1/peutjouer"))
						.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void getJoueurCourantOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Joueur j = new Joueur(pseudo);
		doReturn(j).when(facade).getJoueurCourant(1L);
		
		mvc.perform(get(URI.create("/pandemic/partie/1/joueurcourant"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void getJoueurCourantPartieNonExistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).getJoueurCourant(2L);
		
		mvc.perform(get(URI.create("/pandemic/partie/2/joueurcourant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getJoueurCourantPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).getJoueurCourant(2L);
		
		mvc.perform(get(URI.create("/pandemic/partie/2/joueurcourant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getJoueurCourantPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).getJoueurCourant(2L);
		
		mvc.perform(get(URI.create("/pandemic/partie/2/joueurcourant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getNbJoueursOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doReturn(2).when(facade).getNbJoueurs(1L);
		
		mvc.perform(get(URI.create("/pandemic/partie/1/nbjoueurs"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void getNbJoueursPartieNonExistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).getNbJoueurs(2L);
		
		mvc.perform(get(URI.create("/pandemic/partie/2/nbjoueurs"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void joueurSuivantOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Joueur j = new Joueur(pseudo);
		doReturn(j).when(facade).joueurSuivant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/joueursuivant"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}
	
	@Test
	void joueurSuivantPartieNonExistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).joueurSuivant(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/joueursuivant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void joueurSuivantPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).joueurSuivant(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/joueursuivant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void joueurSuivantPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).joueurSuivant(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/joueursuivant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void joueurSuivantTropDeCartesKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(NombresDeCartesMaxAtteindsException.class).when(facade).joueurSuivant(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/joueursuivant"))
							.header("Authorization", myToken))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void getPartiesOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		Partie p1 = mock(Partie.class);
		Partie p2 = mock(Partie.class);
		Plateau p =mock(Plateau.class);

		doReturn(p).when(p1).getPlateau();
		doReturn(p).when(p2).getPlateau();

		parties.put(1L,p1 );
		parties.put(2L,p2);

		doReturn(parties).when(facade).getParties();
		
		mvc.perform(get(URI.create("/pandemic/parties"))
							.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void getPartiesTermineesOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		Partie p1 = mock(Partie.class);
		Partie p2 = mock(Partie.class);
		Plateau p =mock(Plateau.class);

		doReturn(p).when(p1).getPlateau();
		doReturn(p).when(p2).getPlateau();

		doReturn(Arrays.asList(p1,p2)).when(facade).getPartiesTermineesJoueur(pseudo);

		mvc.perform(get(URI.create("/pandemic/parties/terminees"))
						.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void getPartiesNonTermineesOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		Partie p1 = mock(Partie.class);
		Partie p2 = mock(Partie.class);
		Plateau p =mock(Plateau.class);
		Joueur j = mock(Joueur.class);

		doReturn(p).when(p1).getPlateau();
		doReturn(p).when(p2).getPlateau();
		doReturn(j).when(p1).getJoueurByPseudo(pseudo);
		doReturn(j).when(p2).getJoueurByPseudo(pseudo);
		doReturn(Arrays.asList(p1,p2)).when(facade).getPartiesNonTerminees();

		mvc.perform(get(URI.create("/pandemic/parties/non-terminees"))
						.header("Authorization", myToken))
				.andExpect(status().isOk());
	}

	@Test
	void piocherCarteJoueurOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isOk());
	}
	
	@Test
	void piocherCarteJoueurPartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).piocherCarteJoueur(2L, pseudo, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void piocherCarteJoueurPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).piocherCarteJoueur(2L, pseudo, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void piocherCarteJoueurPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).piocherCarteJoueur(2L, pseudo, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void piocherCarteJoueurPiocheVideKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PiocheJoueurVideException.class).when(facade).piocherCarteJoueur(2L, pseudo, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void piocherCarteJoueurNbCartesMaxKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(NombresDeCartesMaxAtteindsException.class).when(facade).piocherCarteJoueur(2L, pseudo, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void piocherCarteJoueurIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(JoueurIntrouvablePartie.class).when(facade).piocherCarteJoueur(2L, pseudo, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/piocherJoueur"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("nbCartes", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementParUneNuitTranquilleOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerEvenement/parUneNuitTranquille"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerEvenementParUneNuitTranquillePartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerEvenementParUneNuitTranquille(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/parUneNuitTranquille"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementParUneNuitTranquillePartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerEvenementParUneNuitTranquille(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/parUneNuitTranquille"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementParUneNuitTranquillePartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).jouerEvenementParUneNuitTranquille(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/parUneNuitTranquille"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementParUneNuitTranquilleCarteIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(CarteIntrouvableException.class).when(facade).jouerEvenementParUneNuitTranquille(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/parUneNuitTranquille"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPontAerienOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.getPlateau().setLesVilles(List.of(new Ville("Atlanta",999999,new Maladie("maladietest"))));
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerEvenement/pontAerien"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("cible", "test2")
							.param("destination", "Atlanta"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerEvenementPontAerienPartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerEvenementPontAerien(2L, pseudo, 0, "test2", "Atlanta");
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/pontAerien"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("cible", "test2")
							.param("destination", "Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPontAerienPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerEvenementPontAerien(2L, pseudo, 0, "test2", "Atlanta");
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/pontAerien"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("cible", "test2")
							.param("destination", "Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPontAerienPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).jouerEvenementPontAerien(2L, pseudo, 0, "test2", "Atlanta");
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/pontAerien"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("cible", "test2")
							.param("destination", "Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPontAerienCarteIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(CarteIntrouvableException.class).when(facade).jouerEvenementPontAerien(2L, pseudo, 3, "test2", "Atlanta");
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/pontAerien"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "3")
							.param("cible", "test2")
							.param("destination", "Atlanta"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPontAerienVilleIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(VilleNonTrouveeException.class).when(facade).jouerEvenementPontAerien(2L, pseudo, 0, "test2", "Atlant");
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/pontAerien"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("cible", "test2")
							.param("destination", "Atlant"))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void jouerEvenementSubventionPublique1OK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Plateau plateau = mock(Plateau.class);
		Ville v = mock(Ville.class);
		Maladie m = mock(Maladie.class);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(plateau).when(p).getPlateau();
		doReturn(v).when(plateau).getVilleByNom("Atlanta");
		doReturn("Rouge").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		doNothing().when(facade).jouerEvenementSubventionPublique(2L, pseudo, 0, "Atlanta");

		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/subventionpublique"))
						.header("Authorization", myToken)
						.param("pseudo",pseudo)
						.param("carte", "0")
						.param("ville", "Atlanta"))
				.andExpect(status().isOk());
	}

	@Test
	void jouerEvenementSubventionPublique2OK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Plateau plateau = mock(Plateau.class);
		Ville v = mock(Ville.class);
		Maladie m = mock(Maladie.class);
		doNothing().when(facade).jouerEvenementSubventionPublique2(2L, pseudo, 0, "Atlanta","Moscou");
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(plateau).when(p).getPlateau();
		doReturn(v).when(plateau).getVilleByNom("Atlanta");
		doReturn("Rouge").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/subventionpublique2"))
						.header("Authorization", myToken)
						.param("pseudo",pseudo)
						.param("carte", "0")
						.param("ville", "Atlanta")
						.param("villeaenlever","Moscou")
				)

				.andExpect(status().isOk());
	}
	
	@Test
	void jouerEvenementPopResilOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerEvenement/popResil"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("prop", "2"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerEvenementPopResilPartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerEvenementPopulationResiliente(2L, pseudo, 0, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/popResil"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("prop", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPopResilPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerEvenementPopulationResiliente(2L, pseudo, 0, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/popResil"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("prop", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPopResilPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).jouerEvenementPopulationResiliente(2L, pseudo, 0, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/popResil"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("prop", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPopResilCarteIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(CarteIntrouvableException.class).when(facade).jouerEvenementPopulationResiliente(2L, pseudo, 0, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/popResil"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("prop", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPopResilCartePasTrouveeDansDefaussePropagationKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(CartePropagationPasDansDefaussePropagationException.class).when(facade).jouerEvenementPopulationResiliente(2L, pseudo, 0, 2);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/popResil"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0")
							.param("prop", "2"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision1OK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerEvenement/prevision1"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerEvenementPrevision1PartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerEvenementPrevisionPhase1(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision1"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision1PartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerEvenementPrevisionPhase1(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision1"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision1PartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).jouerEvenementPrevisionPhase1(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision1"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision1PiochePropagationVideKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PiochePropagationVideException.class).when(facade).jouerEvenementPrevisionPhase1(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision1"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision1EvenementDejaEnCoursKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(EvenementDejaEnCoursException.class).when(facade).jouerEvenementPrevisionPhase1(2L, pseudo, 0);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision1"))
							.header("Authorization", myToken)
							.param("pseudo",pseudo)
							.param("carte", "0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision2OK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.setCartesEntrepPrevision(List.of(new CartePropagation(new Ville("Atlanta",999999,new Maladie("maladietest")))));
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerEvenement/prevision2"))
							.header("Authorization", myToken)
							.param("indexCartes","0"))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerEvenementPrevision2PartieIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.setCartesEntrepPrevision(List.of(new CartePropagation(new Ville("Atlanta",999999,new Maladie("maladietest")))));
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerEvenementPrevisionPhase2(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision2"))
							.header("Authorization", myToken)
							.param("indexCartes","0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision2PartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.setCartesEntrepPrevision(List.of(new CartePropagation(new Ville("Atlanta",999999,new Maladie("maladietest")))));
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerEvenementPrevisionPhase2(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision2"))
							.header("Authorization", myToken)
							.param("indexCartes","0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision2PartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.setCartesEntrepPrevision(List.of(new CartePropagation(new Ville("Atlanta",999999,new Maladie("maladietest")))));
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		doThrow(PartieTermineeException.class).when(facade).jouerEvenementPrevisionPhase2(2L);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerEvenement/prevision2"))
							.header("Authorization", myToken)
							.param("indexCartes","0"))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerEvenementPrevision2EvenementPasEnCoursKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		Map<Long, Partie> parties = new HashMap<>();
		Partie partie = new Partie();
		partie.setCartesEntrepPrevision(List.of(new CartePropagation(new Ville("Atlanta",999999,new Maladie("maladietest")))));
		parties.put(1L, partie);
		doReturn(parties).when(facade).getParties();
		
		doThrow(EvenementPasEnCoursException.class).when(facade).jouerEvenementPrevisionPhase2(1L);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerEvenement/prevision2"))
							.header("Authorization", myToken)
							.param("indexCartes","0"))
				.andExpect(status().isNotAcceptable());
	}
	@Test
	void jouerActionDeplacerPionRepartiteurOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Plateau plateau = mock(Plateau.class);
		Ville v = mock(Ville.class);
		Maladie m = mock(Maladie.class);

		doNothing().when(facade).jouerActionDeplacerPionParRepartiteur(2L, pseudo, "test2", "VOLDIRECT","Moscou");
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(plateau).when(p).getPlateau();
		doReturn(v).when(plateau).getVilleByNom("Moscou");
		doReturn("Noire").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionDeplacerPionRepartiteur"))
						.header("Authorization", myToken)
						.param("pseudojcontrole","test2")
						.param("typeAction", "VOLDIRECT")
						.param("villeDest","Moscou")
				)

				.andExpect(status().isOk());
	}

	@Test
	void jouerActionDeplacerVersJoueurParRepartiteurOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Maladie m = mock(Maladie.class);
		Ville v = mock(Ville.class);
		Joueur j = mock(Joueur.class);

		doNothing().when(facade).jouerActionDeplacerVersJoueurParRepartiteur(2L, pseudo, "test2", pseudo);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(j).when(p).getJoueurByPseudo(pseudo);
		doReturn(v).when(j).getEmplacement();
		doReturn("Noire").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionDeplacerVersJoueurParRepartiteur"))
						.header("Authorization", myToken)
						.param("jadeplacer","test2")
						.param("jarejoindre", pseudo)
				)
				.andExpect(status().isOk());
	}
	@Test
	void jouerActionDeplacerVersJoueurParRepartiteurKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doThrow(JoueurIntrouvablePartie.class).when(facade)
				.jouerActionDeplacerVersJoueurParRepartiteur(2L, pseudo, "toto", pseudo);
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionDeplacerVersJoueurParRepartiteur"))
						.header("Authorization", myToken)
						.param("jadeplacer","toto")
						.param("jarejoindre", pseudo)
				)
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void jouerActionPiocherEventPlanificateurOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doNothing().when(facade)
				.jouerActionPiocherCarteEvenementParPlanificateur(2L, pseudo, "SUBVENTION_PUBLIQUE");
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionPiocherEventPlanificateur"))
						.header("Authorization", myToken)
						.param("evenement","SUBVENTION_PUBLIQUE")
				)
				.andExpect(status().isOk());
	}

	@Test
	void jouerActionConstruireStationParExpertOpeOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Maladie m = mock(Maladie.class);
		Ville v = mock(Ville.class);
		Joueur j = mock(Joueur.class);

		doNothing().when(facade).jouerActionConstruireStationParExpertOpe(2L, pseudo);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(j).when(p).getJoueurCourant();
		doReturn(v).when(j).getEmplacement();
		doReturn("Noire").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionConstruireStationParExpertOpe"))
						.header("Authorization", myToken)
				)
				.andExpect(status().isOk());
	}

	@Test
	void jouerActionDeplacerStationParExpertOpeOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Maladie m = mock(Maladie.class);
		Ville v = mock(Ville.class);
		Joueur j = mock(Joueur.class);

		doNothing().when(facade).jouerActionDeplacerStationParExpertOpe(2L, pseudo,"Moscou");
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(j).when(p).getJoueurCourant();
		doReturn(v).when(j).getEmplacement();
		doReturn("Noire").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionDeplacerStationParExpertOpe"))
						.header("Authorization", myToken)
						.param("ville","Moscou")
				)
				.andExpect(status().isOk());
	}

	@Test
	void jouerActionStationVersVilleExpertOpeOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		Map<Long,Partie> parties = mock(Map.class);
		Partie p = mock(Partie.class);
		Maladie m = mock(Maladie.class);
		Ville v = mock(Ville.class);
		Joueur j = mock(Joueur.class);

		doNothing().when(facade).jouerActionStationVersVilleExpertOpe(2L, pseudo,"Moscou",2);
		doReturn(parties).when(facade).getParties();
		doReturn(p).when(parties).get(2L);
		doReturn(j).when(p).getJoueurCourant();
		doReturn(v).when(j).getEmplacement();
		doReturn("Noire").when(m).getCouleur();
		doReturn(m).when(v).getMaladieParDefaut();
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionStationVersVilleExpertOpe"))
						.header("Authorization", myToken)
						.param("ville","Moscou")
						.param("carte","2")
				)
				.andExpect(status().isOk());
	}

	@Test
	void jouerActionStationVersVilleExpertOpeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);

		doThrow(StationRechercheNonExistanteException.class).when(facade).jouerActionStationVersVilleExpertOpe(2L, pseudo,"Moscou",2);
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionStationVersVilleExpertOpe"))
						.header("Authorization", myToken)
						.param("ville","Moscou")
						.param("carte","2")
				)
				.andExpect(status().isNotAcceptable());
	}

	
	@Test
	void jouerActionRetirerCubeMedecinOK() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionMedecin"))
							.header("Authorization", myToken)
							.param("pseudo", pseudo))
				.andExpect(status().isOk());
	}
	
	@Test
	void jouerActionRetirerCubeMedecinPartieInexistanteKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonExistanteException.class).when(facade).jouerActionRetirerCubeParMedecin(2L, pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/2/jouerActionMedecin"))
							.header("Authorization", myToken)
							.param("pseudo", pseudo))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionRetirerCubeMedecinPartieNonDemarreeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieNonDemarreeException.class).when(facade).jouerActionRetirerCubeParMedecin(1L, pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionMedecin"))
							.header("Authorization", myToken)
							.param("pseudo", pseudo))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionRetirerCubeMedecinPartieTermineeKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(PartieTermineeException.class).when(facade).jouerActionRetirerCubeParMedecin(1L, pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionMedecin"))
							.header("Authorization", myToken)
							.param("pseudo", pseudo))
				.andExpect(status().isNotAcceptable());
	}
	
	@Test
	void jouerActionRetirerCubeMedecinJoueurIntrouvableKO() throws Exception {
		String pseudo = "test";
		String myToken = "Bearer "+genereToken.apply(pseudo);
		
		doThrow(JoueurIntrouvablePartie.class).when(facade).jouerActionRetirerCubeParMedecin(1L, pseudo);
		
		mvc.perform(post(URI.create("/pandemic/partie/1/jouerActionMedecin"))
							.header("Authorization", myToken)
							.param("pseudo", pseudo))
				.andExpect(status().isNotAcceptable());
	}
}
