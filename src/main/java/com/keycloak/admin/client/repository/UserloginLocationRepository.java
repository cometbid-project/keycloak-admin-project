/**
 * 
 */
package com.keycloak.admin.client.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.repository.base.BaseRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Repository
public interface UserloginLocationRepository extends BaseRepository<UserloginLocation, String> {

	// Done
	Flux<UserloginLocation> findByIpAddr(String ipAddr, Pageable pageable);

	Mono<Long> countByIpAddr(String ipAddr);

	// Done
	Mono<UserloginLocation> findOneByUsernameIgnoreCase(String username);

	// Done
	Flux<UserloginLocation> findByIpAddrAndStatusIgnoreCase(String ipAddr, String status, Pageable pageable);

	// Done
	Mono<Long> countByIpAddrAndStatusIgnoreCase(String ipAddr, String status);

	// Done
	Flux<UserloginLocation> findByStatusIgnoreCase(String status, Pageable pageable);

	Mono<Long> countByStatusIgnoreCase(String status);

	// Done
	Flux<UserloginLocation> findByUsernameOrIpAddrAndStatusAllIgnoreCase(String username, String ipAddr, String status,
			Pageable pageable);

	// Done
	Mono<Long> countByUsernameOrIpAddrAndStatusAllIgnoreCase(String username, String ipAddr, String status);

	// Done
	Flux<UserloginLocation> findByUsernameIgnoreCaseOrIpAddr(String username, String ipAddr, Pageable pageable);

	// Done
	Mono<Long> countByUsernameIgnoreCaseOrIpAddr(String username, String ipAddr);

	// Done
	Mono<UserloginLocation> findByUsernameIgnoreCase(String username);

	// Done
	Mono<UserloginLocation> findByUsernameAndStatusAllIgnoreCase(String username, String status);

	// Done
	Mono<Long> countByUsernameIgnoreCase(String username);

	// Done
	Mono<UserloginLocation> findOneByUsernameAndStatusAllIgnoreCase(String username, String status);

	// Done
	Mono<Long> countByUsernameAndStatusAllIgnoreCase(String username, String status);

	// Done
	Flux<UserloginLocation> findByUsernameIgnoreCaseAndIpAddr(String username, String ipAddr, Pageable pageable);

	// Done
	Mono<Long> countByUsernameIgnoreCaseAndIpAddr(String username, String ipAddr);

	// Done
	Flux<UserloginLocation> findByUsernameAndIpAddrAndStatusAllIgnoreCase(String username, String ipAddr, String status,
			Pageable pageable);

	// Done
	Mono<Long> countByUsernameAndIpAddrAndStatusAllIgnoreCase(String username, String ipAddr, String status);

	// @Query(value = "SELECT b FROM UserloginLocation b WHERE b.loginTime BETWEEN
	// :startTime AND :endTime ORDER BY b.loginTime")
	Flux<UserloginLocation> findByLoginTimeBetweenOrderByLoginTime(LocalDate startTime, LocalDate endTime,
			Pageable pageable);

	// @CountQuery(value = "SELECT count(b) FROM UserloginLocation b WHERE
	// b.loginTime
	// BETWEEN :startTime AND :endTime ORDER BY b.loginTime")
	Mono<Long> countByLoginTimeBetweenOrderByLoginTime(LocalDateTime startTime, LocalDateTime endTime);

	Mono<Long> countByLoginTimeBetweenOrderByLoginTime(LocalDate startDate, LocalDate endDate);

	// Mono<Map<String, Object>> findByLoginTimeBetweenOrderByLoginTime(LocalDate
	// startDate, LocalDate endDate);

}
