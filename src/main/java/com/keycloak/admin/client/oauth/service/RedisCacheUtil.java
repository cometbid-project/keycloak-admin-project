/**
 * 
 */
package com.keycloak.admin.client.oauth.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Service("redis")
public class RedisCacheUtil {

	private final ReactiveRedisTemplate<String, Object> redisValueTemplate;

	private ReactiveValueOperations<String, Object> reactiveValueOps;

	private ReactiveListOperations<String, String> reactiveListOps;

	private final ReactiveStringRedisTemplate redisListTemplate;

	public RedisCacheUtil(ReactiveRedisTemplate<String, Object> redisValueTemplate,
			ReactiveRedisTemplate<String, String> redisStringTemplate, ReactiveStringRedisTemplate redisListTemplate) {
		this.redisValueTemplate = redisValueTemplate;
		this.redisListTemplate = redisListTemplate;
	}

	/**
	 * 
	 */
	@PostConstruct
	public void init() {
		reactiveValueOps = redisValueTemplate.opsForValue();

		reactiveListOps = redisListTemplate.opsForList();
	}

	/**
	 * Prepend value to key, that is add entry in front(stack)
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Long> stack(@NonNull String key, @NonNull String str) {

		Mono<Long> lPush = reactiveListOps.leftPush(key, str).log(String.format("Prepend Key(%s) entry", key));
		return lPush;
	}

	/**
	 * Prepend array of values to key as entry, that is add multiple entries in
	 * front(stack).
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Long> stack(@NonNull String key, @NonNull String... strValues) {

		Mono<Long> lPush = reactiveListOps.leftPushAll(key, strValues)
				.log(String.format("Prepend Key(%s) entries", key));
		return lPush;
	}

	/**
	 * Prepend Collection of values to key as entry, that is add multiple entries in
	 * front(stack).
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Long> stack(@NonNull String key, @NonNull List<String> listValues) {

		Mono<Long> lPush = reactiveListOps.leftPushAll(key, listValues)
				.log(String.format("Prepend Key(%s) entries", key));
		return lPush;
	}

	/**
	 * Append value to key, that is add entry as Last entry(queue).
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Mono<Long> stackIfPresent(@NonNull String key, String value) {

		Mono<Long> lPush = reactiveListOps.leftPushIfPresent(key, value)
				.log(String.format("Prepend Key(%s) entry only if Present", key));
		return lPush;
	}

	/**
	 * Append value to key, that is add entry as Last entry(queue).
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Mono<Long> append(@NonNull String key, String value) {

		Mono<Long> rPush = reactiveListOps.rightPush(key, value).log(String.format("Append Key(%s) entry", key));
		return rPush;
	}

	/**
	 * Append value to key, that is add entry as Last entry(queue).
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Mono<Long> appendIfPresent(@NonNull String key, String value) {

		Mono<Long> rPush = reactiveListOps.rightPushIfPresent(key, value)
				.log(String.format("Append Key(%s) entry only if Present", key));
		return rPush;
	}

	/**
	 * Append array of values to key as entry, that is add multiple entries as
	 * Last(Queue).
	 * 
	 * @param key
	 * @param strValues
	 * @return
	 */
	public Mono<Long> append(@NonNull String key, @NonNull String... strValues) {

		Mono<Long> rPush = reactiveListOps.rightPushAll(key, strValues)
				.log(String.format("Append Key(%s) array of entries", key));
		return rPush;
	}

	/**
	 * Append Collection of values to key as entry, that is add multiple entries as
	 * Last(Queue).
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Long> append(@NonNull String key, @NonNull List<String> list) {

		Mono<Long> rPush = reactiveListOps.rightPushAll(key, list)
				.log(String.format("Append Key(%s) Collection of entries", key));
		return rPush;
	}

	/**
	 * Get an entry by key. First locate the index of the entry, and uses the index
	 * to locate the entry. Used mostly to confirm the existence of an entry value
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<String> get(@NonNull String key, @NonNull String entryValue) {

		Mono<String> result = reactiveListOps.indexOf(key, entryValue)
				.log(String.format("find index of %s by Key(%s)", entryValue, key))
				.flatMap(index -> reactiveListOps.index(key, index)).log(String.format("use index %s to locate Value"));

		return result;
	}

	/**
	 * Remove entries by count of a key
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Long> delete(@NonNull String key, Long count, @NonNull String entryValue) {

		Mono<Long> result = reactiveListOps.remove(key, count, entryValue)
				.log(String.format("remove %s entry of %s by Key(%s)", count, entryValue, key));

		return result;
	}
	
	/**
	 * Removes the given key and it's corresponding entries.
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Long> deleteAll(@NonNull String key, @NonNull List<String> list) {

		Mono<Long> result = reactiveListOps.delete(key)
				.log(String.format("Key(%s) entries removed", key))
				.flatMap(index -> this.append(key, list))
				.log(String.format("Key(%s) re-created, and initialized"));

		return result;
	}

	// ==============================================================================================================

	/**
	 * Remove the last element from list at sourceKey, append it to destinationKey
	 * and return its value.
	 * 
	 * @param srcKey
	 * @param destKey
	 * @return
	 */
	public Mono<String> stackAndPush(@NonNull String srcKey, @NonNull String destKey) {

		Mono<String> lPush = reactiveListOps.rightPopAndLeftPush(srcKey, destKey).log(String.format(
				"Removes Source Key(%s) Last entry, Add to Destination Key(%s) as Last entry", srcKey, destKey));
		return lPush;
	}

	/**
	 * Remove the last element from list at srcKey, append it to dstKey and return
	 * its value. Results return once an element available or timeout reached.
	 * 
	 * @param srcKey
	 * @param destKey
	 * @param timeoutInSecs
	 * @return
	 */
	public Mono<String> detachAndPush(@NonNull String srcKey, @NonNull String destKey, long timeoutInSecs) {

		Mono<String> lPush = reactiveListOps.rightPopAndLeftPush(srcKey, destKey, Duration.ofSeconds(timeoutInSecs))
				.log(String.format(
						"Removes Source Key(%s) Last entry, Add to Destination Key(%s) as Last entry with timeout %d",
						srcKey, destKey, timeoutInSecs));
		return lPush;
	}

	/**
	 * 
	 * Removes and returns first element in list stored at key.
	 * 
	 * @param key
	 * @return
	 */
	public Mono<String> pop(@NonNull String key) {

		Mono<String> lPop = reactiveListOps.leftPop(key).log(String.format("Removes Key(%s) entry in FIFO order", key));
		return lPop;
	}

	/**
	 * Removes and returns first element from lists stored at key. Results return
	 * once an element available or timeout reached.
	 * 
	 * @param key
	 * @param timeout
	 * @return
	 */
	public Mono<String> pop(@NonNull String key, long timeoutInSecs) {

		Mono<String> lPop = reactiveListOps.leftPop(key, Duration.ofSeconds(timeoutInSecs))
				.log(String.format("Removes Key(%s) entry in FIFO order", key));
		return lPop;
	}

	/**
	 * Removes and returns last element in list stored at key.
	 * 
	 * @param key
	 * @return
	 */
	public Mono<String> detach(@NonNull String key) {

		Mono<String> rPop = reactiveListOps.rightPop(key)
				.log(String.format("Removes Key(%s) entry in LIFO order", key));
		return rPop;
	}

	/**
	 * Removes and returns last element from lists stored at key. Results return
	 * once an element available or timeout reached.
	 * 
	 * @param key
	 * @param timeoutInSecs
	 * @return
	 */
	public Mono<String> detach(@NonNull String key, long timeoutInSecs) {

		Mono<String> rPop = reactiveListOps.rightPop(key, Duration.ofSeconds(timeoutInSecs))
				.log(String.format("Removes Key(%s) entry in LIFO order", key));
		return rPop;
	}

	// ========================================================================================================

	/**
	 * Decrements the number stored at key by one.
	 * 
	 * @param key
	 * @return
	 */
	public Mono<Long> decrementThis(@NonNull String key) {
		Mono<Long> lPush = reactiveValueOps.decrement(key).log(String.format("Key(%s) entry Decremented by 1", key));
		return lPush;
	}

	/**
	 * Decrements the number stored at key by delta value.
	 * 
	 * @param key
	 * @param delta
	 * @return
	 */
	public Mono<Long> decrementThis(@NonNull String key, Long delta) {
		Mono<Long> lPush = reactiveValueOps.decrement(key, delta)
				.log(String.format("Key(%s) entry Decremented by %d", key, delta));
		return lPush;
	}

	/**
	 * Increments the number stored at key by one.
	 * 
	 * @param key
	 * @return
	 */
	public Mono<Long> incrementThis(@NonNull String key) {
		Mono<Long> lPush = reactiveValueOps.increment(key).log(String.format("Key(%s) entry Incremented by 1", key));
		return lPush;
	}

	/**
	 * Increments the number stored at key by delta value.
	 * 
	 * @param key
	 * @param delta
	 * @return
	 */
	public Mono<Long> incrementThis(@NonNull String key, Long delta) {
		Mono<Long> lPush = reactiveValueOps.increment(key, delta)
				.log(String.format("Key(%s) entry Incremented by %d", key, delta));
		return lPush;
	}

	/**
	 * Increment the string representing a floating point number stored at key by
	 * 'Double' delta value.
	 * 
	 * @param key
	 * @param delta
	 * @return
	 */
	public Mono<Double> incrementThis(@NonNull String key, Double delta) {
		Mono<Double> lPush = reactiveValueOps.increment(key, delta)
				.log(String.format("Key(%s) entry Incremented by %1$,.2f", key, delta));
		return lPush;
	}

	/**
	 * Set key to hold the string value if key is absent.
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Boolean> putIfAbsent(@NonNull String key, @NonNull String pojo) {

		Mono<Boolean> lPush = reactiveValueOps.setIfAbsent(key, pojo).log(String.format("Key(%s) entry Pushed", key));
		return lPush;
	}

	/**
	 * Set key to hold the string value if key is absent.
	 * 
	 * @param key
	 * @param list
	 * @return
	 */
	public Mono<Boolean> putIfAbsent(@NonNull String key, @NonNull Object pojo, long timeoutInSecs) {

		Mono<Boolean> lPush = reactiveValueOps.setIfAbsent(key, pojo, Duration.ofSeconds(timeoutInSecs))
				.log(String.format("Key(%s) entry Pushed %d sec timeout", key, timeoutInSecs));
		return lPush;
	}

	/**
	 * Set the value and expiration timeout for key entry.
	 * 
	 * @param key
	 * @param pojo
	 * @param timeoutInSecs
	 * @return
	 */
	public Mono<Boolean> putPojo(@NonNull String key, @NonNull Object pojo) {
		Mono<Boolean> lPush = reactiveValueOps.set(key, pojo).log(String.format("Key(%s) entry Pushed", key));
		return lPush;
	}

	/**
	 * Set the value and expiration timeout for key entry.
	 * 
	 * @param key
	 * @param pojo
	 * @param timeoutInSecs
	 * @return
	 */
	public Mono<Boolean> putPojo(@NonNull String key, @NonNull Object pojo, long timeoutInSecs) {
		Mono<Boolean> lPush = reactiveValueOps.set(key, pojo, Duration.ofSeconds(timeoutInSecs))
				.log(String.format("Key(%s) entry Pushed with %d sec timeout", key, timeoutInSecs));
		return lPush;
	}

	/**
	 * Set multiple keys to multiple values using key-value pairs provided in tuple.
	 * 
	 * @param key
	 * @param multiPojo
	 * @return
	 */
	public Mono<Boolean> putMultiPojo(@NonNull Map<String, Object> multiKeyValues) {
		Mono<Boolean> lPush = reactiveValueOps.multiSet(multiKeyValues)
				.log(String.format("Key(%s) entries Pushed", multiKeyValues.keySet()));
		return lPush;
	}

	/**
	 * Get the value of key entry.
	 * 
	 * @param key
	 * @return
	 */
	public Mono<Object> getPojo(@NonNull String key) {

		Mono<Object> lPop = reactiveValueOps.get(key).log(String.format("Key(%s) entry Popped", key));
		return lPop;
	}

	/**
	 * Get multiple keys. Values are returned in the order of the requested keys.
	 * 
	 * @param multikeys
	 * @return
	 */
	public Mono<List<Object>> getMultiPojo(@NonNull Collection<String> multikeys) {

		Mono<List<Object>> lPush = reactiveValueOps.multiGet(multikeys)
				.log(String.format("Key(%s) entries Popped", multikeys));
		return lPush;
	}

	/**
	 * Set new value for key and return its old value.
	 * 
	 * @param key
	 * @param pojo
	 * @return
	 */
	public Mono<Object> replacePojo(@NonNull String key, @NonNull Object pojo) {

		Mono<Object> lPop = reactiveValueOps.getAndSet(key, pojo)
				.log(String.format("Old Key(%s) entry Popped and new entry Pushed", key));
		return lPop;
	}

	/**
	 * Removes the given key entry
	 * 
	 * @param key
	 * @return
	 */
	public Mono<Boolean> deletePojo(@NonNull String key) {

		Mono<Boolean> lPop = reactiveValueOps.delete(key).log(String.format("Key(%s) entry Deleted", key));
		return lPop;
	}

}
