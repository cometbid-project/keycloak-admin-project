/**
 * 
 */
package com.keycloak.admin.client.common.activity;

import static com.keycloak.admin.client.common.activity.ActivityLog.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.models.Username;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Data
@JsonRootName("Activities")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true)
@JsonIgnoreProperties(ignoreUnknown = false)
@NoArgsConstructor
public class ActivityLogVO {

	@EqualsAndHashCode.Include
	@JsonProperty("id")
	private String id;

	@JsonProperty(USER)
	@NotBlank(message = "{ActivityLog.user.notBlank}")
	@Field(name = USER_ID_FIELD)
	protected Username username;

	@JsonProperty(ACTIVITY_STMT)
	@NotBlank(message = "{ActivityLog.activityStmt.notBlank}")
	@Field(name = ACTIVITY_STMT_FIELD)
	protected String activityStmt;

	@JsonProperty(COMMENT)
	@NotBlank(message = "{ActivityLog.comment.notBlank}")
	@Field(name = COMMENT_FIELD)
	protected String comment;

	@Setter
	@JsonProperty(STATUS)
	@VerifyValue(message = "{Activity.status.verifyValue}", value = StatusType.class)
	// @Indexed(name = "Activity_status_index")
	@Field(name = STATUS_FIELD)
	protected String status; //

	@JsonProperty(MODIFIED_DTE)
	@Field(name = MODIFIED_DTE_FIELD)
	protected LocalDateTime lastModifiedDate;

	@JsonProperty(CREATION_DTE)
	@Field(name = TIMESTAMP_FIELD)
	protected ZonedDateTime creationDate;

	/**
	 * 
	 * @param userId
	 * @param rcNo
	 * @param branchCode
	 * @param activityStmt
	 * @param comment
	 * @param status
	 * @param lastModifiedDate
	 * @param creationDate
	 */
	public ActivityLogVO(String id, Username username, String activityStmt, String comment, String status,
			ZonedDateTime creationDate, LocalDateTime lastModifiedDate) {

		this.id = id;
		this.username = username;
		this.activityStmt = activityStmt;
		this.comment = comment;
		this.status = status;
		this.lastModifiedDate = lastModifiedDate;
		this.creationDate = creationDate;
	}

	/**
	 * 
	 * @param id
	 * @param userId
	 * @param branchCode
	 * @param rcNo
	 * @param activityStmt
	 * @param comment
	 * @param status
	 * @param timestamp
	 */
	@Builder
	public ActivityLogVO(String id, Username username, String activityStmt, String comment, String status,
			ZonedDateTime timestamp) {

		this(id, username, activityStmt, comment, status, timestamp, null);
		// TODO Auto-generated constructor stub
	}

}
