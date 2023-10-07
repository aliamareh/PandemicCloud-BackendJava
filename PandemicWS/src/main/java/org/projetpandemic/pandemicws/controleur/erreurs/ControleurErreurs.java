package org.projetpandemic.pandemicws.controleur.erreurs;

import modele.actions.actionsRole.exceptions.*;
import modele.actions.exceptions.*;
import modele.exceptions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
@ControllerAdvice
public class ControleurErreurs extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {

            //modele.exceptiopns
            EvenementDejaEnCoursException.class,
            EvenementPasEnCoursException.class,
            JoueurIntrouvablePartie.class,
            JoueurNonConnecteException.class,
            JoueurNonCourantException.class,
            ListeJoueursCompletetException.class,
            ListeJoueursNonCompletetException.class,
            LoginDejaUtiliseException.class,
            MaladieEradiqueException.class,
            MaladieNonExistanteException.class,
            MaladiesNonIntialiseesException.class,
            MauvaisLoginException.class,
            NombreDeJoueursIncorrectException.class,
            NombreDeStationsDeRechercheIncorrect.class,
            PartieNonDemarreeException.class,
            PartieNonExistanteException.class,
            PartieNonTermineeException.class,
            PartieTermineeException.class,
            PiocheJoueurVideException.class,
            PiochePropagationVideException.class,
            VilleMaladiesDejaInitialiseesException.class,
            StationRechercheExisteException.class,
            VilleVoisineAElleMemeException.class,
            VilleDoublonVoisinException.class,
            PlusDeCubesMaladieDisponible.class,
            StationRechercheExisteException.class,
            StationRechercheNonExistanteException.class,
            VilleNonTrouveeException.class,
            VillePasAssezDeMaladieException.class,
            VillePasDeMaladieException.class,

            //actions.exceptions
            CartePropagationPasDansCartesPropagationEnMainException.class,
            CartePropagationPasDansDefaussePropagationException.class,
            CarteVilleNonPossedeException.class,
            CarteVillePasPareilVilleDestinationException.class,
            NombreDeStationsDeRecherches6AtteindsException.class,
            NombreDeStationsDeRecherchesMaxDépasséException.class,
            NombresDeCartesMaxAtteindsException.class,
            NombresDeReorganisationMaxAtteindsException.class,
            NombresDeStationDeRechercheInférieurà6Exception.class,
            PartageConnaissancesException.class,
            VillePasVoisineException.class,
            VilleSansCetteMaladieException.class,
            VilleSansStationDeRechercheException.class,
            VillesPasPareilException.class,

            //actionsRole.exceptions
            AttendreProchainTourPourCetteActionException.class,
            CarteIntrouvableException.class,
            PlanificateurUrgenceCarteDejaPriseException.class,
            RolePasExpertAuxOperationsException.class,
            RolePasMedecinException.class,
            RolePasPlanificateurUrgenceException.class,
            RolePasRepartiteurException.class

    })
    private ResponseEntity<Object> handleNotAcceptable(Exception ex, WebRequest request){
        String msg = ex.getMessage();
        return handleExceptionInternal(ex,msg,new HttpHeaders(), HttpStatus.NOT_ACCEPTABLE,request);
    }

    @ExceptionHandler(value = {
            JoueurDejaPresentException.class,
            PartieDejaDemarreeException.class})
    private ResponseEntity<Object> handleConflict(Exception ex, WebRequest request){
        String msg = ex.getMessage();
        return handleExceptionInternal(ex,msg,new HttpHeaders(), HttpStatus.CONFLICT,request);
    }

}
