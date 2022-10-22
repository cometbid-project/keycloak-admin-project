/**
 * 
 */
package com.keycloak.admin.client.auth.audit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

/**
 * @author Gbenga
 *
 */
@Log4j2
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Audit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3708223993588759581L;

	@JsonProperty(AUDIT_CREATEDBY)
	@CreatedBy
	@Field(name = AUDITCREATOR_FIELD)
	private User createdBy;

	@JsonProperty(AUDIT_CREATIONDATE)
	@CreatedDate
	@Field(name = AUDITCREATEDATE_FIELD)
	private LocalDateTime creationDate;

	@JsonProperty(AUDIT_MODIFIEDBY)
	@LastModifiedBy
	@Field(name = AUDITMODIFIER_FIELD)
	private User lastModifiedBy;

	@JsonProperty(AUDIT_MODIFIEDDATE)
	@LastModifiedDate
	@Field(name = AUDITMODIFIEDDATE_FIELD)
	private LocalDateTime lastModifiedDate;

	public boolean isNew() {
		// TODO Auto-generated method stub
		return true;
	}

	public static final String AUDIT = "audit";
	public static final String AUDIT_CREATEDBY = "created_by";
	public static final String AUDIT_CREATIONDATE = "creation_date";
	public static final String AUDIT_MODIFIEDBY = "last_modified_by";
	public static final String AUDIT_MODIFIEDDATE = "last_modified_date";

	// =========================================================================

	public static final String AUDIT_FIELD = "AUDIT";
	public static final String AUDITCREATOR_FIELD = "CREATED_BY";
	public static final String AUDITCREATEDATE_FIELD = "CREATED_DTE";
	public static final String AUDITMODIFIER_FIELD = "MODIFIED_BY";
	public static final String AUDITMODIFIEDDATE_FIELD = "MODIFIED_DTE";

}
