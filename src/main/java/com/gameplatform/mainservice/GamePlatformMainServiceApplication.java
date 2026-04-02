package com.gameplatform.mainservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GamePlatformMainServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamePlatformMainServiceApplication.class, args);
	}

}
