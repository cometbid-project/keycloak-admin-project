/**
 * 
 */
package com.keycloak.admin.client.api;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
//import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;   
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.javafaker.Faker;
import com.keycloak.admin.client.error.handlers.GlobalControllerExceptionHandler;

import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WithMockUser
@WebFluxTest
@DisplayName("Miscellaneous endpoints")
//@AutoConfigureWebTestClient
@Import({ UtilProfile.class, AppConfig.class, GlobalControllerExceptionHandler.class })
@ContextConfiguration(classes = { AppConfig.class, IndexController.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
public class MiscellaneousEndpointsTest {
	
	@Autowired
	protected WebTestClient webTestClient;

	@Autowired
	private ApplicationContext applicationContext;

	protected String PATH = "";

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).configureClient()
				.baseUrl("http://localhost:9091")
				.filter(
					documentationConfiguration(restDocumentation)
						.operationPreprocessors()
						.withRequestDefaults(prettyPrint())
						.withResponseDefaults(prettyPrint()))
				.build();
					
	}
	
	protected MultiValueMap<String, String> buildParameters(Map<String, String> params) {

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
		parameters.setAll(params);

		return parameters;
	}
	
	
	/**  
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to test header parameters")
	@Test
	public void headersExample() throws Exception {
		
		String endPointURI = "/headers";
		final Map<String, String> params = new HashMap<>();
		params.put("lang", "fr");
		
		Map<String, Object> templateVar = new HashMap<>();
		
		String apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1...";
		String token = RandomString.generateSecureRandomHexToken(16);
		Faker faker = new Faker();
		String username = faker.internet().safeEmailAddress();
		
		this.webTestClient
			.mutateWith(csrf())
			.get()
			.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
			.queryParams(buildParameters(params))
			.build(templateVar))
			.header("Authorization", "Bearer " + Base64Utils
                    .encodeToString((username + ":" + token)
                    		.getBytes(StandardCharsets.UTF_8)))
			.header("api-key", apiKey)
			.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()				
			.consumeWith(
	            document(
	                    "headers-example", 			                    
	                    preprocessRequest(prettyPrint()), 
	                    preprocessResponse(prettyPrint()),
				requestHeaders(
					headerWithName("Content-Type")
					  .description("The Content-Type of the request payload, e.g. `application/json`"),
					headerWithName("api-key")
					  .description("The API Key unique to each registered user for Rate-limiting"),
					headerWithName("Authorization")
					  .description("The Oauth2 `Bearer` token issued at login for Request Authentication")
				),
				responseHeaders(
							headerWithName("Content-Type")
							  .description("The Content-Type of the response payload, e.g. `application/json`")),
				relaxedRequestParameters( 
						parameterWithName("lang")
						.description("To instruct the resource to use a language of choice in the response messages. en=English, fr=French etc"))
			));
	}

	/**  
	 * 
	 */
	@WithMockUser
	@SuppressWarnings("unchecked")
	@DisplayName("to test error responses format")
	@Test
	public void errorExample() throws Exception {
		
		String endPointURI = "/error";
		final Map<String, String> params = new HashMap<>();
		Map<String, Object> templateVar = new HashMap<>();
		
		restCallResponseOne(endPointURI, templateVar,
				 params)			
			.consumeWith(
		            document(
		                    "error-example", 			                    
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint()),
		                    relaxedResponseFields( 
		                			fieldWithPath("apiVersion")
		                			.description("The resources version being consumed"), 
		                			fieldWithPath("apiError.error")
		                			.description("The HTTP error that occurred, e.g. `Unprocessable Entity` or `Bad Request`"),
		                			fieldWithPath("apiError.error_code")
		                			.description("Custom error code for specificity and quick error identification e.g. INV-DATA-001"),
		                			fieldWithPath("apiError.reason")
		                			.description("The reason for the error, most times the same as `apiError.error`"),
		                			fieldWithPath("apiError.path")
		                			.description("The path to which the request was made"),
		                			fieldWithPath("apiError.message")
		                			.description("A description of the cause of the error"), 
		                			fieldWithPath("apiError.status")
		                			.description("The HTTP status code, e.g. `422` or `400`"),
		                			fieldWithPath("apiError.error_time")
		                			.description("The time, in UTC timezone format, at which the error occurred e.g. 2021-02-04T04:55:59.134622200Z"),
		                			fieldWithPath("apiError.trace_id")
		                			.description("This is a unique id used for quick search or tracing of errors in log files. Mostly for internal use"),
		                			fieldWithPath("apiError.debug_message")
		                			.description("A detail message on how to resolve the error"),
		                			fieldWithPath("apiError.error_details[].object")
		                			.description("The object that resulted in the error, mostly null for simple fields"),
		                			fieldWithPath("apiError.error_details[].field")
		                			.description("The field that resulted in the error"),
		                			fieldWithPath("apiError.error_details[].rejected_value")
		                			.description("The field or object value that was rejected for some reasons"),
		                			fieldWithPath("apiError.error_details[].message")
		                			.description("A detail description of why the field or object value is not acceptable"),
		                			fieldWithPath("sendReport")
		                			.description("A URL where the error details can be posted to be handled by the Admin"))
		                    ));
	}

	protected BodyContentSpec restCallResponseOne(String endPointURI, Map<String, Object> templateVar,
			Map<String, String> params) {

		return this.webTestClient
				.mutateWith(csrf())
				.get()
				.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
				.queryParams(buildParameters(params))
				.build(templateVar))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.is4xxClientError()
				.expectBody()		
				.jsonPath("$.apiVersion")
				.exists()
				.jsonPath("$.apiError.error")
				.exists()
				.jsonPath("$.apiError.error_code")
				.exists()
				.jsonPath("$.apiError.reason")
				.exists()
				.jsonPath("$.apiError.path")
				.exists()
				.jsonPath("$.apiError.message")
				.exists()
				.jsonPath("$.apiError.status")
				.exists()
				.jsonPath("$.apiError.error_time")
				.exists()
				.jsonPath("$.apiError.trace_id")
				.hasJsonPath()
				.jsonPath("$.apiError.debug_message")
				.exists()
				.jsonPath("$.apiError.error_details")
				.exists()
				.jsonPath("$.sendReport")
				.exists();
	}
}
