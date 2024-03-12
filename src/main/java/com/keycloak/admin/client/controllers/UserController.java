/**
 * 
 */
package com.keycloak.admin.client.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.swagger.v3.oas.annotations.media.Schema;

import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import com.keycloak.admin.client.response.model.AppResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users")
//@PreAuthorize("isAnonymous() or isAuthenticated()")
@Tag(name = "User", description = "API for user information.")
@SecurityScheme(name = "Bearer Token Authentication", type = SecuritySchemeType.HTTP, scheme = "token")
public class UserController {

	private final UserCredentialFinderService userFinderService;
	private final UserCredentialService userService;
	private final ResponseCreator responseCreator;

	/**
	 * 
	 * @return
	 */
	@Loggable
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Get Users by search criteria", description = "API endpoint to search users by specified criteria", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}. Users found", content = {
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class, description = "Users details"))),
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class, description = "Users details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@PostMapping(produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, value = "/search", params = {})
	public Flux<UserVO> searchUser(@RequestBody @Valid SearchUserRequest searchCriteria,			
			@Parameter(in = ParameterIn.QUERY, description = "for pagination") @RequestParam(name = "page", defaultValue = "1", required = false) @Min(value = 1, message = "{pagination.min}") int page,
			@Parameter(in = ParameterIn.QUERY, description = "for pagination size") @RequestParam(name = "size", defaultValue = "10", required = false) @Min(value = 1, message = "{pagination.size.min}") @Max(value = 50, message = "{pagination.size.max}") int limit) {

		PagingModel pagingModel = PagingModel.builder().pageNo(page).pageSize(limit).build();

		// SearchUserRequest searchRequest =
		// SearchUserRequest.builder().email(email).firstName(firstName)
		// .lastName(lastName).emailVerified(verifiedEmails).build();

		return userFinderService.search(searchCriteria, pagingModel);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	// @CrossOrigin
	@Loggable
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get a User by id", description = "API endpoint to find user by id", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users", "id" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class, description = "the user found")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the user found")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@GetMapping(value = "/id/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<UserVO> findById(
			@Parameter(in = ParameterIn.PATH, description = "id of User to be searched") @PathVariable("id") @NotBlank String id) {

		return userFinderService.findUserById(id);
	}

	/**
	 * 
	 * @return
	 */
	@Loggable
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Get Users by email", description = "API endpoint to find users by email", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}. Users found", content = {
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class, description = "Users details"))),
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class, description = "Users details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@GetMapping(value = "/email/{email}", params = {}, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Flux<UserVO> findByEmail(
			@Parameter(in = ParameterIn.PATH, description = "User email search") @PathVariable("email") @NotBlank String email,
			@Parameter(in = ParameterIn.QUERY, description = "for pagination") @RequestParam(name = "page", defaultValue = "1", required = false) @Min(value = 1, message = "{pagination.min}") int page,
			@Parameter(in = ParameterIn.QUERY, description = "for pagination size") @RequestParam(name = "size", defaultValue = "10", required = false) @Min(value = 1, message = "{pagination.size.min}") @Max(value = 50, message = "{pagination.size.max}") int limit) {

		PagingModel pagingModel = PagingModel.builder().pageNo(page).pageSize(limit).build();

		return userFinderService.findUserByEmail(email, pagingModel);
	}

	/**
	 * 
	 * @param username
	 * @param roleName
	 * @return
	 */
	@Loggable
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Assign realm role to User", description = "API endpoint to assign a realm role to an existing User", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"role", "realm", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class, description = "Success message")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@PutMapping(value = "/{id}/role/{role_name}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> assignRole(
			@Parameter(in = ParameterIn.PATH, description = "User unique id") @PathVariable("id") @NotBlank String id,
			@Parameter(in = ParameterIn.PATH, description = "Realm role name") @PathVariable("role_name") @NotBlank String roleName,
			ServerWebExchange ex) {

		return userService.assignRealmRole(id, roleName).map(msg -> this.responseCreator.createAppResponse(msg, ex));
	}

	/**
	 * 
	 * @param username
	 * @param roleName
	 * @return
	 */
	@Loggable
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Assign client role to User", description = "API endpoint to assign a client role to an existing User", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"role", "client", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class, description = "Success message")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@PutMapping(value = "/{id}/role/{role_name}/client/{client_id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> assignClientRole(
			@Parameter(in = ParameterIn.PATH, description = "User unique id") @PathVariable("id") @NotBlank String id,
			@Parameter(in = ParameterIn.PATH, description = "Client role name") @PathVariable("role_name") @NotBlank String roleName,
			@Parameter(in = ParameterIn.PATH, description = "Client id") @PathVariable("client_id") String clientId,
			ServerWebExchange ex) {

		return userService.assignClientRoleToUser(id, roleName, clientId)
				.map(msg -> this.responseCreator.createAppResponse(msg, ex));
	}

	/**
	 * 
	 * @param id
	 * @param groupId
	 * @return
	 */
	@Loggable
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Assign user to a group", description = "API endpoint to assign user to an existing Group", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"group", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class, description = "Success message")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@PutMapping(value = "/{id}/group/{group_id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> assignToGroup(
			@Parameter(in = ParameterIn.PATH, description = "User unique id") @PathVariable("id") @NotBlank String id,
			@Parameter(in = ParameterIn.PATH, description = "Group unique id") @PathVariable("group_id") @NotBlank String groupId,
			ServerWebExchange ex) {

		return userService.assignToGroup(id, groupId).map(msg -> this.responseCreator.createAppResponse(msg, ex));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	// @CrossOrigin
	@Loggable
	@Operation(summary = "Get Users by username", description = "API endpoint to find users by username", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class, description = "Success message")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@GetMapping(value = "/username/{username}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<UserVO> findByUsername(
			@Parameter(in = ParameterIn.PATH, description = "username of User to be searched") @PathVariable String username) {

		return userFinderService.findByUsername(username);
	}

	/**
	 * 
	 * @return
	 */
	// @CrossOrigin
	@Loggable
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Get all Users", description = "API endpoint to find all users", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class, description = "Success message")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Flux<UserVO> findAllUsers(
			@Parameter(in = ParameterIn.QUERY, description = "for pagination") @RequestParam(name = "page", defaultValue = "1", required = false) @Min(value = 1, message = "{pagination.min}") int page,
			@Parameter(in = ParameterIn.QUERY, description = "for pagination size") @RequestParam(name = "size", defaultValue = "10", required = false) @Min(value = 1, message = "{pagination.size.min}") @Max(value = 50, message = "{pagination.size.max}") int limit) {

		PagingModel pagingModel = PagingModel.builder().pageNo(page).pageSize(limit).build();

		return userFinderService.findAll(pagingModel);
	}

	/**
	 * 
	 * @return
	 */
	@Loggable
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a user", description = "API endpoint to create a user", tags = { "create", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class, description = "the created user details")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<UserVO> createUser(@RequestBody @Valid UserRegistrationRequest regRequest, ServerWebExchange ex) {

		return userService.signupUser(regRequest, Role.ROLE_USER, ex.getRequest());

	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Delete user by id", description = "API endpoint to delete user by id", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"delete", "user", "id" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)),
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class)) }) })

	@DeleteMapping(value = "/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> deleteUserProfile(
			@Parameter(in = ParameterIn.PATH, description = "id of the user to delete") @PathVariable("id") @NotBlank String userId,
			ServerWebExchange ex) {

		return userService.deleteUser(userId).map(message -> this.responseCreator.createAppResponse(message, ex));
	}

}
