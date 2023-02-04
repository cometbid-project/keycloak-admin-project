/**
 * 
 */
package com.keycloak.admin.client.integration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;

import com.keycloak.admin.client.KeycloakAdminProjectApplication;
import com.keycloak.admin.client.config.ReactiveRedisConfig;
import com.keycloak.admin.client.oauth.service.GatewayRedisCache;
import com.keycloak.admin.client.redis.condition.EnabledOnRedisAvailable;
import com.keycloak.admin.client.redis.service.ReactiveRedisComponent;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Gbenga
 *
 */
@Log4j2
@DisplayName("Gateway redis cache test")
//@SpringBootTest(classes = ReactiveRedisConfig.class)
@ContextConfiguration(classes = { ReactiveRedisConfig.class, ReactiveRedisCacheServiceTest.TestConfig.class })
@Import({ ReactiveRedisComponent.class })
@DataRedisTest(properties = { "spring.redis.password=", 
		"spring.redis.host=localhost", "spring.redis.port=8850" })
class ReactiveRedisCacheServiceTest extends AbstractRedisRepositoryTest {
	
	@Autowired
	@Qualifier("redis")
	private ReactiveRedisComponent redisComponent;
	
	@ComponentScan(
		    basePackages = {
		      "com.keycloak.admin.client.redis.service",		  
		    }
		)
		@EnableWebFlux
		//@EnableWebFluxSecurity
		@EnableAutoConfiguration(
		      exclude = {
		        MongoReactiveAutoConfiguration.class,
		        MongoAutoConfiguration.class,
		        MongoDataAutoConfiguration.class,
		        EmbeddedMongoAutoConfiguration.class,
		        MongoReactiveRepositoriesAutoConfiguration.class,
		        MongoRepositoriesAutoConfiguration.class
		})	
		static class TestConfig {}
	
	/**
	 * 
	 */
	@DisplayName("to test stack function with simple String key/String value")
	@Test
	void verifyStackFunctionWithSimpleKeyValueWorks() {

		String key = "key";
		String stringVal1 = "value1";

		Mono<Long> result = redisComponent.stack(key, stringVal1);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		String stringVal2 = "value2";
		result = redisComponent.stack(key, stringVal2);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		String stringVal3 = "value3";
		result = redisComponent.stack(key, stringVal3);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal3).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal2).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal1).verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Pop/Pop with timeout function with simple String key/String value")
	@Test
	void verifyPopWithTimeoutFunctionWithSimpleKeyValueWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3", "value4", "value5", "value6" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.stack(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value6").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value5").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value4").verifyComplete();
		
		// =================================================================
		
		Mono<String> popTimeOutResult = redisComponent.pop(key, 2);
		StepVerifier.create(popTimeOutResult).expectNext("value3").verifyComplete();
				
		popTimeOutResult = redisComponent.pop(key, 2);
		StepVerifier.create(popTimeOutResult).expectNext("value2").verifyComplete();
				
		popTimeOutResult = redisComponent.pop(key, 2);
		StepVerifier.create(popTimeOutResult).expectNext("value1").verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test detach function with simple String key/String value")
	@Test
	void verifyDetachFunctionWithSimpleKeyValueWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3", "value4", "value5", "value6" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.stack(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.detach(key);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();
				
		popResult = redisComponent.detach(key);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
				
		popResult = redisComponent.detach(key);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
		
		// =================================================================
		
		Mono<String> popTimeOutResult = redisComponent.detach(key, 2);
		StepVerifier.create(popTimeOutResult).expectNext("value4").verifyComplete();
						
		popTimeOutResult = redisComponent.detach(key, 2);
		StepVerifier.create(popTimeOutResult).expectNext("value5").verifyComplete();
						
		popTimeOutResult = redisComponent.detach(key, 2);
		StepVerifier.create(popTimeOutResult).expectNext("value6").verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test stack if present function")
	@Test
	void verifyStackIfPresentFunction() {

		String key = "key";
		String stringVal1 = "value1";

		// Not pushed because List doesn't exist
		Mono<Long> result = redisComponent.stackIfPresent(key, stringVal1);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// Pushed to create a List
		String stringVal2 = "value2";
		result = redisComponent.stack(key, stringVal2);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// Push successful because List now exist
		String stringVal3 = "value3";
		result = redisComponent.stackIfPresent(key, stringVal3);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal3).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal2).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectError();			
	}
	
	/**
	 * 
	 */
	@DisplayName("to test stack function with simple String key/Array of String values")
	@Test
	void verifyStackFunctionWithSimpleKeyArrayOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3" };

		Mono<Long> result = redisComponent.stack(key, stringArraVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();
	}
	
	
	
	/**
	 * 
	 */
	@DisplayName("to test stack function with simple String key/List of String values")
	@Test
	void verifyStackFunctionWithSimpleKeyListOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.stack(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();
	}
	
	
	/**
	 * 
	 */
	@DisplayName("to test stack and Push function")
	@Test
	void verifyStackAndPushFunction() {

		String srcKey = "srcKey";
		String destKey = "destKey";
		
		String[] stringArrayVal = { "value1", "value2", "value3" };
		Mono<Long> stackResult = redisComponent.stack(srcKey, stringArrayVal);		
		StepVerifier.create(stackResult).expectNextCount(1L).verifyComplete();
		
		// ============== Stack and Push function 3 times ======================
		
		Mono<String> stackAndPushResult = redisComponent.stackAndPush(srcKey, destKey); 
		StepVerifier.create(stackAndPushResult).expectNext("value1").verifyComplete();
		
		stackAndPushResult = redisComponent.stackAndPush(srcKey, destKey); 
		StepVerifier.create(stackAndPushResult).expectNext("value2").verifyComplete();
		
		stackAndPushResult = redisComponent.stackAndPush(srcKey, destKey); 
		StepVerifier.create(stackAndPushResult).expectNext("value3").verifyComplete();
		
		// ============ Verify that Pushing to Destination was Successful =========
		
		Mono<String> popResult = redisComponent.pop(destKey);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
						
		popResult = redisComponent.pop(destKey);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
						
		popResult = redisComponent.pop(destKey);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();				
	}
	
	/**
	 * 
	 */
	@DisplayName("to test detach and Push function")
	@Test
	void verifyDetachAndPushFunction() {

		String srcKey = "srcKey";
		String destKey = "destKey";
		
		String[] stringArrayVal = { "value1", "value2", "value3" };
		Mono<Long> stackResult = redisComponent.stack(srcKey, stringArrayVal);		
		StepVerifier.create(stackResult).expectNextCount(1L).verifyComplete();
		
		// ============== Detach and Push function 3 times ======================
		
		Mono<String> stackAndPushResult = redisComponent.detachAndPush(srcKey, destKey, 2); 
		StepVerifier.create(stackAndPushResult).expectNext("value1").verifyComplete();
		
		stackAndPushResult = redisComponent.detachAndPush(srcKey, destKey, 2);
		StepVerifier.create(stackAndPushResult).expectNext("value2").verifyComplete();
		
		stackAndPushResult = redisComponent.detachAndPush(srcKey, destKey, 2);
		StepVerifier.create(stackAndPushResult).expectNext("value3").verifyComplete();
		
		// ============ Verify that Pushing to Destination was Successful =========
		
		Mono<String> popResult = redisComponent.pop(destKey);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
						
		popResult = redisComponent.pop(destKey);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
						
		popResult = redisComponent.pop(destKey);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();				
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Append function with simple String key/String value")
	@Test
	void verifyAppendFunctionWithSimpleKeyValueWorks() {

		String key = "key";
		String stringVal1 = "value1";

		Mono<Long> result = redisComponent.append(key, stringVal1);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		String stringVal2 = "value2";
		result = redisComponent.append(key, stringVal2);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		String stringVal3 = "value3";
		result = redisComponent.append(key, stringVal3);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal1).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal2).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal3).verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test append if present function")
	@Test
	void verifyAppendIfPresentFunction() {

		String key = "key";
		String stringVal1 = "value1";

		// Not pushed because List doesn't exist
		Mono<Long> result = redisComponent.appendIfPresent(key, stringVal1);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// Pushed to create a List
		String stringVal2 = "value2";
		result = redisComponent.append(key, stringVal2);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// Push successful because List now exist
		String stringVal3 = "value3";
		result = redisComponent.appendIfPresent(key, stringVal3);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal2).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext(stringVal3).verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectError();			
	}
	
	/**
	 * 
	 */
	@DisplayName("to test append function with simple String key/Array of String values")
	@Test
	void verifyAppendFunctionWithSimpleKeyArrayOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3" };

		Mono<Long> result = redisComponent.append(key, stringArraVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
	}	

	/**
	 * 
	 */
	@DisplayName("to test append function with simple String key/List of String values")
	@Test
	void verifyAppendFunctionWithSimpleKeyListOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.append(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// =================================================================
		
		Mono<String> popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value1").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value2").verifyComplete();
		
		popResult = redisComponent.pop(key);
		StepVerifier.create(popResult).expectNext("value3").verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Get function with simple String key/List of String values")
	@Disabled
	void verifyGetFunctionWithSimpleKeyListOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3", "value4", "value5" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.stack(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// ====================================================================
		
		Mono<String> getResult = redisComponent.get(key, "value3");
		StepVerifier.create(getResult).expectNext("value3").verifyComplete();
				
		getResult = redisComponent.get(key, "value5");
		StepVerifier.create(getResult).expectNext("value5").verifyComplete();
				
		getResult = redisComponent.get(key, "value1");
		StepVerifier.create(getResult).expectNext("value1").verifyComplete();		
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Delete All function with simple String key/List of String values")
	@Test
	void verifyDeleteAllFunctionWithSimpleKeyListOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3", "value4", "value5" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.stack(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// ====================================================================
		
		Mono<Long> deleteResult = redisComponent.deleteAll(key, listOfVal);
		StepVerifier.create(deleteResult).expectNextCount(1L).verifyComplete();
				
		Mono<String> popResult = redisComponent.pop(key); 
		StepVerifier.create(popResult).expectError();		
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Delete function with simple String key/List of String values")
	@Test
	void verifyDeleteFunctionWithSimpleKeyListOfValuesWorks() {

		String key = "key";
		String[] stringArraVal = { "value1", "value2", "value3", "value5", "value5" };
		List<String> listOfVal = Arrays.asList(stringArraVal);

		Mono<Long> result = redisComponent.stack(key, listOfVal);
		StepVerifier.create(result).expectNextCount(1L).verifyComplete();
		
		// ======================================================================
		
		Mono<Long> deleteResult = redisComponent.delete(key, 1L, "value3");
		StepVerifier.create(deleteResult).expectNext(1L).verifyComplete();
				
		deleteResult = redisComponent.delete(key, 2L, "value5");
		StepVerifier.create(deleteResult).expectNext(2L).verifyComplete();
				
		deleteResult = redisComponent.delete(key, 1L, "value2");
		StepVerifier.create(deleteResult).expectNext(1L).verifyComplete();		
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Decrement This function")
	@Test
	void verifyDecrementThisWorks() {

		String key = "key";
		
		Mono<Boolean> resultSave = redisComponent.putIfAbsent(key, Integer.valueOf(10)); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();

		Mono<Long> resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(9L).verifyComplete();
		
		// ======================================================================
		
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(8L).verifyComplete();
				
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(7L).verifyComplete();
				
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(6L).verifyComplete();	
		
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(5L).verifyComplete();
		
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(4L).verifyComplete();
				
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(3L).verifyComplete();
				
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(2L).verifyComplete();	
		
		resultDecrement = redisComponent.decrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(1L).verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Decrement This By Value function")
	@Test
	void verifyDecrementThisByValueWorks() {

		String key = "key";
		
		Mono<Boolean> resultSave = redisComponent.putIfAbsent(key, Integer.valueOf(10)); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();

		Mono<Long> resultDecrement = redisComponent.decrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(8L).verifyComplete();
		
		// ======================================================================
		
		resultDecrement = redisComponent.decrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(6L).verifyComplete();
				
		resultDecrement = redisComponent.decrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(4L).verifyComplete();
				
		resultDecrement = redisComponent.decrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(2L).verifyComplete();	
		
		resultDecrement = redisComponent.decrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(0L).verifyComplete();		
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Increment This function")
	@Test
	void verifyIncrementThisWorks() {

		String key = "key";
		
		Mono<Boolean> resultSave = redisComponent.putIfAbsent(key, Integer.valueOf(0)); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();

		Mono<Long> resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(1L).verifyComplete();
		
		// ======================================================================
		
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(2L).verifyComplete();
				
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(3L).verifyComplete();
				
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(4L).verifyComplete();	
		
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(5L).verifyComplete();
		
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(6L).verifyComplete();
				
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(7L).verifyComplete();
				
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(8L).verifyComplete();	
		
		resultDecrement = redisComponent.incrementThis(key);
		StepVerifier.create(resultDecrement).expectNext(9L).verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Increment This By Value function")
	@Test
	void verifyIncrementThisByValueWorks() {

		String key = "key";
		
		Mono<Boolean> resultSave = redisComponent.putIfAbsent(key, Integer.valueOf(0)); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();

		Mono<Long> resultDecrement = redisComponent.incrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(2L).verifyComplete();
		
		// ======================================================================
		
		resultDecrement = redisComponent.incrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(4L).verifyComplete();
				
		resultDecrement = redisComponent.incrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(6L).verifyComplete();
				
		resultDecrement = redisComponent.incrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(8L).verifyComplete();	
		
		resultDecrement = redisComponent.incrementThis(key, 2L);
		StepVerifier.create(resultDecrement).expectNext(10L).verifyComplete();		
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Increment This By Double Value function")
	@Test
	void verifyIncrementThisByDoubleValueWorks() {

		String key = "key";
		
		Mono<Boolean> resultSave = redisComponent.putIfAbsent(key, Double.valueOf(5.25)); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();

		Mono<Double> resultDecrement = redisComponent.incrementThis(key, Double.valueOf(2.25)); 
		StepVerifier.create(resultDecrement).expectNext(7.50).verifyComplete();
	
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Put if Absent Value function")
	@Test
	void verifyPutIfAbsentValueWorks() {

		String key = "key";
		String message = "Saved message";
		String editMessage = "Edited message";
		
		Mono<Boolean> resultSave = redisComponent.putPojo(key, message); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Boolean> resultPut = redisComponent.putIfAbsent(key, editMessage); 
		StepVerifier.create(resultPut).expectNext(Boolean.FALSE).verifyComplete();
		
		Mono<Object> expectedMessage = redisComponent.getPojo(key);  
		StepVerifier.create(expectedMessage).expectNext(message).verifyComplete();
		
		// ========================================================================

		Mono<Boolean> resultDelete = redisComponent.deletePojo(key);  
		StepVerifier.create(resultDelete).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Boolean> expectedSuccessSave = redisComponent.putIfAbsent(key, editMessage); 
		StepVerifier.create(expectedSuccessSave).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Object> expectedEditedMessage = redisComponent.getPojo(key);  
		StepVerifier.create(expectedEditedMessage).expectNext(editMessage).verifyComplete();
	}
	
	/**
	 * 
	 */
	@DisplayName("to test Put if Absent with Timeout Value function")
	@Test
	void verifyPutIfAbsentWithTimeoutValueWorks() {

		String key = "key";
		String message = "Saved message";
		String editMessage = "Edited message";
		
		Mono<Boolean> resultSave = redisComponent.putPojo(key, message); 
		StepVerifier.create(resultSave).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Boolean> resultPut = redisComponent.putIfAbsent(key, editMessage, 1L); 
		StepVerifier.create(resultPut).expectNext(Boolean.FALSE).verifyComplete();
		
		Mono<Object> expectedMessage = redisComponent.getPojo(key);  
		StepVerifier.create(expectedMessage).expectNext(message).verifyComplete();
		
		// ==============================================================================

		Mono<Boolean> resultDelete = redisComponent.deletePojo(key);  
		StepVerifier.create(resultDelete).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Boolean> expectedSuccessSave = redisComponent.putIfAbsent(key, editMessage, 1L); 
		StepVerifier.create(expectedSuccessSave).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Object> expectedEditedMessage = redisComponent.getPojo(key);  
		StepVerifier.create(expectedEditedMessage).expectNext(editMessage).verifyComplete();
		
		// ==============================================================================
		/*
		String srcKey = "another_key";
		String anotherMessage = "Another message";	
		
		Mono<Boolean> expectedTimeoutSave = redisComponent.putIfAbsent(srcKey, anotherMessage, 2L); 
		StepVerifier.create(expectedTimeoutSave).expectNext(Boolean.TRUE).verifyComplete();
		
		Mono<Boolean> expectSuccessTimeout = redisComponent.putIfAbsent(srcKey, editMessage); 		
		Mono<Object> getMessage = redisComponent.getPojo(srcKey);
		
		log.info("Blocking pop...waiting for message");
		StepVerifier.create(getMessage) //
				.then(() -> {
					
					Mono.delay(Duration.ofSeconds(3)).doOnSuccess(it -> {

								log.info("Subscriber produces message");

							}).then(expectSuccessTimeout)
								
					.subscribe();
					
				})			
				.expectNext(editMessage).verifyComplete();

		log.info("Blocking pop...done!");		
		*/
	}
}
