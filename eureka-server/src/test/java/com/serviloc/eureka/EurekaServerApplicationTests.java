package com.serviloc.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"eureka.client.register-with-eureka=false",
				"eureka.client.fetch-registry=false",
				"eureka.server.enable-self-preservation=false"
		}
)
class EurekaServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
