/**
 * 
 */
package com.keycloak.admin.client.controllers;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.server.ServerRequest;

import io.swagger.v3.oas.annotations.media.Schema;

import com.keycloak.admin.client.common.enums.Role;
import com.keycloak.admin.client.common.utils.ResponseCreator;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.UserRegistrationRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialService;
import com.keycloak.admin.client.response.model.AppResponse;

import io.netty.util.internal.ThreadLocalRandom;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/v1/users")
@Tag(name = "User", description = "API for user information.")
@SecurityScheme(name = "Bearer Token Authentication", type = SecuritySchemeType.HTTP, scheme = "token")
public class UserController {

	private final UserCredentialFinderService userCredentialService;

	private final UserCredentialService userService;

	private final ResponseCreator responseCreator;

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "Get Users by search criteria", description = "API endpoint to search users by specified criteria", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}. Users found", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class, description = "Users details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/search", params = { "page", "size", "email", "first_name", "last_name", "verified_emails" })
	public Flux<UserVO> searchUser(
			@Parameter(description = "User's email to search by") @RequestParam(name = "email", required = false) String email,
			@Parameter(description = "User's first name to search by") @RequestParam(name = "first_name", required = false) String firstName,
			@Parameter(description = "User's last name to search by") @RequestParam(name = "last_name", required = false) String lastName,
			@RequestParam(name = "verified_emails") boolean verifiedEmails,
			@RequestParam(name = "page", defaultValue = "1") @Min(value = 1, message = "{page.no.min}") int page,
			@RequestParam(name = "size", defaultValue = "10") @Max(value = 50, message = "{page.size.max}") int size) {

		PagingModel pagingModel = PagingModel.builder().pgNo(page).pgSize(size).build();

		SearchUserRequest searchRequest = SearchUserRequest.builder().email(email).firstName(firstName)
				.lastName(lastName).emailVerified(verifiedEmails).build();
		// GlobalProgrammaticValidator.validateInput(searchRequest);

		return userCredentialService.search(searchRequest, pagingModel);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	// @CrossOrigin
	@Operation(summary = "Get a User by id", description = "API endpoint to find user by id", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users", "id" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the user found")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping("/{id}")
	public Mono<ResponseEntity<UserVO>> findById(
			@Parameter(description = "id of User to be searched") @PathVariable @NotBlank String id) {

		return userCredentialService.findUserById(id).map(userFound -> ResponseEntity.ok(userFound));
	}

	/**
	 * 
	 * @return
	 */
	@Operation(summary = "Get Users by email", description = "API endpoint to find users by email", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}. Users found", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class, description = "Users details"))) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping(value = "/email/{email}", params = { "page", "size" })
	public Flux<UserVO> findByEmail(
			@Parameter(description = "User email search") @PathVariable("email") @NotBlank String email,
			@RequestParam(name = "page", defaultValue = "1") @Min(value = 1, message = "{page.no.min}") int page,
			@RequestParam(name = "size", defaultValue = "10") @Max(value = 50, message = "{page.size.max}") int size) {

		PagingModel pagingModel = PagingModel.builder().pgNo(page).pgSize(size).build();

		return userCredentialService.findUserByEmail(email, pagingModel);
	}

	/**
	 * 
	 * @param username
	 * @param roleName
	 * @return
	 */
	@Operation(summary = "Assign realm role to User", description = "API endpoint to assign a realm role to an existing User", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"role", "realm", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping("/{id}/role/{roleName}")
	public Mono<AppResponse> assignRole(
			@Parameter(description = "User unique id") @PathVariable("id") @NotBlank String id,
			@Parameter(description = "Realm role name") @PathVariable("role_name") @NotBlank String roleName,
			ServerRequest r) {

		return userService.assignRealmRole(id, roleName).map(msg -> this.responseCreator.createAppResponse(msg, r));

	}

	/**
	 * 
	 * @param username
	 * @param roleName
	 * @return
	 */
	@Operation(summary = "Assign client role to User", description = "API endpoint to assign a client role to an existing User", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"role", "client", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping("/{id}/role/{roleName}/client/{client_id}")
	public Mono<AppResponse> assignClientRole(
			@Parameter(description = "User unique id") @PathVariable("id") @NotBlank String id,
			@Parameter(description = "Client role name") @PathVariable("role_name") @NotBlank String roleName,
			@PathVariable("client_id") String clientId, ServerRequest r) {

		return userService.assignClientRoleToUser(id, roleName, clientId)
				.map(msg -> this.responseCreator.createAppResponse(msg, r));
	}

	/**
	 * 
	 * @param id
	 * @param groupId
	 * @return
	 */
	@Operation(summary = "Assign user to a group", description = "API endpoint to assign user to an existing Group", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"group", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping("/{id}/group/{groupId}")
	public Mono<AppResponse> assignToGroup(
			@Parameter(description = "User unique id") @PathVariable("id") @NotBlank String id,
			@Parameter(description = "Group unique id") @PathVariable("groupId") @NotBlank String groupId,
			ServerRequest r) {

		return userService.assignToGroup(id, groupId).map(msg -> this.responseCreator.createAppResponse(msg, r));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	// @CrossOrigin
	@Operation(summary = "Get Users by username", description = "API endpoint to find users by username", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping("/{username}")
	public Mono<ResponseEntity<UserVO>> findByUsername(
			@Parameter(description = "username of User to be searched") @PathVariable String username) {

		return userCredentialService.findByUsername(username).map(userFound -> ResponseEntity.ok(userFound));

	}

	/**
	 * 
	 * @return
	 */
	// @CrossOrigin
	@Operation(summary = "Get all Users", description = "API endpoint to find all users", security = @SecurityRequirement(name = "Token Authentication"), tags = {
			"users" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class, description = "Success message")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@GetMapping("/")
	public Flux<UserVO> findAllUsers(
			@RequestParam(name = "page", defaultValue = "1") @Min(value = 1, message = "{page.no.min}") int page,
			@RequestParam(name = "size", defaultValue = "10") @Max(value = 50, message = "{page.size.max}") int size) {

		PagingModel pagingModel = PagingModel.builder().pgNo(page).pgSize(size).build();

		return userCredentialService.findAll(pagingModel);
	}

	/**
	 * 
	 * @return
	 */
	// @CrossOrigin
	@Operation(summary = "Create a user", description = "API endpoint to create a user", tags = { "create", "user" })

	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}.", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class, description = "the created user details")) }),
			@ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "401", description = "${api.responseCodes.unauthorized.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "403", description = "${api.responseCodes.forbidden.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "409", description = "${api.responseCodes.conflict.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "500", description = "${api.responseCodes.server.error.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))),
			@ApiResponse(responseCode = "503", description = "${api.responseCodes.server.unavalable.description}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppResponse.class))) })

	@PostMapping("/")
	public Mono<UserVO> createUser(@RequestBody @Valid UserRegistrationRequest regRequest, ServerRequest r) {

		Role role = getRandomRole();
		ServerHttpRequest httpRequest = r.exchange().getRequest();

		return userService.signupUser(regRequest, role, httpRequest);

	}

	private Role getRandomRole() {
		Set<String> setOfRoles = Role.getAllTypes();
		int num = setOfRoles.size();

		List<String> arrayList = List.copyOf(setOfRoles);

		int i = ThreadLocalRandom.current().nextInt(0, num - 1);

		return Role.fromString(arrayList.get(i));
	}

}
