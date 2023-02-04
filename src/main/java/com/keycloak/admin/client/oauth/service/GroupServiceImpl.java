/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
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
import com.keycloak.admin.client.models.CreateGroupRequest;
import com.keycloak.admin.client.models.GroupVO;
import com.keycloak.admin.client.models.mappers.GroupMapper;
import com.keycloak.admin.client.oauth.service.it.GroupService;

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
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class GroupServiceImpl implements GroupService {

	private final Keycloak keycloak;
	private final CustomMessageSourceAccessor i8nMessageAccessor;
	private final ApplicationEventPublisher eventPublisher;
	private final AuthProperties authProperties;

	public GroupServiceImpl(Keycloak keycloak, AuthProperties authProperties, ApplicationEventPublisher eventPublisher,
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
	public Mono<GroupVO> createRealmGroup(@Valid final CreateGroupRequest newGroup) {
		final String groupName = newGroup.getGroupName();
		final String groupDesc = newGroup.getDescription();

		Mono<List<String>> monoList = findAllRealmGroupNames().filter(group -> group.equalsIgnoreCase(groupName))
				.collectSortedList();

		return monoList.flatMap(list -> {
			if (!list.isEmpty()) {
				raiseResourceAlreadyExistException(new Object[] { groupName });
			}

			GroupRepresentation realmGroup = new GroupRepresentation();
			realmGroup.setName(groupName.toUpperCase());
			realmGroup.singleAttribute("description", groupDesc == null ? "Group: " + groupName : groupDesc);
			realmGroup.setRealmRoles(Collections.emptyList());
			realmGroup.setClientRoles(Collections.emptyMap());
			realmGroup.setSubGroups(Collections.emptyList());
			realmGroup.setAccess(Collections.emptyMap());
			realmGroup.setPath("");

			log.info("Realm group {}", realmGroup);

			return Mono.fromCallable(() -> this.realmResource().groups().add(realmGroup)).map(this::getCreatedGroupId)
					.flatMap(this::findRealmGroupById);

		}).doOnSuccess(profile -> this.eventPublisher
				.publishEvent(new GenericSpringEvent<>(ActivityEventTypes.CREATE_REALM_GROUP_EVENT, StringUtils.EMPTY,
						"Realm group creation was successful", ObjectType.USER_AUTH, ContentType.USER_GROUP, newGroup)))
				.doOnError(ex -> log.error("Error occured while creating Realm group", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("group.creation.error"), ex));

	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Flux<String> findAllRealmGroupNames() {

		Mono<List<String>> monoList = Mono.fromCallable(() -> this.realmResource().groups().groups().stream()
				.map(group -> group.getName()).collect(Collectors.toList()));

		return monoList.flatMapIterable(list -> list)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_REALM_GROUP_EVENT, StringUtils.EMPTY,
								"Find realm group names", ObjectType.USER_AUTH, ContentType.USER_GROUP)))
				.doOnError(ex -> log.error("Error occured while finding all corresponding Realm group name", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("group.search.error"), ex));
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Flux<GroupVO> findAllRealmGroups() {

		return Mono.fromCallable(() -> this.realmResource().groups().groups()).flatMapIterable(list -> list)
				.map(GroupMapper::toViewObject)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_REALM_GROUP_EVENT, StringUtils.EMPTY,
								"Find all realm groups", ObjectType.USER_AUTH, ContentType.USER_GROUP)))
				.doOnError(ex -> log.error("Error occured while finding all corresponding Realm groups", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(i8nMessageAccessor.getLocalizedMessage("group.search.error"), ex));
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Mono<GroupVO> findRealmGroupById(@NotBlank final String id) {

		return Mono.fromCallable(() -> this.realmResource().groups().group(id).toRepresentation())
				.map(GroupMapper::toViewObject)
				.switchIfEmpty(raiseResourceNotFoundError("group.notFound", new Object[] { id }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_REALM_GROUP_EVENT, StringUtils.EMPTY,
								"Find realm group by id: " + id, ObjectType.USER_AUTH, ContentType.USER_GROUP)))
				.doOnError(ex -> log.error("Error occured while finding corresponding Realm group by id", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(
								i8nMessageAccessor.getLocalizedMessage("group.searchById.error", new Object[] { id }),
								ex));
	}

	private String getCreatedGroupId(Response response) {
		// URI location = response.getLocation();
		log.info("Response: {}-{}", response.getStatus(), response.getStatusInfo());
		log.info("Resource location {}", response.getLocation());

		return CreatedResponseUtil.getCreatedId(response);
	}

	/**
	 * 
	 */
	@Override
	public Mono<String> deleteRealmGroup(@NotBlank final String id) {

		return Mono.fromRunnable(() -> this.realmResource().removeDefaultGroup(id))
				.then(Mono.just(i8nMessageAccessor.getLocalizedMessage("group.deleteById.success")))
				.doOnError(ex -> log.error("Error occured while finding corresponding Realm group by id", ex))
				.onErrorResume(ERROR_Predicate,
						(ex) -> raiseRuntimeError(
								i8nMessageAccessor.getLocalizedMessage("group.deleteById.error", new Object[] { id }),
								ex));
	}

}
