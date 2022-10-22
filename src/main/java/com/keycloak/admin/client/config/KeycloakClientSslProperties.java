/**
 * 
 */
package com.keycloak.admin.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.keycloak.admin.client.common.utils.YamlPropertySourceFactory;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Gbenga
 *
 */
@Getter
@Setter
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class KeycloakClientSslProperties {

	// SSL Configurations
	@Value("${server.ssl.key-password}")
	private String privateKeyPassword;

	@Value("${server.ssl.key-store}")
	private String keyStorePath;

	@Value("${server.ssl.key-store-password}")
	private String keyStorePassword;

	@Value("${server.ssl.trust-store}")
	private String trustStorePath;

	@Value("${server.ssl.trust-store-password}")
	private String trustStorePassword;
}
