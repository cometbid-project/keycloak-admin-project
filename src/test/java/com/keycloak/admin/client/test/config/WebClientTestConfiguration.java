/**
 * 
 */
package com.keycloak.admin.client.test.config;

import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.spi.ExecutorServiceProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.KeycloakClientSslProperties;
//import com.keycloak.admin.client.config.KeycloakConfig.MyExecutorServiceProvider;

import lombok.RequiredArgsConstructor;

/**
 * @author Gbenga
 *
 */
@RequiredArgsConstructor
@TestConfiguration
public class WebClientTestConfiguration {
	
	private final AuthProperties keycloakProperties;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		// with new spring security 5
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	/**
	 * User "admin client" needs at least "manage-users, view-clients, view-realm,
	 * view-users" roles for "realm-management"
	 */
	@Bean
	public Keycloak keycloakAdminClientFactory(KeycloakClientSslProperties keycloakSslProps) throws Exception {
		String clientId = keycloakProperties.getAdminClientId();
		String clientSecret = keycloakProperties.getAdminClientSecret();
		String serverUrl = keycloakProperties.getBaseUrl();
		String realm = keycloakProperties.getAppRealm();

		Integer connectionPoolSize = keycloakProperties.getConnectionPoolSize();
		Integer readTimeoutInMillis = keycloakProperties.getReadTimeoutInMillis();
		Integer connectTimeoutInMillis = keycloakProperties.getConnectTimeoutInMillis();
		Integer connectionTTLInSecs = keycloakProperties.getConnectTTLInSeconds();
		Integer checkoutInMillis = keycloakProperties.getConnectCheckoutTimeoutInMillis();

		// final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
		final ClientBuilder clientBuilder = ResteasyClientBuilder.newBuilder();  
				//.connectionPoolSize(connectionPoolSize);

		clientBuilder.hostnameVerifier(new NoopHostnameVerifier());
		// clientBuilder.hostnameVerifier(new DefaultHostnameVerifier());

		final ClientConfig clientConfig = new ClientConfig();
		clientConfig.property(ClientProperties.CONNECT_TIMEOUT, connectTimeoutInMillis);
		clientConfig.property(ClientProperties.READ_TIMEOUT, readTimeoutInMillis);
		clientBuilder.withConfig(clientConfig);

		ResteasyClient resteasyClient = (ResteasyClient) clientBuilder.build();
		resteasyClient.register(new MyExecutorServiceProvider());

		// Get keycloak client
		Keycloak keycloak = KeycloakBuilder.builder()
				//
				.serverUrl(serverUrl)
				//
				.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
				//
				.realm(realm)
				// Client-id
				.clientId(clientId)
				// Client-secret
				.clientSecret(clientSecret)
				//
				.resteasyClient(resteasyClient).build();

		return keycloak;
	}

	@ClientAsyncExecutor
	static class MyExecutorServiceProvider implements ExecutorServiceProvider {

		@Autowired
		@Qualifier("keycloakClientWorkerThreadPool")
		private ExecutorService executorService;

		@Override
		public ExecutorService getExecutorService() {
			// System.out.println("Calling getExecutorService()");

			return executorService;
		}

		@Override
		public void dispose(ExecutorService executorService) {
			executorService.shutdown();
		}
	}

}
