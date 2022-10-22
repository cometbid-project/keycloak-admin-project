/**
 * 
 */
package com.keycloak.admin.client.repository.base;

import java.io.Serializable;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author Gbenga
 *
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends ReactiveMongoRepository<T, ID> {
	
	// void refresh(T t);	
}
