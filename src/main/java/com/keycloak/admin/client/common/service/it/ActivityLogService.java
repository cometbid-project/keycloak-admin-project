/**
 * 
 */
package com.keycloak.admin.client.common.service.it;


import com.keycloak.admin.client.common.activity.ActivityLog;
import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.utils.QueryParams;
import com.keycloak.admin.client.models.Username;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface ActivityLogService<T, E> {

	/**
	 * 
	 * @param merchantCriteria
	 * @return
	 */
	Flux<String> findDefaultFields(Class<T> domainClass);

	/**
	 * 
	 * @param merchantCriteria
	 * @param pgModel
	 * @return
	 */
	Mono<Integer> countDefaultFields(Class<T> domainClass);

	/**
	 * 
	 * @param queryParams
	 * @param contextMerchant
	 * @param domainClass
	 * @return
	 */
	//Flux<E> search(final QueryParams queryParams, final String username, Class<T> domainClass);

	/**
	 * 
	 * @param queryParams
	 * @return
	 */
	//Mono<Long> countAll(final QueryParams queryParams, final String username, Class<T> domainClass);

	/**
	 * 
	 * @param message
	 * @param contentType
	 * @return
	 */
	Mono<ActivityLog> saveActivity(String message, ObjectType objectType, ContentType contentType, 
			Action actionType, Long timestamp);

	/**
	 * Invoked by the Scheduler to expire Activity logs records to mark them for
	 * Archiving
	 * 
	 * @return
	 */
	//Mono<Void> expireActivityLogs();

	/**
	 * Invoked by the Scheduler to archive expired Activity logs
	 * 
	 * @return
	 */
	//Mono<Void> archiveActivityLogs();

}
