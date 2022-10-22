/**
 * 
 */
package com.keycloak.admin.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import lombok.extern.log4j.Log4j2;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;
import org.springframework.util.ResourceUtils;

import com.keycloak.admin.client.common.utils.YamlPropertySourceFactory;
import com.maxmind.geoip2.DatabaseReader;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class AppConfiguration implements EnvironmentAware, ApplicationContextAware {

	private ApplicationContext context;
	
	@Value("${totp.time.period}")
	private int defaultTimePeriod;
	
	@Value("${totp.code.length}")
	private int defaultDigits;

	@Override
	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public void setEnvironment(final Environment environment) {
		String truststoreLocation = environment.getProperty("server.ssl.key-store");
		String truststorePassword = environment.getProperty("server.ssl.key-store-password");

		if (truststoreLocation != null) {
			System.setProperty("javax.net.ssl.trustStore", truststoreLocation);
		}

		if (truststorePassword != null) {
			System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
		}
	}

	@Bean
	public IdGenerator idGenerator() {
		return new JdkIdGenerator();
	}

	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return modelMapper;
	}
	
	/**
	 * Totp hashing algorithm for Totp code verification
	 * 
	 * @return
	 */
	@Bean
	public HashingAlgorithm hashingAlgorithm() {
		return HashingAlgorithm.SHA512;
	}
	
	@Bean
	public CodeVerifier codeVerifier() {
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		
		return new DefaultCodeVerifier(codeGenerator, timeProvider);
	}
	
	@Bean
	public SecretGenerator secretGenerator() {
		
		return new DefaultSecretGenerator();
	}
	
	@Bean
	public QrDataFactory qrDataFactory() {
		
		return new QrDataFactory(hashingAlgorithm(), defaultDigits, defaultTimePeriod);
	}
	
	@Bean
	public QrGenerator qrGenerator() {		
		return new ZxingPngQrGenerator();
	}
	
	
}
