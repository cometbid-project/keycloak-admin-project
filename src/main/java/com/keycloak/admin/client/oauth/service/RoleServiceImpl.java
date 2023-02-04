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
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class RoleServiceImpl implements RoleService {

	public final static String ROLE_PREFIX = "ROLE_";

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
	public Mono<RoleVO> createRealmRole(@Valid final CreateRoleRequest roleRequest) {

		final String upperRoleName = roleRequest.getRoleName().toUpperCase();
		final String realmRoleName = StringUtils.prependIfMissing(upperRoleName, ROLE_PREFIX);

		final String roleDesc = roleRequest.getDescription();
		log.info("Rolename {}", realmRoleName);

		Mono<List<RoleVO>> monoList = findAllRealmRoles().filter(role -> role.getName().equalsIgnoreCase(realmRoleName))
				.collectSortedList();

		return monoList.flatMap(list -> {

			if (!list.isEmpty()) {
				raiseResourceAlreadyExistException(new Object[] { realmRoleName });
			}

			RoleRepresentation realmRole = getRoleRepresentation(realmRoleName, roleDesc);

			return createRole(realmRoleName, realmRole);

		}).doOnSuccess(profile -> this.eventPublisher.publishEvent(new GenericSpringEvent<>(
				ActivityEventTypes.CREATE_REALM_ROLE_EVENT, StringUtils.EMPTY, "Realm role creation was successful",
				ObjectType.USER_AUTH, ContentType.USER_ROLE, roleRequest)))
				.doOnError(ex -> log.error("Error occured while creating Realm role", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("role.creation.error"), ex));
	}

	RoleRepresentation getRoleRepresentation(String realmRoleName, String roleDesc) {
		RoleRepresentation realmRole = new RoleRepresentation();
		realmRole.setName(realmRoleName);
		realmRole.setDescription(StringUtils.isBlank(roleDesc) ? "Realm role: " + realmRoleName : roleDesc);
		realmRole.setClientRole(false);
		realmRole.setComposite(false);

		return realmRole;
	}

	private Mono<RoleVO> createRole(String roleName, RoleRepresentation realmRole) {

		return Mono.fromRunnable(() -> getRolesResource().create(realmRole)).then(this.findRealmRoleByName(roleName));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 * @return
	 */
	@Override
	public Mono<RoleVO> createClientRole(@Valid final CreateRoleRequest roleRequest, @NotBlank final String clientId) {

		final String upperRoleName = roleRequest.getRoleName().toUpperCase();
		final String clientRoleName = StringUtils.prependIfMissing(upperRoleName, ROLE_PREFIX);

		final String roleDesc = roleRequest.getDescription();

		Mono<List<RoleVO>> monoList = findAllClientRoles(clientId)
				.filter(role -> role.getName().equalsIgnoreCase(clientRoleName)).collectSortedList();

		return monoList.flatMap(list -> {
			log.info("Roles list returned {}", list);

			if (!list.isEmpty()) {
				raiseResourceAlreadyExistException(new Object[] { clientRoleName });
			}

			RoleRepresentation clientRole = new RoleRepresentation();
			clientRole.setName(clientRoleName);
			clientRole.setDescription(StringUtils.isBlank(roleDesc) ? "Client role " + clientRoleName : roleDesc);
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
			ClientRepresentation clientRep = getClientById(clientId).get(0);

			getClientResource(clientRep.getId()).roles().create(clientRole);
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

		Mono<List<RoleVO>> monoList = Mono.fromCallable(
				() -> getRolesResource().list().stream().map(RoleMapper::toViewObject).collect(Collectors.toList()));

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

			List<ClientRepresentation> clientsList = getClientById(clientId);
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

		return Mono.fromCallable(() -> getClientResource(clientRep.getId()).roles().list().stream()
				.map(RoleMapper::toViewObject).collect(Collectors.toList()));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	public Mono<RoleVO> findRealmRoleByName(@NotBlank final String roleName) {

		final String upperRoleName = roleName.toUpperCase();
		final String clientRoleName = StringUtils.prependIfMissing(upperRoleName, ROLE_PREFIX);

		return Mono.fromCallable(() -> getRolesResource().get(clientRoleName).toRepresentation())
				.map(RoleMapper::toViewObject)
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { clientRoleName }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(new GenericSpringEvent<>(
						ActivityEventTypes.SEARCH_REALM_ROLE_EVENT, StringUtils.EMPTY,
						"Find realm role by name: " + clientRoleName, ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while finding corresponding Realm role by name", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(i8nMessageAccessor
						.getLocalizedMessage("role.searchByName.error", new Object[] { clientRoleName }), ex));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	public Mono<RoleVO> findClientRoleByName(@NotBlank final String roleName, @NotBlank final String clientId) {

		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = getClientById(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		final String upperRoleName = roleName.toUpperCase();
		final String clientRoleName = StringUtils.prependIfMissing(upperRoleName, ROLE_PREFIX);

		return monoClientRepresentation.flatMap(clientRep -> getClientRole(clientRoleName, clientRep))
				.map(RoleMapper::toViewObject)
				.switchIfEmpty(
						raiseResourceNotFoundError("Client.role.notFound", new Object[] { clientId, clientRoleName }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(new GenericSpringEvent<>(
						ActivityEventTypes.SEARCH_CLIENT_ROLE_EVENT, StringUtils.EMPTY,
						"Find client role by name: " + clientRoleName, ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while finding corresponding Client role by name", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(i8nMessageAccessor
						.getLocalizedMessage("client.role.searchByName.error", new Object[] { clientRoleName }), ex));
	}

	private Mono<RoleRepresentation> getClientRole(String roleName, ClientRepresentation clientRep) {

		return Mono.fromCallable(() -> getClientResource(clientRep.getId()).roles().get(roleName).toRepresentation());
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

		final String roleToAdd = roleRequest.getRoleName().toUpperCase();
		final String realmRoleToMakeComposite = roleToMakeComposite.toUpperCase();

		final String realmRoleName = StringUtils.prependIfMissing(roleToAdd, ROLE_PREFIX);
		final String localRealmRoleName = StringUtils.prependIfMissing(realmRoleToMakeComposite, ROLE_PREFIX);

		// final String roleToAdd = roleRequest.getRoleName().toUpperCase();
		if (realmRoleName.equalsIgnoreCase(localRealmRoleName)) {
			raiseBadRequestException("role.conflict", new Object[] { localRealmRoleName, realmRoleName });
		}

		Mono<RoleRepresentation> monoResult = Mono
				.fromCallable(() -> getRolesResource().get(localRealmRoleName).toRepresentation())
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { localRealmRoleName }));

		return monoResult.flatMap(role -> {

			Mono<RoleRepresentation> monoToAddResult = Mono
					.fromCallable(() -> getRolesResource().get(realmRoleName).toRepresentation())
					.switchIfEmpty(altRoleRepresentation(realmRoleName, roleRequest.getDescription()));

			return addToRealmRole(role, monoToAddResult);
		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.CREATE_COMPOSITE_ROLE_EVENT,
						StringUtils.EMPTY, "Realm role " + localRealmRoleName + " was made composite successfully",
						ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while making Realm role composite", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.composite.error", new Object[] {}), ex));

	}

	private Mono<RoleRepresentation> altRoleRepresentation(String realmRoleName, String description) {

		return Mono.fromRunnable(() -> getRolesResource().create(getRoleRepresentation(realmRoleName, description)))
				.thenReturn(getRolesResource().get(realmRoleName).toRepresentation());
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

		final String roleToAdd = roleRequest.getRoleName().toUpperCase();
		final String clientRoleToMakeComposite = roleToMakeComposite.toUpperCase();

		final String clientRoleName = StringUtils.prependIfMissing(roleToAdd, ROLE_PREFIX);
		final String localClientRoleName = StringUtils.prependIfMissing(clientRoleToMakeComposite, ROLE_PREFIX);

		if (clientRoleName.equalsIgnoreCase(localClientRoleName)) {
			raiseBadRequestException("role.conflict", new Object[] { localClientRoleName, clientRoleName });
		}

		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = getClientById(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		return monoClientRepresentation
				.flatMap(clientRep -> prepareClientRole(localClientRoleName, clientRoleName,
						roleRequest.getDescription(), clientRep.getClientId()))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.CREATE_COMPOSITE_ROLE_EVENT, StringUtils.EMPTY,
								"Client role " + localClientRoleName + " was made composite successfully",
								ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while making Client role composite", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.composite.error", new Object[] {}), ex));
	}

	private Mono<String> prepareClientRole(String roleToMakeComposite, String clientRoleName, String description,
			String clientId) {

		Mono<RoleRepresentation> monoResult = Mono
				.fromCallable(() -> getClientResource(clientId).roles().get(roleToMakeComposite).toRepresentation())
				.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { roleToMakeComposite }));

		return monoResult.flatMap(role -> {

			Mono<RoleRepresentation> monoToAddResult = Mono
					.fromCallable(() -> getRolesResource().get(clientRoleName).toRepresentation())
					.switchIfEmpty(createClientRole(clientId, clientRoleName, description));

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

		final String roleToAdd = clientRole.getRoleName().toUpperCase();
		final String realmRoleToMakeComposite = realmRoleName.toUpperCase();

		final String clientRoleName = StringUtils.prependIfMissing(roleToAdd, ROLE_PREFIX);
		final String localRealmRoleName = StringUtils.prependIfMissing(realmRoleToMakeComposite, ROLE_PREFIX);

		if (clientRoleName.equalsIgnoreCase(localRealmRoleName)) {
			raiseBadRequestException("role.conflict", new Object[] { localRealmRoleName, clientRoleName });
		}

		// final String clientRoleName = clientRole.getRoleName().toUpperCase();
		Mono<ClientRepresentation> monoClientRepresentation = Mono.fromCallable(() -> {

			List<ClientRepresentation> clientsList = getClientById(clientId);
			if (clientsList.isEmpty()) {
				raiseResourceNotFoundError("client.notFound", new Object[] { clientId });
			}
			return clientsList.get(0);
		});

		return monoClientRepresentation.flatMap(clientRep -> {
			Mono<RoleRepresentation> monoResult = Mono
					.fromCallable(() -> getRolesResource().get(localRealmRoleName).toRepresentation())
					.switchIfEmpty(raiseResourceNotFoundError("role.notFound", new Object[] { localRealmRoleName }));

			return monoResult.flatMap(
					role -> addToRealmRole(role, clientRoleName, clientRole.getDescription(), clientRep.getClientId()));

		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.CREATE_COMPOSITE_ROLE_EVENT,
						StringUtils.EMPTY, "Realm role " + localRealmRoleName + " was made composite successfully",
						ObjectType.USER_AUTH, ContentType.USER_ROLE)))
				.doOnError(ex -> log.error("Error occured while making Realm role composite", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.composite.error", new Object[] {}), ex));
	}

	private Mono<String> addToRealmRole(RoleRepresentation role, final String clientRoleName, String description,
			String clientId) {

		Mono<RoleRepresentation> monoClientRole = Mono
				.fromCallable(() -> getClientResource(clientId).roles().get(clientRoleName).toRepresentation())
				.switchIfEmpty(createClientRole(clientId, clientRoleName, description));

		return monoClientRole.flatMap(roleRepresentation -> {
			List<RoleRepresentation> composites = new LinkedList<>();
			composites.add(roleRepresentation);

			return Mono.fromRunnable(() -> realmResource().rolesById().addComposites(role.getId(), composites))
					.thenReturn(String.format("Role %s made a composite role of '%s'", role.getName(),
							roleRepresentation.getName()));
		});
	}

	private Mono<RoleRepresentation> createClientRole(String clientId, String clientRoleName, String description) {

		return Mono.fromRunnable(() -> {
			ClientRepresentation clientRep = getClientById(clientId).get(0);

			RoleRepresentation roleRep = getRoleRepresentation(clientRoleName, description);

			getClientResource(clientRep.getId()).roles().create(roleRep);

		}).thenReturn(getRolesResource().get(clientRoleName).toRepresentation());
	}

	/**
	 * 
	 */
	@Override
	public Mono<String> deleteRealmRole(String realmRoleName) {

		return Mono.fromRunnable(() -> this.getRolesResource().deleteRole(realmRoleName))
				.then(Mono.just(i8nMessageAccessor.getLocalizedMessage("role.delete.success")))
				.doOnError(ex -> log.error("Error occured while deleting corresponding Realm role by name", ex))
				.onErrorResume(ERROR_Predicate, (ex) -> raiseRuntimeError(
						i8nMessageAccessor.getLocalizedMessage("role.delete.error", new Object[] { realmRoleName }),
						ex));
	}

	/**
	 * 
	 */
	@Override
	public Mono<String> deleteClientRole(String clientId, String roleName) {

		return Mono.fromRunnable(() -> getClientResource(clientId).roles().deleteRole(roleName))
				.then(Mono.just(i8nMessageAccessor.getLocalizedMessage("role.delete.success")))
				.doOnError(ex -> log.error("Error occured while deleting corresponding Client role by name", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(
								i8nMessageAccessor.getLocalizedMessage("role.delete.error", new Object[] { roleName }),
								ex));
	}

	private ClientResource getClientResource(String clientId) {
		return this.realmResource().clients().get(clientId);
	}

	private RolesResource getRolesResource() {
		return this.realmResource().roles();
	}

	private List<ClientRepresentation> getClientById(String clientId) {
		return realmResource().clients().findByClientId(clientId);
	}
}
