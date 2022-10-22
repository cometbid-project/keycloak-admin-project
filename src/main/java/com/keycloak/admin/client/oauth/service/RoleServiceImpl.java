/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.config.AuthProperties;
import com.keycloak.admin.client.models.CreateRoleRequest;
import com.keycloak.admin.client.models.RoleVO;
import com.keycloak.admin.client.models.mappers.RoleMapper;
import com.keycloak.admin.client.oauth.service.it.RoleService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service
@Validated
@PreAuthorize("hasAnyRole('ADMIN')")
public class RoleServiceImpl implements RoleService {

	private final Keycloak keycloak;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final ApplicationEventPublisher eventPublisher;
	private final AuthProperties authProperties;

	public RoleServiceImpl(Keycloak keycloak, AuthProperties authProperties, ApplicationEventPublisher eventPublisher,
			CustomMessageSourceAccessor i8nMessageAccessor) {
		this.keycloak = keycloak;
		this.i8nMessageAccessor = i8nMessageAccessor;
		this.eventPublisher = eventPublisher;
		this.authProperties = authProperties;
	}

	// @PostConstruct
	private RealmResource realmResource() {
		// Get realm role "tester" (requires view-realm role)
		final String appRealm = authProperties.getAppRealm();

		return keycloak.realm(appRealm);
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 * @return
	 */
	@Override
	public Mono<RoleVO> createRealmRole(@Valid final CreateRoleRequest newRole) {

		// String roleName = StringUtils.prependIfMissing(roleRequest.getRoleName(),
		// ROLE_PREFIX);
		final String roleName = newRole.getRoleName();
		log.info("Rolename {}", roleName);

		Mono<List<RoleVO>> monoList = findAllRealmRoles().filter(role -> role.getName().equalsIgnoreCase(roleName))
				.collectSortedList();

		return monoList.flatMap(list -> {

			if (!list.isEmpty()) {
				raiseResourceAlreadyExistException(new Object[] { roleName });
			}

			RoleRepresentation realmRole = new RoleRepresentation();
			realmRole.setName(roleName.toUpperCase());
			realmRole.setDescription("Realm role: " + roleName);
			realmRole.setClientRole(false);
			realmRole.setComposite(false);

			return createRole(roleName, realmRole);

		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.CREATE_REALM_ROLE_EVENT, StringUtils.EMPTY,
						"Realm role creation was successful", ObjectType.USER_AUTH, ContentType.USER_ROLE, newRole)))
				.doOnError(ex -> log.error("Error occured while creating Realm role", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("role.creation.error"), ex));
	}

	private Mono<RoleVO> createRole(String roleName, RoleRepresentation realmRole) {

		return Mono.fromRunnable(() -> realmResource().roles().create(realmRole))
				.then(this.findRealmRoleByName(roleName));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 * @return
	 */
	@Override
	public Mono<RoleVO> createClientRole(@Valid final CreateRoleRequest roleRequest, @NotBlank final String clientId) {

		// String clientRole = StringUtils.prependIfMissing(roleName, ROLE_PREFIX);
		final String roleName = roleRequest.getRoleName().toUpperCase();

		Mono<List<RoleVO>> monoList = findAllClientRoles(clientId)
				.filter(role -> role.getName().equalsIgnoreCase(roleName)).collectSortedList();

		return monoList.flatMap(list -> {
			log.info("Roles list returned {}", list);

			if (!list.isEmpty()) {
				raiseResourceAlreadyExistException(new Object[] { roleName });
			}

			RoleRepresentation clientRole = new RoleRepresentation();
			clientRole.setName(roleName);
			clientRole.setDescription("Client role " + roleName);
			clientRole.setClientRole(true);
			clientRole.setComposite(false);

			return createClientRole(clientRole, clientId);

		}).doOnSuccess(profile -> this.eventPublisher.publishEvent(new GenericSpringEvent<>(
				ActivityEventTypes.CREATE_CLIENT_ROLE_EVENT, StringUtils.EMPTY, "Client role creation was successful",
				ObjectType.USER_AUTH, ContentType.USER_ROLE, roleRequest)))
				.doOnError(ex -> log.error("Error occured while creating Client role", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("role.creation.error"), ex));

	}

	private Mono<RoleVO> createClientRole(RoleRepresentation clientRole, String clientId) {

		return Mono.fromRunnable(() -> {
			ClientRepresentation clientRep = realmResource().clients().findByClientId(clientId).get(0);

			realmResource().clients().get(clientRep.getId()).roles().create(clientRole);
		})

				.then(this.findClientRoleByName(clientRole.getName(), clientId));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	public Flux<RoleVO> findAllRealmRoles() {

		Mono<List<RoleVO>> monoList = Mono.fromCallable(() -> realmResource().roles().list().stream()
				.map(RoleMapper::toViewObject).collect(Collectors.toList()));

		return monoList.flatMapIterable(list -> list)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_REALM_ROLE_EVENT, StringUtils.EMPTY,
								"Find all realm roles", ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while finding all corresponding Realm roles", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("role.search.error"), ex));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	public Flux<RoleVO> findAllClientRoles(@NotBlank final String clientId) {

		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = realmResource().clients().findByClientId(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		return monoClientRepresentation.flatMap(clientRep -> allClientRoles(clientRep)).flatMapIterable(list -> list)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_CLIENT_ROLE_EVENT, StringUtils.EMPTY,
								"Find all client roles", ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while finding all corresponding Client roles", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("client.role.search.error"),
								ex));
	}

	private Mono<List<RoleVO>> allClientRoles(ClientRepresentation clientRep) {

		return Mono.fromCallable(() -> realmResource().clients().get(clientRep.getId()).roles().list().stream()
				.map(RoleMapper::toViewObject).collect(Collectors.toList()));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	public Mono<RoleVO> findRealmRoleByName(@NotBlank final String roleName) {

		return Mono.fromCallable(() -> realmResource().roles().get(roleName).toRepresentation())
				.map(RoleMapper::toViewObject)
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleName }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_REALM_ROLE_EVENT, StringUtils.EMPTY,
								"Find realm role by name: " + roleName, ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while finding corresponding Realm role by name", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.searchByName.error", new Object[] { roleName }),
						ex));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	public Mono<RoleVO> findClientRoleByName(@NotBlank final String roleName, @NotBlank final String clientId) {

		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = realmResource().clients().findByClientId(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		return monoClientRepresentation.flatMap(clientRep -> getClientRole(roleName, clientRep))
				.map(RoleMapper::toViewObject)
				.switchIfEmpty(raiseResourceNotFoundError("Client.role.notFound", new Object[] { clientId, roleName }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_CLIENT_ROLE_EVENT, StringUtils.EMPTY,
								"Find client role by name: " + roleName, ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while finding corresponding Client role by name", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(i8nMessageAccessor
						.getLocalizedMessage("client.role.searchByName.error", new Object[] { roleName }), ex));
	}

	private Mono<RoleRepresentation> getClientRole(String roleName, ClientRepresentation clientRep) {

		return Mono.fromCallable(
				() -> realmResource().clients().get(clientRep.getId()).roles().get(roleName).toRepresentation());
	}

	/**
	 * 
	 * @param roleToMakeComposite
	 * @param roleToAdd
	 * @return
	 */
	@Override
	public Mono<String> makeRealmRoleComposite(@NotBlank final String roleToMakeComposite,
			@Valid final CreateRoleRequest roleRequest) {

		// String realmRole = StringUtils.prependIfMissing(realmRoleName, ROLE_PREFIX);
		// String roleToAdd = StringUtils.prependIfMissing(realmRoleToAdd, ROLE_PREFIX);

		final String roleToAdd = roleRequest.getRoleName().toUpperCase();
		if (roleToAdd.equalsIgnoreCase(roleToMakeComposite)) {
			raiseBadRequestException("role.conflict", new Object[] { roleToMakeComposite, roleToAdd });
		}

		Mono<RoleRepresentation> monoResult = Mono
				.fromCallable(() -> realmResource().roles().get(roleToMakeComposite).toRepresentation())
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleToMakeComposite }));

		return monoResult.flatMap(role -> {

			Mono<RoleRepresentation> monoToAddResult = Mono
					.fromCallable(() -> realmResource().roles().get(roleToAdd).toRepresentation())
					.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleToAdd }));

			return addToRealmRole(role, monoToAddResult);
		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.CREATE_COMPOSITE_ROLE_EVENT,
						StringUtils.EMPTY, "Realm role " + roleToMakeComposite + " was made composite successfully",
						ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while making Realm role composite", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.composite.error", new Object[] {}), ex));

	}

	private Mono<String> addToRealmRole(RoleRepresentation role, Mono<RoleRepresentation> monoToAddResult) {

		return monoToAddResult.flatMap(roleRepresentation -> {

			List<RoleRepresentation> composites = new LinkedList<>();
			composites.add(roleRepresentation);

			return Mono.fromRunnable(() -> realmResource().rolesById().addComposites(role.getId(), composites))
					.thenReturn(String.format("Role %s made a composite role of '%s'", role.getName(),
							roleRepresentation.getName()));
		});
	}

	/**
	 * 
	 * @param roleToMakeComposite
	 * @param roleToAdd
	 * @return
	 */
	@Override
	public Mono<String> makeClientRoleComposite(@Valid final CreateRoleRequest roleRequest,
			@NotBlank final String roleToMakeComposite, @NotBlank final String clientId) {
		// String clientRole = StringUtils.prependIfMissing(clientRoleName,
		// ROLE_PREFIX);
		// String roleToAdd = StringUtils.prependIfMissing(clientRoleToAdd,
		// ROLE_PREFIX);

		final String roleToAdd = roleRequest.getRoleName().toUpperCase();
		if (roleToAdd.equalsIgnoreCase(roleToMakeComposite)) {
			raiseBadRequestException("role.conflict", new Object[] { roleToMakeComposite, roleToAdd });
		}

		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = realmResource().clients().findByClientId(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		return monoClientRepresentation
				.flatMap(clientRep -> prepareClientRole(roleToMakeComposite, roleToAdd, clientRep))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.CREATE_COMPOSITE_ROLE_EVENT, StringUtils.EMPTY,
								"Client role " + roleToMakeComposite + " was made composite successfully",
								ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while making Client role composite", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.composite.error", new Object[] {}), ex));
	}

	private Mono<String> prepareClientRole(String roleToMakeComposite, String roleToAdd,
			ClientRepresentation clientRep) {

		Mono<RoleRepresentation> monoResult = Mono
				.fromCallable(() -> realmResource().clients().get(clientRep.getId()).roles().get(roleToMakeComposite)
						.toRepresentation())
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleToMakeComposite }));

		return monoResult.flatMap(role -> {

			Mono<RoleRepresentation> monoToAddResult = Mono
					.fromCallable(() -> realmResource().roles().get(roleToAdd).toRepresentation())
					.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleToAdd }));

			return addToClientRole(role, monoToAddResult);
		});
	}

	private Mono<String> addToClientRole(RoleRepresentation role, Mono<RoleRepresentation> monoToAddResult) {

		return monoToAddResult.flatMap(roleRepresentation -> {
			List<RoleRepresentation> composites = new LinkedList<>();
			composites.add(roleRepresentation);

			return Mono.fromRunnable(() -> realmResource().rolesById().addComposites(role.getId(), composites))
					.thenReturn(String.format("Role %s made a composite role of '%s'", role.getName(),
							roleRepresentation.getName()));
		});
	}

	/**
	 * 
	 * @param realmRoleName
	 * @param clientRoleName
	 * @param clientId
	 * @return
	 */
	@Override
	public Mono<String> makeRealmRoleCompositeWithClientRole(@NotBlank final String realmRoleName,
			@Valid final CreateRoleRequest clientRole, @NotBlank final String clientId) {

		// String realmRole = StringUtils.prependIfMissing(realmRoleName, ROLE_PREFIX);
		// String clientRole = StringUtils.prependIfMissing(clientRoleName,
		// ROLE_PREFIX);

		final String roleToAdd = clientRole.getRoleName().toUpperCase();
		if (roleToAdd.equalsIgnoreCase(realmRoleName)) {
			raiseBadRequestException("role.conflict", new Object[] { realmRoleName, roleToAdd });
		}

		final String clientRoleName = clientRole.getRoleName().toUpperCase();
		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = realmResource().clients().findByClientId(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		return monoClientRepresentation.flatMap(clientRep -> {

			Mono<RoleRepresentation> monoResult = Mono
					.fromCallable(() -> realmResource().roles().get(realmRoleName).toRepresentation())
					.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { realmRoleName }));

			return monoResult.flatMap(role -> addToRealmRole(role, clientRoleName, clientRep));

		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.CREATE_COMPOSITE_ROLE_EVENT,
						StringUtils.EMPTY, "Realm role " + realmRoleName + " was made composite successfully",
						ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while making Realm role composite", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.composite.error", new Object[] {}), ex));
	}

	private Mono<String> addToRealmRole(RoleRepresentation role, final String clientRoleName,
			ClientRepresentation clientRep) {

		Mono<RoleRepresentation> monoClientRole = Mono.fromCallable(
				() -> realmResource().clients().get(clientRep.getId()).roles().get(clientRoleName).toRepresentation())
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { clientRoleName }));

		return monoClientRole.flatMap(roleRepresentation -> {
			List<RoleRepresentation> composites = new LinkedList<>();
			composites.add(roleRepresentation);

			return Mono.fromRunnable(() -> realmResource().rolesById().addComposites(role.getId(), composites))
					.thenReturn(String.format("Role %s made a composite role of '%s'", role.getName(),
							roleRepresentation.getName()));
		});
	}
}
