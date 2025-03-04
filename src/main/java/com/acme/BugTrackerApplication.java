package com.acme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
public class BugTrackerApplication {

	public static void main(String[] args) {

		SpringApplication.run(BugTrackerApplication.class, args);
	}

}
