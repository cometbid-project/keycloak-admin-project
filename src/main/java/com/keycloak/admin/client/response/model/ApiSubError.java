/**
 * 
 */
package com.keycloak.admin.client.response.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Gbenga
 *
 */
abstract class ApiSubError {

	/**
	 *
	 */
	private static final long serialVersionUID = -6444882637188942761L;

	private String object;
	private String code;
	private String message;

	@JsonCreator
	ApiSubError(String object, String code, String message) {
		this.object = object;
		this.code = code;
		this.message = message;
	}
}
