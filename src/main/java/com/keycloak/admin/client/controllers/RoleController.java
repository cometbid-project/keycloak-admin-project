/**
 * 
 */
package com.keycloak.admin.client.controllers;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.models.RoleVO;
import com.keycloak.admin.client.oauth.service.RoleServiceImpl;
import com.keycloak.admin.client.oauth.service.it.RoleService;
import com.keycloak.admin.client.response.model.AppResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
@RequestMapping("/v1/roles")
@Tag(name = "User Roles", description = "API for roles(Realm and Client) information.")
@SecurityScheme(name = "Bearer Token Authentication", type = SecuritySchemeType.HTTP, scheme = "token")
public class RoleController {

	private final RoleService roleService;

	private final ResponseCreator responseCreator;

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "List all realm roles", security = @SecurityRequirement(name = "Bearer token Authentication"), tags = {
			"roles", "realm" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Realm roles found", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleVO.class, description = "the realm roles details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/")
	public Flux<RoleVO> findAllRoles() {

		return roleService.findAllRealmRoles();
	}

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "List all client roles", security = @SecurityRequirement(name = "Bearer token Authentication"), tags = {
			"roles", "client" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Client roles found", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RoleVO.class, description = "the client roles details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/client/{client_id}")
	public Flux<RoleVO> findAllClientRoles(@PathVariable("client_id") @NotBlank String clientId) {

		return roleService.findAllClientRoles(clientId);
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a realm role", description = "API endpoint to create realm role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "create" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}. Realm role created successfully", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the created role details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping(value = "/")
	public Mono<RoleVO> createRealmRole(@RequestBody @Valid CreateRoleRequest roleRequest) {

		return roleService.createRealmRole(roleRequest);
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Operation(summary = "Make a realm role composite", description = "API endpoint to add a role to existing realm role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "composite" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping(value = "/{role_name}/realm")
	public Mono<AppResponse> addToRealmRole(@RequestBody @Valid CreateRoleRequest realmRoleToAdd,
			@Parameter(description = "Realm role to make composite") @PathVariable("role_name") @NotBlank String realmRole,
			ServerRequest r) {

		return roleService.makeRealmRoleComposite(realmRole, realmRoleToAdd)
				.map(msg -> this.responseCreator.createAppResponse(msg, r));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Operation(summary = "Make a realm role composite with client role", description = "API endpoint to add a client role to existing realm role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "composite" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping(value = "/{role_name}/realm/client/{client_id}")
	public Mono<AppResponse> addClientRoleToRealmRole(
			@Parameter(description = "Realm role name to make composite") @PathVariable("role_name") @NotBlank String realmRole,
			@Parameter(description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			@RequestBody @Valid CreateRoleRequest clientRole, ServerRequest r) {

		return roleService.makeRealmRoleCompositeWithClientRole(realmRole, clientRole, clientId)
				.map(msg -> this.responseCreator.createAppResponse(msg, r));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@Operation(summary = "Make a client role composite", description = "API endpoint to add a role to an existing client role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm", "composite" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping(value = "/client/{client_id}/role_name/{role_name}")
	public Mono<AppResponse> addToClientRole(
			@Parameter(description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			@Parameter(description = "Client role name to make composite") @NotBlank String clientRoleName,
			@RequestBody @Valid CreateRoleRequest clientRoleToAdd, ServerRequest r) {

		return roleService.makeClientRoleComposite(clientRoleToAdd, clientRoleName, clientId)
				.map(msg -> this.responseCreator.createAppResponse(msg, r));
	}

	/**
	 * 
	 * @param roleName
	 * @return
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a client role", description = "API endpoint to create client role", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "client", "create" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}. Client role created successfully", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the created role details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })
	
	@PostMapping(value = "/client/{client_id}")
	public Mono<RoleVO> createClientRole(
			@Parameter(description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId,
			@RequestBody @Valid CreateRoleRequest roleToCreate, ServerRequest r) {

		return roleService.createClientRole(roleToCreate, clientId);
	}

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "Find realm role by name", description = "API endpoint to find realm role by name", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "realm" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the realm role found")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/role_name/realm", params = {"role_name"})
	public Mono<RoleVO> findRealmRole(@RequestParam("role_name") @NotBlank String roleName) {

		return roleService.findRealmRoleByName(roleName);
	}

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "Find client role by name", description = "API endpoint to find client role by name", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"roles", "client" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RoleVO.class, description = "the client role found")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/role_name/client/{client_id}", params = {"role_name"})
	public Mono<RoleVO> findClientRole(
			@RequestParam("role_name") @NotBlank String roleName,
			@Parameter(description = "Client id of client") @PathVariable("client_id") @NotBlank String clientId) {

		return roleService.findClientRoleByName(roleName, clientId);
	}
}
