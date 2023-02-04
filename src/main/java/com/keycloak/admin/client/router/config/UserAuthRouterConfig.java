/**
 * 
 */
package com.keycloak.admin.client.router.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.keycloak.admin.client.common.utils.RouterHelper;
import com.keycloak.admin.client.handlers.UserAuthenticationHandler;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.LogoutRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.response.model.AppResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Configuration
public class UserAuthRouterConfig {

	/**
	 * 
	 * @param loginHandler
	 * @return
	 */
	@Bean
	@RouterOperations({ 		
		@RouterOperation(
				path = "/api/v1/signin", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "signin",
				operation = @Operation(summary = "Login a User", description = "API endpoint for login", tags = { "login", "token" },
						responses = {
								@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AuthenticationResponse.class)),
										@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class, description = "Token details")) }),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
						},
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = AuthenticationRequest.class)))
				)
		),
		@RouterOperation(
				path = "/api/v1/signout/{id}", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "logout",
				operation = @Operation(operationId = "logout", summary = "logout a user", tags = { "logout" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Ends User session by invalidating the refresh token", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
										@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = LogoutRequest.class))))
		),
		@RouterOperation(
				path = "/api/v1/signmeout", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "logMeOut",
				operation = @Operation(operationId = "logMeOut", summary = "logout current user", tags = { "logout" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Ends User session by invalidating the refresh token", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
										@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = LogoutRequest.class))))
		),
		@RouterOperation(
				path = "/api/v1/my-info", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.GET, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "findMyProfile",
				operation = @Operation(operationId = "findMyProfile", summary = "find current user profile", tags = { "profile user" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/my-info", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateMyProfile",
				operation = @Operation(operationId = "updateMyProfile", summary = "update current user profile", tags = { "update user profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserDetailsUpdateRequest.class))),
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/user-info/{id}", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.GET, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "findUserProfile",
				operation = @Operation(operationId = "findUserProfile", summary = "find user profile", tags = { "profile user" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						parameters = {
								@Parameter(in = ParameterIn.PATH, name = "id")
						})
		),
		@RouterOperation(
				path = "/api/v1/user-info/{id}", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateUserProfile",
				operation = @Operation(operationId = "updateUserProfile", summary = "update user profile", tags = { "update user profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserDetailsUpdateRequest.class))),
						parameters = {
								@Parameter(in = ParameterIn.PATH, name = "id")
						})
		),
		@RouterOperation(
				path = "/api/v1/my/status", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateMyStatus",
				operation = @Operation(operationId = "updateMyStatus", summary = "update my status", tags = { "update status" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = StatusUpdateRequest.class))),
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/user/{id}/status", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateUserStatus",
				operation = @Operation(operationId = "updateUserStatus", summary = "update user status", tags = { "update status" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = StatusUpdateRequest.class))),
						parameters = {
								@Parameter(in = ParameterIn.PATH, name = "id")
						})
		),
		@RouterOperation(
				path = "/api/v1/email/status", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateUserEmailStatus",
				operation = @Operation(operationId = "updateUserEmailStatus", summary = "update email status", tags = { "update email status" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = EmailStatusUpdateRequest.class))),
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/access-token", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.DELETE, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "revokeToken",
				operation = @Operation(operationId = "revokeToken", summary = "revoke access token", tags = { "revoke token" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation message", 
										content = @Content(schema = @Schema(implementation = String.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = LogoutRequest.class))),
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/access-token", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.PATCH, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "refreshToken",
				operation = @Operation(operationId = "refreshToken", summary = "refresh access token", tags = { "refresh token" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AuthenticationResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = LogoutRequest.class))),
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/user/{id}", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "enableUserProfile",
				operation = @Operation(operationId = "enableUserProfile", summary = "enable user profile", tags = { "enable profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						}, 
						parameters = {
								@Parameter(in = ParameterIn.PATH, name = "id")
						})
		),
		@RouterOperation(
				path = "/api/v1/user/{id}", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
						consumes = {
								MediaType.APPLICATION_XML_VALUE,
								MediaType.APPLICATION_JSON_VALUE	
						},
				method = RequestMethod.DELETE, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "disableUserProfile",
				operation = @Operation(operationId = "disableUserProfile", summary = "disable user profile", tags = { "disable profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful delete operation with a message", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
						},
						parameters = {
								@Parameter(in = ParameterIn.PATH, name = "id")
						})
		),
	})
	RouterFunction<ServerResponse> userAuthRoute(@Autowired final UserAuthenticationHandler userDetHandler) {

		return RouterFunctions.route(RouterHelper.i(POST("/api/v1/signin")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						userDetHandler::signin)
				.andRoute(RouterHelper.i(POST("/api/v1/signout/{id}")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						userDetHandler::logout)
				.andRoute(RouterHelper.i(POST("/api/v1/signmeout")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::logMeOut)
				.andRoute(RouterHelper.i(GET("/api/v1/my-info")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::findMyProfile)
				.andRoute(RouterHelper.i(PUT("/api/v1/my-info")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::updateMyProfile)
				.andRoute(RouterHelper.i(GET("/api/v1/user-info/{id}")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::findUserProfile)
				.andRoute(RouterHelper.i(PUT("/api/v1/user-info/{id}")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::updateUserProfile)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/my/status")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::updateMyStatus)
				.andRoute(RouterHelper.i(PUT("/api/v1/user/{id}/status")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::updateUserStatus)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/email/status")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::updateUserEmailStatus)
				
				.andRoute(RouterHelper.i(DELETE("/api/v1/access-token")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::revokeToken)				
				.andRoute(RouterHelper.i(PATCH("/api/v1/access-token/{username}")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::refreshToken)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/user/{id}")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::enableUserProfile)
				.andRoute(RouterHelper.i(DELETE("/api/v1/user/{id}")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						userDetHandler::disableUserProfile);
	}
}
