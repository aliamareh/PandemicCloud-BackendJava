package org.projetpandemic.pandemicws.modele.dtos;

import modele.Partie;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieTermineeException;

import java.util.*;


public class EtatPartieDTO {
	private long idPartie;
	private int nbJoueurs;
	private Map<String,String> joueurs;
	private String jc;
	private int etat;

	private int nvPropagation;
	private int eclosion;

	private List<String> centreRecherche;
	private List<String> gueri;
	private List<String> eradique;
	private Map<String,Integer> cubesMaladie;
	private List<CarteDTO> defausseJoueur;
	private List<CarteDTO> defaussePropagation;
	//private List<CarteDTO> carteJoueur;
	//private List<CarteDTO> CartePropagation;

	public List<CarteDTO> getDefausseJoueur() {
		return defausseJoueur;
	}

	public void setDefausseJoueur(List<CarteDTO> defausseJoueur) {
		this.defausseJoueur = defausseJoueur;
	}

	public List<CarteDTO> getDefaussePropagation() {
		return defaussePropagation;
	}

	public void setDefaussePropagation(List<CarteDTO> defaussePropagation) {
		this.defaussePropagation = defaussePropagation;
	}

	public EtatPartieDTO(long idPartie, int nbJoueurs, int etat) {
		this.idPartie = idPartie;
		this.nbJoueurs = nbJoueurs;
		this.etat = etat;
		this.centreRecherche = new ArrayList<>();
		this.joueurs = new HashMap<>();
		this.cubesMaladie = new HashMap<>();
	}

	public long getIdPartie() {
		return idPartie;
	}

	public int getNbJoueurs() {
		return nbJoueurs;
	}

	public void setNbJoueurs(int nbJoueurs) {
		this.nbJoueurs = nbJoueurs;
	}

	public Map<String,String> getJoueurs() {
		return joueurs;
	}

	public String getJc() {
		return jc;
	}

	public int getEtat() {
		return etat;
	}

	public void setEtat(int etat) {
		this.etat = etat;
	}

	public void setIdPartie(long idPartie) {
		this.idPartie = idPartie;
	}

	public static EtatPartieDTO toDTO(Partie p){
		int etat;
		String jc=null;
		
		if(p.partieTerminee()){
			etat = 2;
		}
		else if(p.getPartieDemaree()){
			etat = 1;
			try {
				jc = p.getJoueurCourant().getPseudo();
			} catch (PartieNonDemarreeException e) {
				throw new RuntimeException(e);
			} catch (PartieTermineeException ignored) {}
		}
		else
		{
			etat = 0;
		}
		
		EtatPartieDTO dto = new EtatPartieDTO(p.getIdPartie(), p.getNbJoueur(), etat);
		
		if(!Objects.isNull(jc)){
			dto.setJc(jc);
		}

		//centres de recherches
		List<String> tmpCentreRecherche = new ArrayList<>();
		p.getPlateau().getLesVilles().forEach(ville -> {
				if(ville.hasStationDeRecherche()) tmpCentreRecherche.add(ville.getNom());
			}
		);

		//Map contenant le pseudo des joueur et leurs emplacement
		Map<String,String> tmpJoueurs = new HashMap<>();
		p.getJoueurs().forEach(j ->
		{
			tmpJoueurs.put(j.getPseudo(),j.getEmplacement().getNom());
		}
		);

		//map indiquand les cube restant pour chaque maladie
		Map<String,Integer> tmpCubeMaladies = new HashMap<>();
		p.getPlateau().getMaladies().forEach(m ->
				{
					tmpCubeMaladies.put(m.getCouleur(),m.getCubesRestants());
				}
		);

		//liste des cartes deffausse joueur
		List<CarteDTO> tmpDafausseJoueur = new ArrayList<>();
		p.getPlateau().getDefausseJoueur().forEach(c ->
				tmpDafausseJoueur.add(CarteDTO.toDTO(c))
		);

		//liste des cartes deffausse propagation
		List<CarteDTO> tmpDafausseProp = new ArrayList<>();
		p.getPlateau().getDefaussePropagation().forEach(c ->
				tmpDafausseProp.add(CarteDTO.toDTO(c))
		);

		//liste des maladies éradiqué
		List<String> tmpEradique = new ArrayList<>();
		p.getPlateau().getMaladies().forEach(m ->
				{if(m.isEradique()) tmpEradique.add(m.getCouleur());}
		);

		//liste des maladies gueri
		List<String> tmpGueri = new ArrayList<>();
		p.getPlateau().getMaladies().forEach(m ->
				{if(m.remedeEtabli()) tmpEradique.add(m.getCouleur());}
		);

		dto.setEclosion(p.getPlateau().getCompteurEclosion());
		dto.setNvPropagation(p.getPlateau().getIndicePropagation());
		dto.setEradique(tmpEradique);
		dto.setGueri(tmpGueri);
		dto.setJoueurs(tmpJoueurs);
		dto.setCubesMaladie(tmpCubeMaladies);
		dto.setCentreRecherche(tmpCentreRecherche);
		dto.setDefausseJoueur(tmpDafausseJoueur);
		dto.setDefaussePropagation(tmpDafausseProp);
		return dto;
	}

	public int getNvPropagation() {
		return nvPropagation;
	}

	public void setNvPropagation(int nvPropagation) {
		this.nvPropagation = nvPropagation;
	}

	public int getEclosion() {
		return eclosion;
	}

	public void setEclosion(int eclosion) {
		this.eclosion = eclosion;
	}

	public List<String> getCentreRecherche() {
		return centreRecherche;
	}

	public void setCentreRecherche(List<String> centreRecherche) {
		this.centreRecherche = centreRecherche;
	}

	public List<String> getGueri() {
		return gueri;
	}

	public void setGueri(List<String> gueri) {
		this.gueri = gueri;
	}

	public List<String> getEradique() {
		return eradique;
	}

	public void setEradique(List<String> eradique) {
		this.eradique = eradique;
	}

	public Map<String, Integer> getCubesMaladie() {
		return cubesMaladie;
	}

	public void setCubesMaladie(Map<String, Integer> cubesMaladie) {
		this.cubesMaladie = cubesMaladie;
	}

	public void setJoueurs(Map<String,String> joueurs) {
		this.joueurs.putAll(joueurs);
	}
	
	public void setJc(String jc) {
		this.jc = jc;
	}

}