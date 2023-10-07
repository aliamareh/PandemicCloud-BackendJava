package org.projetpandemic.pandemicws.modele.dtos;

import modele.Joueur;
import modele.Partie;
import modele.exceptions.PartieNonDemarreeException;
import modele.exceptions.PartieNonTermineeException;

import java.util.List;

public record PartieNonTermineeDTO(long idPartie, List<String> joueurs) {
    public static PartieNonTermineeDTO toDTO(Partie p){
        List<String> js = p.getJoueurs().stream().map(Joueur::getPseudo).toList();
        return new PartieNonTermineeDTO(p.getIdPartie(), js);
    }
}
