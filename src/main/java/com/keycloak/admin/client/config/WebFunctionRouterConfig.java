/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import com.keycloak.admin.client.common.utils.RouterHelper;
import com.keycloak.admin.client.handlers.PasswordManagementHandler;
import com.keycloak.admin.client.handlers.RegistrationHandler;
import com.keycloak.admin.client.handlers.TokenManagementHandler;
import com.keycloak.admin.client.handlers.UserAuthenticationHandler;
import com.keycloak.admin.client.models.AuthenticationRequest;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EmailStatusUpdateRequest;
import com.keycloak.admin.client.models.EnableMfaResponse;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.LogoutRequest;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.StatusUpdateRequest;
import com.keycloak.admin.client.models.TotpRequest;
import com.keycloak.admin.client.models.UserDetailsUpdateRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.response.model.AppResponse;

import io.swagger.v3.oas.annotations.parameters.*;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springdoc.core.fn.builders.operation.Builder;

import lombok.extern.log4j.Log4j2;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;

/**
 *
 * @author Gbenga
 */
@Log4j2
@Configuration
public class WebFunctionRouterConfig {

	public static String VALIDATE_TOTP_PATH = "/totp";
	public static String REFRESH_TOKEN_PATH = "";

	/**
	 * 
	 * @param registrationHandler
	 * @return
	 */
	@Bean
	@RouterOperations({ 
		@RouterOperation(
				path = "/api/v1/signup", 
				produces = { MediaType.APPLICATION_XML_VALUE,
							 MediaType.APPLICATION_JSON_VALUE 
							 
				}, 
				method = RequestMethod.POST, 
				beanClass = RegistrationHandler.class, 
				beanMethod = "signupAdmin",
				operation = @Operation(
						operationId = "signupAdmin", summary = "register a user with ADMIN role", tags = {	"create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
									@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))))
			),
	    @RouterOperation(
	    		path = "/api/v1/ping", 
				produces = { MediaType.APPLICATION_XML_VALUE,
							 MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				method = RequestMethod.GET, 
				beanClass = RegistrationHandler.class, 
				beanMethod = "hello") 
	})
	RouterFunction<ServerResponse> signupRoute(@Autowired final RegistrationHandler registrationHandler) {

		return RouterFunctions
				.route(RouterHelper.i(POST("/api/v1/signup")).and(accept(APPLICATION_JSON)),
						registrationHandler::signupAdmin)
				.andRoute(RouterHelper.i(GET("/api/v1/ping")).and(accept(APPLICATION_JSON)), registrationHandler::hello);
	}

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
				method = RequestMethod.POST, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "signin",
				operation = @Operation(summary = "Login a User", description = "API endpoint for login", tags = { "login", "token" },
						responses = {
								@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
										@Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class, description = "Token details")) }),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
						},
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = AuthenticationRequest.class)))
				)
		),
		@RouterOperation(
				path = "/api/v1/signout", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				method = RequestMethod.POST, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "logout",
				operation = @Operation(operationId = "logout", summary = "logout a user", tags = { "logout" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Ends User session by invalidating the refresh token", content = {
										@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = LogoutRequest.class))))
		),
		@RouterOperation(
				path = "/api/v1/signmeout", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				method = RequestMethod.POST, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "logMeOut",
				operation = @Operation(operationId = "logMeOut", summary = "logout current user", tags = { "logout" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Ends User session by invalidating the refresh token", content = {
										@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
						}, 
						requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = LogoutRequest.class))))
		),
		@RouterOperation(
				path = "/api/v1/my-info", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				method = RequestMethod.GET, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "findMyProfile",
				operation = @Operation(operationId = "findMyProfile", summary = "find current user profile", tags = { "profile user" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = @Content(schema = @Schema(implementation = UserVO.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
						}, 
						parameters = {})
		),
		@RouterOperation(
				path = "/api/v1/my-info", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 
				}, 
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateMyProfile",
				operation = @Operation(operationId = "updateMyProfile", summary = "update current user profile", tags = { "update user profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = @Content(schema = @Schema(implementation = UserVO.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.GET, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "findUserProfile",
				operation = @Operation(operationId = "findUserProfile", summary = "find user profile", tags = { "profile user" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = @Content(schema = @Schema(implementation = UserVO.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateUserProfile",
				operation = @Operation(operationId = "updateUserProfile", summary = "update user profile", tags = { "update user profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with user profile details", 
										content = @Content(schema = @Schema(implementation = UserVO.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateMyStatus",
				operation = @Operation(operationId = "updateMyStatus", summary = "update my status", tags = { "update status" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = @Content(schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateUserStatus",
				operation = @Operation(operationId = "updateUserStatus", summary = "update user status", tags = { "update status" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = @Content(schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "updateUserEmailStatus",
				operation = @Operation(operationId = "updateUserEmailStatus", summary = "update email status", tags = { "update email status" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = @Content(schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.DELETE, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "revokeToken",
				operation = @Operation(operationId = "revokeToken", summary = "revoke access token", tags = { "revoke token" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation message", 
										content = @Content(schema = @Schema(implementation = String.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.PATCH, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "refreshToken",
				operation = @Operation(operationId = "refreshToken", summary = "refresh access token", tags = { "refresh token" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.PUT, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "enableUserProfile",
				operation = @Operation(operationId = "enableUserProfile", summary = "enable user profile", tags = { "enable profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful operation with a message", 
										content = @Content(schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
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
				method = RequestMethod.DELETE, 
				beanClass = UserAuthenticationHandler.class, 
				beanMethod = "disableUserProfile",
				operation = @Operation(operationId = "disableUserProfile", summary = "disable user profile", tags = { "disable profile" }, 
						responses = {
								@ApiResponse(responseCode = "200", description = "successful delete operation with a message", 
										content = @Content(schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
						},
						parameters = {
								@Parameter(in = ParameterIn.PATH, name = "id")
						})
		),
	})
	RouterFunction<ServerResponse> userAuthRoute(@Autowired final UserAuthenticationHandler userDetHandler) {

		return RouterFunctions.route(RouterHelper.i(POST("/api/v1/signin")).and(accept(APPLICATION_JSON)),
						userDetHandler::signin)
				.andRoute(RouterHelper.i(POST("/api/v1/signout")).and(accept(APPLICATION_JSON)),
						userDetHandler::logout)
				.andRoute(RouterHelper.i(POST("/api/v1/signmeout")).and(accept(APPLICATION_JSON)), 
						userDetHandler::logMeOut)
				.andRoute(RouterHelper.i(GET("/api/v1/my-info")).and(accept(APPLICATION_JSON)), 
						userDetHandler::findMyProfile)
				.andRoute(RouterHelper.i(PUT("/api/v1/my-info")).and(accept(APPLICATION_JSON)), 
						userDetHandler::updateMyProfile)
				.andRoute(RouterHelper.i(GET("/api/v1/user-info/{id}")).and(accept(APPLICATION_JSON)), 
						userDetHandler::findUserProfile)
				.andRoute(RouterHelper.i(PUT("/api/v1/user-info/{id}")).and(accept(APPLICATION_JSON)), 
						userDetHandler::updateUserProfile)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/my/status")).and(accept(APPLICATION_JSON)), 
						userDetHandler::updateMyStatus)
				.andRoute(RouterHelper.i(PUT("/api/v1/user/{id}/status")).and(accept(APPLICATION_JSON)), 
						userDetHandler::updateUserStatus)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/email/status")).and(accept(APPLICATION_JSON)), 
						userDetHandler::updateUserEmailStatus)
				
				.andRoute(RouterHelper.i(DELETE("/api/v1/access-token")).and(accept(APPLICATION_JSON)), 
						userDetHandler::revokeToken)				
				.andRoute(RouterHelper.i(PATCH("/api/v1/access-token")).and(accept(APPLICATION_JSON)), 
						userDetHandler::refreshToken)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/user/{id}")).and(accept(APPLICATION_JSON)), 
						userDetHandler::enableUserProfile)
				.andRoute(RouterHelper.i(DELETE("/api/v1/user/{id}")).and(accept(APPLICATION_JSON)), 
						userDetHandler::disableUserProfile);
	}

	/**
	 * 
	 * @param verificationHandler
	 * @return
	 */
	@Bean
	@RouterOperations({ 
		@RouterOperation(
				path = "/api/v1/totp", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				method = RequestMethod.POST, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "validateTotp",
				operation = @Operation(operationId = "validateTotp", summary = "Validate Totp code in Multi-Factor Authentication", tags = { "totp code" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successful totp validation operation with response token", 
									content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
					},
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = TotpRequest.class))),
					parameters = {})
			),
		@RouterOperation(
				path = "/api/v1/totp", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				method = RequestMethod.PUT, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "sendOtpCode",
				operation = @Operation(operationId = "sendOtpCode", summary = "Send Otp code in Multi-Factor Authentication", tags = { "Send Otp code" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successfully sent Otp code as alternative to Totp", 
									content = @Content(schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
					},
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = SendOtpRequest.class))),
					parameters = {})
			),
		@RouterOperation(
				path = "/api/v1/mfa", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				method = RequestMethod.POST, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "activateMfa",
				operation = @Operation(operationId = "activateMfa", summary = "Activate Multi-Factor Authentication", tags = { "activate totp code" },
						responses = {
								@ApiResponse(responseCode = "200", description = "successful activation of Multi-Factor Authentication", 
										content = @Content(schema = @Schema(implementation = EnableMfaResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
						},
						parameters = {})
			),
		@RouterOperation(
				path = "/api/v1/mfa", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				method = RequestMethod.DELETE, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "deactivateMfa",
				operation = @Operation(operationId = "deactivateMfa", summary = "De-Activate Multi-Factor Authentication", tags = { "deactivate totp code" },
						responses = {
								@ApiResponse(responseCode = "200", description = "successful de-activation of Multi-Factor Authentication", 
										content = @Content(schema = @Schema(implementation = EnableMfaResponse.class))),
								@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
								@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
						},
						parameters = {})
			),
		@RouterOperation(
				path = "/api/v1/email", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				method = RequestMethod.POST, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "renewActivationToken",
				operation = @Operation(operationId = "renewActivationToken", summary = "Renew email activation token", tags = { "Renew email activation token" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successfully sent Otp code as alternative to Totp", 
									content = @Content(schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
					},
					parameters = {
							@Parameter(in = ParameterIn.QUERY, name = "token")
					})
			),
		@RouterOperation(
				path = "/api/v1/email", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				method = RequestMethod.PUT, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "validateActivationToken",
				operation = @Operation(operationId = "validateActivationToken", summary = "Validate email activation token", tags = { "validation email activation token" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successful sent Otp code as alternative to Totp", 
									content = @Content(schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)))
					},
					parameters = {
							@Parameter(in = ParameterIn.QUERY, name = "token")
					})
			),
	})
	RouterFunction<ServerResponse> totpRoute(@Autowired final TokenManagementHandler authHandler) {

		return RouterFunctions
				.route(RouterHelper.i(POST("/api/v1/totp")).and(accept(APPLICATION_JSON)),
						authHandler::validateTotp)
				.andRoute(RouterHelper.i(PUT("/api/v1/totp")).and(accept(APPLICATION_JSON)),
						authHandler::sendOtpCode)
				
				.andRoute(RouterHelper.i(POST("/api/v1/mfa")).and(accept(APPLICATION_JSON)),
						authHandler::activateMfa)
				.andRoute(RouterHelper.i(DELETE("/api/v1/mfa")).and(accept(APPLICATION_JSON)),
						authHandler::deactivateMfa)
				
				.andRoute(RouterHelper.i(POST("/api/v1/email")).and(accept(APPLICATION_JSON)),
						authHandler::renewActivationToken)
				.andRoute(RouterHelper.i(PUT("/api/v1/email")).and(accept(APPLICATION_JSON)),
						authHandler::validateActivationToken);
	}
	
	
	/**
	 * 
	 * @param indexHtml
	 * @return
	 */
	@Bean
	@RouterOperations({ 
		@RouterOperation(
				path = "/api/v1/password", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE							 
				}, 
				method = RequestMethod.POST, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "signupAdmin",
				operation = @Operation(operationId = "signupAdmin", summary = "register a user with ADMIN role", tags = {	"create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
									@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))))
			),
		@RouterOperation(
				path = "/api/v1/password/token", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				method = RequestMethod.POST, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "signupAdmin",
				operation = @Operation(operationId = "signupAdmin", summary = "register a user with ADMIN role", tags = {	"create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
									@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))))
			),
		@RouterOperation(
				path = "/api/v1/password", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				method = RequestMethod.POST, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "signupAdmin",
				operation = @Operation(operationId = "signupAdmin", summary = "register a user with ADMIN role", tags = {	"create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
									@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))))
			),
		@RouterOperation(
				path = "/api/v1/password", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				method = RequestMethod.POST, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "signupAdmin",
				operation = @Operation(operationId = "signupAdmin", summary = "register a user with ADMIN role", tags = {	"create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
									@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))))
			),
		@RouterOperation(
				path = "/api/v1/username", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				method = RequestMethod.GET, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "recoverUsername",
				operation = @Operation(operationId = "recoverUsername", summary = "send Username as part of profile recovery process", tags = { "create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
									@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ForgotUsernameRequest.class))))
			),
	})
	RouterFunction<ServerResponse> passwordRouter(@Autowired final PasswordManagementHandler passwdHandler) {
		
		return RouterFunctions
				.route(RouterHelper.i(POST("/api/v1/password")).and(accept(APPLICATION_JSON)),
						passwdHandler::initResetPassword)
				.andRoute(RouterHelper.i(POST("/api/v1/password/token")).and(accept(APPLICATION_JSON)),
						passwdHandler::validateResetToken)
				.andRoute(RouterHelper.i(PUT("/api/v1/password")).and(accept(APPLICATION_JSON)),
						passwdHandler::resetPassword)
				.andRoute(RouterHelper.i(PATCH("/api/v1/password")).and(accept(APPLICATION_JSON)),
						passwdHandler::changePassword)
				
				.andRoute(RouterHelper.i(GET("/api/v1/username")).and(accept(APPLICATION_JSON)),  
						passwdHandler::recoverUsername);
	}

	/**
	 * 
	 * @param indexHtml
	 * @return
	 */
	@Bean
	RouterFunction<ServerResponse> indexRouter(@Value("classpath:/public/index.html") final Resource indexHtml) {
		return RouterFunctions.route(GET("/**"),
				request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
	}

}
