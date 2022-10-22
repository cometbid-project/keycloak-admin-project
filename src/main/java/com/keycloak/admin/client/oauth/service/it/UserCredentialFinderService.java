/**
 * 
 */
package com.keycloak.admin.client.oauth.service.it;

import com.keycloak.admin.client.common.utils.QueryParams;
import com.keycloak.admin.client.models.PagingModel;
import com.keycloak.admin.client.models.SearchUserRequest;
import com.keycloak.admin.client.models.UserVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
public interface UserCredentialFinderService {

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Mono<UserVO> findByUsername(final String username);

	/**
	 * 
	 * @param id
	 * @return
	 */
	Mono<UserVO> findUserById(final String id);

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Flux<UserVO> findUserByEmail(final String email, final PagingModel pagingModel); 

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Flux<UserVO> findAll(final PagingModel pagingModel);

	/**
	 * 
	 * @param newRole
	 * @return
	 */
	Flux<UserVO> search(SearchUserRequest searchFields, PagingModel pagingModel);

}
