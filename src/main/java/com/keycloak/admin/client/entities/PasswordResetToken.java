/**
 * 
 */
package com.keycloak.admin.client.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Getter
@Builder
@NoArgsConstructor
//@AllArgsConstructor
@TypeAlias("Password reset token")
@ToString(includeFieldNames = true)
@Document(collection = PasswordResetToken.PASSWORDRESET_TOKEN_COLLECTION_NAME)
public class PasswordResetToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6892704864109745632L;
	public static final String PASSWORDRESET_TOKEN_COLLECTION_NAME = "ACTIVATION_TOKEN";

	@JsonProperty(USERNAME)
	@NotBlank(message = "{Activation.username.notBlank}")
	@Indexed(name = "activation_username_index")
	@Field(name = USERNAME_FIELD)
	private String username;

	@Id
	@JsonProperty(TOKEN)
	@Indexed(unique = true, name = "token_unique_index_1")
	@NotBlank(message = "{Activation.token.notBlank}")
	@Field(name = TOKEN_FIELD)
	private String token;

	@JsonProperty(CREATION_DATETIME)
	@NotNull(message = "{Activation.creationDate.notNull}")
	@ValidDate(message = "{Activation.creationDate.validDate}")
	@Field(name = CREATION_DATE_FIELD)
	private LocalDateTime creationDate;

	@JsonProperty(EXPIRY_DATETIME)
	@ValidDate(message = "{Activation.expiredTime.validDate}")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Field(name = EXPIRY_DATE_FIELD)
	private LocalDateTime expiredTime; // TIMESTAMP,

	@Setter
	@JsonProperty(STATUS)
	@NotBlank(message = "{Activation.token.status.notBlank}")
	@VerifyValue(message = "{Activation.status.verifyValue}", value = StatusType.class)
	@Indexed(name = "activation_status_index")
	@Field(name = STATUS_FIELD)
	private String status;

	@Version
	@Setter(AccessLevel.PROTECTED)
	@JsonIgnore
	@Field(name = "VERSION")
	private Long version;

	/**
	 * @param username
	 * @param token
	 * @param creationDate
	 * @param expiredTime
	 * @param status
	 */
	@Builder
	public PasswordResetToken(String username, String token, LocalDateTime creationDate, LocalDateTime expiredTime,
			String status) {
		super();
		this.username = username;
		this.token = token;
		this.creationDate = creationDate;
		this.expiredTime = expiredTime;
		this.status = status;
	} //

	/**
	 * @param username
	 * @param token
	 * @param creationDate
	 * @param expiredTime
	 * @param status
	 * @param version
	 */
	protected PasswordResetToken(String username, String token, LocalDateTime creationDate, LocalDateTime expiredTime,
			String status, Long version) {
		super();
		this.username = username;
		this.token = token;
		this.creationDate = creationDate;
		this.expiredTime = expiredTime;
		this.status = status;
		this.version = version;
	}

	// Json fields definition
	public static final String USERNAME = "username";
	public static final String TOKEN = "token";
	public static final String CREATION_DATETIME = "creation_time";
	public static final String EXPIRY_DATETIME = "expiry_time";
	public static final String STATUS = "status";

	/**********************************************/
	/******** Column fields definition ***********/
	/*********************************************/
	public static final String USERNAME_FIELD = "USERNAME";
	public static final String TOKEN_FIELD = "TOKEN";
	public static final String CREATION_DATE_FIELD = "CREATED_DTE";
	public static final String EXPIRY_DATE_FIELD = "EXPIRY_DTE";
	public static final String STATUS_FIELD = "STATUS";
}
