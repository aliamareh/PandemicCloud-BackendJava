package org.projetpandemic.pandemicws.modele.dtos;

import modele.Joueur;
import modele.Partie;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieNonTermineeException;

import java.util.List;

public record PartieTermineeDTO(long idPartie, List<String> joueurs, String resultat) {
	public static PartieTermineeDTO toDTO(Partie p){
		List<String> js = p.getJoueurs().stream().map(Joueur::getPseudo).toList();
		
		String result = null;
		
		try {
			if(p.partieGagnee()){
				result = "oui";
			}
			else
			{
				result = "non";
			}
		} catch (PartieNonTermineeException | PartieNonDemarreeException e) {
			result = "non";
		}
		return new PartieTermineeDTO(p.getIdPartie(), js, result);
	}
}
