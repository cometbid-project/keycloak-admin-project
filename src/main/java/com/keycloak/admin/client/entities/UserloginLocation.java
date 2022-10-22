/**
 * 
 */
package com.keycloak.admin.client.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.auth.audit.Audit;
import static com.keycloak.admin.client.auth.audit.Audit.*;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.models.LoginLocation;
import com.keycloak.admin.client.validators.qualifiers.IpAddress;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TypeAlias("User Location records")
@ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Document(collection = UserloginLocation.USRLOGIN_LOCATION_COLLECTION_NAME)
// @CompoundIndex(def = "{'ID': 1, 'USERNAME': 1}", unique = true, name = "user_unique_index_1", sparse = true)
public class UserloginLocation extends Entity implements Serializable {

	private static final long serialVersionUID = 590984095803496545L;
	public static final String USRLOGIN_LOCATION_COLLECTION_NAME = "USRLOGIN_LOCATION";
	
	@Setter
	@JsonProperty(USERNAME)
	@EqualsAndHashCode.Include
	@Size(min = 6, max = 330, message = "{Login.username.size}")
	@NotBlank(message = "{Login.username.notBlank}")
	@Field(name = USERNAME_FIELD)
	private String username;

	@Setter
	@IpAddress(message = "{Login.ipAddr.invalid}")
	@JsonProperty(IP_ADDRESS)
	@Indexed(name = "successlogin_ipAddr_index")
	@NotBlank(message = "{Login.ipAddr.notBlank}")
	@Field(name = IP_ADDRESS_FIELD)
	private String ipAddr;

	@Setter
	@JsonProperty(STATUS)
	@VerifyValue(message = "{Login.status.verifyValue}", value = StatusType.class)
	@Indexed(name = "successlogin_status_index")
	@Field(name = STATUS_FIELD)
	private String status; //

	@Setter(AccessLevel.PRIVATE)
	@JsonIgnore
	@ToString.Exclude
	@Builder.Default
	@Field(name = LOCATION_LIST_FIELD)
	private Collection<@Valid LoginLocation> loginLocHis = new ArrayList<>();

	@Setter(AccessLevel.PRIVATE)
	@JsonIgnore
	@ToString.Exclude
	@Builder.Default
	@Field(name = DISABLED_LOCATION_LIST_FIELD)
	private Collection<@Valid LoginLocation> disabledLocations = new ArrayList<>();

	@Setter
	@JsonProperty(AUDIT)
	@Field(name = AUDIT_FIELD)
	private Audit audit;

	@Version
	@Setter(AccessLevel.PROTECTED)
	@JsonIgnore
	@Field(name = "VERSION")
	private Long version;

	/**
	 * 
	 * @param userLocations
	 * @param maxSizeAllowed
	 */
	public void addToLoginLocHis(@NonNull List<LoginLocation> userLocations, int maxSizeAllowed) {
		log.info("New login history to add {}", userLocations);
		
		Collection<LoginLocation> loginLocationHistory = this.sortAscending(this.loginLocHis);				
		log.info("Before login history {}", this.loginLocHis);
		
		userLocations.stream().forEach(userLocation -> {
			if (loginLocationHistory.size() >= maxSizeAllowed) {
				CircularFifoQueue<LoginLocation> fifoQueue = new CircularFifoQueue<>(maxSizeAllowed);
				fifoQueue.addAll(loginLocationHistory);
				fifoQueue.add(userLocation);

				loginLocationHistory.clear();
				loginLocationHistory.addAll(fifoQueue);
			} else {
				loginLocationHistory.add(userLocation);
			}			
		});
		this.setLoginLocHis(loginLocationHistory);
		
		log.info("After login history {}", this.loginLocHis);
	}

	public boolean doDeviceCheck(@NonNull LoginLocation userLocation) {

		return this.loginLocHis.stream().filter(Objects::nonNull).anyMatch(p -> deviceCheck(p, userLocation));
	}

	public boolean isLocationInRecord(@NonNull LoginLocation userLocation) {
		if (this.loginLocHis.isEmpty()) {
			log.info("login History is empty");
			return false;
		}

		return this.loginLocHis.stream().filter(Objects::nonNull).anyMatch(p -> locationCheck(p, userLocation));
	}

	private boolean locationCheck(LoginLocation existingLoc, LoginLocation userLocation) {
		String countryCode = existingLoc.getCountryCode();
		String stateCode = existingLoc.getStateCode();
		
		if (StringUtils.isNotBlank(countryCode) && StringUtils.isNotBlank(stateCode)) {
			return countryCode.equalsIgnoreCase(userLocation.getCountryCode())
					&& stateCode.equalsIgnoreCase(userLocation.getStateCode());
		}

		return false;
	}

	private boolean disabledLocationCheck(LoginLocation existingLoc, String countryCode, String stateCode) {
		String existingCountryCode = existingLoc.getCountryCode();
		String existingStateCode = existingLoc.getStateCode();

		if (StringUtils.isNotBlank(countryCode) && StringUtils.isNotBlank(stateCode)) {
			return countryCode.equalsIgnoreCase(existingCountryCode) && stateCode.equalsIgnoreCase(existingStateCode);
		}

		return false;
	}

	private boolean deviceCheck(LoginLocation existingLoc, LoginLocation userLocation) {
		String countryCode = existingLoc.getCountryCode();
		String deviceDetails = existingLoc.getDeviceDetails();
		
		if (StringUtils.isNotBlank(countryCode) && StringUtils.isNotBlank(deviceDetails)) {
			return countryCode.equalsIgnoreCase(userLocation.getCountryCode())
					&& deviceDetails.equalsIgnoreCase(userLocation.getStateCode());
		}

		return false;
	}

	@Builder
	public UserloginLocation(String id, String username, String ipAddr, String status,
			Collection<LoginLocation> loginLocHis, Collection<LoginLocation> disabledLocations) {
		super.id = id;
		this.username = username;
		this.ipAddr = ipAddr;
		this.status = status;
		this.loginLocHis = loginLocHis;
		this.disabledLocations = disabledLocations;
	}

	public void enableLocation(String country, String state) {
		// TODO Auto-generated method stub
		List<LoginLocation> userLocations = this.disabledLocations.stream().filter(Objects::nonNull)
				.filter(p -> disabledLocationCheck(p, country, state)).collect(Collectors.toList());

		log.info("Disabled locations found  {}", userLocations); 
		
		userLocations.stream().forEach(p -> {
			loginLocHis.add(p);
			disabledLocations.remove(p);
		});
	}

	public void addToDisabledLocations(LoginLocation loginLocHis2, int maxSizeAllowed) {
		// TODO Auto-generated method stub		
		Collection<LoginLocation> disabledLoginLocationHistory = this.sortAscending(this.disabledLocations);				
		log.info("Before login history {}", this.loginLocHis);

		if (disabledLoginLocationHistory.size() >= maxSizeAllowed) {
			CircularFifoQueue<LoginLocation> fifoQueue = new CircularFifoQueue<>(maxSizeAllowed);
			fifoQueue.addAll(disabledLoginLocationHistory);
			fifoQueue.add(loginLocHis2);
			
			disabledLoginLocationHistory.clear();
			disabledLoginLocationHistory.addAll(fifoQueue);
		} else {
			disabledLoginLocationHistory.add(loginLocHis2);
		}
		
		this.setDisabledLocations(disabledLoginLocationHistory);
		log.info("After disabled login history {}", this.disabledLocations);
	}

	public UserloginLocation removeStaleLocations(Collection<LoginLocation> disabledLocations2,
			LocalDateTime expirationDate) {

		// TODO Auto-generated method stub
		List<LoginLocation> newList = disabledLocations2.stream().filter(p -> p.getLoginTime().isAfter(expirationDate))
				.collect(Collectors.toList());
		this.setDisabledLocations(newList);
		return this;
	}

	public List<LoginLocation> filterOutBetweenDate(LocalDate startDate, LocalDate endDate) {
		// TODO Auto-generated method stub
		return this.loginLocHis.stream().filter(p -> filterBetweenDate(p, startDate, endDate))
				.collect(Collectors.toList());
	}

	private boolean filterBetweenDate(LoginLocation userLocation, LocalDate startDate, LocalDate endDate) {

		LocalDate locDate = userLocation.getLoginTime().toLocalDate();

		return Objects.nonNull(locDate) && (locDate.isAfter(startDate) || locDate.isEqual(startDate))
				&& locDate.isBefore(endDate);
	}

	Collection<LoginLocation> sortAscending(Collection<LoginLocation> loginHistory) {
		// 2. ascending-order sorting
        List<LoginLocation> sortedLoginHistAsc = loginHistory
                .stream()
                .sorted(Comparator.comparing(LoginLocation::getLoginTime))
                .collect(Collectors.toList());
        
        return sortedLoginHistAsc;
	}

	Collection<LoginLocation> sortDescending(Collection<LoginLocation> loginHistory) {
		 // 3. descending-order sorting
        List<LoginLocation> sortedLoginHistDesc = loginHistory
                .stream()
                .sorted(Comparator.comparing(LoginLocation::getLoginTime).reversed())
                .collect(Collectors.toList());
        
        return sortedLoginHistDesc;
	}
	
	/**********************************************/
	/******** Json fields definition *************/
	/*********************************************/
	public static final String USERNAME = "username";
	public static final String IP_ADDRESS = "ip_addr";
	public static final String STATUS = "status";
	
	/**********************************************/
	/******** Column fields definition ***********/
	/*********************************************/
	public static final String USERNAME_FIELD = "USERNAME";
	public static final String IP_ADDRESS_FIELD = "IP_ADDR";
	public static final String LOCATION_LIST_FIELD = "USER_LOCATIONS";
	public static final String DISABLED_LOCATION_LIST_FIELD = "DISABLED_LOCATIONS";
	public static final String STATUS_FIELD = "STATUS";
}
