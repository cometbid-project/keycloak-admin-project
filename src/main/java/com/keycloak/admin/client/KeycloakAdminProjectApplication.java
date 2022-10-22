package com.keycloak.admin.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.thymeleaf.TemplateEngine;

import reactor.blockhound.BlockHound;
import reactor.core.publisher.Hooks;

/**
 * 
 * @author Gbenga
 *
 */
@SpringBootApplication
public class KeycloakAdminProjectApplication {

	static {
		Hooks.onOperatorDebug();
		BlockHound.builder() // <1>
		.allowBlockingCallsInside( // 
				null,
				//TemplateEngine.class.getCanonicalName(), 
				"process") // <2>
		.install(); // <3>
	}

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(KeycloakAdminProjectApplication.class);

		springApplication.addListeners(new ApplicationStartingEventListener());
		springApplication.run(args);
	}

}
