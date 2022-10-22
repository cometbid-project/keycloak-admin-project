/**
 * 
 */
package com.keycloak.admin.client.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

//import com.cometbid.oauth2.demo.enums.StatusType;
//import com.cometbid.oauth2.demo.validators.qualifiers.VerifyValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.keycloak.admin.client.common.enums.StatusType;
import com.keycloak.admin.client.validators.qualifiers.VerifyValue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@NoArgsConstructor
public class StatusUpdateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3812598934962451724L;

	@Schema(name = "username", description = "username (email)", required = true, example = "john_doe@yahoo.com")
	@JsonProperty("username")
	@Size(max = 330, message = "{User.username.size}")
	protected String username;

	@NotBlank(message = "{status.notBlank}")
	@VerifyValue(message = "{status.verifyValue}", value = StatusType.class)
	private String status;

	/**
	 * @param username
	 * @param status
	 */
	public StatusUpdateRequest(String username, String status) {
		super();
		this.status = status;
		this.username = username;
	}

}
