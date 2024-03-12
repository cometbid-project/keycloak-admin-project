/**
 * 
 */
package com.keycloak.admin.client.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.RoleVO;
import com.keycloak.admin.client.oauth.service.it.RoleService;
import com.keycloak.admin.client.response.model.AppResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Validated
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping(value = "/api/v1/roles")
@Tag(name = "User Roles", description = "API for roles(Realm and Client) information.")
@SecurityScheme(name = "Bearer Token Authentication", type = SecuritySchemeType.HTTP, scheme = "token")
public class RoleController {

	private final RoleService roleService;

	private final ResponseCreator responseCreator;

	/**
	 * 
	 * @return
	 */
	@Loggable
	@Operation(summary = "List all realm roles", security = @SecurityRequirement(name = "Bearer token Authentication"), tags = {
			"roles", "realm" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Realm roles found", content = {
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RoleVO.class, description = "the realm roles details"))),
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleVO.class, description = "the realm roles details"))) }),
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

	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Flux<RoleVO> findAllRoles() {

		return roleService.findAllRealmRoles();
	}

	/**
	 * 
	 * @return
	 */
	@Loggable
	@Operation(summary = "List all client roles", security = @SecurityRequirement(name = "Bearer token Authentication"), tags = {
			"roles", "client" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Client roles found", content = {
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RoleVO.class, description = "the client roles details"))),
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleVO.class, description = "the client roles details"))) }),
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

	@GetMapping(value = "/client/{client_id}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public Flux<RoleVO> findAllClientRoles(
			@Parameter(in = ParameterIn.PATH, description = "Client id") @PathVariable("client_id") @NotBlank String clientId) {

		return roleService.findAllClientRoles(clientId);
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a realm role", description = "API endpoint to create realm role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "create" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}. Realm role created successfully", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RoleVO.class, description = "the created role details")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the created role details")) }),
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

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<RoleVO> createRealmRole(@RequestBody @Valid CreateRoleRequest roleRequest) {

		return roleService.createRealmRole(roleRequest);
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@Operation(summary = "Make a realm role composite", description = "API endpoint to add a role to existing realm role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "composite" })

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

	@PostMapping(value = "/{role_name}/realm")
	public Mono<AppResponse> addToRealmRole(@RequestBody @Valid CreateRoleRequest realmRoleToAdd,
			@Parameter(in = ParameterIn.PATH, description = "Realm role to make composite") @PathVariable("role_name") @NotBlank String realmRole,
			ServerWebExchange ex) {

		return roleService.makeRealmRoleComposite(realmRole, realmRoleToAdd)
				.map(msg -> this.responseCreator.createAppResponse(msg, ex));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@Operation(summary = "Make a realm role composite with client role", description = "API endpoint to add a client role to existing realm role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "composite" })

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

	@PostMapping(value = "/{role_name}/realm/client/{client_id}")
	public Mono<AppResponse> addClientRoleToRealmRole(
			@Parameter(in = ParameterIn.PATH, description = "Realm role name to make composite") @PathVariable("role_name") @NotBlank String realmRole,
			@Parameter(in = ParameterIn.PATH, description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			@RequestBody @Valid CreateRoleRequest clientRole, ServerWebExchange ex) {

		return roleService.makeRealmRoleCompositeWithClientRole(realmRole, clientRole, clientId)
				.map(msg -> this.responseCreator.createAppResponse(msg, ex));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@Operation(summary = "Make a client role composite", description = "API endpoint to add a role to an existing client role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "composite" })

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

	@PostMapping(value = "/client/{client_id}/role_name/{role_name}")
	public Mono<AppResponse> addToClientRole(
			@Parameter(in = ParameterIn.PATH, description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			@Parameter(in = ParameterIn.PATH, description = "Client role name to make composite") @PathVariable("role_name") @NotBlank String clientRoleName,
			@RequestBody @Valid CreateRoleRequest clientRoleToAdd, ServerWebExchange ex) {

		return roleService.makeClientRoleComposite(clientRoleToAdd, clientRoleName, clientId)
				.map(msg -> this.responseCreator.createAppResponse(msg, ex));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a client role", description = "API endpoint to create client role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "client", "create" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}. Client role created successfully", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RoleVO.class, description = "the created role details")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the created role details")) }),
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

	@PostMapping(value = "/client/{client_id}")
	public Mono<RoleVO> createClientRole(
			@Parameter(in = ParameterIn.PATH, description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			@RequestBody @Valid CreateRoleRequest roleToCreate, ServerWebExchange ex) {

		return roleService.createClientRole(roleToCreate, clientId);
	}

	/**
	 * 
	 * @return
	 */
	@Loggable
	@Operation(summary = "Find realm role by name", description = "API endpoint to find realm role by name", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RoleVO.class, description = "the realm role found")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the realm role found")) }),
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

	@GetMapping(value = "/{role_name}/realm")
	public Mono<RoleVO> findRealmRole(
			@Parameter(in = ParameterIn.PATH, description = "name of realm role to find") @PathVariable("role_name") @NotBlank String roleName) {

		return roleService.findRealmRoleByName(roleName);
	}

	/**
	 * 
	 * @return
	 */
	@Loggable
	@Operation(summary = "Find client role by name", description = "API endpoint to find client role by name", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "client" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RoleVO.class, description = "the created role details")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the client role found")) }),
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

	@GetMapping(value = "/{role_name}/client/{client_id}")
	public Mono<RoleVO> findClientRole(
			@Parameter(in = ParameterIn.PATH, description = "name of realm role to find") @PathVariable("role_name") @NotBlank String roleName,
			@Parameter(in = ParameterIn.PATH, description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId) {

		return roleService.findClientRoleByName(roleName, clientId);
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@Operation(summary = "Delete realm role by name", description = "API endpoint to delete realm role by name", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"delete", "role", "realm", "name" })
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

	@DeleteMapping(value = "/{role_name}/realm", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> deleteRealmRole(
			@Parameter(in = ParameterIn.PATH, description = "name of realm role to delete") @PathVariable("role_name") @NotBlank String roleName,
			ServerWebExchange ex) {

		return roleService.deleteRealmRole(roleName)
				.map(message -> this.responseCreator.createAppResponse(message, ex));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Loggable
	@Operation(summary = "Delete client role by name", description = "API endpoint to delete client role by name", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"delete", "role", "client", "name" })
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

	@DeleteMapping(value = "/{role_name}/client/{client_id}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> deleteClientRole(
			@Parameter(in = ParameterIn.PATH, description = "Client role name to delete") @PathVariable("role_name") @NotBlank String roleName,
			@Parameter(in = ParameterIn.PATH, description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			ServerWebExchange ex) {

		return roleService.deleteClientRole(clientId, roleName)
				.map(message -> this.responseCreator.createAppResponse(message, ex));
	}
}
