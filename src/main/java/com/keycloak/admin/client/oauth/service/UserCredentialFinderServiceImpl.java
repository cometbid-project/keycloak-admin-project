/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import static com.keycloak.admin.client.error.handlers.ExceptionHandler.*;
import static com.keycloak.admin.client.error.helpers.ErrorPublisher.*;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.events.ActivityEventTypes;
import com.keycloak.admin.client.common.events.GenericSpringEvent;
import com.keycloak.admin.client.components.CustomMessageSourceAccessor;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.UserVO;
import com.keycloak.admin.client.models.mappers.UserMapper;
import com.keycloak.admin.client.oauth.service.it.KeycloakOauthClientService;
import com.keycloak.admin.client.oauth.service.it.UserCredentialFinderService;

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
@PreAuthorize("isAuthenticated()")
public class UserCredentialFinderServiceImpl implements UserCredentialFinderService {

	private final ApplicationEventPublisher eventPublisher;
	private final KeycloakOauthClientService keycloakClient;
	private final CustomMessageSourceAccessor i8nMessageAccessor;

	public UserCredentialFinderServiceImpl(@Qualifier("keycloak-client") KeycloakOauthClientService oauthClient,
			CustomMessageSourceAccessor messageAccessor, ApplicationEventPublisher eventPublisher) {

		this.eventPublisher = eventPublisher;
		this.keycloakClient = oauthClient;
		this.i8nMessageAccessor = messageAccessor;
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("hasAnyRole('ADMIN')")
	public Flux<UserVO> search(@NotNull @Valid final SearchUserRequest searchFields,
			@NotNull final PagingModel pagingModel) {

		Mono<List<UserRepresentation>> monoUserRepresentations = keycloakClient.search(pagingModel, searchFields);

		Mono<List<UserVO>> monoList = monoUserRepresentations
				.map(list -> list.stream().map(UserMapper::toViewObject).collect(Collectors.toList()));

		return monoList.flatMapIterable(list -> list)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Searched users auth profiles with criteria", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while finding all users", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("find.user.error")));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'APP_MANAGER')")
	public Flux<UserVO> findAll(@NotNull final PagingModel pagingModel) {

		Mono<List<UserRepresentation>> monoUserRepresentations = keycloakClient.findAllUsers(pagingModel);

		Mono<List<UserVO>> monoList = monoUserRepresentations
				.map(list -> list.stream().map(UserMapper::toViewObject).collect(Collectors.toList()));

		return monoList.flatMapIterable(list -> list)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Searched all users auth profiles", ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while finding all users", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("find.user.error")));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("isAnonymous() or isAuthenticated()")
	public Mono<UserVO> findByUsername(@NotBlank final String username) {

		return keycloakClient.findUserByUsername(username).map(UserMapper::toViewObject)
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { username }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Searched users auth profiles by username: " + username, ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while finding user by username", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("find.user.error")));
	}

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'APP_MANAGER')")
	public Flux<UserVO> findUserByEmail(@NotBlank final String email, @NotNull final PagingModel pagingModel) {

		return keycloakClient.findUserByEmail(email, pagingModel).flatMapIterable(list -> list)
				.map(UserMapper::toViewObject)
				.doOnComplete(() -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Searched users auth profiles by email: " + email, ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(e -> log.error("Failed to update course", e.getMessage()))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("find.user.error")));
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Override
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'APP_MANAGER')")
	public Mono<UserVO> findUserById(@NotBlank final String userId) {

		return keycloakClient.findUserById(userId).map(UserMapper::toViewObject)
				.switchIfEmpty(raiseResourceNotFoundError("user.notFound", new Object[] { userId }))
				.doOnSuccess(profile -> this.eventPublisher.publishEvent(
						new GenericSpringEvent<>(ActivityEventTypes.SEARCH_AUTH_PROFILE_EVENT, StringUtils.EMPTY,
								"Searched user auth profiles by userId: " + userId, ObjectType.USER_AUTH, ContentType.AUTH)))
				.doOnError(ex -> log.error("Error occured while finding corresponding User by id", ex))
				.onErrorResume(handleWebFluxError(i8nMessageAccessor.getLocalizedMessage("find.user.error")));
	}

}
