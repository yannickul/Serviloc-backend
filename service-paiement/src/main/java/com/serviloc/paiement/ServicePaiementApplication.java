package com.serviloc.paiement;

import com.serviloc.paiement.infrastructure.external.CamPayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(CamPayProperties.class)
public class ServicePaiementApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServicePaiementApplication.class, args);
	}
}