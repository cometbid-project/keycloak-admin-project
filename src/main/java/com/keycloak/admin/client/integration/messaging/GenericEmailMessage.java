/**
 * 
 */
package com.keycloak.admin.client.integration.messaging;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Gbenga
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericEmailMessage {

	@Email(message = "{message.email.valid}")
	@NotBlank(message = "{message.email.notBlank}")
	private String emailAddr;
	
	@NotBlank(message = "{message.title.notBlank}")
	private String title;
	
	@NotBlank(message = "{message.payload.notBlank}")
	private String payload;

	@NotBlank(message = "{message.type.notBlank}")
	private String type;

	@NotBlank(message = "{message.refNo.notBlank}")
	private String refNo;

}
