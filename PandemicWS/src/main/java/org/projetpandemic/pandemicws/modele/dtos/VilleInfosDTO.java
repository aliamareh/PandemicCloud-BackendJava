package org.projetpandemic.pandemicws.modele.dtos;

import modele.Ville;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record VilleInfosDTO(String nom, int population,
                            String maladieParDefaut,
                            String centreRecherche,
                            Map<String,Integer> cubesMaladie,
        List<String> villesAlentours,
        List<String> joueursDansLaVille)
{
    public static VilleInfosDTO toDTO(Ville ville){
        Map<String,Integer> tmpCubesMaladie = new HashMap<>();
        ville.getNiveauxMaladies().forEach((m,nv) ->
        {
            tmpCubesMaladie.put(m.getCouleur(),nv);

        });
        List<String> tmpVillesAlentours = new ArrayList<>();
        ville.getVillesAlentours().forEach(v ->
        {
            tmpVillesAlentours.add(v.getNom());

        });

        List<String> tmpJoueursDansLaVille = new ArrayList<>();
        ville.getJoueursDansLaVille().forEach(j ->
        {
            tmpJoueursDansLaVille.add(j.getPseudo());

        });
        return new VilleInfosDTO(ville.getNom(),
                ville.getPopulation(),
                ville.getMaladieParDefaut().getCouleur(),
                ville.hasStationDeRecherche()?"oui":"non",
                tmpCubesMaladie,tmpVillesAlentours,tmpJoueursDansLaVille);
    }
}
