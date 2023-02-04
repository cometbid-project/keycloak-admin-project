/**
 * 
 */
package com.keycloak.admin.client.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.relaxedRequestParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.config.SecurityConfig;
import com.keycloak.admin.client.config.keycloak.KeycloakConfiguration;
import com.keycloak.admin.client.controllers.GroupController;
import com.keycloak.admin.client.dataacess.GroupBuilder;
import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.oauth.service.it.GroupService;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@WebFluxTest(controllers = GroupController.class) // <1>
@DisplayName("User Group endpoints API")
@Import({ AuthProperties.class, ResponseCreator.class, GroupController.class })
@ExtendWith({ RestDocumentationExtension.class, SpringExtension.class })
@ContextConfiguration(classes = { SecurityConfig.class, KeycloakConfiguration.class })
class GroupApiAuthenticationTest {

	@Autowired 
	private ApplicationContext applicationContext;

	@Autowired
	private WebTestClient webTestClient;

    @MockBean 
    private GroupService groupService;
    
    protected String PATH = "/api/v1/groups";
    private String apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1...";
	
	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		
		this.webTestClient = 
				WebTestClient.bindToApplicationContext(applicationContext)
				.apply(springSecurity())
				.configureClient()
				.baseUrl("http://localhost:8080")
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
	
	@DisplayName("as authenticated user is granted")
	@Nested
	class AuthenticatedGroupApi {		
		
		/**  
		 * 
		 */
		@WithMockUser(roles = { "ADMIN" })
		@SuppressWarnings("unchecked")
		@DisplayName("to get all realm groups, authenticated")
		@Test
		void verifyGetAllGroupsAuthenticated() {
			log.info("running  " + this.getClass().getName() + ".getAllRealmGroups()");
			List<GroupVO> listGroup = GroupBuilder.groupList();
			
			given(groupService.findAllRealmGroups())
					.willReturn(Flux.fromIterable(listGroup));

			String endPointURI = "";	
			final Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			Map<String, Object> templateVar = new HashMap<>();			
			
			restCallGroupResponseMultiple(endPointURI,
				 templateVar, params, listGroup.toArray(GroupVO[]::new))			
				.consumeWith(
		            document(
		                   "get all realm groups", 			                    
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Content-Type")
						  .description("The Content-Type of the request payload, e.g. `application/json`"),
						headerWithName("api-key")
						  .description("The API Key unique to each registered user for Rate-limiting")
						//headerWithName("Authorization")
						  //.description("The Oauth2 `Bearer` token issued at login for Request Authentication")
					),
					responseHeaders(
								headerWithName("Content-Type")
								  .description("The Content-Type of the response payload, e.g. `application/json`")),
					relaxedRequestParameters( 
							parameterWithName("lang")
							.description("To instruct the resource to use a language of choice in"
									+ " the response messages. en=English, fr=French etc"))
			));
		}
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
		@DisplayName("to get a single group, authenticated")
	    void verifyGetGroupByIdAuthenticated() {

			log.info("running  " + this.getClass().getName() + ".findRealmGroup()");
			List<GroupVO> listGroup = GroupBuilder.groupList();
			
			GroupVO groupVo = listGroup.get(0);
			log.info("GroupVO {}", groupVo);
			
			given(groupService.findRealmGroupById(anyString()))
					.willReturn(Mono.just(groupVo));
			
			String endPointURI = "/group/" + groupVo.getId();	
			final Map<String, String> params = new HashMap<>();
			params.put("lang", "fr");
			
			Map<String, Object> templateVar = new HashMap<>();			
				
			restCallGroupResponseOne(
					endPointURI, templateVar, 
					params, groupVo)			
				 .consumeWith(
		            document(
		                    "get a single group", 			                    
		                    preprocessRequest(prettyPrint()), 
		                    preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName("Content-Type")
						  .description("The Content-Type of the request payload, e.g. `application/json`"),
						headerWithName("api-key")
						  .description("The API Key unique to each registered user for Rate-limiting")
						//headerWithName("Authorization")
						  //.description("The Oauth2 `Bearer` token issued at login for Request Authentication")
					),
					responseHeaders(
								headerWithName("Content-Type")
								  .description("The Content-Type of the response payload, e.g. `application/json`")),
					relaxedRequestParameters( 
							parameterWithName("lang")
							.description("To instruct the resource to use a language of choice in"
									+ " the response messages. en=English, fr=French etc"))
			));
		}
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
		@DisplayName("to delete a single group, authenticated")
	    void verifyDeleteGroupByIdAuthenticated() {

			when(groupService.deleteRealmGroup(anyString()))
					.thenReturn(Mono.just("SUCCESS"));
			
			UUID groupId = UUID.randomUUID();
		      
		      String endPointURI = "/group/" + groupId;

		      webTestClient
		          .mutateWith(csrf())
		          .delete()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isOk()
		          .expectBody()
		          .consumeWith(
				       document(
				          "delete a single group", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
		}	
		
		@WithMockUser(roles = { "ADMIN" })
		@Test
	    @DisplayName("to create a new group, authenticated-user")
	    void verifyCreateGroupAuthenticated() throws JsonProcessingException {

	    	 UUID groupId = UUID.randomUUID();
	    	 String endPointURI = "";
	    	 
		     GroupVO expectedGroup = GroupBuilder.group().withId(groupId).build();
	
		     CreateGroupRequest groupResource =  GroupBuilder.group()
		    		 			.buildCreateGroupRequest();	          
	
		     log.info("Group Resource {}", groupResource); 
		     when(groupService.createRealmGroup(any())).thenReturn(Mono.just(expectedGroup));
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(Mono.just(groupResource), CreateGroupRequest.class)
		          .exchange()
		          .expectStatus()
		          .isCreated()
		          .expectBody()
		          .consumeWith(
				       document(
				          "create a new group", 			                    
				             preprocessRequest(prettyPrint()), 
				             preprocessResponse(prettyPrint())));
	    }
	  }

	  @DisplayName("as unauthenticated user is denied with 401")
	  @Nested
	  class UnAuthenticatedGroupApi {

	    @Test
	    @DisplayName("to get list of groups, unauthenticated-user")
	    void verifyGetAllRealmGroupsUnAuthenticated() {

	    	String endPointURI = "";
		    
	    	webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to get a single group, unauthenticated-user")
	    void verifyGetGroupUnAuthenticated() {

	      UUID groupId = UUID.randomUUID();

	      String endPointURI = "/group/" + groupId;
		    
	    	webTestClient
		          .get()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to delete a group, unauthenticated-user")
	    void verifyDeleteGroupUnAuthenticated() {

	      UUID groupId = UUID.randomUUID();
	      
	      String endPointURI = "/group/" + groupId;

	      webTestClient
	          .mutateWith(csrf())
	          .delete()
	          .uri(PATH + endPointURI)
	          .accept(MediaType.APPLICATION_JSON)
	          .exchange()
	          .expectStatus()
	          .isUnauthorized();
	    }

	    @Test
	    @DisplayName("to create a new group, unauthenticated-user")
	    void verifyCreateGroupUnAuthenticated() throws JsonProcessingException {

	    	 UUID groupId = UUID.randomUUID();
	    	 String endPointURI = "";
	    	 
		     GroupVO expectedGroup = GroupBuilder.group().withId(groupId).build();
	
		     CreateGroupRequest groupResource =  GroupBuilder.group()
		    		 			.buildCreateGroupRequest();	          
	
		     when(groupService.createRealmGroup(any())).thenReturn(Mono.empty());
	
		     webTestClient
		          .mutateWith(csrf())
		          .post()
		          .uri(PATH + endPointURI)
		          .accept(MediaType.APPLICATION_JSON)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body(BodyInserters.fromValue(new ObjectMapper().writeValueAsString(groupResource)))
		          .exchange()
		          .expectStatus()
		          .isUnauthorized();
	    }	    
	}
	  
	protected BodyContentSpec restCallGroupResponseOne(
			 String endPointURI, Map<String, Object> templateVar,
			 Map<String, String> params, GroupVO groupVO) {
		  
		  return this.webTestClient
					.mutateWith(csrf())
					.get()
					.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
					.queryParams(buildParameters(params))
					.build(templateVar))
					.header("api-key", apiKey)
					.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
					.accept(MediaType.APPLICATION_JSON)
					.exchange()
					.expectStatus()
					.isOk()
					.expectBody()
					.jsonPath("$.id")
					.isEqualTo(groupVO.getId())
					.jsonPath("$.name")
					.isEqualTo(groupVO.getName())
					.jsonPath("$.path")					
					.isEqualTo(groupVO.getPath())
					.jsonPath("$.realm_roles")
					.isEqualTo(groupVO.getRealmRoles())
					.jsonPath("$.attributes")
					.exists()					
					.jsonPath("$.client_roles")
					.exists();
		}

		protected BodyContentSpec restCallGroupResponseMultiple(
				String endPointURI,	Map<String, Object> templateVar, 
				Map<String, String> params, GroupVO... groupVO) {

			return this.webTestClient
					.mutateWith(csrf())
					.get()
					.uri(uriBuilder -> uriBuilder.path(PATH + "" + endPointURI)
					.queryParams(buildParameters(params))
					.build(templateVar))
					.header("api-key", apiKey)
					.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
					.accept(MediaType.APPLICATION_JSON)
					.exchange()
					.expectStatus()
					.isOk()
					.expectBody()
					.jsonPath("$.[0].id")
					.isEqualTo(groupVO[0].getId())
					.jsonPath("$.[0].name")
					.isEqualTo(groupVO[0].getName())
					.jsonPath("$.[0].path")					
					.isEqualTo(groupVO[0].getPath())
					.jsonPath("$.[0].attributes")
					.isEmpty()					
					.jsonPath("$.[0].realm_roles")
					.isEqualTo(groupVO[0].getRealmRoles())
					.jsonPath("$.[0].client_roles")
					.isEmpty()
					.jsonPath("$.[1].id")
					.isEqualTo(groupVO[1].getId())
					.jsonPath("$.[1].name")
					.isEqualTo(groupVO[1].getName())
					.jsonPath("$.[1].path")					
					.isEqualTo(groupVO[1].getPath())
					.jsonPath("$.[1].attributes")
					.isEmpty()					
					.jsonPath("$.[1].realm_roles")
					.isEqualTo(groupVO[1].getRealmRoles())
					.jsonPath("$.[1].client_roles")
					.isEmpty();
		}
}
