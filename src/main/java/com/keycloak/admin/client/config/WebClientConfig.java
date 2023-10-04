/**
 * 
 */
package com.keycloak.admin.client.config;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.keycloak.admin.client.filters.LogFilters;
import com.keycloak.admin.client.filters.WebClientFilters;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

	private final KeycloakClientSslProperties keycloakSslProps;

	private final AuthProperties serverProperties;
	
	@Bean("keycloak-webClient")
	public WebClient keycloackWebClient(WebClient.Builder webClientBuilder) throws SSLException {
		//String BASE_URL = serverProperties.getBaseUrl();
		//log.info("Keycloak Container Base Url {}", BASE_URL);

		//DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(BASE_URL);
		//uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
		
		Integer codecMemorySize = serverProperties.getCodecInMemorySize();

		webClientBuilder.filter(LogFilters.logRequest());
		webClientBuilder.filter(LogFilters.logResponse());
		webClientBuilder.filter(WebClientFilters.tracingFilter());

		webClientBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE);
		//webClientBuilder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT);

		webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(codecMemorySize)).build());

		return webClientBuilder//.uriBuilderFactory(uriBuilderFactory).baseUrl(BASE_URL)
				.clientConnector(getConnector()).build();
	}

	private ReactorClientHttpConnector getNoSSLConnector() throws SSLException {

		HttpClient httpClient = HttpClient.create().noSSL();

		return new ReactorClientHttpConnector(httpClient);
	}
	
	private ReactorClientHttpConnector getConnector() throws SSLException {

		Integer connectTimeoutInMillis = serverProperties.getConnectTimeoutInMillis();
		Integer readTimeoutInMillis = serverProperties.getRequestTimeoutInMillis();
		Integer writeTimeoutInMillis = serverProperties.getWriteTimeoutInMillis();
		
		Integer handshakeTimeout = serverProperties.getHandshakeTimeout();
		Integer notifyReadTimeout = serverProperties.getNotifyReadTimeout();
		Integer notifyFlushTimeout = serverProperties.getNotifyFlushTimeout();
		
		Integer responseTimeout = serverProperties.getResponseTimeout();
		
		final ConnectionProvider theTcpClientPool = ConnectionProvider.create("tcp-client-pool"); 
		final LoopResources theTcpClientLoopResources = LoopResources.create("tcp-client-loop", 100, true);

		HttpClient httpClient = HttpClient.create(theTcpClientPool).compress(true)
				.secure(sslContextSpec -> sslContextSpec.sslContext(noSecureSSL())
						.handshakeTimeout(Duration.ofSeconds(handshakeTimeout))
						.closeNotifyFlushTimeout(Duration.ofSeconds(notifyReadTimeout))
						.closeNotifyReadTimeout(Duration.ofSeconds(notifyFlushTimeout)))
				// configure a response timeout
				.responseTimeout(Duration.ofSeconds(responseTimeout))
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMillis)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.runOn(theTcpClientLoopResources).doOnConnected(connection -> {
					// set the read and write timeouts
					connection.addHandlerLast(
							new ReadTimeoutHandler(readTimeoutInMillis, TimeUnit.MILLISECONDS));
					connection.addHandlerLast(
							new WriteTimeoutHandler(writeTimeoutInMillis, TimeUnit.MILLISECONDS));
				});

		// ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

		return new ReactorClientHttpConnector(httpClient);
	}

	private SslContext noSecureSSL() {

		try {
			return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	private SslContext getTwoWaySecureSslContext() {

		try (FileInputStream keyStoreFileInputStream = new FileInputStream(
				ResourceUtils.getFile(keycloakSslProps.getKeyStorePath()));
				FileInputStream trustStoreFileInputStream = new FileInputStream(
						ResourceUtils.getFile(keycloakSslProps.getTrustStorePath()));) {

			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyStoreFileInputStream, keycloakSslProps.getKeyStorePassword().toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keycloakSslProps.getKeyStorePassword().toCharArray());

			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(trustStoreFileInputStream, keycloakSslProps.getTrustStorePassword().toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trustStore);

			return SslContextBuilder.forClient().keyManager(keyManagerFactory).trustManager(trustManagerFactory)
					.build();

		} catch (Exception e) {
			log.error("An error has occurred: ", e);

			throw new RuntimeException("Failed to complete TLS Handshake, SSL certifate couldn't processed");
		}
	}

}
