/**
 * 
 */
package com.keycloak.admin.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

	private final AuthProperties keycloakProperties;
	
	private final KeycloakClientSslProperties keycloakSslProps;

	/**
	 * User "admin client" needs at least "manage-users, view-clients, view-realm,
	 * view-users" roles for "realm-management"
	 */
	@Bean
	public Keycloak keycloakAdminClientFactory(@Qualifier("keycloakClient-ExecutorService") 
											ExecutorService executorService) throws Exception {
		String clientId = keycloakProperties.getAdminClientId();
		String clientSecret = keycloakProperties.getAdminClientSecret();
		String serverUrl = keycloakProperties.getBaseUrl();
		String realm = keycloakProperties.getAppRealm();

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
				.resteasyClient(resteasyClient(executorService))
				//
				.build();

		return keycloak;
	}

	private ResteasyClient resteasyClient(ExecutorService executorService) throws Exception {
		
		Integer connectionPoolSize = keycloakProperties.getConnectionPoolSize();
		Integer readTimeoutInMillis = keycloakProperties.getReadTimeoutInMillis();
		Integer connectTimeoutInMillis = keycloakProperties.getConnectTimeoutInMillis();
		Integer connectionTTLInSecs = keycloakProperties.getConnectTTLInSeconds();
		Integer checkoutInMillis = keycloakProperties.getConnectCheckoutTimeoutInMillis();
				
		ResteasyClientBuilder clientBuilder = new ResteasyClientBuilderImpl()
			      .connectTimeout(connectTimeoutInMillis, TimeUnit.MILLISECONDS)
			      .readTimeout(readTimeoutInMillis, TimeUnit.MILLISECONDS)
			      .connectionPoolSize(connectionPoolSize) 
			      .connectionTTL(connectionTTLInSecs, TimeUnit.SECONDS)
			      .connectionCheckoutTimeout(checkoutInMillis, TimeUnit.SECONDS) 
			      .executorService(executorService)
			      //.trustStore(trustStore)
			      .hostnameVerification(ResteasyClientBuilder.HostnameVerificationPolicy.ANY);
		
		boolean keycloakSslEnabled = keycloakProperties.isKeycloakSslEnabled();
		if (keycloakSslEnabled) {
			final SSLContext sslContext = createSslContext(keycloakSslProps);
			if (sslContext != null) {
				clientBuilder.sslContext(sslContext);
			}
		}	
		
		return clientBuilder.build();
	}	
	
	/**
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	@Bean(name = "keycloakClient-ExecutorService", destroyMethod = "shutdown")
	public ExecutorService keycloackWorkerExecutor() throws InterruptedException {

		BasicThreadFactory customThreadfactory = new BasicThreadFactory.Builder()
				.namingPattern("Keycloak-Client-thread-%d")
				.daemon(false)
				.priority(Thread.MAX_PRIORITY)
				.uncaughtExceptionHandler(new UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {

						log.error(String.format("Thread %s threw exception - %s", t.getName(), e.getMessage()), e);
					}
				}).build();

		int awaitTerminationInMillis = keycloakProperties.getThreadAwaitTimeInMillis();
		int threadSize = keycloakProperties.getThreadPoolSize();

		ExecutorService executorService = Executors.newFixedThreadPool(threadSize, customThreadfactory);
		executorService.awaitTermination(awaitTerminationInMillis, TimeUnit.MILLISECONDS);

		return executorService;
	}

	private SSLContext createSslContext(KeycloakClientSslProperties keycloakSslProps) throws Exception {

		// SSL Configurations
		String PRIVATEKEY_PASSWORD = keycloakSslProps.getPrivateKeyPassword();
		String KEYSTORE_PATH = keycloakSslProps.getKeyStorePath();
		String KEYSTORE_PASS = keycloakSslProps.getKeyStorePassword();
		String TRUSTSTORE_PATH = keycloakSslProps.getTrustStorePath();
		String TRUSTSTORE_PASS = keycloakSslProps.getTrustStorePassword();

		if (StringUtils.isNotBlank(KEYSTORE_PATH)) {
			KEYSTORE_PATH = KEYSTORE_PATH.startsWith("file:///") ? StringUtils.removeStart(KEYSTORE_PATH, "file:///")
					: KEYSTORE_PATH;
		}

		if (StringUtils.isNotBlank(TRUSTSTORE_PATH)) {
			TRUSTSTORE_PATH = TRUSTSTORE_PATH.startsWith("file:///")
					? StringUtils.removeStart(TRUSTSTORE_PATH, "file:///")
					: TRUSTSTORE_PATH;
		}

		log.info("Key-store-path {}", KEYSTORE_PATH);
		log.info("Key-store-password {}", KEYSTORE_PASS);
		log.info("Trust-store-path {}", TRUSTSTORE_PATH);
		log.info("Trust-store-password {}", TRUSTSTORE_PASS);

		// SSLContext instance = SSLContext.getInstance("TLSv1.2");
		SSLContext instance = SSLContext.getInstance("SSL");

		KeyStore keyStore = KeyStore.getInstance("JKS");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		try (InputStream input = new FileInputStream(new File(KEYSTORE_PATH))) {
			keyStore.load(input, KEYSTORE_PASS.toCharArray());
		}

		kmf.init(keyStore, PRIVATEKEY_PASSWORD.toCharArray());

		KeyStore trustStore = KeyStore.getInstance("JKS");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		try (InputStream input = new FileInputStream(new File(TRUSTSTORE_PATH))) {
			trustStore.load(input, TRUSTSTORE_PASS.toCharArray());
		}
		tmf.init(trustStore);

		instance.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		return instance;
	}

	/*
	@PreDestroy
	public void closeKeycloak() {
		Keycloak keycloak = context.getBean(Keycloak.class);
		if (keycloak != null && !keycloak.isClosed()) {
			keycloak.close();
		}
	}
	*/
}
