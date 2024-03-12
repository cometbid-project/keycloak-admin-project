/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.keycloak.admin.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import static com.keycloak.admin.client.common.geo.GeolocationUtils.*;
import com.keycloak.admin.client.validators.qualifiers.ValidDate;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
 *
 * @author Gbenga
 */
@Value
@Builder
//@NoArgsConstructor
//@AllArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoginLocation implements Serializable {

	/**    
	 *
	 */
	private static final long serialVersionUID = -4578947272482201036L;

	@JsonProperty("ip_addr")
	@Field(name = "IP_ADDR")
	private String ipAddr;

	@JsonProperty("isp_addr")
	@Field(name = "ISP_ADDR")
	private String ispAddr;

	@JsonProperty("device_id")
	@Field(name = "DEVICE_ID")
	private String deviceId;

	@JsonProperty("device_type")
	@Field(name = "DEVICE_TYPE")
	private String deviceType;

	@JsonProperty("device_details")
	@Field(name = "DEVICE_DETAILS")
	private String deviceDetails;

	@JsonProperty("country_code")
	@Field(name = "COUNTRY_CODE")
	private String countryCode;

	@JsonProperty("state_code")
	@Field(name = "STATE_CODE")
	private String stateCode;

	@JsonProperty("city")
	@Field(name = "CITY")
	private String city;

	@JsonProperty("longitude")
	@Field(name = "LONGITUDE")
	private Double longitude;

	@JsonProperty("latitude")
	@Field(name = "LATITUDE")
	private Double latitude;

	@JsonProperty("login_time")
	@ValidDate(message = "{loginTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message = "{loginTime.notNull}")
	@Field(name = "LOGIN_TIME")
	private LocalDateTime loginTime;

	/**
	 * @param ipAddr
	 * @param ispAddr
	 * @param deviceId
	 * @param deviceType
	 * @param deviceDetails
	 * @param countryCode
	 * @param stateCode
	 * @param city
	 * @param longitude
	 * @param latitude
	 * @param loginTime
	 */
	@JsonCreator
	public LoginLocation(String ipAddr, String ispAddr, String deviceId, String deviceType, String deviceDetails,
			String countryCode, String stateCode, String city, Double longitude, Double latitude,
			LocalDateTime loginTime) {

		super();
		this.ipAddr = ipAddr;
		this.ispAddr = ispAddr;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.deviceDetails = StringUtils.isBlank(deviceDetails) ? UNKNOWN_DEVICE : deviceDetails;
		this.countryCode = StringUtils.isBlank(countryCode) ? UNKNOWN_COUNTRY : countryCode;
		this.stateCode = StringUtils.isBlank(stateCode) ? UNKNOWN_STATE : stateCode;
		this.city = StringUtils.isBlank(city) ? UNKNOWN_CITY : city;
		this.longitude = longitude;
		this.latitude = latitude;
		this.loginTime = loginTime;
	}

}
