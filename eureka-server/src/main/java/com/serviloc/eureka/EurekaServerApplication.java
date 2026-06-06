package com.serviloc.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ServiLoc — Eureka Server
 *
 * <p>Registre centralisé de service discovery pour tous les microservices ServiLoc.
 * Ce service DOIT être démarré EN PREMIER — tous les autres services en dépendent
 * pour s'enregistrer et résoudre les adresses dynamiquement.</p>
 *
 * <p>Dashboard accessible sur : http://localhost:8761</p>
 * <p>Healthcheck Actuator    : http://localhost:8761/actuator/health</p>
 *
 * <p>Ordre de démarrage obligatoire :</p>
 * <pre>
 *   1. eureka-server       (ce service)
 *   2. api-gateway         (dépend d'Eureka au démarrage)
 *   3. tous les microservices (s'enregistrent après démarrage)
 * </pre>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
