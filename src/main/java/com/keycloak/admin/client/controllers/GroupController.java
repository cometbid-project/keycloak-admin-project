/**
 * 
 */
package com.keycloak.admin.client.controllers;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.oauth.service.GroupServiceImpl;
import com.keycloak.admin.client.oauth.service.it.GroupService;
import com.keycloak.admin.client.response.model.AppResponse;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/v1/groups")
@Tag(name = "Realm Group", description = "API for Realm Groups information.")
@SecurityScheme(name = "Bearer Token Authentication", type = SecuritySchemeType.HTTP, scheme = "token")
public class GroupController {

	private final GroupService groupService;

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "List all realm groups", security = @SecurityRequirement(name = "Bearer token Authentication"), tags = {
			"find", "groups", "realm" })

	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Realm groups found", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class, description = "the realm groups details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/")
	public Flux<GroupVO> findAllRealmGroup() {

		return groupService.findAllRealmGroups();
	}

	/**
	 * 
	 * @param groupName
	 * @return
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a realm group", description = "API endpoint to create realm group", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"create", "groups", "realm" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}. Group created successfully", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class, description = "the created group details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping(value = "/")
	public Mono<GroupVO> createRealmGroup(@RequestBody @Valid CreateGroupRequest groupRequest) {

		return groupService.createRealmGroup(groupRequest);
	}

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "Find realm group by id", description = "API endpoint to find realm group by id", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"find", "group", "realm", "id" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/{id}")
	public Mono<GroupVO> findRealmGroup(
			@Parameter(description = "id of realm group to be searched") @PathVariable("id") @NotBlank String groupId) {

		return groupService.findRealmGroupById(groupId);
	}

}
