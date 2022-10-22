/**
 * 
 */
package com.keycloak.admin.client.common.activity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.common.activity.enums.Action;
import com.keycloak.admin.client.common.activity.enums.ContentType;
import com.keycloak.admin.client.common.activity.enums.ObjectType;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.entities.Entity;
import com.keycloak.admin.client.models.Username;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Gbenga
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(includeFieldNames = true)
@Document(collection = ActivityLog.ACTIVITY_COLLECTION_NAME)
public class ActivityLog extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7498767569240182153L;
	public static final String ACTIVITY_COLLECTION_NAME = "ACTIVITY_LOG";

	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm a");

	private static final CharSequence SPACE = " ";
	private static final CharSequence QUOTE = "\"";
	private static final CharSequence ADJ_DATE = "on";
	private static final CharSequence OPEN_BRACKET = "(";
	private static final CharSequence CLOSE_BRACKET = ")";
	private static final CharSequence APOSTROPHE = "'s";

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

	@Getter
	@Setter
	public class Activity {

		private String subject;
		private Action action;
		private ObjectType objectType;
		private String actionReceiver;
		private ContentType content;
		private String transactionId;

		public Activity() {
			super();
			this.subject = null;
			this.action = null;
			this.objectType = null;
			this.actionReceiver = null;
			this.content = null;
			this.transactionId = null;
		}

		public Activity(String subject, Action action, ObjectType objectType, String actionReceiver,
				ContentType content, String transactionId) {
			super();
			this.subject = subject;
			this.action = action;
			this.objectType = objectType;
			this.actionReceiver = actionReceiver;
			this.content = content;
			this.transactionId = transactionId;
		}

		public String getActivityStmt() {
			StringBuilder activitySentence = new StringBuilder();

			String formatDateTime = LocalDateTime.now().format(TIME_FORMATTER);

			if (null != action) {
				switch (action) {
				case LOGIN:
				case LOGOUT:
					return activitySentence.append(subject).append(SPACE).append(action.getStmt()).append(SPACE)
							.append(ADJ_DATE).append(SPACE).append(formatDateTime).toString();
				case UPDATED:
					activitySentence = activitySentence.append(subject).append(SPACE).append(action.getStmt())
							.append(SPACE).append(objectType.getObject());

					if (StringUtils.isNotBlank(actionReceiver)) {
						activitySentence.append(OPEN_BRACKET).append(actionReceiver).append(CLOSE_BRACKET);
					}

					return activitySentence.append(SPACE).append(QUOTE).append(content.getContent()).append(QUOTE)
							.append(SPACE).append(ADJ_DATE).append(SPACE).append(formatDateTime).toString();
				case CREATED:
					activitySentence = activitySentence.append(subject).append(SPACE).append(action.getStmt())
							.append(SPACE).append(objectType.getObject());

					if (StringUtils.isNotBlank(actionReceiver)) {
						activitySentence.append(OPEN_BRACKET).append(actionReceiver).append(CLOSE_BRACKET);
					}
					return activitySentence.append(SPACE).append(ADJ_DATE).append(SPACE).append(formatDateTime)
							.toString();
				case DEACTIVATED:
					activitySentence = activitySentence.append(subject).append(SPACE).append(action.getStmt())
							.append(SPACE).append(objectType.getObject());

					if (StringUtils.isNotBlank(actionReceiver)) {
						activitySentence.append(OPEN_BRACKET).append(actionReceiver).append(CLOSE_BRACKET);
					}
					return activitySentence.append(SPACE).append(ADJ_DATE).append(SPACE).append(formatDateTime)
							.toString();
				case SEARCHED:
					activitySentence = activitySentence.append(subject).append(SPACE).append(action.getStmt())
							.append(SPACE).append(objectType.getObject()).append(APOSTROPHE);

					if (StringUtils.isNotBlank(actionReceiver)) {
						activitySentence.append(OPEN_BRACKET).append(actionReceiver).append(CLOSE_BRACKET);
					}
					return activitySentence.append(SPACE).append(QUOTE).append(content.getContent()).append(QUOTE)
							.append(SPACE).append(ADJ_DATE).append(SPACE).append(formatDateTime).toString();
				case CREDIT:
				case DEBIT:
					activitySentence = activitySentence.append(subject).append(SPACE).append(action.getStmt())
							.append(SPACE).append(objectType.getObject()).append(APOSTROPHE);

					if (StringUtils.isNotBlank(actionReceiver)) {
						activitySentence.append(OPEN_BRACKET).append(actionReceiver).append(CLOSE_BRACKET);
					}
					return activitySentence.append(SPACE).append(QUOTE).append(content.getContent()).append(QUOTE)
							.append(SPACE).append("Transaction id: ").append(QUOTE).append(transactionId).append(QUOTE)
							.append(SPACE).append(ADJ_DATE).append(SPACE).append(formatDateTime).toString();
				default:
					break;

				}
			}
			return SPACE.toString();
		}
	}

	@Builder
	public ActivityLog(String id, Username username, String activityStmt, String comment, String status,
			ZonedDateTime timestamp) {
		super.id = id;
		this.username = username;
		this.activityStmt = activityStmt;
		this.comment = comment;
		this.status = status;
		this.creationDate = timestamp;
	}

	// Json fields definition
	public static final String USER = "user";
	public static final String ACTIVITY_STMT = "activity";
	public static final String COMMENT = "comment";
	public static final String STATUS = "status";
	public static final String MODIFIED_DTE = "modified_date";
	public static final String CREATION_DTE = "creation_date";

	/**********************************************/
	/******** Column fields definition ***********/
	/*********************************************/
	public static final String USER_ID_FIELD = "USER";
	public static final String ACTIVITY_STMT_FIELD = "ACTIVITY_STMT";
	public static final String COMMENT_FIELD = "COMMENT";
	public static final String STATUS_FIELD = "STATUS";
	public static final String MODIFIED_DTE_FIELD = "MODIFIED_DATE";
	public static final String TIMESTAMP_FIELD = "CREATION_DATE";

	public static final Map<String, String> defaultFields = Collections.synchronizedMap(new HashMap<>());
	public static final Map<String, String> jsonfieldPropertyMapping = Collections.synchronizedMap(new HashMap<>());

	static {
		defaultFields.put(USER, USER_ID_FIELD);
		defaultFields.put(ACTIVITY_STMT, ACTIVITY_STMT_FIELD);
		defaultFields.put(COMMENT, COMMENT_FIELD);
		defaultFields.put(STATUS, STATUS_FIELD);
		defaultFields.put(MODIFIED_DTE, MODIFIED_DTE_FIELD);
		defaultFields.put(CREATION_DTE, TIMESTAMP_FIELD);

		// jsonfieldPropertyMapping.put(WEBSITE_URL, "WEBSITE");
	}

	public static String getMappedField(String jsonField) {

		String field = defaultFields.get(jsonField);

		return field != null ? field : jsonfieldPropertyMapping.getOrDefault(jsonField, TIMESTAMP_FIELD);
	}

	public static Collection<String> getMappedDefaultFields() {

		return defaultFields.keySet();
	}

	public static Collection<String> getMappedOtherFields() {

		return jsonfieldPropertyMapping.keySet();
	}

	public static Collection<String> getAllMappedFields() {

		Collection<String> defaultFields = getMappedDefaultFields();
		Collection<String> nonDefaultFields = getMappedOtherFields();

		List<String> all = new ArrayList<>();
		all.addAll(defaultFields);
		all.addAll(nonDefaultFields);

		return all;
	}

}