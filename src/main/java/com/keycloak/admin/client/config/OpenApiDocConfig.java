/**
 * 
 */
package com.keycloak.admin.client.config;

import java.util.Arrays;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import com.keycloak.admin.client.common.utils.YamlPropertySourceFactory;

/**
 * @author Gbenga
 *
 */
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class OpenApiDocConfig {
	
	@Value("${local.server.apidoc.url}")
	private String localServerUrl;

	@Value("${test.server.apidoc.url}")
	private String testServerUrl;

	@Value("${api.common.version}")
	private String appVersion;

	@Value("${api.common.title}")
	private String appTitle;

	@Value("${api.common.description}")
	private String appDescription;

	@Value("${api.common.termsOfService}")
	private String appTermsOfService;

	@Value("${api.common.license}")
	private String appLicenseName;

	@Value("${api.common.licenseUrl}")
	private String appLicenseUrl;
	
	@Value("${api.common.externalDocDesc}")
	String apiExternalDocDesc;
	
	@Value("${api.common.externalDocUrl}")
	String apiExternalDocUrl;
	
	@Value("${api.common.contact.name}")
	String apiContactName;
	
	@Value("${api.common.contact.url}")
	String apiContactUrl;
	
	@Value("${api.common.contact.email}")
	String apiContactEmail;
	
	@Bean 
	public GroupedOpenApi controllerApi() {
		return GroupedOpenApi.builder()
				  .group("Gateway Controller Services")
				  .packagesToScan("com.keycloak.admin.client.controllers")
				  .build();
	}
	
	@Bean 
	public GroupedOpenApi handlerApi() {
		return GroupedOpenApi.builder()
				  .group("Gateway Handler Services")
				  .packagesToScan("com.keycloak.admin.client.handlers")
				  .build();
	}

	@Bean
	public OpenAPI OpenApiDocumentation() {
		Server localServer = new Server();
		localServer.setDescription("for local usages only");
		localServer.setUrl(localServerUrl);

		Server testServer = new Server();
		testServer.setDescription("for testing purposes only");
		testServer.setUrl(testServerUrl);
		
		OpenAPI openAPI = new OpenAPI()
				.components(new Components()
						.addSecuritySchemes("apikey",
								new SecurityScheme().type(SecurityScheme.Type.APIKEY)
								.scheme("apikey"))
						.addSecuritySchemes("openIdConnect",
								new SecurityScheme().type(SecurityScheme.Type.OPENIDCONNECT)
								.scheme("openIdConnect")))
				.info(new Info().title(appTitle)
						.description(appDescription)
						.version(appVersion)
						.contact(new Contact()
								//
								.name(apiContactName)
								//
								.url(apiContactUrl)
								//
								.email(apiContactEmail))
						//
						.termsOfService(appTermsOfService)
						//
						.license(new License()
								//
								.name(appLicenseName)
								//
								.url(appLicenseUrl)))
				.externalDocs(new ExternalDocumentation()
						.description(apiExternalDocDesc)
						.url(apiExternalDocUrl));
		
		openAPI.setServers(Arrays.asList(localServer, testServer));

		return openAPI;
	}
}
