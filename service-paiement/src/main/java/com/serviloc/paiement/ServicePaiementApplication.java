package com.serviloc.paiement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
public class ServicePaiementApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServicePaiementApplication.class, args);
	}
}