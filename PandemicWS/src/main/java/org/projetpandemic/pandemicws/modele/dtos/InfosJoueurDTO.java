package org.projetpandemic.pandemicws.modele.dtos;

import modele.Joueur;
import modele.Role;
import modele.Ville;

import java.util.Map;

public record InfosJoueurDTO(String nomRole, String descRole,String emplacement, int nbActions) {
    public static InfosJoueurDTO toDTO(Joueur joueur){
        return new InfosJoueurDTO(joueur.getRole().getNom(),joueur.getRole().getDescription(),joueur.getEmplacement().getNom(), joueur.getNbActions());
    }
}
