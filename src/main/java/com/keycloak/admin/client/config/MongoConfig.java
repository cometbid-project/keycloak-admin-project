/**
 * 
 */
package com.keycloak.admin.client.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.WriteConcernResolver;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.security.core.userdetails.User;

import com.keycloak.admin.client.auth.audit.AuditAwareImpl;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author Gbenga
 *
 */
@Log4j2
@Configuration
@PropertySource(value = "classpath:mongo_db.properties")
@EnableReactiveMongoAuditing(auditorAwareRef = "auditorProvider")
@EnableReactiveMongoRepositories(basePackages = { "com.keycloak.admin.client.repository",
		"com.keycloak.admin.client.repository.base" })
public class MongoConfig extends AbstractReactiveMongoConfiguration {

	/*
	 * @Autowired private Environment environment;
	 */
	@Value("${spring.data.mongodb.database}")
	private String db;

	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;

	@Override
	public MongoCustomConversions customConversions() {
		// converters.add(new UserWriterConverter());
		return new MongoCustomConversions(converters);
	}

	@Override
	protected boolean autoIndexCreation() {
		return true;
	}

	@Bean
	ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory reactiveMongoDbFactory) {

		return new ReactiveMongoTransactionManager(reactiveMongoDbFactory);
	}

	@Bean
	public ReactiveAuditorAware<User> auditorProvider() {
		return new AuditAwareImpl();
	}

	private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();

	@Bean
	public ReactiveGridFsTemplate gridMongoFsTemplate(ReactiveMongoDatabaseFactory reactiveMongoDbFactory,
			MongoConverter mongoConverter) throws Exception {
		return new ReactiveGridFsTemplate(reactiveMongoDbFactory, mongoConverter);
	}

	@Override
	public MongoClient reactiveMongoClient() {
		return MongoClients.create(mongoUri);
	}

	@Override
	protected String getDatabaseName() {
		// TODO Auto-generated method stub
		return db;
	}

	@Bean
	public WriteConcernResolver writeConcernResolver() {
		return action -> {
			if (action.getCollectionName().contains("AUDIT")) {
				return WriteConcern.UNACKNOWLEDGED;
			} else if (action.getCollectionName().contains("Metadata")) {
				return WriteConcern.JOURNALED;
			}

			System.out.println("Using Write Concern of Acknowledged");
			return action.getDefaultWriteConcern();

			// return WriteConcern.ACKNOWLEDGED;
		};
	}

	public ReactiveMongoDatabaseFactory mongoDbFactory() {
		return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClient(), getDatabaseName());
	}

	@Bean
	public ReactiveMongoTemplate reactiveMongoTemplate() {

		ReactiveMongoTemplate mongoTemplate = new ReactiveMongoTemplate(mongoDbFactory());
		mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
		
		return mongoTemplate;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initIndicesAfterStartup() {

		log.info("Mongo InitIndicesAfterStartup init");
		long init = System.currentTimeMillis();

		final MongoMappingContext mappingContext = (MongoMappingContext) reactiveMongoTemplate().getConverter()
				.getMappingContext();

		if (mappingContext instanceof MongoMappingContext) {
			MongoMappingContext mongoMappingContext = (MongoMappingContext) mappingContext;

			for (MongoPersistentEntity<?> persistentEntity : mongoMappingContext.getPersistentEntities()) {
				Class<?> clazz = persistentEntity.getType();
				if (clazz.isAnnotationPresent(Document.class)) {
					ReactiveIndexOperations indexOps = reactiveMongoTemplate().indexOps(clazz);
					IndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
					resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
				}
			}

		}

		log.info("MongoDb InitIndicesAfterStartup takes: {}ms", (System.currentTimeMillis() - init));
	}

}