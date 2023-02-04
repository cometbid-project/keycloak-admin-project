/**
 * 
 */
package com.keycloak.admin.client.router.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
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
import com.keycloak.admin.client.handlers.TokenManagementHandler;
import com.keycloak.admin.client.models.AuthenticationResponse;
import com.keycloak.admin.client.models.EnableMfaResponse;
import com.keycloak.admin.client.models.SendOtpRequest;
import com.keycloak.admin.client.models.TotpRequest;
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
public class MfaAuthRouterConfig {

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
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "validateTotp",
				operation = @Operation(operationId = "validateTotp", summary = "Validate Totp code in Multi-Factor Authentication", tags = { "totp code" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successful totp validation operation with response token", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AuthenticationResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
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
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.PUT, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "sendOtpCode",
				operation = @Operation(operationId = "sendOtpCode", summary = "Send Otp code in Multi-Factor Authentication", tags = { "Send Otp code" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successfully sent Otp code as alternative to Totp", 
									content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
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
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "activateMfa",
				operation = @Operation(operationId = "activateMfa", summary = "Activate Multi-Factor Authentication", tags = { "activate totp code" },
						responses = {
								@ApiResponse(responseCode = "200", description = "successful activation of Multi-Factor Authentication", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = EnableMfaResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = EnableMfaResponse.class))}),
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
				path = "/api/v1/mfa", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.DELETE, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "deactivateMfa",
				operation = @Operation(operationId = "deactivateMfa", summary = "De-Activate Multi-Factor Authentication", tags = { "deactivate totp code" },
						responses = {
								@ApiResponse(responseCode = "200", description = "successful de-activation of Multi-Factor Authentication", 
										content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = EnableMfaResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = EnableMfaResponse.class))}),
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
				path = "/api/v1/email", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "renewActivationToken",
				operation = @Operation(operationId = "renewActivationToken", summary = "Renew email activation token", tags = { "Renew email activation token" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successfully sent Otp code as alternative to Totp", 
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
							@Parameter(in = ParameterIn.QUERY, name = "token")
					})
			),
		@RouterOperation(
				path = "/api/v1/email", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 						 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.PUT, 
				beanClass = TokenManagementHandler.class, 
				beanMethod = "validateActivationToken",
				operation = @Operation(operationId = "validateActivationToken", summary = "Validate email activation token", tags = { "validation email activation token" }, 
					responses = {
							@ApiResponse(responseCode = "200", description = "successful sent Otp code as alternative to Totp", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))})
					},
					parameters = {
							@Parameter(in = ParameterIn.QUERY, name = "token")
					})
			),
	})
	RouterFunction<ServerResponse> totpRoute(@Autowired final TokenManagementHandler authHandler) {

		return RouterFunctions
				.route(RouterHelper.i(POST("/api/v1/totp")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						authHandler::validateTotp)
				.andRoute(RouterHelper.i(PUT("/api/v1/totp")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						authHandler::sendOtpCode)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/mfa")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						authHandler::activateMfa)
				.andRoute(RouterHelper.i(DELETE("/api/v1/mfa")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						authHandler::deactivateMfa)
				
				.andRoute(RouterHelper.i(PUT("/api/v1/email/activation")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						authHandler::renewActivationToken)
				.andRoute(RouterHelper.i(GET("/api/v1/email/activation")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						authHandler::validateActivationToken);
	}
	
}
