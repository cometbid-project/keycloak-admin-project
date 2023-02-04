/**
 * 
 */
package com.keycloak.admin.client.router.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
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
import com.keycloak.admin.client.handlers.PasswordManagementHandler;
import com.keycloak.admin.client.models.ForgotUsernameRequest;
import com.keycloak.admin.client.models.PasswordUpdateRequest;
import com.keycloak.admin.client.models.ResetPasswordFinalRequest;
import com.keycloak.admin.client.models.ResetPasswordRequest;
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
public class PasswordAuthRouterConfig {
	
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
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "initResetPassword",
				operation = @Operation(operationId = "initResetPassword", summary = "starts the reset password process", tags = { "starts", "reset", "password" }, 
					responses = {
							@ApiResponse(responseCode = "202", description = "Request Accepted. Password reset link with token has been sent", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ResetPasswordRequest.class)))
				)
			),
		@RouterOperation(
				path = "/api/v1/password/token-validation", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.GET, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "validateResetToken",
				operation = @Operation(operationId = "validateResetToken", summary = "to validate password reset token", tags = { "validate", "reset", "password" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "Password reset token validation successful", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
					},
					parameters = {
							@Parameter(in = ParameterIn.QUERY, name = "token")
					}
				)
			),
		@RouterOperation(
				path = "/api/v1/password", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.PUT, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "resetPassword",
				operation = @Operation(operationId = "resetPassword", summary = "to reset password. Final stage", tags = { "reset", "password" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "password reset completed successfully", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ResetPasswordFinalRequest.class))))
			),
		@RouterOperation(
				path = "/api/v1/password", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.PATCH, 
				beanClass = PasswordManagementHandler.class, 
				beanMethod = "changePassword",
				operation = @Operation(operationId = "changePassword", summary = "to change password", tags = { "change", "password" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "change password completed successfully", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = PasswordUpdateRequest.class))))
			),
		@RouterOperation(
				path = "/api/v1/username", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.GET, 
				beanClass = PasswordManagementHandler.class,
				beanMethod = "recoverUsername",
				operation = @Operation(operationId = "recoverUsername", summary = "Send username as part of Credential recovery process", tags = { "send", "username" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "username recovered and sent successfully", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", 
							 		content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}",
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ForgotUsernameRequest.class))))
			),
	})
	RouterFunction<ServerResponse> passwordRouter(@Autowired final PasswordManagementHandler passwdHandler) {
		
		return RouterFunctions
				.route(RouterHelper.i(POST("/api/v1/password")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						passwdHandler::initResetPassword)
				.andRoute(RouterHelper.i(GET("/api/v1/password/token-validation")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						passwdHandler::validateResetToken)
				.andRoute(RouterHelper.i(PUT("/api/v1/password")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						passwdHandler::resetPassword)
				.andRoute(RouterHelper.i(PATCH("/api/v1/password")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						passwdHandler::changePassword)				
				.andRoute(RouterHelper.i(POST("/api/v1/username")).and(accept(APPLICATION_JSON, APPLICATION_XML)),  
						passwdHandler::recoverUsername);
	}
}
