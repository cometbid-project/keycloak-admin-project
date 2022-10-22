/**
 * 
 */
package com.keycloak.admin.client.dataacess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ua_parser.Parser;
import com.github.javafaker.Faker;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.common.geo.GeolocationUtils;
import com.keycloak.admin.client.common.utils.DateUtil;
import com.keycloak.admin.client.common.utils.RandomGenerator;
import com.keycloak.admin.client.entities.NewLocationToken;
import com.keycloak.admin.client.entities.UserloginLocation;
import com.keycloak.admin.client.models.LoginLocation;

import lombok.Data;

/**
 * @author Gbenga
 *
 */
@Data
public class LoginLocationBuilder {

	private Faker faker;

	private String id;

	private String username;

	private String ipAddress;

	private String status;

	private Collection<LoginLocation> loginLocHis;

	private Collection<LoginLocation> disabledLocations;

	private LoginLocationBuilder() {
		faker = new Faker();

		this.id = UUID.randomUUID().toString();
		this.username = faker.internet().emailAddress();
		this.ipAddress = faker.internet().ipV4Address();
		this.status = faker.bool().bool() ? StatusType.VALID.toString() : StatusType.DISABLED.toString();

		this.loginLocHis = Collections.emptyList();
		this.disabledLocations = Collections.emptyList();
	}

	public static LoginLocationBuilder loginLoc() {
		return new LoginLocationBuilder();
	}

	public LoginLocationBuilder withId(UUID id) {
		this.id = id.toString();
		return this;
	}

	public LoginLocationBuilder withUsername(String username) {
		this.username = username;
		return this;
	}

	public LoginLocationBuilder withIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}

	public UserloginLocation build() {
 
		return UserloginLocation.builder().id(id).ipAddr(ipAddress).loginLocHis(buildList())
				.disabledLocations(buildList()).status(status).username(username).build();
	}

	public LoginLocation buildLocation() {
		String city = faker.address().cityName();
		String country = faker.address().country();
		String ipAddr = faker.internet().ipV4Address();
		String latitude = faker.address().latitude();
		String longitude = faker.address().longitude();
		Date past5Days = faker.date().past(faker.random().nextInt(1, 10), TimeUnit.DAYS);
		String userAgent = faker.internet().userAgentAny();

		String deviceDetails = GeolocationUtils.getDeviceDetails(userAgent, new Parser());

		return LoginLocation.builder().city(city).ipAddr(ipAddr).latitude(Double.valueOf(latitude))
				.longitude(Double.valueOf(longitude)).countryCode(country).deviceDetails(deviceDetails)
				.loginTime(DateUtil.asLocalDateTime(past5Days)).build();
	}

	public List<LoginLocation> buildList() {
		List<LoginLocation> loginLocList = new ArrayList<>();
		loginLocList.add(buildLocation());
		loginLocList.add(buildLocation());
		loginLocList.add(buildLocation());

		return loginLocList;
	}
	
	public NewLocationToken newLoginLocToken() {
		
		String token = RandomGenerator.generateNewToken();
		
		return NewLocationToken.builder()
				.id(UUID.randomUUID().toString())
				.username(username)
				.token(token)
				.creationDate(DateUtil.now())
				.status(StatusType.VALID.toString())
				.countryCode(faker.address().country())
				.stateCode(faker.address().state())
				.build();
	}
	
   public NewLocationToken newLoginLocToken(String country, String stateCode) {
		
		String token = RandomGenerator.generateNewToken();
		
		return NewLocationToken.builder()
				.id(UUID.randomUUID().toString())
				.username(username)
				.token(token)
				.creationDate(DateUtil.now())
				.status(StatusType.VALID.toString())
				.countryCode(country)
				.stateCode(stateCode)
				.build();
	}
}
