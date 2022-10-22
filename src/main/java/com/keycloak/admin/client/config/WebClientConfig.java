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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
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

	@Value("${keycloak.auth.baseUrl:}")
	private String authServerUrl;

	private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel MacOS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";

	@Bean("keycloakClient")
	public WebClient keycloackWebClient(WebClient.Builder webClientBuilder) throws SSLException {
		String BASE_URL = this.authServerUrl;

		DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(BASE_URL);
		uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);

		webClientBuilder.filter(LogFilters.logRequest());
		webClientBuilder.filter(LogFilters.logResponse());
		webClientBuilder.filter(WebClientFilters.tracingFilter());

		webClientBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		webClientBuilder.defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT);

		webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(30 * 1024 * 1024)).build());

		return webClientBuilder.clone().uriBuilderFactory(uriBuilderFactory).baseUrl(BASE_URL)
				.clientConnector(getConnector()).build();
	}

	private ReactorClientHttpConnector getNoSSLConnector() throws SSLException {

		HttpClient httpClient = HttpClient.create().noSSL();

		return new ReactorClientHttpConnector(httpClient);
	}

	private ReactorClientHttpConnector getConnector() throws SSLException {

		final ConnectionProvider theTcpClientPool = ConnectionProvider.create("tcp-client-pool"); // default pool size
		// 500
		final LoopResources theTcpClientLoopResources = LoopResources.create("tcp-client-loop", 100, true);

		HttpClient httpClient = HttpClient.create(theTcpClientPool).compress(true)
				.secure(sslContextSpec -> sslContextSpec.sslContext(noSecureSSL())
						.handshakeTimeout(Duration.ofSeconds(30)).closeNotifyFlushTimeout(Duration.ofSeconds(10))
						.closeNotifyReadTimeout(Duration.ofSeconds(10)))
				// configure a response timeout
				.responseTimeout(Duration.ofSeconds(1))
				// .proxy(spec ->
				// spec.type(ProxyProvider.Proxy.HTTP).host("proxy").port(8080).connectTimeoutMillis(30000))
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverProperties.getConnectTimeoutInMillis())
				.option(ChannelOption.SO_TIMEOUT, serverProperties.getRequestTimeoutInMillis()) // Socket Timeout
				.option(ChannelOption.SO_KEEPALIVE, true).option(EpollChannelOption.TCP_KEEPIDLE, 300)
				.option(EpollChannelOption.TCP_KEEPINTVL, 60).option(EpollChannelOption.TCP_KEEPCNT, 8)
				.runOn(theTcpClientLoopResources).doOnConnected(connection -> {
					// set the read and write timeouts
					connection.addHandlerLast(
							new ReadTimeoutHandler(serverProperties.getReadTimeoutInMillis(), TimeUnit.MILLISECONDS));
					connection.addHandlerLast(
							new WriteTimeoutHandler(serverProperties.getWriteTimeoutInMillis(), TimeUnit.MILLISECONDS));
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
