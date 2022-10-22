/**
 * 
 */
package com.keycloak.admin.client.auth.audit;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.userdetails.User;

//import com.cometbid.oauth2.demo.pojos.Username;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.models.Username;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author Gbenga
 *
 */
@Data
@Log4j2
@Accessors(chain = true)
public class AuditDetails {

	@JsonProperty(Audit.AUDIT_CREATEDBY)
	private Username createdBy;

	@JsonProperty(Audit.AUDIT_CREATIONDATE)
	private LocalDateTime creationDate;

	@JsonProperty(Audit.AUDIT_MODIFIEDBY)
	private Username lastModifiedBy;

	@JsonProperty(Audit.AUDIT_MODIFIEDDATE)
	private LocalDateTime lastModifiedDate;

	/**
	 * 
	 * @param auditable
	 * @return
	 */
	public void setCreatedBy(User auditable) {

		Optional<User> userOptional = Optional.ofNullable(auditable);

		if (userOptional.isEmpty()) {
			// business logic
			log.info("Create: No authenticated user found");
		} else {
			User authUser = userOptional.get();
			log.info("Create: Authenticated user found {}", authUser);

			Username userDet = Username.createUser(authUser);
			this.createdBy = userDet;
		}
	}

	// @PreRemove
	// @PreUpdate
	public void setLastModifiedBy(User auditable) {

		Optional<User> userOptional = Optional.ofNullable(auditable);

		if (userOptional.isEmpty()) {
			// business logic
			log.info("Create: No authenticated user found");
		} else {
			User authUser = userOptional.get();
			log.info("Create: Authenticated user found {}", authUser);

			Username userDet = Username.createUser(authUser);
			this.lastModifiedBy = userDet;
		}

	}
}
