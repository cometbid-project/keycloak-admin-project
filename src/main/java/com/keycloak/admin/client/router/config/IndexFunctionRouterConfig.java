/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.router.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
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

import com.keycloak.admin.client.common.utils.RouterHelper;
import com.keycloak.admin.client.handlers.RegistrationHandler;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.response.model.AppResponse;
import io.swagger.v3.oas.annotations.parameters.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

import lombok.extern.log4j.Log4j2;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

/**
 *
 * @author Gbenga
 */
@Log4j2
@Configuration
public class IndexFunctionRouterConfig {

	/**
	 * 
	 * @param registrationHandler
	 * @return
	 */
	@Bean
	@RouterOperations({ 
		@RouterOperation(
				path = "/api/v1/signup", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.POST, 
				beanClass = RegistrationHandler.class, 
				beanMethod = "signupAdmin",
				operation = @Operation(
						operationId = "signupAdmin", summary = "register a user with ADMIN role", tags = {	"create", "Admin" }, 
					responses = {
							@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
							@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
							@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavailable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
					}, 
					requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))))
			),
	    @RouterOperation(
	    		path = "/api/v1/ping", 
				produces = { 
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE 							 
				}, 
				consumes = {
						MediaType.APPLICATION_XML_VALUE,
						MediaType.APPLICATION_JSON_VALUE	
				},
				method = RequestMethod.GET, 
				beanClass = RegistrationHandler.class, 
				beanMethod = "hello") 
	})
	RouterFunction<ServerResponse> signupRoute(@Autowired final RegistrationHandler registrationHandler) {

		return RouterFunctions
				.route(RouterHelper.i(POST("/api/v1/signup")).and(accept(APPLICATION_JSON, APPLICATION_XML)),
						registrationHandler::signupAdmin)
				.andRoute(RouterHelper.i(GET("/api/v1/ping")).and(accept(APPLICATION_JSON, APPLICATION_XML)), 
						registrationHandler::hello);
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
