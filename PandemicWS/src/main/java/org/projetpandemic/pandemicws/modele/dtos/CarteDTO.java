package org.projetpandemic.pandemicws.modele.dtos;

import modele.cartes.*;

public record CarteDTO(String typeCarte, String nomVilleOuEvenement, String maladieOuDescr) {
    public static CarteDTO toDTO(Object c){
        CarteDTO carteDTO = null;
        if(c instanceof CarteVilleJoueur){
            CarteVilleJoueur carteVilleJoueur = (CarteVilleJoueur) c;
            carteDTO = new CarteDTO("ville",
                    carteVilleJoueur.getLaVille().getNom(),
                    carteVilleJoueur.getLaVille().getMaladieParDefaut().getCouleur());
        }
        if(c instanceof CarteEvenement){
            CarteEvenement carteEvenement= (CarteEvenement) c;
            carteDTO = new CarteDTO("evenement",
                    carteEvenement.getTypeEvenement().getNomEvenement(),
                    carteEvenement.getTypeEvenement().getDescription());
        }
        if(c instanceof CartePropagation){
            CartePropagation cartePropagation= (CartePropagation) c;
            carteDTO = new CarteDTO("propagation",
                    cartePropagation.getLaVille().getNom(),
                    cartePropagation.getLaVille().getMaladieParDefaut().getCouleur());
        }
        if(c instanceof CarteEpidemie){
            CarteEpidemie carteEpidemie = (CarteEpidemie) c;
            carteDTO = new CarteDTO("epidemie",
                    "epidemie",
                    "epidemie");
        }
        return carteDTO;
    }
}
