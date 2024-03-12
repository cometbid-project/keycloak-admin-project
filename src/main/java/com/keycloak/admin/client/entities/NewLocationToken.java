/**
 * 
 */
package com.keycloak.admin.client.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.validators.qualifiers.ValidDate;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TypeAlias("Location Token")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(includeFieldNames = true)
@Document(collection = NewLocationToken.LOCATION_TOKEN_COLLECTION_NAME)
public class NewLocationToken extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6892704864109745632L;
	public static final String LOCATION_TOKEN_COLLECTION_NAME = "NEW_LOCATION_TOKEN";

	@JsonProperty(USERNAME)
	@NotBlank(message = "{locToken.username.notBlank}")
	@Indexed(name = "locToken_username_index")
	@Field(name = USERNAME_FIELD)
	private String username;

	@Id
	@JsonProperty(TOKEN)
	@Indexed(unique = true, name = "token_unique_index_1")
	@NotBlank(message = "{locToken.token.notBlank}")
	@Field(name = TOKEN_FIELD)
	private String token;

	@JsonProperty(COUNTRY_CODE)
	@Field(name = COUNTRY_CODE_FIELD)
	private String countryCode;

	@JsonProperty(STATE_CODE)
	@Field(name = STATE_CODE_FIELD)
	private String stateCode;

	@JsonProperty(CREATION_DATETIME)
	@NotNull(message = "{locToken.creationDate.notNull}")
	@ValidDate(message = "{locToken.creationDate.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = CREATION_DATE_FIELD)
	private LocalDateTime creationDate;

	@Setter
	@JsonProperty(STATUS)
	@VerifyValue(message = "{locToken.status.verifyValue}", value = StatusType.class)
	@Indexed(name = "locToken_status_index")
	@Field(name = STATUS_FIELD)
	private String status;
	
	@Version
	@Setter(AccessLevel.PROTECTED)
	@JsonIgnore
	@Field(name = "VERSION")
	private Long version;

	@Builder
	public NewLocationToken(String id, String username, String token, LocalDateTime creationDate, String status,
			String countryCode, String stateCode) {

		super.id = id;
		this.username = username;
		this.token = token;
		this.creationDate = creationDate;
		this.countryCode = countryCode;
		this.stateCode = stateCode;
		this.status = status;
	} //

	// Json fields definition
	public static final String USERNAME = "username";
	public static final String TOKEN = "token";
	public static final String COUNTRY_CODE = "country_code";
	public static final String STATE_CODE = "state_code";
	public static final String CREATION_DATETIME = "creation_time";
	public static final String STATUS = "status";

	/**********************************************/
	/******** Column fields definition ***********/
	/*********************************************/
	public static final String USERNAME_FIELD = "USERNAME";
	public static final String TOKEN_FIELD = "TOKEN";
	public static final String COUNTRY_CODE_FIELD = "COUNTRY_CODE";
	public static final String STATE_CODE_FIELD = "STATE_CODE";
	public static final String CREATION_DATE_FIELD = "CREATED_DTE";
	public static final String STATUS_FIELD = "STATUS";
}
