/**
 * 
 */
package com.keycloak.admin.client.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.keycloak.admin.client.KeycloakAdminProjectApplication;

/**
 * @author Gbenga
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("ComponentTest")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = KeycloakAdminProjectApplication.class)
@ActiveProfiles("component")
@AutoConfigureWebTestClient(timeout = "30000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 9900)
public @interface ComponentTest { }
