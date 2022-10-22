/**
 * 
 */
package com.keycloak.admin.client.common.event.listener;

import com.keycloak.admin.client.common.events.GenericSpringEvent;

import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface ApplicationEventListener {

	Mono<Void> handleGenericApplicationEvent(GenericSpringEvent<String> event);

}
