/**
 * 
 */
package com.keycloak.admin.client.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.keycloak.admin.client.aop.qualifiers.Loggable;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.GroupService;
import com.keycloak.admin.client.response.model.AppResponse;

import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Realm Group", description = "API for Realm Groups information.")
@SecurityScheme(name = "Bearer Token Authentication", type = SecuritySchemeType.HTTP, scheme = "token")
public class GroupController {

	private final GroupService groupService;
	
	private final ResponseCreator responseCreator;
	
	/**
	 * 
	 * @return
	 */
	@Loggable
	@Operation(summary = "List all realm groups", security = @SecurityRequirement(name = "Bearer token Authentication"), tags = {
			"find", "groups", "realm" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description} Realm groups found", content = {
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class, description = "the realm groups details"))),
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class, description = "the realm groups details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
	})
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Publisher<GroupVO> findAllRealmGroup() {

		return groupService.findAllRealmGroups();
	}

	/**
	 * 
	 * @param groupName
	 * @return
	 */
	@Loggable
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a realm group", description = "API endpoint to create realm group", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"create", "groups", "realm" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}. Group created successfully", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class, description = "the created group details")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class, description = "the created group details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
	})

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<GroupVO> createRealmGroup(@RequestBody @Valid CreateGroupRequest groupRequest) {

		System.out.println("Create Group Request "+ groupRequest);
		return groupService.createRealmGroup(groupRequest);
	}

	/**
	 * 
	 * @return
	 */
	@Loggable
	@Operation(summary = "Find realm group by id", description = "API endpoint to find realm group by id", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"find", "group", "realm", "id" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class, description = "the group found")),
					@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class, description = "the group found")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
	})

	@GetMapping(value = "/group/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<GroupVO> findRealmGroup(
			@Parameter(description = "id of realm group to be searched") @PathVariable("id") @NotBlank String groupId) {

		return groupService.findRealmGroupById(groupId);
	}

	/**
	 * 
	 * @param groupId
	 * @return
	 */
	@Loggable
	@Operation(summary = "Delete realm group by id", description = "API endpoint to delete realm group by id", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"delete", "group", "realm", "id" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = {@Content(mediaType = "application/xml", schema = @Schema(implementation = AppResponse.class)), @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))}) 
	})	
	@DeleteMapping(value = "/group/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Mono<AppResponse> deleteRealmGroup(
			@Parameter(description = "id of realm group to delete") @PathVariable("id") @NotBlank String groupId, ServerWebExchange ex) {

		return groupService.deleteRealmGroup(groupId).map(message -> this.responseCreator.createAppResponse(message, ex));
	}

}
