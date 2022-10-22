/**
 * 
 */
package com.keycloak.admin.client.response.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Gbenga
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AppResponseMetadata {

	@JsonProperty("api_version")
	private String apiVersion;

	@JsonProperty("report_to")
	private String sendReport;

	@JsonProperty("more_info")
	private String moreInfo;

	@JsonProperty("status")
	private String status;

	@JsonProperty("message")
	private String shortMessage;
	
}
