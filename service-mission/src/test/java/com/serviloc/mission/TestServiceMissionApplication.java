package com.serviloc.mission;

import org.springframework.boot.SpringApplication;

public class TestServiceMissionApplication {

	public static void main(String[] args) {
		SpringApplication.from(ServiceMissionApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
