package org.projetpandemic.pandemicws;

import modele.cartes.ICarteJoueur;
import org.projetpandemic.pandemicws.modele.FacadePandemicImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class PandemicWsApplication {
	@Autowired
	FacadePandemicImpl facadePandemic;
	public static void main(String[] args) {
		SpringApplication.run(PandemicWsApplication.class, args);
	}

}
