/**
 * 
 */
package com.keycloak.admin.client.repository.base;

import java.io.Serializable;

import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;

import lombok.Getter;

/**
 * @author Gbenga
 *
 */
@Getter
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleReactiveMongoRepository<T, ID>
		implements BaseRepository<T, ID> {

	private final ReactiveMongoOperations entityManager;

	public BaseRepositoryImpl(MongoEntityInformation<T, ID> entityInformation,
			ReactiveMongoOperations mongoOperations) {
		super(entityInformation, mongoOperations);
		this.entityManager = mongoOperations;
	}

}
